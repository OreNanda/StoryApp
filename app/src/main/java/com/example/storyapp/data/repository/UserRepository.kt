package com.example.storyapp.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import com.example.storyapp.data.paging.StoryPagingSource
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.pref.UserPreference
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.response.StoryResponse
import com.example.storyapp.data.retrofit.ApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.Response

class UserRepository private constructor(
    private val userPreference: UserPreference,
    private val apiService: ApiService
) {

    suspend fun saveSession(user: UserModel) {
        userPreference.saveSession(user)
    }

    fun getSession(): Flow<UserModel> {
        return userPreference.getSession()
    }

    suspend fun logout() {
        userPreference.logout()
    }

    fun getStoriesPaging(token: String): Flow<PagingData<ListStoryItem>> {
        return Pager(
            config = PagingConfig(
                pageSize = StoryPagingSource.PAGE_SIZE,
                enablePlaceholders = false
            ),
            pagingSourceFactory = { StoryPagingSource(apiService, token) }
        ).flow
    }

    suspend fun getStoriesWithLocation(): Response<StoryResponse> {
        val user = userPreference.getSession().first()
        if (user.token.isNotEmpty()) {
            return apiService.getStoriesWithLocation("Bearer ${user.token}")
        } else {
            throw IllegalStateException("User not logged in")
        }
    }

    companion object {
        @Volatile
        private var instance: UserRepository? = null
        fun getInstance(
            userPreference: UserPreference,
            apiService: ApiService
        ): UserRepository =
            instance ?: synchronized(this) {
                instance ?: UserRepository(userPreference, apiService)
            }.also { instance = it }
    }
}