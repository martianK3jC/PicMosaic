package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast


class DummyHomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.dummy_home_page)
        checkLoginStatus()
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        Toast.makeText(this, "Welcome to the Home Page!", Toast.LENGTH_SHORT).show()

        settingsButton.setOnClickListener {
            val intent = Intent(this, DummySettingsActivity::class.java)
            startActivity(intent)
        }
    }

    private fun checkLoginStatus(){
        val sharedPref = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val savedEmail = sharedPref.getString("current_user_email",null)

        if(savedEmail == null){
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}