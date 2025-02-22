package com.android.picmosaic

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

    fun getUserProfile(email: String): UserProfile? {
        return userProfiles[email]
    }

    fun updateUserProfile(email: String, profile: UserProfile) {
        userProfiles[email] = profile
    }
}
