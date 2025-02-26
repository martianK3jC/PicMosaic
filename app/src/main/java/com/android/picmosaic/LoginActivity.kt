package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.android.picmosaic.utils.isRegistered
import com.android.picmosaic.utils.isValidEntry
import com.android.picmosaic.utils.toast

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

        intent?.let{
            it.getBooleanExtra("isValidRegistered", true).let{
                isValidRegistered -> isGoodtoLogin
            }
            it.getStringExtra("email")?.let{
                email -> emailEditText.setText(email)
            }
            it.getStringExtra("password")?.let{
                password -> passwordEditText.setText(password)
            }
        }

        // 🔹 Handle login button click
        loginButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            val password = passwordEditText.text.toString().trim()

            if(emailEditText.isValidEntry()){
                this.toast("Please enter email and password")
                return@setOnClickListener
            }else if(passwordEditText.isValidEntry()){
                this.toast("Please enter password")
            }

            if(isGoodtoLogin){
                Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                navigateToHome()
            }

//            if (!DummyUserData.validateCredentials(email, password)) {
//                Toast.makeText(this, "Invalid email or password", Toast.LENGTH_SHORT).show()
//                return@setOnClickListener
//            }

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
