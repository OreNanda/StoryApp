package com.example.storyapp.data.retrofit

import com.example.storyapp.data.response.AddResponse
import com.example.storyapp.data.response.DetailResponse
import com.example.storyapp.data.response.LoginResponse
import com.example.storyapp.data.response.RegisterResponse
import com.example.storyapp.data.response.StoryResponse
import com.google.gson.JsonObject
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {
    @POST("login")
    fun login(
        @Body jsonObject: JsonObject
    ): Call<LoginResponse>

    @POST("register")
    fun register(
        @Body jsonObject: JsonObject
    ): Call<RegisterResponse>

    @GET("stories")
    suspend fun getStories(
        @Header("Authorization") authToken: String,
        @Query("page") page: Int? = 1,
        @Query("size") size: Int? = 20,
    ): Response<StoryResponse>

    @GET("stories/{id}")
    fun getDetailStories(
        @Header("Authorization") authToken: String,
        @Path("id") id:String
    ): Call<DetailResponse>

    @Multipart
    @POST("stories")
    fun addStory(
        @Part("description") description: RequestBody,
        @Part photo: MultipartBody.Part,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null,
        @Header("Authorization") authToken: String
    ): Call<AddResponse>

    @GET("stories")
    suspend fun getStoriesWithLocation(
        @Header("Authorization") authToken: String,
        @Query("location") location: Int = 1
    ): Response<StoryResponse>

}