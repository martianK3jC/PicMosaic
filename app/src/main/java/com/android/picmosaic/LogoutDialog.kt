package com.android.picmosaic

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Window
import android.view.WindowManager
import android.widget.Button

class LogoutDialog(private val activity: Activity) : Dialog(activity) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.logout_dialog)

        val noButton = findViewById<Button>(R.id.no_button)
        val yesButton = findViewById<Button>(R.id.yes_button)

        //Click the "NO" button
        noButton.setOnClickListener {
            dismiss()
        }

        //Click the "YES" button
        yesButton.setOnClickListener {
            val sharedPref = activity.getSharedPreferences("PicMosaic", Context.MODE_PRIVATE)
            val profileImagePath = sharedPref.getString("profile_image_path", null) // Keep image

            // Clear everything except profile image
            sharedPref.edit().clear().apply()

            // Restore profile image path
            sharedPref.edit().putString("profile_image_path", profileImagePath).apply()

            val intent = Intent(context, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            context.startActivity(intent)
            dismiss()
            activity.finish()
        }
    }
}