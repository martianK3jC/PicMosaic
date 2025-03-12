package com.android.picmosaic

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import android.widget.ViewFlipper
import com.android.picmosaic.utils.isRegistered
import com.android.picmosaic.utils.txt
import de.hdodenhof.circleimageview.CircleImageView
import java.io.File
import java.io.FileOutputStream

class ProfileActivity : Activity() {
    //Request code for picking an image
    private val PICK_IMAGE_REQUEST = 100
    private val CAMERA_REQUEST = 101

    //-------Initializing stuff-------
    //Initialize the ViewFlipper
    private lateinit var viewFlipper: ViewFlipper
    //Buttons
    private lateinit var editProfileButton: Button
    private lateinit var saveButton: Button
    private lateinit var arrowBackButton: ImageButton
    private lateinit var arrowBackEditButton: ImageButton
    private lateinit var settingsButton: ImageButton
    private lateinit var settingsButtonEdit: ImageButton
    private lateinit var logoutButton: Button
    private lateinit var editProfilePictureButton: ImageButton
    //Profile Views
    private lateinit var profileImage: CircleImageView
    private lateinit var profileUsername: TextView
    private lateinit var profileFirstName: TextView
    private lateinit var profileLastName: TextView
    private lateinit var profilePhone: TextView
    private lateinit var profileAddress: TextView
    private lateinit var profileEmail: TextView
    private lateinit var profileCity: TextView
    //Edit Profile Views
    private lateinit var profileImageEdit: CircleImageView
    private lateinit var profileUsernameEdit: TextView
    private lateinit var firstNameEdit: EditText
    private lateinit var lastNameEdit: EditText
    private lateinit var phoneEdit: EditText
    private lateinit var addressEdit: EditText
    private lateinit var profileEmailEdit: EditText // Disabled field
    private lateinit var cityEdit: EditText

    private var selectedImageUri: Uri? = null

//✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨✨
        override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.profile_page_with_edit)
        //load saved image
        initializeViews()

        loadSavedProfileImage()
        loadProfileData()

        setupButtonListeners()
    }

//-----------------S-O-M-E--C-O-M-P-L-I-C-A-T-E-D--S-T-U-F-F-----------------
    //Initialize Views
    private fun initializeViews(){
        // Initialize ViewFlipper
        viewFlipper = findViewById(R.id.viewFlipper)
        // Initialize Buttons
        editProfileButton = findViewById(R.id.edit_profile_button)
        saveButton = findViewById(R.id.save_button)
        arrowBackButton = findViewById(R.id.arrow_back_button)
        arrowBackEditButton = findViewById(R.id.arrow_back_edit_button)
        settingsButton = findViewById(R.id.settings_button)
        settingsButtonEdit = findViewById(R.id.settings_button_edit)
        logoutButton = findViewById(R.id.logout_button)
        // Initialize Profile Views
        profileImage = findViewById(R.id.profile_image)
        profileUsername = findViewById(R.id.profile_username)
        profileFirstName = findViewById(R.id.profile_first_name)
        profileLastName = findViewById(R.id.profile_last_name)
        profilePhone = findViewById(R.id.profile_phone)
        profileAddress = findViewById(R.id.profile_address)
        profileEmail = findViewById(R.id.profile_email)
        profileCity = findViewById(R.id.profile_city)
        // Initialize Edit Profile Views
        profileImageEdit = findViewById(R.id.profile_image_edit)
        profileUsernameEdit = findViewById(R.id.profile_username_edit)
        firstNameEdit = findViewById(R.id.first_name_edit)
        lastNameEdit = findViewById(R.id.last_name_edit)
        phoneEdit = findViewById(R.id.phone_edit)
        addressEdit = findViewById(R.id.address_edit)
        profileEmailEdit = findViewById(R.id.profile_email_edit) // Disabled field
        cityEdit = findViewById(R.id.city_edit)
        editProfilePictureButton = findViewById(R.id.edit_profile_picture)
    }

    //Set upp button listeners
    private fun setupButtonListeners(){
        // Edit Profile Button - Switch to Edit View
        editProfileButton.setOnClickListener {viewFlipper.showNext()}
        // Save Button - Save Changes
        saveButton.setOnClickListener {
            showSaveChangesDialog()
        }
        // Back Button in Profile - Navigate to Home
        arrowBackButton.setOnClickListener {
            navigateToHomePage()
        }
        // Back Button - Switch to Profile View
        arrowBackEditButton.setOnClickListener {
            showDiscardChangesDialog()
        }
        //Settings Button
        settingsButton.setOnClickListener {navigateToSettingsPage()}
        //Settings Button2
        settingsButtonEdit.setOnClickListener {navigateToSettingsPage()}
        //Logout Button
        logoutButton.setOnClickListener {showLogoutDialog()}
        //Edit Profile Picture Button
        editProfilePictureButton.setOnClickListener {showImagePickerDialog()}
    }

    //Load the saved profile image
    private fun loadSavedProfileImage() {
        val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val email = sharedPreferences.getString("current_user_email", null) ?: return showToast("Error: No user logged in")

        val savedImagePath = sharedPreferences.getString("profile_image_path_$email", null)

        if (savedImagePath.isNullOrEmpty()) {
            showToast("No saved profile image found for $email")
            return
        }

        val file = File(savedImagePath)
        if (!file.exists()) {
            showToast("Saved profile image not found")
            return
        }

        BitmapFactory.decodeFile(savedImagePath)?.let { bitmap ->
            profileImage.setImageBitmap(bitmap)
            profileImageEdit.setImageBitmap(bitmap)
        } ?: showToast("Error: Profile image is invalid")
    }




    //Load the user profile data
    private fun loadProfileData() {
        val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val email = sharedPreferences.getString("current_user_email", null) ?: return

        val profile = DummyUserData.getUserProfile(email, this) ?: return

        profileUsername.text = profile.firstName
        profileFirstName.text = profile.firstName
        profileLastName.text = profile.lastName
        profilePhone.text = profile.phone
        profileAddress.text = profile.address
        profileEmail.text = profile.email
        profileCity.text = profile.city

        profileUsernameEdit.text = profile.firstName
        firstNameEdit.setText(profile.firstName)
        lastNameEdit.setText(profile.lastName)
        phoneEdit.setText(profile.phone)
        addressEdit.setText(profile.address)
        profileEmailEdit.hint = profile.email
        cityEdit.setText(profile.city)
    }

    // Save Profile Changes
    private fun saveProfileChanges() {
        val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val email = sharedPreferences.getString("current_user_email", null) ?: return showToast("Error: No user logged in")

        val updatedProfile = UserProfile(
            email = email,
            username = profileUsernameEdit.text.toString(),
            firstName = firstNameEdit.text.toString(),
            lastName = lastNameEdit.text.toString(),
            phone = phoneEdit.text.toString(),
            address = addressEdit.text.toString(),
            city = cityEdit.text.toString()
        )

        // ✅ Save to SharedPreferences
        DummyUserData.updateUserProfile(email, updatedProfile, this)

        // ✅ Reload the saved profile
        loadProfileData()
        loadSavedProfileImage()
        // ✅ Switch back to the profile view
        viewFlipper.showPrevious()  // 🔹 This moves back to the non-editing state

        showToast("Profile updated successfully")
    }



    // Helper function for showing a toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    private fun copyImageToInternalStorage(uri: Uri, email: String): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
                val file = File(filesDir, "profile_image_$email.jpg") // 🔹 Unique filename per user

                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                file.absolutePath // Return saved file path
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }




    // Only allow choosing from the gallery
    private fun showImagePickerDialog() {
        openGallery() // Directly open gallery instead of showing a dialog
    }


    //Show Discard Changes Dialog box
    private fun showDiscardChangesDialog() {
        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Discard Changes")
            .setMessage("Are you sure you want to discard your changes?")
            .setPositiveButton("Yes") { _, _ ->
                viewFlipper.showPrevious()
            }
            .setNegativeButton("No"){ dialog, _ -> dialog.dismiss() }
            .show()
    }

    //Show Save Changes Dialog box
    private fun showSaveChangesDialog() {
        AlertDialog.Builder(this, R.style.CustomAlertDialog)
            .setTitle("Save Changes?")
            .setMessage("Are you sure you want to save the changes?")
            .setPositiveButton("Yes") { _, _ -> saveProfileChanges() }
            .setNegativeButton("No"){ dialog, _ -> dialog.dismiss() }
            .show()
    }

    //Show Logout Dialog
    private fun showLogoutDialog() {
        val logoutDialog = LogoutDialog(this)
        logoutDialog.show()
    }

    //Go to Settings Page
    private fun navigateToSettingsPage(){
        val intent = Intent(this, DummySettingsActivity::class.java)
        startActivity(intent)
    }

    //Go to Home Page
    private fun navigateToHomePage(){
        val intent = Intent(this, DummyHomeActivity::class.java)
        startActivity(intent)
    }

    //Open Gallery
    private fun openGallery(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent,PICK_IMAGE_REQUEST)
    }

    // THIS HANDLES IMAGE SELECTION
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data ?: return  // If no image is selected, exit early

            val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
            val email = sharedPreferences.getString("current_user_email", null) ?: return showToast("Error: No user logged in")

            val savedPath = copyImageToInternalStorage(imageUri, email) ?: run {
                showToast("Failed to save image")
                return
            }

            // Check if file exists before setting the image
            val file = File(savedPath)
            if (file.exists()) {
                profileImageEdit.setImageBitmap(BitmapFactory.decodeFile(savedPath))
                selectedImageUri = Uri.fromFile(file)

                // ✅ Save the image path specific to the logged-in user
                sharedPreferences.edit()
                    .putString("profile_image_path_$email", savedPath) // Unique key per user
                    .apply()
            } else {
                showToast("Error: Image file missing")
            }
        }
    }

    fun handleLogout() {
        val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)

        // Clear login session
        sharedPreferences.edit().remove("current_user_email").apply()

        // 🔹 Clear cached profile images
        profileImage.setImageDrawable(null)
        profileImageEdit.setImageDrawable(null)

        // 🔹 Go back to login screen
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)

        finish() // Close ProfileActivity
    }



}
