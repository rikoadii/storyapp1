package com.submissionandroid.storyapp.data

import com.submissionandroid.storyapp.data.pref.UserPreference
import com.submissionandroid.storyapp.service.ApiService
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import okhttp3.MultipartBody
import okhttp3.RequestBody

class StoryRepository(
    private val apiService: ApiService,
    private val userPreference: UserPreference
) {

    fun uploadStory(
        token: String,
        photo: MultipartBody.Part,
        description: RequestBody
    ) = apiService.uploadStory(token, photo, description)

    fun getToken(): String {
        return runBlocking {
            val user = userPreference.getSession().first()
            user.token ?: ""
        }
    }
}
