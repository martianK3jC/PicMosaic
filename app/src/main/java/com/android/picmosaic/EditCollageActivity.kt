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
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.android.picmosaic.utils.CollageConfig
import androidx.recyclerview.widget.ItemTouchHelper
import com.android.picmosaic.utils.ColorPickerDialog
import java.io.File
import java.io.FileOutputStream

class EditCollageActivity : Activity() {
    private lateinit var layoutButton: ImageButton
    private lateinit var backgroundButton: ImageButton
    private lateinit var borderButton: ImageButton
    private lateinit var saveButton: ImageButton
    private lateinit var backButton: ImageButton
    private lateinit var cropButton: ImageButton

    private lateinit var layoutControls: LinearLayout
    private lateinit var backgroundControls: LinearLayout
    private lateinit var borderControls: LinearLayout
    private lateinit var borderColorControls: LinearLayout

    private lateinit var borderWidthSeekBar: SeekBar
    private lateinit var cornerRadiusSeekBar: SeekBar
    private lateinit var borderColorPreview: View
    private lateinit var backgroundColorPreview: View
    private lateinit var collageRecyclerView: RecyclerView
    private lateinit var adapter: CollageAdapter

    private var borderWidth = 8f
    private var cornerRadius = 0f
    private var borderColor = Color.WHITE
    private var backgroundColor = Color.WHITE
    private var selectedUris: MutableList<Uri> = mutableListOf()
    private var originalUris: MutableList<Uri> = mutableListOf() // Store original URIs
    private var spanCount = 3
    private var layoutType = "grid"
    private var croppingPosition = -1

    private val COLLAGE_WIDTH = 1080
    private val COLLAGE_HEIGHT = 1080
    private val CROP_REQUEST_CODE = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_collage)

        selectedUris = (intent.getStringArrayListExtra("imageUris")?.map { Uri.parse(it) } ?: listOf()).toMutableList()
        originalUris = selectedUris.toMutableList() // Initialize original URIs

        if (selectedUris.size > 6) {
            Toast.makeText(this, "Cannot create collage with more than 6 photos", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        initializeViews()
        setupRecyclerView()
        setupClickListeners()
        setupSeekBarListeners()
        setupLayoutControls()

        setDefaultLayout()
    }

    private fun initializeViews() {
        layoutButton = findViewById(R.id.layoutButton)
        backgroundButton = findViewById(R.id.backgroundButton)
        borderButton = findViewById(R.id.borderButton)
        saveButton = findViewById(R.id.saveButton)
        backButton = findViewById(R.id.backButton)
        cropButton = findViewById(R.id.cropButton)

        layoutControls = findViewById(R.id.layoutControls)
        backgroundControls = findViewById(R.id.backgroundControls)
        borderControls = findViewById(R.id.borderControls)
        borderColorControls = findViewById(R.id.borderColorControls)

        borderWidthSeekBar = findViewById(R.id.borderWidthSeekBar)
        cornerRadiusSeekBar = findViewById(R.id.cornerRadiusSeekBar)
        borderColorPreview = findViewById(R.id.borderColorPreview)
        backgroundColorPreview = findViewById(R.id.backgroundColorPreview)

        collageRecyclerView = findViewById(R.id.collageRecyclerView)
    }

    private fun setupRecyclerView() {
        if (selectedUris.isEmpty()) {
            Toast.makeText(this, "No images selected to display", Toast.LENGTH_LONG).show()
            return
        }

        adapter = CollageAdapter(selectedUris, originalUris).apply {
            setOnImageClickListener { position ->
                croppingPosition = position
                startCropActivity(originalUris[position]) // Use original URI for cropping
            }
        }
        adapter.setBorderProperties(borderWidth.toInt(), cornerRadius.toInt(), borderColor)
        adapter.setRecyclerView(collageRecyclerView)

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val topBarHeight = 56
        val controlSectionHeight = 125
        val bottomNavHeight = 90
        val density = displayMetrics.density
        val totalFixedHeightPx = (topBarHeight + controlSectionHeight + bottomNavHeight) * density
        val screenHeight = displayMetrics.heightPixels
        val availableHeight = screenHeight - totalFixedHeightPx
        val recyclerViewSize = minOf(screenWidth, availableHeight.toInt())

        val params = collageRecyclerView.layoutParams
        params.width = recyclerViewSize
        params.height = recyclerViewSize
        collageRecyclerView.layoutParams = params

        val scaleFactor = recyclerViewSize.toFloat() / COLLAGE_WIDTH.toFloat()
        val padding = (COLLAGE_WIDTH * 0.02f * scaleFactor).toInt()
        collageRecyclerView.setPadding(padding, padding, padding, padding)
        collageRecyclerView.clipToPadding = false

        collageRecyclerView.setBackgroundColor(backgroundColor)
        collageRecyclerView.adapter = adapter
        collageRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
        collageRecyclerView.visibility = View.VISIBLE

        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(
            ItemTouchHelper.UP or ItemTouchHelper.DOWN or ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT,
            0
        ) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                adapter.swapItems(fromPosition, toPosition)
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {}

            override fun isLongPressDragEnabled(): Boolean = true

            override fun isItemViewSwipeEnabled(): Boolean = false

            override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
                super.onSelectedChanged(viewHolder, actionState)
                if (actionState == ItemTouchHelper.ACTION_STATE_DRAG) {
                    viewHolder?.itemView?.alpha = 0.7f
                }
            }

            override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
                super.clearView(recyclerView, viewHolder)
                viewHolder.itemView.alpha = 1.0f
            }
        }

        val itemTouchHelper = ItemTouchHelper(itemTouchHelperCallback)
        itemTouchHelper.attachToRecyclerView(collageRecyclerView)
    }

    private fun startCropActivity(uri: Uri) {
        val intent = Intent(this, CropActivity::class.java).apply {
            putExtra("image_uri", uri)
        }
        startActivityForResult(intent, CROP_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == CROP_REQUEST_CODE && resultCode == RESULT_OK && croppingPosition != -1) {
            try {
                val croppedUri = data?.getParcelableExtra<Uri>("cropped_uri")
                if (croppedUri != null) {
                    selectedUris[croppingPosition] = croppedUri
                    adapter.notifyItemChanged(croppingPosition)
                } else {
                    Toast.makeText(this, "No cropped image returned", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error retrieving cropped image", Toast.LENGTH_SHORT).show()
            }
            croppingPosition = -1
        } else if (resultCode != RESULT_CANCELED) {
            Toast.makeText(this, "Cropping failed", Toast.LENGTH_SHORT).show()
            croppingPosition = -1
        }
    }

    private fun setupLayoutControls() {
        layoutControls.removeAllViews()
        val photoCount = selectedUris.size

        val layouts = when (photoCount) {
            2 -> listOf(
                LayoutOption("2x1", 2, "2x1"),
                LayoutOption("1x2", 1, "1x2")
            )
            3 -> listOf(
                LayoutOption("3x1", 3, "3x1"),
                LayoutOption("1x3", 1, "1x3")
            )
            4 -> listOf(
                LayoutOption("2x2", 2, "2x2"),
                LayoutOption("4x1", 4, "4x1"),
                LayoutOption("1x4", 1, "1x4")
            )
            5 -> listOf(
                LayoutOption("5x1", 5, "5x1"),
                LayoutOption("1x5", 1, "1x5"),
                LayoutOption("2x3", 2, "2x3")
            )
            6 -> listOf(
                LayoutOption("3x2", 3, "3x2"),
                LayoutOption("2x3", 2, "2x3"),
                LayoutOption("1x6", 1, "1x6")
            )
            else -> listOf()
        }

        layouts.forEach { layout ->
            val button = Button(this).apply {
                text = layout.label
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
                ).apply {
                    setMargins(4, 4, 4, 4)
                }
                setOnClickListener {
                    spanCount = layout.spanCount
                    layoutType = layout.type
                    collageRecyclerView.layoutManager = GridLayoutManager(this@EditCollageActivity, spanCount)
                    adapter.setLayoutType(layout.type)
                }
            }
            layoutControls.addView(button)
        }
    }

    private fun setDefaultLayout() {
        val photoCount = selectedUris.size
        val layouts = when (photoCount) {
            2 -> listOf(
                LayoutOption("2x1", 2, "2x1"),
                LayoutOption("1x2", 1, "1x2")
            )
            3 -> listOf(
                LayoutOption("3x1", 3, "3x1"),
                LayoutOption("1x3", 1, "1x3")
            )
            4 -> listOf(
                LayoutOption("2x2", 2, "2x2"),
                LayoutOption("4x1", 4, "4x1"),
                LayoutOption("1x4", 1, "1x4")
            )
            5 -> listOf(
                LayoutOption("5x1", 5, "5x1"),
                LayoutOption("1x5", 1, "1x5"),
                LayoutOption("2x3", 2, "2x3")
            )
            6 -> listOf(
                LayoutOption("3x2", 3, "3x2"),
                LayoutOption("2x3", 2, "2x3"),
                LayoutOption("1x6", 1, "1x6")
            )
            else -> listOf()
        }

        if (layouts.isNotEmpty()) {
            val defaultLayout = layouts[0]
            spanCount = defaultLayout.spanCount
            layoutType = defaultLayout.type
            collageRecyclerView.layoutManager = GridLayoutManager(this, spanCount)
            adapter.setLayoutType(layoutType)
        }
    }

    private fun setupClickListeners() {
        layoutButton.setOnClickListener {
            showLayoutControls()
        }

        saveButton.setOnClickListener {
            val collageBitmap = createCollageBitmap()
            navigateToSaveShareActivity()
        }

        backgroundButton.setOnClickListener {
            showBackgroundControls()
        }

        borderButton.setOnClickListener {
            showBorderControls()
        }

        cropButton.setOnClickListener {
            Toast.makeText(this, "Select an image to crop", Toast.LENGTH_SHORT).show()
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
                updateColorPreview(backgroundColorPreview, backgroundColor)
                collageRecyclerView.setBackgroundColor(backgroundColor)
            }
        }

        backgroundColorPreview.setOnClickListener {
            val dialog = ColorPickerDialog(this, backgroundColor) { color ->
                backgroundColor = color
                updateColorPreview(backgroundColorPreview, backgroundColor)
                collageRecyclerView.setBackgroundColor(backgroundColor)
            }
            dialog.show()
        }

        borderColorPreview.setOnClickListener {
            val dialog = ColorPickerDialog(this, borderColor) { color ->
                borderColor = color
                updateColorPreview(borderColorPreview, borderColor)
                adapter.setBorderProperties(borderWidth.toInt(), cornerRadius.toInt(), borderColor)
            }
            dialog.show()
        }
    }

    private fun updateColorPreview(previewView: View, color: Int) {
        val drawable = GradientDrawable()
        drawable.setShape(GradientDrawable.RECTANGLE)
        drawable.setStroke(1, Color.GRAY)
        drawable.setColor(color)
        previewView.background = drawable
    }

    private fun setupSeekBarListeners() {
        borderWidthSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                borderWidth = progress.toFloat()

                val displayMetrics = resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val topBarHeight = 56
                val controlSectionHeight = 125
                val bottomNavHeight = 90
                val density = displayMetrics.density
                val totalFixedHeightPx = (topBarHeight + controlSectionHeight + bottomNavHeight) * density
                val screenHeight = displayMetrics.heightPixels
                val availableHeight = screenHeight - totalFixedHeightPx
                val recyclerViewSize = minOf(screenWidth, availableHeight.toInt())
                val scaleFactor = recyclerViewSize.toFloat() / COLLAGE_WIDTH.toFloat()

                val scaledBorderWidth = (borderWidth * scaleFactor).toInt()
                val scaledCornerRadius = (cornerRadius * scaleFactor).toInt()
                adapter.setBorderProperties(scaledBorderWidth, scaledCornerRadius, borderColor)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        cornerRadiusSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                cornerRadius = progress.toFloat()

                val displayMetrics = resources.displayMetrics
                val screenWidth = displayMetrics.widthPixels
                val topBarHeight = 56
                val controlSectionHeight = 125
                val bottomNavHeight = 90
                val density = displayMetrics.density
                val totalFixedHeightPx = (topBarHeight + controlSectionHeight + bottomNavHeight) * density
                val screenHeight = displayMetrics.heightPixels
                val availableHeight = screenHeight - totalFixedHeightPx
                val recyclerViewSize = minOf(screenWidth, availableHeight.toInt())
                val scaleFactor = recyclerViewSize.toFloat() / COLLAGE_WIDTH.toFloat()

                val scaledBorderWidth = (borderWidth * scaleFactor).toInt()
                val scaledCornerRadius = (cornerRadius * scaleFactor).toInt()
                adapter.setBorderProperties(scaledBorderWidth, scaledCornerRadius, borderColor)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun showLayoutControls() {
        layoutControls.visibility = View.VISIBLE
        backgroundControls.visibility = View.GONE
        borderControls.visibility = View.GONE
        borderColorControls.visibility = View.GONE
    }

    private fun showBackgroundControls() {
        layoutControls.visibility = View.GONE
        backgroundControls.visibility = View.VISIBLE
        borderControls.visibility = View.GONE
        borderColorControls.visibility = View.GONE
    }

    private fun showBorderControls() {
        layoutControls.visibility = View.GONE
        backgroundControls.visibility = View.GONE
        borderControls.visibility = View.VISIBLE
        borderColorControls.visibility = View.VISIBLE
    }

    private fun createCollageBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(COLLAGE_WIDTH, COLLAGE_HEIGHT, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        canvas.drawColor(backgroundColor)

        val padding = (bitmap.width * 0.02f).toInt()
        val availableWidth = bitmap.width - 2 * padding
        val availableHeight = bitmap.height - 2 * padding

        val itemWidth = availableWidth / spanCount
        val rowCount = (selectedUris.size + spanCount - 1) / spanCount
        val itemHeight = if (rowCount > 0) availableHeight / rowCount else availableHeight

        for (i in selectedUris.indices) {
            try {
                val column = i % spanCount
                val row = i / spanCount

                val left = padding + column * itemWidth
                val top = padding + row * itemHeight
                val right = left + itemWidth
                val bottom = top + itemHeight

                val itemPath = Path()
                val itemRect = RectF(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat())
                itemPath.addRoundRect(itemRect, cornerRadius, cornerRadius, Path.Direction.CW)

                canvas.save()
                canvas.clipPath(itemPath)

                val paint = Paint().apply {
                    color = borderColor
                    style = Paint.Style.FILL
                }
                canvas.drawRect(itemRect, paint)

                val borderRect = RectF(
                    left + borderWidth,
                    top + borderWidth,
                    right - borderWidth,
                    bottom - borderWidth
                )

                val borderPath = Path()
                borderPath.addRoundRect(borderRect, cornerRadius, cornerRadius, Path.Direction.CW)
                canvas.clipPath(borderPath)

                val inputStream = contentResolver.openInputStream(selectedUris[i])
                val imageBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()

                if (imageBitmap != null) {
                    val imageWidth = imageBitmap.width.toFloat()
                    val imageHeight = imageBitmap.height.toFloat()
                    val imageAspect = imageWidth / imageHeight
                    val borderAspect = borderRect.width() / borderRect.height()

                    val srcRect: Rect
                    val dstRect = RectF(borderRect)

                    if (imageAspect > borderAspect) {
                        val newWidth = imageHeight * borderAspect
                        val xOffset = (imageWidth - newWidth) / 2
                        srcRect = Rect(
                            xOffset.toInt(),
                            0,
                            (xOffset + newWidth).toInt(),
                            imageHeight.toInt()
                        )
                    } else {
                        val newHeight = imageWidth / borderAspect
                        val yOffset = (imageHeight - newHeight) / 2
                        srcRect = Rect(
                            0,
                            yOffset.toInt(),
                            imageWidth.toInt(),
                            (yOffset + newHeight).toInt()
                        )
                    }

                    canvas.drawBitmap(imageBitmap, srcRect, dstRect, null)
                    imageBitmap.recycle()
                }
                canvas.restore()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return bitmap
    }

    private fun navigateToSaveShareActivity() {
        try {
            val config = CollageConfig(
                imageUris = selectedUris.map { it.toString() },
                spanCount = spanCount,
                borderWidth = borderWidth,
                cornerRadius = cornerRadius,
                backgroundColor = backgroundColor,
                borderColor = borderColor
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

    private data class LayoutOption(val label: String, val spanCount: Int, val type: String)
}