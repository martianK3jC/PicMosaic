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
    private val userCredentials = mutableMapOf(
        "test@email.com" to "password123",
        "john@email.com" to "john123",
        "jane@email.com" to "jane123",
        "keshample@email.com" to "keshample"
    )

    private val userProfiles = mutableMapOf(
        "test@email.com" to UserProfile("test@email.com", "testuser", "Test", "User", "123-456-7890", "123 Test St", "Test City"),
        "john@email.com" to UserProfile("john@email.com", "johndoe", "John", "Doe", "123-456-7891", "456 John St", "John City"),
        "jane@email.com" to UserProfile("jane@email.com", "janesmith", "Jane", "Smith", "123-456-7892", "789 Jane St", "Jane City"),
        "keshample@email.com" to UserProfile("keshample@email.com", "keshample", "Kesh", "Ample", "123-456-7893", "321 Kesh St", "Kesh City")
    )

    fun validateCredentials(email: String, password: String): Boolean {
        return userCredentials[email] == password
    }

    fun getUserProfile(email: String, context: Context): UserProfile? {
        val sharedPreferences = context.getSharedPreferences("PicMosaic", Context.MODE_PRIVATE)

        return if (sharedPreferences.contains("username_$email")) {
            UserProfile(
                email = email,
                username = sharedPreferences.getString("username_$email", "") ?: "",
                firstName = sharedPreferences.getString("first_name_$email", "") ?: "",
                lastName = sharedPreferences.getString("last_name_$email", "") ?: "",
                phone = sharedPreferences.getString("phone_$email", "") ?: "",
                address = sharedPreferences.getString("address_$email", "") ?: "",
                city = sharedPreferences.getString("city_$email", "") ?: ""
            )
        } else {
            userProfiles[email]  // Fallback to in-memory data if not found
        }
    }


    fun updateUserProfile(email: String, profile: UserProfile, context: Context) {
        userProfiles[email] = profile  // ✅ Updates in-memory data

        val sharedPreferences = context.getSharedPreferences("PicMosaic", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()

        with(editor) {
            putString("username_$email", profile.username)
            putString("first_name_$email", profile.firstName)
            putString("last_name_$email", profile.lastName)
            putString("phone_$email", profile.phone)
            putString("address_$email", profile.address)
            putString("city_$email", profile.city)
            apply()  // ✅ Saves data permanently
        }
    }

}
