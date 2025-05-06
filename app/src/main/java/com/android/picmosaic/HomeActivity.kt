package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast

class HomeActivity : Activity() {

    private val PICK_IMAGES_REQUEST = 1
    private val MAX_PHOTOS = 6
    private val STORAGE_PERMISSION_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.homepage)
        checkLoginStatus()

        val settingsButton = findViewById<ImageButton>(R.id.settingsButton)
        val createCollageButton = findViewById<ImageButton>(R.id.create_collage_button)

        settingsButton.setOnClickListener {
            val intent = Intent(this, SettingsActivity::class.java)
            startActivity(intent)
        }

        createCollageButton.setOnClickListener {
            if (checkStoragePermission()) {
                openGallery()
            } else {
                requestStoragePermission()
            }
        }
    }

    private fun checkLoginStatus() {
        val sharedPref = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val savedEmail = sharedPref.getString("current_user_email", null)

        if (savedEmail == null) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun checkStoragePermission(): Boolean {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_MEDIA_IMAGES) == android.content.pm.PackageManager.PERMISSION_GRANTED
        } else {
            androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) == android.content.pm.PackageManager.PERMISSION_GRANTED
        }
    }

    private fun requestStoragePermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_MEDIA_IMAGES),
                STORAGE_PERMISSION_CODE
            )
        } else {
            androidx.core.app.ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                STORAGE_PERMISSION_CODE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED) {
                openGallery()
            } else {
                Toast.makeText(this, "Permission denied. Cannot access photos.", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "image/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
            putExtra(Intent.EXTRA_MIME_TYPES, arrayOf("image/jpeg", "image/png"))
        }
        startActivityForResult(Intent.createChooser(intent, "Select Pictures"), PICK_IMAGES_REQUEST)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGES_REQUEST && resultCode == RESULT_OK && data != null) {
            val selectedUris = mutableListOf<String>()
            val clipData = data.clipData
            if (clipData != null) {
                val count = minOf(clipData.itemCount, MAX_PHOTOS)
                for (i in 0 until count) {
                    val uri = clipData.getItemAt(i).uri
                    selectedUris.add(uri.toString())
                }
            } else {
                val uri = data.data
                if (uri != null) {
                    selectedUris.add(uri.toString())
                }
            }

            if (selectedUris.size < 2 || selectedUris.size > MAX_PHOTOS) {
                Toast.makeText(this, "Please select 2 to 6 photos", Toast.LENGTH_LONG).show()
                return
            }

            val intent = Intent(this, EditCollageActivity::class.java).apply {
                putStringArrayListExtra("imageUris", ArrayList(selectedUris))
            }
            startActivity(intent)
        }
    }
}