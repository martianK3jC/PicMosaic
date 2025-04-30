package com.android.picmosaic
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import com.android.picmosaic.utils.CollageConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

class SaveShareActivity : Activity() {

    private lateinit var collageImageView: ImageView
    private lateinit var backButton: ImageButton
    private lateinit var shareFacebook: ImageButton
    private lateinit var shareInstagram: ImageButton
    private lateinit var shareMessenger: ImageButton
    private lateinit var openMore: ImageButton
    private lateinit var saveButton: Button
    private lateinit var createAnotherButton: Button

    private var collageBitmap: Bitmap? = null
    private var savedFile: File? = null // Store the file for reuse
    private var lastToast: Toast? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_save_share)

        // Initialize views
        collageImageView = findViewById(R.id.collage_image_view)
        backButton = findViewById(R.id.back_button)
        shareFacebook = findViewById(R.id.share_facebook)
        shareInstagram = findViewById(R.id.share_instagram)
        shareMessenger = findViewById(R.id.share_messenger)
        openMore = findViewById(R.id.open_more)
        saveButton = findViewById(R.id.save_button)
        createAnotherButton = findViewById(R.id.create_another_button)

        // Load the collage configuration
        val collageConfig = intent.getParcelableExtra<CollageConfig>("collage_config")
        if (collageConfig == null) {
            showToast("No collage configuration received")
            finish()
            return
        }

        // Use a standalone CoroutineScope to flatten and display the collage
        CoroutineScope(Dispatchers.Main).launch {
            try {
                // Flatten the collage into a bitmap on a background thread
                collageBitmap = withContext(Dispatchers.IO) {
                    createCollageBitmap(collageConfig)
                }

                if (collageBitmap == null) {
                    showToast("Error: Could not create collage")
                    finish()
                    return@launch
                }

                // Set up ImageView for display
                collageImageView.scaleType = ImageView.ScaleType.FIT_CENTER
                collageImageView.adjustViewBounds = false
                collageImageView.setImageBitmap(collageBitmap)

                // Set up button listeners
                setupButtonListeners()
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error loading collage: ${e.message}")
                finish()
            }
        }
    }

    private fun createCollageBitmap(config: CollageConfig): Bitmap? {
        val COLLAGE_WIDTH = 1080
        val COLLAGE_HEIGHT = 1080

        try {
            // Create bitmap with fixed dimensions
            val bitmap = Bitmap.createBitmap(COLLAGE_WIDTH, COLLAGE_HEIGHT, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            // Draw background
            canvas.drawColor(config.backgroundColor)

            // Calculate proper dimensions
            val padding = (bitmap.width * 0.02f).toInt() // 2% padding
            val availableWidth = bitmap.width - 2 * padding
            val availableHeight = bitmap.height - 2 * padding

            // Calculate grid dimensions
            val itemWidth = availableWidth / config.spanCount
            val rowCount = (config.imageUris.size + config.spanCount - 1) / config.spanCount
            val itemHeight = if (rowCount > 0) availableHeight / rowCount else availableHeight

            // Draw each image with proper styling
            val uris = config.getUris()
            for (i in uris.indices) {
                try {
                    val column = i % config.spanCount
                    val row = i / config.spanCount

                    val left = padding + column * itemWidth
                    val top = padding + row * itemHeight
                    val right = left + itemWidth
                    val bottom = top + itemHeight

                    // Create item shape with rounded corners
                    val itemPath = Path()
                    val itemRect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
                    itemPath.addRoundRect(itemRect, config.cornerRadius, config.cornerRadius, Path.Direction.CW)

                    // Save canvas state and apply clipping
                    canvas.save()
                    canvas.clipPath(itemPath)

                    // Draw white border background
                    val paint = Paint().apply {
                        color = Color.WHITE
                        style = Paint.Style.FILL
                    }
                    canvas.drawRect(itemRect, paint)

                    // Apply the border width
                    val borderRect = RectF(
                        left + config.borderWidth,
                        top + config.borderWidth,
                        right - config.borderWidth,
                        bottom - config.borderWidth
                    )

                    // Create clipping path for the image area
                    val borderPath = Path()
                    borderPath.addRoundRect(borderRect, config.cornerRadius, config.cornerRadius, Path.Direction.CW)

                    // Apply clipping to keep image within borders
                    canvas.clipPath(borderPath)

                    // Load the image
                    val inputStream = contentResolver.openInputStream(uris[i])
                    val imageBitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()

                    if (imageBitmap != null) {
                        // Apply centerCrop logic
                        val imageWidth = imageBitmap.width.toFloat()
                        val imageHeight = imageBitmap.height.toFloat()
                        val imageAspect = imageWidth / imageHeight

                        val borderWidth = borderRect.width()
                        val borderHeight = borderRect.height()
                        val borderAspect = borderWidth / borderHeight

                        val srcRect: Rect
                        val dstRect = RectF(borderRect)

                        if (imageAspect > borderAspect) {
                            // Image is wider than cell - crop width
                            val newWidth = imageHeight * borderAspect
                            val xOffset = (imageWidth - newWidth) / 2
                            srcRect = Rect(
                                xOffset.toInt(),
                                0,
                                (xOffset + newWidth).toInt(),
                                imageHeight.toInt()
                            )
                        } else {
                            // Image is taller than cell - crop height
                            val newHeight = imageWidth / borderAspect
                            val yOffset = (imageHeight - newHeight) / 2
                            srcRect = Rect(
                                0,
                                yOffset.toInt(),
                                imageWidth.toInt(),
                                (yOffset + newHeight).toInt()
                            )
                        }

                        // Draw the bitmap with center crop applied
                        canvas.drawBitmap(
                            imageBitmap,
                            srcRect,
                            dstRect,
                            null
                        )
                        imageBitmap.recycle()
                    }

                    // Restore canvas state
                    canvas.restore()
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Continue with the next image if one fails
                }
            }
            return bitmap
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    private fun setupButtonListeners() {
        backButton.setOnClickListener { finish() }
        shareFacebook.setOnClickListener { shareToApp("com.facebook.katana") }
        shareInstagram.setOnClickListener { shareToApp("com.instagram.android") }
        shareMessenger.setOnClickListener { shareToApp("com.facebook.orca") }
        openMore.setOnClickListener { showShareSheet() } // Updated to show share sheet
        saveButton.setOnClickListener { saveCollage() }
        createAnotherButton.setOnClickListener {
            val intent = Intent(this, HomeActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
            startActivity(intent)
            finish()
        }
    }

    private fun showShareSheet() {
        if (collageBitmap == null) {
            showToast("No collage to share")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // If the file hasn’t been saved yet, save it with a temporary name
                if (savedFile == null) {
                    savedFile = withContext(Dispatchers.IO) {
                        val fileName = "temp_collage_${System.currentTimeMillis()}.png"
                        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val picMosaicDir = File(directory, "PicMosaic")
                        if (!picMosaicDir.exists()) {
                            picMosaicDir.mkdirs()
                        }
                        val file = File(picMosaicDir, fileName)
                        FileOutputStream(file).use { out ->
                            collageBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                        }
                        file
                    }
                }

                // Generate URI on a background thread
                val uri = withContext(Dispatchers.IO) {
                    FileProvider.getUriForFile(this@SaveShareActivity, "com.android.picmosaic.provider", savedFile!!)
                }

                // Create a share intent without specifying a package
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                // Show the system share sheet
                startActivity(Intent.createChooser(shareIntent, "Share Collage"))
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error sharing collage: ${e.message}")
            }
        }
    }

    private fun saveCollage() {
        if (collageBitmap == null) {
            showToast("No collage to save")
            return
        }

        // Create a dialog with an EditText for the file name
        val editText = EditText(this).apply {
            hint = "Enter collage name"
            setText("MyCollage") // Default name
        }

        AlertDialog.Builder(this)
            .setTitle("Save Collage")
            .setMessage("Enter a name for your collage:")
            .setView(editText)
            .setPositiveButton("Save") { _, _ ->
                val fileName = editText.text.toString().trim()
                if (fileName.isEmpty()) {
                    showToast("File name cannot be empty")
                    return@setPositiveButton
                }

                // Ensure the file name ends with .png
                val finalFileName = if (fileName.endsWith(".png", ignoreCase = true)) {
                    fileName
                } else {
                    "$fileName.png"
                }

                CoroutineScope(Dispatchers.Main).launch {
                    try {
                        // Save the file with the user-provided name
                        savedFile = withContext(Dispatchers.IO) {
                            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                            val picMosaicDir = File(directory, "PicMosaic")
                            if (!picMosaicDir.exists()) {
                                picMosaicDir.mkdirs()
                            }
                            val file = File(picMosaicDir, finalFileName)
                            FileOutputStream(file).use { out ->
                                collageBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                                out.flush()
                            }
                            file
                        }

                        // Notify the media scanner
                        MediaScannerConnection.scanFile(
                            this@SaveShareActivity,
                            arrayOf(savedFile!!.absolutePath),
                            arrayOf("image/png")
                        ) { _, _ ->
                            runOnUiThread {
                                showToast("Collage saved to Pictures/PicMosaic/$finalFileName")
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        showToast("Error saving collage: ${e.message}")
                    }
                }
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun shareToApp(packageName: String) {
        if (collageBitmap == null) {
            showToast("No collage to share")
            return
        }

        CoroutineScope(Dispatchers.Main).launch {
            try {
                // If the file hasn’t been saved yet, save it with a temporary name
                if (savedFile == null) {
                    savedFile = withContext(Dispatchers.IO) {
                        val fileName = "temp_collage_${System.currentTimeMillis()}.png"
                        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        val picMosaicDir = File(directory, "PicMosaic")
                        if (!picMosaicDir.exists()) {
                            picMosaicDir.mkdirs()
                        }
                        val file = File(picMosaicDir, fileName)
                        FileOutputStream(file).use { out ->
                            collageBitmap!!.compress(Bitmap.CompressFormat.PNG, 100, out)
                            out.flush()
                        }
                        file
                    }
                }

                // Generate URI on a background thread
                val uri = withContext(Dispatchers.IO) {
                    FileProvider.getUriForFile(this@SaveShareActivity, "com.android.picmosaic.provider", savedFile!!)
                }

                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    setPackage(packageName)
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                try {
                    startActivity(shareIntent)
                } catch (e: Exception) {
                    showToast("App not installed")
                }
            } catch (e: Exception) {
                e.printStackTrace()
                showToast("Error sharing collage: ${e.message}")
            }
        }
    }

    private fun showToast(message: String) {
        lastToast?.cancel()
        lastToast = Toast.makeText(this, message, Toast.LENGTH_LONG)
        lastToast?.show()
    }

    override fun onDestroy() {
        super.onDestroy()
        collageBitmap?.recycle() // Free up memory
        collageBitmap = null
    }
}