package com.android.picmosaic

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.request.RequestOptions

class CollageAdapter(private val imageUris: List<Uri>) : RecyclerView.Adapter<CollageAdapter.CollageViewHolder>() {

    private var borderWidth = 8
    private var cornerRadius = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collage_image, parent, false)
        return CollageViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollageViewHolder, position: Int) {
        val uri = imageUris[position]

        // Create a drawable with the specified corner radius
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
            cornerRadius = this@CollageAdapter.cornerRadius.toFloat()
        }

        // Set padding as border
        holder.imageView.setPadding(
            borderWidth,
            borderWidth,
            borderWidth,
            borderWidth
        )



        holder.imageView.background = drawable

        holder.imageView.clipToOutline = true

        // Load image with Glide maintaining aspect ratio
        Glide.with(holder.imageView.context)
            .load(uri)
            .apply(RequestOptions().centerCrop()) // Ensures consistent crop behavior
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageUris.size

    fun setBorderProperties(width: Int, radius: Int) {
        borderWidth = width
        cornerRadius = radius
        notifyDataSetChanged()
    }

    class CollageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.collageImage)
    }
}