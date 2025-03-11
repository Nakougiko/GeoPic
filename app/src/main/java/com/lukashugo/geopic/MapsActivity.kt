package com.lukashugo.geopic

import android.content.Intent
import android.media.ExifInterface
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.util.Log

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.lukashugo.geopic.databinding.ActivityMapsBinding
import java.io.File

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    private val markerMap: MutableMap<LatLng, Marker> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupBottomNavigation()

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // Zoom par défaut sur la France
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(46.603354, 1.888334), 5f))
        Log.d("MAP_DEBUG", "Map ready")
        // Ajout des markers en fonction des photos et leurs coordonnées GPS
        loadMarkers()
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_map

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_camera -> {
                    startActivity(Intent(this, PhotoActivity::class.java))
                    overridePendingTransition(0, 0) // Supprime l’animation de transition
                    finish()
                }
            }
            true
        }
    }

    private fun getPhotosWithLocation(): Map<Pair<Double, Double>, MutableList<File>> {
        val photosWithLocation = mutableMapOf<Pair<Double, Double>, MutableList<File>>()
        val photosDir = File("/storage/emulated/0/Android/media/${packageName}/")

        if (!photosDir.exists()) {
            Log.d("MAP_DEBUG", "⚠️ Aucun dossier de photos")
            return emptyMap()
        }

        Log.d("MAP_DEBUG", "📂 Chargement des photos depuis: ${photosDir.absolutePath}")

        photosDir.listFiles()?.forEach { file ->
            // Exclure les .trashed
            if (file.name.startsWith(".trashed") || file.name.startsWith("trashed")) {
                Log.d("MAP_DEBUG", "⚠️ Fichier .trashed trouvé: ${file.name}")
                return@forEach
            }

            try {
                val exif = ExifInterface(file.absolutePath)
                val latitude = getExifLatLong(exif, ExifInterface.TAG_GPS_LATITUDE, ExifInterface.TAG_GPS_LATITUDE_REF)
                val longitude = getExifLatLong(exif, ExifInterface.TAG_GPS_LONGITUDE, ExifInterface.TAG_GPS_LONGITUDE_REF)

                if (latitude != null && longitude != null) {
                    Log.d("MAP_DEBUG", "📍 Photo trouvée: ${file.name} | Lat: $latitude, Lng: $longitude")
                    val key = Pair(latitude, longitude)
                    photosWithLocation.getOrPut(key) { mutableListOf() }.add(file)
                } else {
                    Log.d("MAP_DEBUG", "⚠️ Aucune localisation pour: ${file.name}")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        Log.d("MAP_DEBUG", "📸 Nombre total de photos avec GPS: ${photosWithLocation.size}")
        return photosWithLocation
    }


    private fun getExifLatLong(exif: ExifInterface, tag: String, refTag: String): Double? {
        val value = exif.getAttribute(tag) ?: return null
        val ref = exif.getAttribute(refTag) ?: return null

        val dms = value.split(",")
        if (dms.size != 3) return null

        val degrees = dms[0].split("/")[0].toDouble()
        val minutes = dms[1].split("/")[0].toDouble() / 60
        val seconds = dms[2].split("/")[0].toDouble() / 3600

        val decimal = degrees + minutes + seconds
        return if (ref == "S" || ref == "W") -decimal else decimal
    }

    private fun loadMarkers() {
        Log.d("MAP_DEBUG", "🗺️ Chargement des markers...")
        val photosWithLocation = getPhotosWithLocation()

        if (photosWithLocation.isEmpty()) {
            Log.d("MAP_DEBUG", "⚠️ Aucune photo avec localisation trouvée")
            return
        }

        var latestLocation: Pair<Double, Double>? = null
        var latestFile: File? = null

        photosWithLocation.forEach { (location, files) ->
            Log.d("MAP_DEBUG", "📍 Ajout du marker à: ${location.first}, ${location.second}")
            val markerOptions = MarkerOptions()
                .position(LatLng(location.first, location.second))
                .title("Photos: ${files.size}") // Afficher le nombre de photos à cet emplacement
            val marker = mMap.addMarker(markerOptions)
            marker?.tag = files

            marker?.let {
                markerMap[LatLng(location.first, location.second)] = it
            }

            // Centrer la carte sur la derniere photos
            files.forEach { file ->
                if (latestFile == null || file.lastModified() > latestFile!!.lastModified()) {
                    latestFile = file
                    latestLocation = location
                }
            }

            // Si on a une dernière photo, centrer la carte sur cette photo
            latestLocation?.let { (lat, long) ->
                val latestlatlong = LatLng(lat, long)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latestlatlong, 15f))
            }
        }

        mMap.setOnMarkerClickListener { marker ->
            val photos = marker.tag as? MutableList<File>
            if (photos != null) {
                showPhotosBottomSheet(photos, marker.position.latitude, marker.position.longitude)
            }
            true
        }
    }

    private fun showPhotosBottomSheet(photos: MutableList<File>, lat: Double, lng: Double) {
        val bottomSheet = PhotoBottomSheetFragment(photos, lat, lng, this) { latToRemove, lngToRemove ->
            // Callback pour supprimer un marker
            removeMarker(latToRemove, lngToRemove)
        }
        bottomSheet.show(supportFragmentManager, "PhotoBottomSheet")
    }

    private fun removeMarker(lat: Double, lng: Double) {
        val latLng = LatLng(lat, lng)
        markerMap[latLng]?.remove()
        markerMap.remove(latLng)
    }
}