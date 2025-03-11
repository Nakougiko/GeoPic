package com.lukashugo.geopic

import android.content.Context
import android.location.Geocoder
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lukashugo.geopic.databinding.FragmentPhotoBottomSheetBinding
import java.io.File
import java.util.Locale

class PhotoBottomSheetFragment(private val photos: MutableList<File>, private val lat: Double, private val lng: Double,
                               private val context: Context,
                               private val onMarkerEmpty: (Double, Double) -> Unit // Callback pour supprimer un marker
) : BottomSheetDialogFragment() {

    private var _binding: FragmentPhotoBottomSheetBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentPhotoBottomSheetBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Charger l'adresse du lieu
        getAddressFromLocation(lat, lng)

        val adapter = PhotoAdapter(photos, requireContext()) { deletedPhoto ->
            // Callback pour supprimer une photo
            // Mettre à jour la liste des photos
            photos.remove(deletedPhoto)
            updatePhotoList()

            // Callback pour supprimer un marker si la liste des photos est vide
            if (photos.isEmpty()) {
                onMarkerEmpty(lat, lng)
                dismiss()
            }
        }


        binding.recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.adapter = adapter
    }

    private fun updatePhotoList() {
        binding.recyclerView.adapter?.notifyDataSetChanged()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getAddressFromLocation(latitude: Double, longitude: Double) {
        val geocoder = Geocoder(context, Locale.getDefault())

        try {
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            val addressText = if (!addresses.isNullOrEmpty()) {
                addresses[0].getAddressLine(0) // Récupère l'adresse complète
            } else {
                "Adresse inconnue"
            }

            binding.labelBottomSheet.text = "Photo prise ici : $addressText"
        } catch (e: Exception) {
            e.printStackTrace()
            binding.labelBottomSheet.text = "Photo prise ici : Adresse introuvable"
        }
    }
}