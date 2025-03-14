package com.android.picmosaic

import android.content.Context

data class UserProfile(
    val email: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val phone: String,
    val address: String,
    val city: String
)
object DummyUserData {
    private val userCredentials = mutableMapOf<String, String>()
    private val userProfiles = mutableMapOf<String, UserProfile>()

    // ✅ CHECK LOGIN CREDENTIALS
    fun validateCredentials(email: String, password: String): Boolean {
        return userCredentials[email] == password
    }

    // ✅ GET USER PROFILE
    fun getUserProfile(email: String, context: Context): UserProfile? {
        val sharedPreferences = context.getSharedPreferences("PicMosaic", Context.MODE_PRIVATE)
        return userProfiles[email] ?: loadFromSharedPreferences(email, sharedPreferences)
    }

    // ✅ ADD A NEW USER
    fun addNewUser(email: String, password: String, profile: UserProfile, context: Context) {
        userCredentials[email] = password
        userProfiles[email] = profile
        saveToSharedPreferences(email, password, profile, context)
    }

    // ✅ UPDATE USER PROFILE
    fun updateUserProfile(email: String, profile: UserProfile, context: Context) {
        userProfiles[email] = profile
        saveToSharedPreferences(email, userCredentials[email] ?: "", profile, context)
    }

    // ✅ SAVE TO SHARED PREFERENCES
    private fun saveToSharedPreferences(email: String, password: String, profile: UserProfile, context: Context) {
        val sharedPreferences = context.getSharedPreferences("PicMosaic", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("password_$email", password)
            putString("first_name_$email", profile.firstName)
            putString("last_name_$email", profile.lastName)
            putString("phone_$email", profile.phone)
            putString("address_$email", profile.address)
            putString("city_$email", profile.city)
            apply()
        }
    }

    // ✅ LOAD FROM SHARED PREFERENCES
    private fun loadFromSharedPreferences(email: String, sharedPreferences: android.content.SharedPreferences): UserProfile? {
        return if (sharedPreferences.contains("password_$email")) {
            UserProfile(
                email = email,
                username = sharedPreferences.getString("first_name_$email", "") ?: "",
                firstName = sharedPreferences.getString("first_name_$email", "") ?: "",
                lastName = sharedPreferences.getString("last_name_$email", "") ?: "",
                phone = sharedPreferences.getString("phone_$email", "") ?: "",
                address = sharedPreferences.getString("address_$email", "") ?: "",
                city = sharedPreferences.getString("city_$email", "") ?: ""
            )
        } else {
            null
        }
    }
}
