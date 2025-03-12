package com.android.picmosaic.utils

import android.app.Activity
import android.widget.EditText
import android.widget.Toast

fun EditText.txt(): String{
    return this.text.toString()
}

fun EditText.isNotValid(): Boolean{
    return this.text.toString().isNullOrEmpty()
}

fun Activity.toast(msg:String){
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Activity.showError(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}

fun isRegistered(check: Boolean): Boolean{
    return check
}
fun Activity.showSuccess(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}