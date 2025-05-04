package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.RectF
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

class CropActivity : Activity() {
    private lateinit var imageView: ImageView
    private lateinit var cropButton: Button
    private lateinit var cancelButton: Button
    private lateinit var overlayView: CropOverlayView
    private var imageUri: Uri? = null
    private var bitmap: Bitmap? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_crop)

        imageView = findViewById(R.id.cropImageView)
        cropButton = findViewById(R.id.cropConfirmButton)
        cancelButton = findViewById(R.id.cropCancelButton)
        overlayView = findViewById(R.id.cropOverlayView)

        imageUri = intent.getParcelableExtra("image_uri")
        if (imageUri == null) {
            Toast.makeText(this, "No image provided", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadImage()
        setupButtons()
    }

    private fun loadImage() {
        try {
            val inputStream = contentResolver.openInputStream(imageUri!!)
            bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap == null) {
                Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
            imageView.setImageBitmap(bitmap)
            // Initialize crop rectangle to cover the image
            overlayView.setCropRect(RectF(0f, 0f, imageView.width.toFloat(), imageView.height.toFloat()))
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error loading image: ${e.message}", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun setupButtons() {
        cropButton.setOnClickListener {
            cropImage()
        }
        cancelButton.setOnClickListener {
            setResult(RESULT_CANCELED)
            finish()
        }
    }

    private fun cropImage() {
        bitmap?.let { bmp ->
            try {
                // Scale crop rectangle to bitmap coordinates
                val imageWidth = bmp.width
                val imageHeight = bmp.height
                val viewWidth = imageView.width
                val viewHeight = imageView.height
                val scaleX = imageWidth.toFloat() / viewWidth
                val scaleY = imageHeight.toFloat() / viewHeight

                val cropRect = overlayView.getCropRect()
                val scaledRect = RectF(
                    cropRect.left * scaleX,
                    cropRect.top * scaleY,
                    cropRect.right * scaleX,
                    cropRect.bottom * scaleY
                )

                // Ensure crop rectangle is within bounds
                scaledRect.left = scaledRect.left.coerceIn(0f, imageWidth.toFloat())
                scaledRect.top = scaledRect.top.coerceIn(0f, imageHeight.toFloat())
                scaledRect.right = scaledRect.right.coerceIn(scaledRect.left, imageWidth.toFloat())
                scaledRect.bottom = scaledRect.bottom.coerceIn(scaledRect.top, imageHeight.toFloat())

                // Crop the bitmap
                val croppedBitmap = Bitmap.createBitmap(
                    bmp,
                    scaledRect.left.toInt(),
                    scaledRect.top.toInt(),
                    (scaledRect.right - scaledRect.left).toInt().coerceAtLeast(1),
                    (scaledRect.bottom - scaledRect.top).toInt().coerceAtLeast(1)
                )

                // Save cropped image to cache
                val file = File(cacheDir, "cropped_${System.currentTimeMillis()}.png")
                FileOutputStream(file).use { out ->
                    croppedBitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }

                // Return result
                val resultIntent = Intent().apply {
                    putExtra("cropped_uri", FileProvider.getUriForFile(this@CropActivity, "com.android.picmosaic.provider", file))
                }
                setResult(RESULT_OK, resultIntent)
                finish()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error cropping image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } ?: run {
            Toast.makeText(this, "No image to crop", Toast.LENGTH_SHORT).show()
        }
    }
}