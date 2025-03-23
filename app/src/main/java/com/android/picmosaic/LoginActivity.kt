package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.picmosaic.utils.showMessage
import com.android.picmosaic.utils.toast
import com.android.picmosaic.utils.txt

class LoginActivity : Activity() {
    private lateinit var emailEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var forgotPasswordText: TextView
    private lateinit var loginButton: Button
    private lateinit var signupButton: Button
    private lateinit var linkTerms: TextView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_page)

        initializeViews()
        checkLoginStatus()

        val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)

        intent?.let {
            val newEmail = it.getStringExtra("registered_email") ?: ""
            val newPassword = it.getStringExtra("registered_password") ?: ""

            if (newEmail.isNotEmpty() && newPassword.isNotEmpty()) {
                sharedPreferences.edit()
                    .putString("registered_email", newEmail)
                    .putString("registered_password", newPassword)
                    .apply()

                emailEditText.setText(newEmail)
                passwordEditText.setText(newPassword)
            }
        }

       // Ensure autofill happens even if intent doesn't provide new data
        val savedEmail = sharedPreferences.getString("registered_email", "")
        val savedPassword = sharedPreferences.getString("registered_password", "")

        if (!savedEmail.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            emailEditText.setText(savedEmail)
            passwordEditText.setText(savedPassword)
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

        linkTerms.setOnClickListener {
            showMessage("Agree nalang jud")
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

        if (!savedEmail.isNullOrEmpty()) {
            navigateToHome() // ✅ Automatically log in if session exists
        } else {
            val registeredEmail = sharedPref.getString("registered_email", null)
            val registeredPassword = sharedPref.getString("password_$registeredEmail", null) // 🔹 Get correct password

            if (!registeredEmail.isNullOrEmpty() && !registeredPassword.isNullOrEmpty()) {
                emailEditText.setText(registeredEmail)
                passwordEditText.setText(registeredPassword)
            }
        }
    }



    // ✅ Save login data so the profile uses it
    private fun saveLoginData(email: String) {
        val sharedPref = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val editor = sharedPref.edit()

        // ✅ Get user profile details from DummyUserData
        val userProfile = DummyUserData.getUserProfile(email, this)
        val userPassword = DummyUserData.getUserPassword(email, this) ?: ""  // 🔹 Ensure password is retrieved

        if (userProfile != null) {
            val profileImagePath = sharedPref.getString("profile_image_path_$email", null) // ✅ Keep existing image path

            editor.putString("current_user_email", email) // 🔹 Save logged-in user
            editor.putString("user_first_name_$email", userProfile.firstName) // ✅ Save First Name
            editor.putString("password_$email", userPassword) // ✅ Save password correctly
            editor.putString("profile_image_path_$email", profileImagePath) // ✅ Ensure image path is saved

            editor.apply()
        } else {
            toast("Error: User not found.")
        }
    }




    private fun navigateToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }

    private fun initializeViews(){
        linkTerms = findViewById(R.id.tvTermsLink)
        emailEditText = findViewById(R.id.emailEditText)
        passwordEditText = findViewById(R.id.passwordEditText)
        forgotPasswordText = findViewById(R.id.forgotPasswordText)
        loginButton = findViewById(R.id.loginButton)
        signupButton = findViewById(R.id.signupButton)
    }
}
