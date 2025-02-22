package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class LoginActivity : Activity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var forgotPasswordText: TextView
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        initializeViews()
        checkLoginStatus()

        // 🔹 Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!DummyUserData.validateCredentials(email, password)) {
                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 🔹 Save login state
            saveLoginData(email)

            // 🔹 Navigate to Home Page
            Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
            navigateToHome()
        }

        forgotPasswordText.setOnClickListener {
            Toast.makeText(this, "Oh naur", Toast.LENGTH_SHORT).show()
        }

        signupButton.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
            finish()
        }
    }

    // ✅ Check if user is already logged in
    private fun checkLoginStatus() {
        val sharedPref = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val savedEmail = sharedPref.getString("current_user_email", null)
        if (savedEmail != null) {
            navigateToHome()
        }
    }

    // ✅ Save login data so the profile uses it
    private fun saveLoginData(email: String) {
        val sharedPref = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        sharedPref.edit().putString("current_user_email", email).apply()
    }

    private fun navigateToHome() {
        startActivity(Intent(this, DummyHomeActivity::class.java))
        finish()
    }

    private fun initializeViews(){
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
    }
}
