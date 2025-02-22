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
    private var capturedPhoto: Bitmap? = null

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
        arrowBackButton.setOnClickListener {navigateToHomePage()}
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
        val savedImagePath = sharedPreferences.getString("profile_image_path", null)

        if (!savedImagePath.isNullOrEmpty()) {
            val file = File(savedImagePath)
            if (file.exists()) {
                val bitmap = BitmapFactory.decodeFile(savedImagePath)
                val resizedBitmap = resizeBitmap(bitmap, 300)
                profileImage.setImageBitmap(resizedBitmap)
                profileImageEdit.setImageBitmap(resizedBitmap)
            } else {
                Toast.makeText(this, "Saved profile image not found", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun resizeBitmap(bitmap: Bitmap, maxSize: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height

        // Calculate aspect ratio
        val scaleFactor = minOf(maxSize.toFloat() / width, maxSize.toFloat() / height)

        val newWidth = (width * scaleFactor).toInt()
        val newHeight = (height * scaleFactor).toInt()

        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }


    //Load the user profile data
    private fun loadProfileData() {
        val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val email = sharedPreferences.getString("current_user_email", null)

        if(email != null){
            DummyUserData.getUserProfile(email)?.let{
                profileUsername.text = it.username
                profileFirstName.text = it.firstName
                profileLastName.text = it.lastName
                profilePhone.text = it.phone
                profileAddress.text = it.address
                profileEmail.text = it.email
                profileCity.text = it.city

                // ✅ Update Edit Views
                profileUsernameEdit.text = it.username
                firstNameEdit.setText(it.firstName)
                lastNameEdit.setText(it.lastName)
                phoneEdit.setText(it.phone)
                addressEdit.setText(it.address)
                profileEmailEdit.hint = it.email
                cityEdit.setText(it.city)
            }
        }else{
            Toast.makeText(this,"Error: No user logged in", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    // Save Profile Changes
    private fun saveProfileChanges() {
        val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
        val email = sharedPreferences.getString("current_user_email", null)

        if (email == null) {
            Toast.makeText(this, "Error: No user logged in", Toast.LENGTH_SHORT).show()
            return
        }

        val editor = sharedPreferences.edit()

        if (selectedImageUri != null) {
            val savedPath = copyImageToInternalStorage(selectedImageUri!!)
            editor.putString("profile_image_path", savedPath)

            val bitmap = BitmapFactory.decodeFile(savedPath)

            //Update both Profile and Edit Profile Images
            profileImage.setImageBitmap(bitmap)
            profileImageEdit.setImageBitmap(bitmap)
        }

        if (selectedImageUri == null && capturedPhoto != null) {
            val savedUri = saveBitmapToInternalStorage(capturedPhoto!!)
            editor.putString("profile_image_path", savedUri)

            val bitmap = BitmapFactory.decodeFile(savedUri)

            // ✅ Update both Profile and Edit Profile Images
            profileImage.setImageBitmap(bitmap)       // Updates Profile Page Image ✅
            profileImageEdit.setImageBitmap(bitmap)   // Updates Edit Page Image ✅
        }

        val updatedProfile = UserProfile(
            email = email,
            username = profileUsernameEdit.text.toString(),
            firstName = firstNameEdit.text.toString(),
            lastName = lastNameEdit.text.toString(),
            phone = phoneEdit.text.toString(),
            address = addressEdit.text.toString(),
            city = cityEdit.text.toString()
        )

        editor.putString("first_name", updatedProfile.firstName)
        editor.putString("last_name", updatedProfile.lastName)
        editor.putString("phone", updatedProfile.phone)
        editor.putString("address", updatedProfile.address)
        editor.putString("city", updatedProfile.city)
        editor.apply()

        DummyUserData.updateUserProfile(email, updatedProfile)

        loadProfileData()  // ✅ Reloads Profile Information
        loadSavedProfileImage()
        viewFlipper.showPrevious()
        Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
    }


    private fun copyImageToInternalStorage(uri: Uri): String {
        val inputStream = contentResolver.openInputStream(uri)
        val file = File(filesDir, "profile_image.jpg")
        val outputStream = FileOutputStream(file)

        inputStream?.copyTo(outputStream)
        inputStream?.close()
        outputStream.close()

        return file.absolutePath
    }


    //Save camera pic to storage
    private fun saveBitmapToInternalStorage(bitmap: Bitmap): String {
        // Compress image if it's too large (max 1024x1024)
        val maxSize = 1024
        var compressedBitmap = bitmap
        if (bitmap.width > maxSize || bitmap.height > maxSize) {
            val scale = minOf(maxSize.toFloat() / bitmap.width, maxSize.toFloat() / bitmap.height)
            val matrix = Matrix()
            matrix.postScale(scale, scale)
            compressedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        val fileName = "profile_image.jpg"
        try {
            openFileOutput(fileName, Context.MODE_PRIVATE).use { stream ->
                compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, stream)
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Failed to save image: ${e.message}", Toast.LENGTH_SHORT).show()
            return ""
        }
        return fileName
    }

    // Only allow choosing from the gallery
    private fun showImagePickerDialog() {
        openGallery() // Directly open gallery instead of showing a dialog
    }


    //Show Discard Changes Dialog box
    private fun showDiscardChangesDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            builder.setTitle("Discard Changes")
            .setMessage("Are you sure you want to discard your changes?")
            .setPositiveButton("Yes") { _, _ ->
                viewFlipper.showPrevious()
            }
            .setNegativeButton("No"){ dialog, _ -> dialog.dismiss() }
            .show()
    }

    //Show Save Changes Dialog box
    private fun showSaveChangesDialog() {
        val builder = AlertDialog.Builder(this, R.style.CustomAlertDialog)
            builder.setTitle("Save Changes?")
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

    //Open Camera
    private fun openCamera() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        // Check if a camera app exists before starting the activity
        if (cameraIntent.resolveActivity(packageManager) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST)
        } else {
            Toast.makeText(this, "No camera app found!", Toast.LENGTH_SHORT).show()
        }
    }


    //THIS IS HANDLING THE IMAGE SELECTION
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST) {
            val imageUri = data?.data
            if (imageUri != null) {
                profileImageEdit.setImageURI(imageUri)
                selectedImageUri = imageUri

                val sharedPreferences = getSharedPreferences("PicMosaic", MODE_PRIVATE)
                sharedPreferences.edit()
                    .putString("profile_image_path", imageUri.toString()).apply()
            }
        }

    }
}
