package com.android.picmosaic

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar

class CollageAdapter(private val imageUris: MutableList<Uri>) : RecyclerView.Adapter<CollageAdapter.CollageViewHolder>() {

    private var borderWidth = 8
    private var cornerRadius = 0
    private var borderColor = Color.WHITE // Default border color
    private var recyclerView: RecyclerView? = null
    private var spanCount: Int = 3 // Default span count
    private var layoutType: String = "grid" // Simplified to only "grid" with dynamic spanCount

    fun setRecyclerView(recyclerView: RecyclerView) {
        this.recyclerView = recyclerView
        val layoutManager = recyclerView.layoutManager as? GridLayoutManager
        layoutManager?.let {
            spanCount = it.spanCount
        }
    }

    fun setLayoutType(layoutType: String) {
        this.layoutType = layoutType
        notifyDataSetChanged()
    }

    fun setBorderProperties(width: Int, radius: Int, color: Int) {
        borderWidth = width
        cornerRadius = radius
        borderColor = color
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CollageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_collage_image, parent, false)
        return CollageViewHolder(view)
    }

    override fun onBindViewHolder(holder: CollageViewHolder, position: Int) {
        val uri = imageUris[position]

        // Get RecyclerView dimensions
        val recyclerViewWidth = recyclerView?.width ?: 1080
        val recyclerViewHeight = recyclerView?.height ?: 1080
        val scaleFactor = recyclerViewWidth.toFloat() / 1080f // Scale factor based on COLLAGE_WIDTH (1080)

        // Apply scaled border properties
        val scaledBorderWidth = (borderWidth * scaleFactor).toInt()
        val scaledCornerRadius = (cornerRadius * scaleFactor).toFloat()

        // Calculate dimensions
        val padding = (recyclerViewWidth * 0.02f).toInt() // 2% padding
        val availableWidth = recyclerViewWidth - 2 * padding
        val availableHeight = recyclerViewHeight - 2 * padding

        // Determine spanCount based on photo count and layoutType
        val (columns, rows) = when (imageUris.size) {
            2 -> when (layoutType) {
                "2x1" -> 2 to 1
                "1x2" -> 1 to 2
                else -> spanCount to (imageUris.size + spanCount - 1) / spanCount
            }
            3 -> when (layoutType) {
                "3x1" -> 3 to 1
                "1x3" -> 1 to 3
                else -> spanCount to (imageUris.size + spanCount - 1) / spanCount
            }
            4 -> when (layoutType) {
                "2x2" -> 2 to 2
                "4x1" -> 4 to 1
                "1x4" -> 1 to 4
                else -> spanCount to (imageUris.size + spanCount - 1) / spanCount
            }
            5 -> when (layoutType) {
                "5x1" -> 5 to 1
                "1x5" -> 1 to 5
                "2x3" -> 2 to 3
                else -> spanCount to (imageUris.size + spanCount - 1) / spanCount
            }
            6 -> when (layoutType) {
                "3x2" -> 3 to 2
                "2x3" -> 2 to 3
                "1x6" -> 1 to 6
                else -> spanCount to (imageUris.size + spanCount - 1) / spanCount
            }
            else -> spanCount to (imageUris.size + spanCount - 1) / spanCount
        }

        // Calculate item dimensions based on the determined grid
        val itemWidth = availableWidth / columns
        val rowCount = rows
        val itemHeight = availableHeight / rowCount

        val layoutParams = holder.itemView.layoutParams
        layoutParams.width = itemWidth
        layoutParams.height = itemHeight
        holder.itemView.layoutParams = layoutParams

        // Create a drawable with the specified corner radius and border color
        val drawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(borderColor)
            cornerRadius = scaledCornerRadius
        }

        // Set padding as border
        holder.imageView.setPadding(
            scaledBorderWidth,
            scaledBorderWidth,
            scaledBorderWidth,
            scaledBorderWidth
        )

        holder.imageView.background = drawable
        holder.imageView.clipToOutline = true

        // Load image with Glide maintaining aspect ratio
        Glide.with(holder.imageView.context)
            .load(uri)
            .apply(RequestOptions().centerCrop())
            .into(holder.imageView)
    }

    override fun getItemCount(): Int = imageUris.size

    fun swapItems(fromPosition: Int, toPosition: Int) {
        val temp = imageUris[fromPosition]
        imageUris[fromPosition] = imageUris[toPosition]
        imageUris[toPosition] = temp
        notifyItemMoved(fromPosition, toPosition)

        recyclerView?.let { rv ->
            Snackbar.make(
                rv,
                "Moved image",
                Snackbar.LENGTH_LONG
            ).setAction("Undo") {
                val temp = imageUris[fromPosition]
                imageUris[fromPosition] = imageUris[toPosition]
                imageUris[toPosition] = temp
                notifyItemMoved(toPosition, fromPosition)
            }.show()
        }
    }

    class CollageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.collageImage)
    }
}