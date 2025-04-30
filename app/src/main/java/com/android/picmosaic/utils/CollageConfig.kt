package com.android.picmosaic.utils

import android.graphics.Color
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class CollageConfig(
    val imageUris: List<String>, // List of URI strings (since Uri isn't Parcelable directly)
    val spanCount: Int,
    val borderWidth: Float,
    val cornerRadius: Float,
    val backgroundColor: Int
) : Parcelable {
    fun getUris(): List<Uri> = imageUris.map { Uri.parse(it) }

    // Constructor for reading from a Parcel
    constructor(parcel: Parcel) : this(
        imageUris = parcel.createStringArrayList() ?: listOf(),
        spanCount = parcel.readInt(),
        borderWidth = parcel.readFloat(),
        cornerRadius = parcel.readFloat(),
        backgroundColor = parcel.readInt()
    )

    // Write the object's data to a Parcel
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(imageUris)
        parcel.writeInt(spanCount)
        parcel.writeFloat(borderWidth)
        parcel.writeFloat(cornerRadius)
        parcel.writeInt(backgroundColor)
    }

    // Describe the kinds of special objects contained in this Parcelable (none in this case)
    override fun describeContents(): Int = 0

    // Companion object with CREATOR for creating instances from a Parcel
    companion object CREATOR : Parcelable.Creator<CollageConfig> {
        override fun createFromParcel(parcel: Parcel): CollageConfig {
            return CollageConfig(parcel)
        }

        override fun newArray(size: Int): Array<CollageConfig?> {
            return arrayOfNulls(size)
        }
    }
}