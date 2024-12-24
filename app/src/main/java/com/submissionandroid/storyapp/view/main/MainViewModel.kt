// MainViewModel.kt
package com.submissionandroid.storyapp.view.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.submissionandroid.storyapp.data.UserRepository
import com.submissionandroid.storyapp.data.pref.UserModel
import kotlinx.coroutines.launch

class MainViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return userRepository.getSession().asLiveData()
    }

    suspend fun getStories(token: String) = userRepository.getStories(token)

    fun logout() {
        viewModelScope.launch {
            userRepository.logout()
        }
    }

}
