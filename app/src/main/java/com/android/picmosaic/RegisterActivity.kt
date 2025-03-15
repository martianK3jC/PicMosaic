package com.android.picmosaic

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import com.android.picmosaic.utils.showError
import com.android.picmosaic.utils.showSuccess
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
        val registerAddress = findViewById<EditText>(R.id.registerAddressEditText)
        val registerPhoneNumber = findViewById<EditText>(R.id.registerPhoneNumberEditText)
        val registerCity = findViewById<EditText>(R.id.registerCityText)
        val registerEmail = findViewById<EditText>(R.id.registerEmailEditText)
        val registerPassword = findViewById<EditText>(R.id.registerPasswordEditText)
        val confirmPassword = findViewById<EditText>(R.id.confirmPasswordEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val registerPageLoginButton = findViewById<Button>(R.id.registerPageLoginButton)

        registerPageLoginButton.setOnClickListener { navigateToLogin() }

        registerButton.setOnClickListener {
            val firstName = registerFirstName.txt()
            val lastName = registerLastName.txt()
            val address = registerAddress.txt()
            val phoneNumber = registerPhoneNumber.txt()
            val city = registerCity.txt()
            val email = registerEmail.txt()
            val password = registerPassword.txt()
            val confirmPass = confirmPassword.txt()

            // 🔹 VALIDATION
            if (firstName.isEmpty() || lastName.isEmpty() || address.isEmpty() ||
                phoneNumber.isEmpty() || city.isEmpty() || email.isEmpty() || password.isEmpty()) {
                showError("Please fill out all fields")
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                showError("Invalid email format")
                return@setOnClickListener
            }
            if (password.length < 6) {
                showError("Password must be at least 6 characters")
                return@setOnClickListener
            }
            if (password != confirmPass) {
                showError("Passwords do not match")
                return@setOnClickListener
            }

            val newUser = UserProfile(email, firstName, firstName, lastName, phoneNumber, address, city)
            DummyUserData.addNewUser(email, password, newUser, this)

            val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
            sharedPreferences.edit()
                .putString("registered_email", email)
                .putString("registered_password", password)
                .apply()

            showSuccess("Registration successful!")
            navigateToLogin()
        }
    }
}
