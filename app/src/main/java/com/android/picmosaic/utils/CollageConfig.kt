package com.android.picmosaic.utils

import android.graphics.Color
import android.net.Uri
import android.os.Parcel
import android.os.Parcelable

data class CollageConfig(
    val imageUris: List<String>,
    val spanCount: Int,
    val borderWidth: Float,
    val cornerRadius: Float,
    val backgroundColor: Int,
    val borderColor: Int = Color.WHITE // Add borderColor with default value
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.createStringArrayList() ?: emptyList(),
        parcel.readInt(),
        parcel.readFloat(),
        parcel.readFloat(),
        parcel.readInt(),
        parcel.readInt() // Read borderColor
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeStringList(imageUris)
        parcel.writeInt(spanCount)
        parcel.writeFloat(borderWidth)
        parcel.writeFloat(cornerRadius)
        parcel.writeInt(backgroundColor)
        parcel.writeInt(borderColor) // Write borderColor
    }

    override fun describeContents(): Int = 0

    fun getUris(): List<Uri> = imageUris.map { Uri.parse(it) }

    companion object CREATOR : Parcelable.Creator<CollageConfig> {
        override fun createFromParcel(parcel: Parcel): CollageConfig {
            return CollageConfig(parcel)
        }

        override fun newArray(size: Int): Array<CollageConfig?> {
            return arrayOfNulls(size)
        }
    }
}