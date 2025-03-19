package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import com.android.picmosaic.utils.showMessage
import com.android.picmosaic.utils.txt

class RegisterActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register_page)

        fun navigateToLogin() {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        val registerFirstName = findViewById<EditText>(R.id.registerFirstNameEditText)
        val registerLastName = findViewById<EditText>(R.id.registerLastnameEditText)
        val registerEmail = findViewById<EditText>(R.id.registerEmailEditText)
        val registerPassword = findViewById<EditText>(R.id.registerPasswordEditText)
        val confirmPassword = findViewById<EditText>(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val registerPageLoginButton = findViewById<Button>(R.id.registerPageLoginButton)

        registerPageLoginButton.setOnClickListener { navigateToLogin() }

        registerButton.setOnClickListener {
            val firstName = registerFirstName.txt()
            val lastName = registerLastName.txt()
            val email = registerEmail.txt()
            val password = registerPassword.txt()
            val confirmPass = confirmPassword.txt()

            // 🔹 VALIDATION
            if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showMessage("Please fill out all fields")
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showMessage("Invalid email format")
                return@setOnClickListener
            }
            if (password.length < 6) {
                showMessage("Password must be at least 6 characters")
                return@setOnClickListener
            }
            if (password != confirmPass) {
                showMessage("Passwords do not match")
                return@setOnClickListener
            }

            val newUser = UserProfile(email, firstName, lastName, firstName, "", "", "")
            DummyUserData.addNewUser(email, password, newUser, this)

            showMessage("Registration successful!")
            navigateToLogin()
        }
    }
}
