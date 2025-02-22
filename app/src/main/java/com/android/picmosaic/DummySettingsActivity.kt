package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

class DummySettingsActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dummy_settings_page)

        val profileButton = findViewById<Button>(R.id.profileButton)

        //To go to Profile page
        profileButton.setOnClickListener {
            Toast.makeText(this, "Loading Profile page...", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }
    }
}