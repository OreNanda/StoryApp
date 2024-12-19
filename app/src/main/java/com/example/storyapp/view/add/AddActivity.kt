package com.example.storyapp.view.add

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.example.storyapp.databinding.ActivityAddBinding
import com.example.storyapp.view.ViewModelFactory
import id.zelory.compressor.Compressor
import id.zelory.compressor.constraint.quality
import id.zelory.compressor.constraint.size
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddBinding
    private val viewModel by viewModels<AddViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private var currentPhotoPath: String? = null
    private var imageUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.switchLocation.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                if (ContextCompat.checkSelfPermission(
                        this, Manifest.permission.ACCESS_FINE_LOCATION
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    binding.switchLocation.isChecked = false

                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                } else {
                    binding.switchLocation.isChecked = true
                    Toast.makeText(this, "Location enabled", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Location disabled", Toast.LENGTH_SHORT).show()
            }
        }

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnGallery.setOnClickListener {
            startGallery()
        }

        binding.btnCamera.setOnClickListener {
            startCamera()
        }

        binding.buttonAdd.setOnClickListener {
            uploadStory()
        }
    }

    @SuppressLint("QueryPermissionsNeeded")
    private fun startCamera() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.resolveActivity(packageManager)?.let {
            val photoFile: File? = try {
                createImageFile()
            } catch (ex: IOException) {
                null
            }

            photoFile?.also {
                val photoURI: Uri = FileProvider.getUriForFile(
                    this,
                    "${packageName}.fileprovider",
                    it
                )
                intent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI)
                launcherCamera.launch(intent)
            }
        }
    }

    private val launcherCamera = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val file = File(currentPhotoPath!!)
            file.let { file ->
                imageUri = Uri.fromFile(file)
                binding.ivPhoto.setImageURI(imageUri)
            }
        }
    }

    @Throws(IOException::class)
    private fun createImageFile(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val storageDir: File = getExternalFilesDir(Environment.DIRECTORY_PICTURES)!!
        return File.createTempFile(
            "JPEG_${timeStamp}_",
            ".jpg",
            storageDir
        ).apply {
            currentPhotoPath = absolutePath
        }
    }

    private fun startGallery() {
        launcherGallery.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
    }

    private val launcherGallery = registerForActivityResult(
        ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        uri?.let {
            imageUri = uri
            binding.ivPhoto.setImageURI(uri)
        }
    }

    private fun uploadStory() {
        val description = binding.edAddDescription.text.toString()

        if (description.isEmpty()) {
            binding.edAddDescription.error = "Deskripsi tidak boleh kosong"
            return
        }

        if (imageUri == null) {
            Toast.makeText(this, "Pilih gambar terlebih dahulu", Toast.LENGTH_SHORT).show()
            return
        }

        val (lat, lon) = getLocationIfNeeded()

        binding.progressBar.visibility = View.VISIBLE
        binding.buttonAdd.isEnabled = false

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val imageFile = uriToFile(imageUri!!, this@AddActivity)
                val compressedFile = Compressor.compress(this@AddActivity, imageFile) {
                    quality(75)
                    size(1_000_000)
                }

                val requestBody = description.toRequestBody("text/plain".toMediaType())
                val requestImageFile = compressedFile.asRequestBody("image/jpeg".toMediaType())
                val multipartBody = MultipartBody.Part.createFormData(
                    "photo",
                    compressedFile.name,
                    requestImageFile
                )

                withContext(Dispatchers.Main) {
                    viewModel.getSession().observe(this@AddActivity) { user ->
                        val token = "Bearer ${user.token}"
                        viewModel.addStory(requestBody, multipartBody, lat, lon, token).observe(this@AddActivity) { result ->
                            binding.progressBar.visibility = View.GONE
                            binding.buttonAdd.isEnabled = true

                            result.onSuccess {
                                Toast.makeText(this@AddActivity, "Upload berhasil!", Toast.LENGTH_SHORT).show()
                                finish()
                            }.onFailure {
                                Toast.makeText(this@AddActivity, "Upload gagal. Coba lagi.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    binding.progressBar.visibility = View.GONE
                    binding.buttonAdd.isEnabled = true
                    Toast.makeText(this@AddActivity, "Gagal memproses gambar", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun getLocationIfNeeded(): Pair<RequestBody?, RequestBody?> {
        var lat: RequestBody? = null
        var lon: RequestBody? = null

        if (binding.switchLocation.isChecked) {
            if (checkPermissions()) {
                val location = getLastKnownLocation()
                lat = location?.latitude?.toString()?.toRequestBody("text/plain".toMediaType())
                lon = location?.longitude?.toString()?.toRequestBody("text/plain".toMediaType())
            } else {
                requestLocationPermission()
            }
        }

        return Pair(lat, lon)
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun getLastKnownLocation(): Location? {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED) {

            try {
                val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
                return locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            } catch (e: SecurityException) {
                Log.e("AddActivity", "Permission denied or error occurred while fetching location", e)
                return null
            }
        } else {
            Log.e("AddActivity", "Location permission not granted")
            return null
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                binding.switchLocation.isChecked = true
                Toast.makeText(this, "Permission granted. Location enabled", Toast.LENGTH_SHORT).show()
            } else {
                binding.switchLocation.isChecked = false
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun uriToFile(imageUri: Uri, context: Context): File {
        val myFile = createCustomTempFile(context)
        val inputStream = context.contentResolver.openInputStream(imageUri)
            ?: throw IOException("Cannot open input stream")

        inputStream.use { input ->
            myFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return myFile
    }

    private fun createCustomTempFile(context: Context): File {
        return try {
            File.createTempFile("temp_image_", ".jpg", context.cacheDir)
        } catch (e: IOException) {
            File(context.cacheDir, "temp_image.jpg")
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 100
    }
}