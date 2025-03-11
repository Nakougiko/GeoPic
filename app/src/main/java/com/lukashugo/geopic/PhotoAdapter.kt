package com.lukashugo.geopic

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat.startActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.RecyclerView
import java.io.File
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface
import android.widget.Toast
import androidx.appcompat.app.AlertDialog

class PhotoAdapter(private val photos: MutableList<File>, private val context: Context,
                   private val onPhotoDeleted: (File) -> Unit // Callback pour supprimer une photo
) : RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder>() {

    class PhotoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
        val deleteButton: ImageView = itemView.findViewById(R.id.deleteButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhotoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_photo, parent, false)
        return PhotoViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhotoViewHolder, position: Int) {
        val photoFile = photos[position]

        val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
        val correctBitmap = correctImageRotation(bitmap, photoFile)

        holder.imageView.setImageBitmap(correctBitmap)

        holder.imageView.setOnClickListener {
            openPhotoInGallery(photoFile)
        }

        holder.deleteButton.setOnClickListener {
            showDeleteConfirmationDialog(photoFile, position)
        }
    }

    private fun showDeleteConfirmationDialog(photoFile: File, position: Int) {
        AlertDialog.Builder(context)
            .setTitle("Supprimer la photo")
            .setMessage("Voulez-vous vraiment supprimer cette photo ?")
            .setPositiveButton("Oui") { _, _ ->
                if (photoFile.exists() && photoFile.delete()) {
                    photos.removeAt(position)
                    notifyItemRemoved(position)
                    onPhotoDeleted(photoFile) // Appel du callback pour mettre à jour la liste
                    Toast.makeText(context, "Photo supprimée", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(context, "Échec de la suppression", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Non", null)
            .show()
    }

    private fun correctImageRotation(bitmap: Bitmap, photoFile: File): Bitmap {
        return try {
            val exif = ExifInterface(photoFile.absolutePath)
            val rotation = when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }

            if (rotation == 0) return bitmap // Pas besoin de rotation

            val matrix = Matrix()
            matrix.postRotate(rotation.toFloat())

            Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        } catch (e: Exception) {
            e.printStackTrace()
            bitmap // Retourne l'image non modifiée en cas d'erreur
        }
    }

    private fun openPhotoInGallery(photo: File) {
        val uri = FileProvider.getUriForFile(
            context,
            "com.lukashugo.geopic.provider",
            photo
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "image/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(intent)
    }

    override fun getItemCount(): Int = photos.size
}