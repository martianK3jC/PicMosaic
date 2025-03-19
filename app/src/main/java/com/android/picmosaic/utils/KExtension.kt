package com.android.picmosaic.utils

import android.app.Activity
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

fun EditText.txt(): String{
    return this.text.toString()
}

fun TextView.txt(): String{
    return this.toString()
}


fun EditText.isNotValid(): Boolean{
    return this.text.toString().isNullOrEmpty()
}

fun Activity.toast(msg:String){
    Toast.makeText(this, msg, Toast.LENGTH_LONG).show()
}

fun Activity.showMessage(message: String) {
    Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
}

