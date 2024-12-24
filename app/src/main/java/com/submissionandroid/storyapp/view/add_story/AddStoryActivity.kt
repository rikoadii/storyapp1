package com.submissionandroid.storyapp.view.add_story

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.submissionandroid.storyapp.data.AddNewStoryResponse
import com.submissionandroid.storyapp.data.StoryRepository
import com.submissionandroid.storyapp.data.pref.UserPreference
import com.submissionandroid.storyapp.data.pref.dataStore
import com.submissionandroid.storyapp.databinding.ActivityAddStoryBinding
import com.submissionandroid.storyapp.service.ApiConfig
import com.submissionandroid.storyapp.utils.FileHelper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.ByteArrayOutputStream
import java.io.File

class AddStoryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAddStoryBinding
    private var selectedImageFile: File? = null
    private lateinit var storyRepository: StoryRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddStoryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val userPreference = UserPreference.getInstance(dataStore)
        storyRepository = StoryRepository(ApiConfig.getApiService(), userPreference)

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnGallery.setOnClickListener { openGallery() }
        binding.btnCamera.setOnClickListener { openCamera() }
        binding.btnUpload.setOnClickListener { uploadStory() }
    }

    private val galleryLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            uri?.let {
                selectedImageFile = FileHelper.uriToFile(it, this)
                Glide.with(this).load(it).into(binding.imagePreview)
            }
        }

    private fun openGallery() {
        galleryLauncher.launch("image/*")
    }

    private val cameraLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap: Bitmap? ->
            bitmap?.let {
                val uri = FileHelper.saveBitmapToFile(it, this)
                selectedImageFile = FileHelper.uriToFile(uri, this)
                Glide.with(this).load(it).into(binding.imagePreview)
            }
        }

    private fun openCamera() {
        cameraLauncher.launch(null)
    }

    private fun uploadStory() {
        val description = binding.etDescription.text.toString()
        if (description.isBlank() || selectedImageFile == null) {
            Toast.makeText(this, "Please add description and image!", Toast.LENGTH_SHORT).show()
            return
        }

        val fileToUpload = if (selectedImageFile!!.length() > 1 * 1024 * 1024) {
            compressImage(selectedImageFile!!)
        } else {
            selectedImageFile!!
        }

        val file = fileToUpload.asRequestBody("image/jpeg".toMediaType())
        val imageMultipart = MultipartBody.Part.createFormData("photo", fileToUpload.name, file)
        val descriptionBody = RequestBody.create("text/plain".toMediaType(), description)

        val token = storyRepository.getToken()
        if (token.isBlank()) {
            Toast.makeText(this, "Invalid token!", Toast.LENGTH_SHORT).show()
            return
        }

        storyRepository.uploadStory("Bearer $token", imageMultipart, descriptionBody)
            .enqueue(object : retrofit2.Callback<AddNewStoryResponse> {
                override fun onResponse(
                    call: retrofit2.Call<AddNewStoryResponse>,
                    response: retrofit2.Response<AddNewStoryResponse>
                ) {
                    if (response.isSuccessful && response.body()?.error == false) {
                        Toast.makeText(this@AddStoryActivity, "Story uploaded!", Toast.LENGTH_SHORT).show()
                        finish()
                    } else {
                        Toast.makeText(this@AddStoryActivity, "Failed to upload story!", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: retrofit2.Call<AddNewStoryResponse>, t: Throwable) {
                    Toast.makeText(this@AddStoryActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }


    private fun compressImage(file: File): File {
        val bitmap = BitmapFactory.decodeFile(file.path)
        var quality = 100
        val maxFileSize = 1 * 1024 * 1024 // 1 MB

        val compressedFile = File(file.parent, "compressed_${file.name}")
        do {
            val outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            val byteArray = outputStream.toByteArray()
            val fileSize = byteArray.size

            if (fileSize > maxFileSize) {
                quality -= 5
            } else {
                compressedFile.writeBytes(byteArray)
                break
            }
        } while (quality > 0)

        return compressedFile
    }
}
