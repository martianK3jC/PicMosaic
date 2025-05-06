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
            (activity as? ProfileActivity)?.handleLogout() // Call the function from ProfileActivity
            dismiss() // Close dialog
        }


    }
}