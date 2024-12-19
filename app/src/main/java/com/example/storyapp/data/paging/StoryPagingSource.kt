package com.example.storyapp.data.paging

import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.data.retrofit.ApiService

class StoryPagingSource(
    private val apiService: ApiService,
    private val authToken: String
) : PagingSource<Int, ListStoryItem>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ListStoryItem> {
        return try {
            val position = params.key ?: INITIAL_PAGE_INDEX
            val response = apiService.getStories(authToken, position, params.loadSize)

            if (response.isSuccessful) {
                val data = response.body()?.listStory ?: emptyList()
                LoadResult.Page(
                    data = data,
                    prevKey = if (position == INITIAL_PAGE_INDEX) null else position - 1,
                    nextKey = if (data.isEmpty()) null else position + 1
                )
            } else {
                LoadResult.Error(Exception("Failed to load data: ${response.message()}"))
            }
        } catch (exception: Exception) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, ListStoryItem>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    companion object {
        fun snapshot(items: List<ListStoryItem>): PagingData<ListStoryItem> {
            return PagingData.from(items)
        }
        const val INITIAL_PAGE_INDEX = 1
        const val PAGE_SIZE = 20
    }
}