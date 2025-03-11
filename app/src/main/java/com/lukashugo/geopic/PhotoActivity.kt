package com.lukashugo.geopic

import android.content.Intent
import android.location.Location
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.lukashugo.geopic.databinding.ActivityPhotoBinding
import android.Manifest
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.media.ExifInterface
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import java.io.File
import java.util.Date
import java.util.Locale
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class PhotoActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPhotoBinding
    private lateinit var cameraExecutor: ExecutorService
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var imageCapture: ImageCapture? = null
    private var currentLocation: Location? = null
    private var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        //setContentView(R.layout.activity_photo)

        // Binding
        binding = ActivityPhotoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialisation de la bottomNavBar
        setupBottomNavigation()

        // Initialisation de la localisation
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Vérifier et demander les permissions
        if (allPermissionsGranted()) {
            startCamera()
            getLastKnownLocation()
        } else {
            requestPermissions.launch(REQUIRED_PERMISSIONS)
        }

        binding.captureButton.setOnClickListener {
            takePhoto()
            // Effet de pression sur le bouton
            binding.captureButton.animate()
                .scaleX(0.85f)
                .scaleY(0.85f)
                .setDuration(100)
                .withEndAction {
                    binding.captureButton.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(100)
                }

            // Masquer brièvement la Preview pour un effet de capture
            binding.viewFinder.animate()
                .alpha(0f)
                .setDuration(100)
                .withEndAction {
                    binding.viewFinder.animate()
                        .alpha(1f)
                        .setDuration(100)
                }
        }

        binding.switchCameraButton.setOnClickListener { switchCamera() }

        cameraExecutor = Executors.newSingleThreadExecutor()


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun switchCamera() {
        cameraSelector = if (cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
            CameraSelector.DEFAULT_FRONT_CAMERA
        } else {
            CameraSelector.DEFAULT_BACK_CAMERA
        }
        startCamera() // Relancer la caméra avec la nouvelle sélection
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.selectedItemId = R.id.nav_camera

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_map -> {
                    startActivity(Intent(this, MapsActivity::class.java))
                    overridePendingTransition(0, 0) // Supprime l’animation de transition
                    finish()
                }
            }
            true
        }
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            val cameraSelector = this.cameraSelector

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageCapture)
            } catch (exc: Exception) {
                Log.e(TAG, "Erreur lors du démarrage de la caméra", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        val photoFile = File(externalMediaDirs.firstOrNull(), "IMG_${SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())}.jpg")

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Erreur de capture", exc)
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    getLastKnownLocation { location ->
                        if (location != null) {
                            saveExifData(photoFile, location)
                        }
                        Toast.makeText(baseContext, "Photo capturée !", Toast.LENGTH_SHORT).show()
                    }
                }
            })
    }

    private fun getLastKnownLocation(callback: (Location?) -> Unit = {}) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                currentLocation = location
                callback(location)
            }
        } else {
            callback(null)
        }
    }

    private fun saveExifData(photoFile: File, location: Location) {
        try {
            val exif = ExifInterface(photoFile.absolutePath)
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE, convertToDMS(location.latitude))
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE, convertToDMS(location.longitude))
            exif.setAttribute(ExifInterface.TAG_GPS_LATITUDE_REF, if (location.latitude >= 0) "N" else "S")
            exif.setAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF, if (location.longitude >= 0) "E" else "W")
            exif.saveAttributes()
            Log.d(TAG, "Localisation ajoutée aux métadonnées EXIF")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun convertToDMS(coordinate: Double): String {
        val degrees = coordinate.toInt()
        val minutesFull = (coordinate - degrees) * 60
        val minutes = minutesFull.toInt()
        val seconds = (minutesFull - minutes) * 60
        return String.format("%d/1,%d/1,%d/1", degrees, minutes, seconds.toInt())
    }

    private val requestPermissions = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
        if (permissions.all { it.value }) {
            startCamera()
            getLastKnownLocation()
        } else {
            Toast.makeText(this, "Permissions refusées", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun allPermissionsGranted(): Boolean {
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private const val TAG = "PhotoActivity"
        private val REQUIRED_PERMISSIONS = arrayOf(Manifest.permission.CAMERA, Manifest.permission.ACCESS_FINE_LOCATION)
    }
}