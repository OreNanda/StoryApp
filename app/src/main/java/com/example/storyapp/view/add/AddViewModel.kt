package com.example.storyapp.view.add

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.AddResponse
import com.example.storyapp.data.retrofit.ApiConfig
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AddViewModel(private val repository: UserRepository) : ViewModel() {

    fun getSession(): LiveData<UserModel> {
        return repository.getSession().asLiveData()
    }

    fun addStory(
        description: RequestBody,
        photo: MultipartBody.Part,
        lat: RequestBody? = null,
        lon: RequestBody? = null,
        authToken: String
    ): LiveData<Result<AddResponse>> {
        val result = MutableLiveData<Result<AddResponse>>()

        val client = ApiConfig.getApiService().addStory(description, photo, lat, lon, authToken)
        client.enqueue(object : Callback<AddResponse> {
            override fun onResponse(call: Call<AddResponse>, response: Response<AddResponse>) {
                if (response.isSuccessful) {
                    response.body()?.let {
                        result.value = Result.success(it)
                    } ?: run {
                        result.value = Result.failure(Exception("Response body is null"))
                    }
                } else {
                    result.value = Result.failure(Exception(response.message()))
                }
            }

            override fun onFailure(call: Call<AddResponse>, t: Throwable) {
                result.value = Result.failure(t)
            }
        })
        return result
    }
}