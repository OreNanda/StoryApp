package com.example.storyapp.view.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.DetailResponse
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.retrofit.ApiConfig
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainViewModel(private val repository: UserRepository) : ViewModel() {
    private val _detailStories = MutableLiveData<DetailResponse>()
    val detailStories: LiveData<DetailResponse> = _detailStories

    private val _storiesPaging = MutableStateFlow<PagingData<ListStoryItem>>(PagingData.empty())
    val storiesPaging: StateFlow<PagingData<ListStoryItem>> = _storiesPaging.asStateFlow()

    init {
        fetchStoriesPaging()
    }

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun logout() {
        viewModelScope.launch {
            repository.logout()
        }
    }

    fun fetchStoriesPaging() {
        viewModelScope.launch {
            repository.getSession().collect { user ->
                if (user.isLogin) {
                    val token = "Bearer ${user.token}"
                    repository.getStoriesPaging(token)
                        .cachedIn(viewModelScope)
                        .collect { pagingData ->
                            _storiesPaging.value = pagingData
                        }
                }
            }
        }
    }

    fun getDetailStories(authToken: String, id: String) {
        val client = ApiConfig.getApiService().getDetailStories(authToken, id)
        client.enqueue(object : Callback<DetailResponse> {
            override fun onResponse(call: Call<DetailResponse>, response: Response<DetailResponse>) {
                if (response.isSuccessful) {
                    _detailStories.value = response.body()
                    Log.e("Fetch Detail Stories", "Successfully Fetch Detail Stories")
                } else {
                    Log.e("Fetch Detail Stories", "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<DetailResponse>, t: Throwable) {
                Log.e("Fetch Detail Stories", "onFailure: ${t.message.toString()}")
            }
        })
    }
}