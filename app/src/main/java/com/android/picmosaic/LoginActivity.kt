package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.picmosaic.utils.isRegistered
import com.android.picmosaic.utils.isNotValid
import com.android.picmosaic.utils.toast
import com.android.picmosaic.utils.txt

class LoginActivity : Activity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var forgotPasswordText: TextView
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    var isGoodtoLogin = false;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        initializeViews()
        checkLoginStatus()

        intent?.let {
            val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
            val savedEmail = sharedPreferences.getString("registered_email", "")
            val savedPassword = sharedPreferences.getString("registered_password", "")

            if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
                emailEditText.setText(savedEmail)
                passwordEditText.setText(savedPassword)
            }
        }


        // 🔹 Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.txt()
            val password = passwordEditText.txt()

            if (email.isEmpty() || password.isEmpty()) {
                toast("Please fill out the forms completely")
                return@setOnClickListener
            }

            // ✅ Validate with stored users
            if (!DummyUserData.validateCredentials(email, password, this)) {
                toast("Invalid email or password")
                return@setOnClickListener
            }

            // ✅ Save login session
            saveLoginData(email)

            // ✅ Navigate to Home Page
            toast("Login successful!")
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

        // ✅ Get user profile details from DummyUserData
        val userProfile = DummyUserData.getUserProfile(email, this)

        if (userProfile != null) {
            sharedPref.edit()
                .putString("current_user_email", email) // 🔹 Save logged-in user
                .putString("user_first_name_$email", userProfile.firstName) // ✅ Save First Name
                .putString("password_$email", DummyUserData.getUserPassword(email, this) ?: "")
                .apply()
        }
    }

    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
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
