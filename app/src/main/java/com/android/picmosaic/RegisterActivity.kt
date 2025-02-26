package com.android.picmosaic

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.android.picmosaic.utils.isRegistered
import com.android.picmosaic.utils.isValidEntry
import com.android.picmosaic.utils.showError
import com.android.picmosaic.utils.showSuccess
import com.android.picmosaic.utils.txt

class RegisterActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.register)
        fun navigateToLogin() {
            startActivity(Intent(this, LoginActivity::class.java))
        }

        val registerFirstNameEditText = findViewById<EditText>(R.id.registerFirstNameEditText)
        val registerLastNameEditText = findViewById<EditText>(R.id.registerLastnameEditText)
        val registerAddressEditText = findViewById<EditText>(R.id.registerAddressEditText)
        val registerPhoneNumberEditText = findViewById<EditText>(R.id.registerPhoneNumberEditText)
        val registerCityEditText = findViewById<EditText>(R.id.registerAddressEditText)
        val registerButton = findViewById<Button>(R.id.registerButton)
        val registerPageLoginButton = findViewById<Button>(R.id.registerPageLoginButton)
        val registerEmailEditText = findViewById<EditText>(R.id.registerEmailEditText)
        val registerPasswordEditText = findViewById<EditText>(R.id.registerPasswordEditText)
        val confirmPasswordEditText = findViewById<EditText>(R.id.confirmPasswordEditText)
        var check = false;


        registerPageLoginButton.setOnClickListener {
            navigateToLogin()
        }

        registerButton.setOnClickListener {
            if(registerFirstNameEditText.isValidEntry() || registerLastNameEditText.isValidEntry() || registerAddressEditText.isValidEntry()||registerPhoneNumberEditText.isValidEntry() || registerCityEditText.isValidEntry() || registerEmailEditText.isValidEntry()||registerPasswordEditText.isValidEntry()){
                showError("Please fill out the forms")
            }
            when {
                !Patterns.EMAIL_ADDRESS.matcher(registerEmailEditText.text.toString()).matches() -> {
                    showError("Please enter a valid email address")
                }

                registerPasswordEditText.text.toString().length < 6 -> {
                    showError("Password must be at least 6 characters")
                }

                registerPasswordEditText.text.toString() != confirmPasswordEditText.text.toString() -> {
                    showError("Passwords do not match")
                }

                else -> {
                    showSuccess("Registration successful!")
                    check = true;
                    startActivity(Intent(this, LoginActivity::class.java).apply {
                        putExtra("firstname", registerFirstNameEditText.txt())
                        putExtra("lastname", registerLastNameEditText.txt())
                        putExtra("address", registerAddressEditText.txt())
                        putExtra("phonenumber", registerPhoneNumberEditText.txt())
                        putExtra("city", registerCityEditText.txt())
                        putExtra("username", registerFirstNameEditText.txt())
                        putExtra("email", registerEmailEditText.txt())
                        putExtra("password", registerPasswordEditText.txt())
                        putExtra("isValidRegistered", isRegistered(check))
                    })
                }
            }

        }
    }

    companion object
}