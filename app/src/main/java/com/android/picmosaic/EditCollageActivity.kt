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
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.picmosaic.utils.CollageConfig
import java.io.File
import androidx.appcompat.app.AlertDialog

class EditCollageActivity : Activity() {
    private lateinit var layoutButton: ImageButton
    private lateinit var backgroundButton: ImageButton
    private lateinit var borderButton: ImageButton
    private lateinit var saveButton: ImageButton
    private lateinit var backButton: ImageButton

    private lateinit var layoutControls: LinearLayout
    private lateinit var backgroundControls: LinearLayout
    private lateinit var borderControls: LinearLayout

    private lateinit var borderWidthSeekBar: SeekBar
    private lateinit var cornerRadiusSeekBar: SeekBar
    private lateinit var collageRecyclerView: RecyclerView
    private lateinit var adapter: CollageAdapter

    private var borderWidth = 8f
    private var cornerRadius = 0f
    private var backgroundColor = Color.WHITE //The container. should we change color?
    private var selectedUris: List<Uri> = listOf()
    private var spanCount = 3 // Default span count (3x3 grid)

    // Fixed dimensions for output image - use the same for preview and final output
    private val COLLAGE_WIDTH = 1080 // Standard width for modern phones
    private val COLLAGE_HEIGHT = 1080 // Square aspect ratio

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_collage)

        // Get selected images from intent
        selectedUris = intent.getStringArrayListExtra("imageUris")?.map { Uri.parse(it) } ?: listOf()

        // Initialize views
        initializeViews()

        // Set up RecyclerView
        setupRecyclerView()

        // Set up click listeners
        setupClickListeners()

        // Set up seek bar listeners
        setupSeekBarListeners()
    }

    private fun initializeViews() {
        layoutButton = findViewById(R.id.layoutButton)
        backgroundButton = findViewById(R.id.backgroundButton)
        borderButton = findViewById(R.id.borderButton)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)

        layoutControls = findViewById(R.id.layoutControls)
        backgroundControls = findViewById(R.id.backgroundControls)
        borderControls = findViewById(R.id.borderControls)

        borderWidthSeekBar = findViewById(R.id.borderWidthSeekBar)
        cornerRadiusSeekBar = findViewById(R.id.cornerRadiusSeekBar)

        collageRecyclerView = findViewById(R.id.collageRecyclerView)
    }

    // Modify setupRecyclerView() to ensure a consistent square preview
    private fun setupRecyclerView() {
        // Validate selectedUris
        if (selectedUris.isEmpty()) {
            Toast.makeText(this, "No images selected to display", Toast.LENGTH_LONG).show()
            return
        }

        // Initialize adapter with current border properties
        adapter = CollageAdapter(selectedUris).apply {
            setBorderProperties(borderWidth.toInt(), cornerRadius.toInt())
        }

        // Set exact dimensions to match the output aspect ratio
        val params = collageRecyclerView.layoutParams
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels

        // Make it exactly square to match our COLLAGE_WIDTH and COLLAGE_HEIGHT (which are equal)
        params.width = screenWidth
        params.height = screenWidth // Square aspect ratio
        collageRecyclerView.layoutParams = params

        // Apply background color
        collageRecyclerView.setBackgroundColor(backgroundColor)

        // Setup adapter and layout manager
        collageRecyclerView.adapter = adapter
        collageRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
        collageRecyclerView.visibility = View.VISIBLE
    }

    private fun setupClickListeners() {
        // Layout button click listener
        layoutButton.setOnClickListener {
            showLayoutControls()
        }

        // Save button click listener
        saveButton.setOnClickListener {
            val collageBitmap = createCollageBitmap()
            navigateToSaveShareActivity()
        }

        // Background button click listener
        backgroundButton.setOnClickListener {
            showBackgroundControls()
        }

        // Border button click listener
        borderButton.setOnClickListener {
            showBorderControls()
        }

        backButton.setOnClickListener {
            AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("Do you want to discard changes?")
                .setPositiveButton("Yes") { _, _ ->
                    startActivity(Intent(this, HomeActivity::class.java))
                    finish()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }

        // Layout format buttons
        findViewById<View>(R.id.layout2x6).setOnClickListener {
            updateLayoutSpanCount(2)
        }

        findViewById<View>(R.id.layout3x3).setOnClickListener {
            updateLayoutSpanCount(3)
        }

        findViewById<View>(R.id.layout4x2).setOnClickListener {
            updateLayoutSpanCount(4)
        }

        // Background color buttons. will change this to the color wheel/color picker. with buttons that has default colors but can be updated when we choose a color in the color wheel
        val colorViews = listOf(
            findViewById<View>(R.id.color1),
            findViewById<View>(R.id.color2),
            findViewById<View>(R.id.color3),
            findViewById<View>(R.id.color4)
        )

        colorViews.forEachIndexed { index, view ->
            view.setOnClickListener {
                val colors = listOf("#0288D1", "#4CAF50", "#FFEB3B", "#F44336")
                backgroundColor = Color.parseColor(colors[index])
                collageRecyclerView.setBackgroundColor(backgroundColor)
            }
        }
    }

    private fun setupSeekBarListeners() {
        borderWidthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                borderWidth = progress.toFloat()
                adapter.setBorderProperties(borderWidth.toInt(), cornerRadius.toInt())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        cornerRadiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                cornerRadius = progress.toFloat()
                adapter.setBorderProperties(borderWidth.toInt(), cornerRadius.toInt())
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun showLayoutControls() {
        layoutControls.visibility = View.VISIBLE
        backgroundControls.visibility = View.GONE
        borderControls.visibility = View.GONE
    }

    private fun showBackgroundControls() {
        layoutControls.visibility = View.GONE
        backgroundControls.visibility = View.VISIBLE
        borderControls.visibility = View.GONE
    }

    private fun showBorderControls() {
        layoutControls.visibility = View.GONE
        backgroundControls.visibility = View.GONE
        borderControls.visibility = View.VISIBLE
    }

    private fun updateLayoutSpanCount(spanCount: Int) {
        this.spanCount = spanCount
        collageRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
    }

    private fun createCollageBitmap(): Bitmap {
        // Create bitmap with fixed dimensions
        val bitmap = Bitmap.createBitmap(COLLAGE_WIDTH, COLLAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Draw background
        canvas.drawColor(backgroundColor)

        // Calculate proper dimensions
        val padding = (bitmap.width * 0.02f).toInt() // 2% padding
        val availableWidth = bitmap.width - 2 * padding
        val availableHeight = bitmap.height - 2 * padding

        // Calculate grid dimensions
        val itemWidth = availableWidth / spanCount
        val rowCount = (selectedUris.size + spanCount - 1) / spanCount
        val itemHeight = if (rowCount > 0) availableHeight / rowCount else availableHeight

        // Draw each image with proper styling
        for (i in selectedUris.indices) {
            try {
                val column = i % spanCount
                val row = i / spanCount

                val left = padding + column * itemWidth
                val top = padding + row * itemHeight
                val right = left + itemWidth
                val bottom = top + itemHeight

                // Create item shape with rounded corners (same as in preview)
                val itemPath = Path()
                val itemRect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
                itemPath.addRoundRect(itemRect, cornerRadius, cornerRadius, Path.Direction.CW)

                // Save canvas state and apply clipping
                canvas.save()
                canvas.clipPath(itemPath)

                // Draw white border background
                val paint = Paint().apply {
                    color = Color.WHITE
                    style = Paint.Style.FILL
                }
                canvas.drawRect(itemRect, paint)

                // Apply the same border width as in the preview
                val borderRect = RectF(
                    left + borderWidth,
                    top + borderWidth,
                    right - borderWidth,
                    bottom - borderWidth
                )

                // Create clipping path for the image area
                val borderPath = Path()
                borderPath.addRoundRect(borderRect, cornerRadius, cornerRadius, Path.Direction.CW)

                // Apply clipping to keep image within borders
                canvas.clipPath(borderPath)

                // Load the image - will use centerCrop style like in the adapter
                val inputStream = contentResolver.openInputStream(selectedUris[i])
                val imageBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (imageBitmap != null) {
                    // Apply centerCrop logic manually to match preview
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
            }
        }
        return bitmap
    }

    // Calculate destination rectangle to maintain aspect ratio
    private fun calculateDestRect(imageAspect: Float, containerRect: RectF): RectF {
        val containerWidth = containerRect.width()
        val containerHeight = containerRect.height()
        val containerAspect = containerWidth / containerHeight

        var destWidth: Float
        var destHeight: Float

        if (imageAspect > containerAspect) {
            // Image is wider than container - fit width
            destWidth = containerWidth
            destHeight = destWidth / imageAspect
        } else {
            // Image is taller than container - fit height
            destHeight = containerHeight
            destWidth = destHeight * imageAspect
        }

        // Center the image in the container
        val destLeft = containerRect.left + (containerWidth - destWidth) / 2
        val destTop = containerRect.top + (containerHeight - destHeight) / 2

        return RectF(
            destLeft,
            destTop,
            destLeft + destWidth,
            destTop + destHeight
        )
    }


    private fun navigateToSaveShareActivity() {
        try {
            val config = CollageConfig(
                imageUris = selectedUris.map { it.toString() },
                spanCount = spanCount,
                borderWidth = borderWidth,
                cornerRadius = cornerRadius,
                backgroundColor = backgroundColor
            )

            val intent = Intent(this, SaveShareActivity::class.java).apply {
                putExtra("collage_config", config)
            }
            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error navigating to SaveShare: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}