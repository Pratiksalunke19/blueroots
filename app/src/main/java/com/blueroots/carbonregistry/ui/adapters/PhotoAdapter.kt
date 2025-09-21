package com.blueroots.carbonregistry.ui.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.blueroots.carbonregistry.data.models.MonitoringPhoto
import com.blueroots.carbonregistry.databinding.ItemMonitoringPhotoBinding

class PhotoAdapter(
    private val photos: List<MonitoringPhoto>,
    private val onPhotoRemove: (MonitoringPhoto) -> Unit
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val binding = ItemMonitoringPhotoBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PhotoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        holder.bind(photos[position])
    }

    override fun getItemCount(): Int = photos.size

    inner class PhotoViewHolder(
        private val binding: ItemMonitoringPhotoBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(photo: MonitoringPhoto) {
            binding.apply {
                textViewPhotoName.text = photo.filename
                textViewTimestamp.text = photo.timestamp.toString()

                // Load image thumbnail (placeholder implementation)
                // In real app, use Glide or similar library
                imageViewThumbnail.setImageResource(android.R.drawable.ic_menu_camera)

                buttonRemovePhoto.setOnClickListener {
                    onPhotoRemove(photo)
                }
            }
        }
    }
}
