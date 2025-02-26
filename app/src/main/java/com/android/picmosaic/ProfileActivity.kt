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

        intent?.let{
            it.getStringExtra("firstname")?.let{
                    firstname -> profileUsername.setText(firstname)
            }
            it.getStringExtra("lastname")?.let{
                    lastname -> profileLastName.setText(lastname)
            }

            it.getStringExtra("phonenumber")?.let{
                    phonenumber -> profilePhone.setText(phonenumber)
            }

            it.getStringExtra("address")?.let {
                    address -> profileAddress.setText(address)
            }

            it.getStringExtra("city")?.let {
                    address -> profileCity.setText(address)
            }

            it.getStringExtra("email")?.let{
                    email -> profileEmail.setText(email)
            }
        }

        val savedImagePath = getSharedPreferences("PicMosaic", MODE_PRIVATE)
            .getString("profile_image_path", null) ?: return println("No saved profile image path in SharedPreferences")

        val file = File(savedImagePath)
        if (!file.exists()) {
            println("Saved profile image not found at: $savedImagePath")
            return showToast("Saved profile image not found")
        }

        BitmapFactory.decodeFile(savedImagePath)?.let { bitmap ->
            println("✅ Successfully loaded profile image from: $savedImagePath")
            profileImage.setImageBitmap(bitmap)
            profileImageEdit.setImageBitmap(bitmap)
        } ?: showToast("Error: Profile image is invalid").also {
            println("Failed to decode saved image. File might be corrupted.")
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
                profileUsername.text = it.firstName
                profileFirstName.text = it.firstName
                profileLastName.text = it.lastName
                profilePhone.text = it.phone
                profileAddress.text = it.address
                profileEmail.text = it.email
                profileCity.text = it.city

                profileUsernameEdit.text = it.firstName
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
        val email = sharedPreferences.getString("current_user_email", null) ?: return showToast("Error: No user logged in")

        val editor = sharedPreferences.edit()

        selectedImageUri?.let { uri ->
            copyImageToInternalStorage(uri)?.let { savedPath ->
                editor.putString("profile_image_path", savedPath)
                BitmapFactory.decodeFile(savedPath)?.let { bitmap ->
                    profileImage.setImageBitmap(bitmap)
                    profileImageEdit.setImageBitmap(bitmap)
                }
            }
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

        with(editor) {
            putString("username", updatedProfile.username)
            putString("first_name", updatedProfile.firstName)
            putString("last_name", updatedProfile.lastName)
            putString("phone", updatedProfile.phone)
            putString("address", updatedProfile.address)
            putString("city", updatedProfile.city)
            apply()
        }

        DummyUserData.updateUserProfile(email, updatedProfile)

        loadProfileData()
        loadSavedProfileImage()
        viewFlipper.showPrevious()
        showToast("Profile updated successfully")
    }

    // Helper function for showing a toast
    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }



    private fun copyImageToInternalStorage(uri: Uri): String? {
        return try {
            contentResolver.openInputStream(uri)?.use { inputStream ->
                val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
                val file = File(filesDir, "profile_image.jpg")

                FileOutputStream(file).use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                }

                file.absolutePath
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

    //THIS IS HANDLING THE IMAGE SELECTION
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            val imageUri = data?.data ?: return  // If no image is selected, exit early

            val savedPath = copyImageToInternalStorage(imageUri) ?: run {
                Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
                return
            }

            // Check if file exists before setting the image
            val file = File(savedPath)
            if (file.exists()) {
                profileImageEdit.setImageBitmap(BitmapFactory.decodeFile(savedPath))
                selectedImageUri = Uri.fromFile(file)

                getSharedPreferences("PicMosaic", MODE_PRIVATE)
                    .edit()
                    .putString("profile_image_path", savedPath)
                    .apply()
            } else {
                Toast.makeText(this, "Error: Image file missing", Toast.LENGTH_SHORT).show()
            }
        }
    }

}
