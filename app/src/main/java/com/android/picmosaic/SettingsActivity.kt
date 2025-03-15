package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.io.File
class SettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_page)

        loadProfileData() // ✅ Load data when opening settings

        val developersButton = findViewById<ImageButton>(R.id.developersButton)
        val profileButton = findViewById<ImageButton>(R.id.profileButton)

        profileButton.setOnClickListener {
            Toast.makeText(this, "Loading Profile page...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }

        developersButton.setOnClickListener {
            Toast.makeText(this, "Developers Page", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DevelopersActivity::class.java)
            startActivity(intent)
        }

        val buttonBack = findViewById<ImageButton>(R.id.arrow_back_button)
        buttonBack.setOnClickListener {
            Log.e("Back", "Going Back")
            Toast.makeText(this, "Button is clicked", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, HomeActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()
        loadProfileData() // ✅ Refresh user info when returning to Settings page
    }

    private fun loadProfileData() {
        val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val email = sharedPreferences.getString("current_user_email", null) ?: return

        // ✅ Load First Name
        val usernameTextView = findViewById<TextView>(R.id.profile_username)
        val firstName = sharedPreferences.getString("user_first_name_$email", "User")
        usernameTextView.text = firstName

        // ✅ Load Profile Picture
        val profileImageView = findViewById<de.hdodenhof.circleimageview.CircleImageView>(R.id.profile_image)
        val savedImagePath = sharedPreferences.getString("profile_image_path_$email", null)

        if (!savedImagePath.isNullOrEmpty()) {
            val file = File(savedImagePath)
            if (file.exists()) {
                BitmapFactory.decodeFile(savedImagePath)?.let { bitmap ->
                    profileImageView.setImageBitmap(bitmap)
                }
            }
        }
    }
}
