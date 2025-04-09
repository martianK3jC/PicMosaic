package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class DemoCollageMakerActivity : Activity() {

        private lateinit var photo1: ImageView
        private lateinit var photo2: ImageView
        private lateinit var photo3: ImageView
        private lateinit var photo4: ImageView
        private lateinit var photo5: ImageView
        private lateinit var photo6: ImageView
        private lateinit var downloadButton: ImageButton

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_demo_collage_maker)

            // Initialize ImageViews
            photo1 = findViewById(R.id.photo1)
            photo2 = findViewById(R.id.photo2)
            photo3 = findViewById(R.id.photo3)
            photo4 = findViewById(R.id.photo4)
            photo5 = findViewById(R.id.photo5)
            photo6 = findViewById(R.id.photo6)

            // Initialize Download Button
            downloadButton = findViewById(R.id.download_button)
            downloadButton.setOnClickListener {
                val intent = Intent(this, SaveShareActivity::class.java)
                // For demo, pass a mock photo count (all placeholders are present)
                intent.putExtra("photo_count", 6)
                startActivity(intent)
            }
        }
    }