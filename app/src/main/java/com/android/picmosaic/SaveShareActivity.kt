package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.io.File
import java.io.FileOutputStream

class SaveShareActivity : Activity() {

        private lateinit var photo1: ImageView
        private lateinit var photo2: ImageView
        private lateinit var photo3: ImageView
        private lateinit var photo4: ImageView
        private lateinit var photo5: ImageView
        private lateinit var photo6: ImageView
        private lateinit var backButton: ImageButton
        private lateinit var shareFacebook: ImageButton
        private lateinit var shareInstagram: ImageButton
        private lateinit var shareSnapchat: ImageButton
        private lateinit var openMore: ImageButton
        private lateinit var saveButton: Button
        private lateinit var createAnotherButton: Button

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_save_share)

            // Initialize ImageViews
            photo1 = findViewById(R.id.photo1)
            photo2 = findViewById(R.id.photo2)
            photo3 = findViewById(R.id.photo3)
            photo4 = findViewById(R.id.photo4)
            photo5 = findViewById(R.id.photo5)
            photo6 = findViewById(R.id.photo6)

            // Initialize Buttons
            backButton = findViewById(R.id.back_button)
            shareFacebook = findViewById(R.id.share_facebook)
            shareInstagram = findViewById(R.id.share_instagram)
            shareSnapchat = findViewById(R.id.share_snapchat)
            openMore = findViewById(R.id.open_more)
            saveButton = findViewById(R.id.save_button)
            createAnotherButton = findViewById(R.id.create_another_button)

            // Set up button listeners
            backButton.setOnClickListener { finish() }
            shareFacebook.setOnClickListener { shareToApp("com.facebook.katana") }
            shareInstagram.setOnClickListener { shareToApp("com.instagram.android") }
            shareSnapchat.setOnClickListener { shareToApp("com.snapchat.android") }
            openMore.setOnClickListener { shareToApp("com.google.android.gm") }
            saveButton.setOnClickListener { saveCollage() }
            createAnotherButton.setOnClickListener {
                val intent = Intent(this, HomeActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            }
        }

        private fun createCollageBitmap(): Bitmap {
            val bitmap = Bitmap.createBitmap(300, 200, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val imageViews = listOf(photo1, photo2, photo3, photo4, photo5, photo6)
            imageViews.forEachIndexed { i, imageView ->
                val drawable = imageView.drawable as? BitmapDrawable
                if (drawable != null) {
                    val left = (i % 3) * 100
                    val top = (i / 3) * 100
                    canvas.drawBitmap(drawable.bitmap, left.toFloat(), top.toFloat(), null)
                }
            }
            return bitmap
        }

        private fun countPhotos(): Int {
            val imageViews = listOf(photo1, photo2, photo3, photo4, photo5, photo6)
            return imageViews.count { it.drawable != null }
        }

        private fun saveCollage() {
            val photoCount = countPhotos()
            if (photoCount < 2) {
                Toast.makeText(this, "Add at least 2 photos to save or share", Toast.LENGTH_SHORT).show()
                return
            }

            val bitmap = createCollageBitmap()
            val fileName = "collage_${System.currentTimeMillis()}.png"
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(directory, fileName)

            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }

                val mediaScanIntent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
                val contentUri = Uri.fromFile(file)
                mediaScanIntent.data = contentUri
                sendBroadcast(mediaScanIntent)

                Toast.makeText(this, "Collage saved to Pictures/$fileName", Toast.LENGTH_LONG).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error saving collage: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }

        private fun shareToApp(packageName: String) {
            val photoCount = countPhotos()
            if (photoCount < 2) {
                Toast.makeText(this, "Add at least 2 photos to save or share", Toast.LENGTH_SHORT).show()
                return
            }

            val bitmap = createCollageBitmap()
            val fileName = "collage_${System.currentTimeMillis()}.png"
            val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            val file = File(directory, fileName)

            try {
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                    out.flush()
                }

                val uri = FileProvider.getUriForFile(this, "${packageName}.provider", file)
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    setPackage(packageName)
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                try {
                    startActivity(shareIntent)
                } catch (e: Exception) {
                    Toast.makeText(this, "App not installed", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error sharing collage: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }