package com.example.storyapp.view.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.LoginResponse
import com.example.storyapp.data.retrofit.ApiConfig
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Response

class LoginViewModel(private val repository: UserRepository) : ViewModel() {
    private val _login = MutableLiveData<LoginResponse>()
    val login: LiveData<LoginResponse> = _login


    fun saveSession(user: UserModel) {
        viewModelScope.launch {
            repository.saveSession(user)
        }
    }

    fun loginUser(email: String, password: String){
        val jsonObject = JsonObject().apply {
            addProperty("email", email)
            addProperty("password", password)
        }
        val client = ApiConfig.getApiService().login(jsonObject)
        client.enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    _login.value = response.body()
                    Log.e("Login Auth", "Succesfully Login")
                }else{
                    Log.e("Login Auth", "onFailure: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                Log.e("Login Auth", "onFailure: ${t.message.toString()}")
            }
        })
    }
}