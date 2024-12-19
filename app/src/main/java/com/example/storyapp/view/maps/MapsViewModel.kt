package com.example.storyapp.view.maps

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.ListStoryItem
import kotlinx.coroutines.launch

class MapsViewModel(private val repository: UserRepository) : ViewModel() {
    fun getStoriesWithLocation(): LiveData<List<ListStoryItem>> {
        val result = MutableLiveData<List<ListStoryItem>>()
        viewModelScope.launch {
            try {
                val response = repository.getStoriesWithLocation()
                if (response.isSuccessful) {
                    result.postValue(response.body()?.listStory ?: emptyList())
                }
            } catch (e: Exception) {
                result.postValue(emptyList())
            }
        }
        return result
    }
}