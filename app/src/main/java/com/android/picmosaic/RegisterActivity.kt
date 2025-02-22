package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil

class RegisterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)

        val registerButton = findViewById<Button>(R.id.registerButton)
        val registerPageLoginButton = findViewById<Button>(R.id.registerPageLoginButton)
        val registerEmailEditText = findViewById<EditText>(R.id.registerEmailEditText)
        val registerPasswordEditText = findViewById<EditText>(R.id.registerPasswordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)

        fun showError(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_LONG).show()
        }

        fun showSuccess(message: String) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        fun navigateToLogin() {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
        registerPageLoginButton.setOnClickListener {
            navigateToLogin()
        }

        registerButton.setOnClickListener {
            val email = registerEmailEditText.text?.toString()?.trim() ?: ""
            val password = registerPasswordEditText.text?.toString()?.trim() ?: ""
            val confirmPassword = confirmPasswordEditText.text?.toString()?.trim() ?: ""

            when {
                email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                    showError("All fields are required")
                }

                !Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    showError("Please enter a valid email address")
                }

                password.length < 6 -> {
                    showError("Password must be at least 6 characters")
                }

                password != confirmPassword -> {
                    showError("Passwords do not match")
                }

                else -> {
                    // TODO: Implement actual registration logic
                    showSuccess("Registration successful!")
                    navigateToLogin()
                }
            }

        }
    }
}