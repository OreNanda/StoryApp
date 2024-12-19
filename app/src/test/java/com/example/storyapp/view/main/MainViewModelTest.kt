package com.example.storyapp.view.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import androidx.recyclerview.widget.ListUpdateCallback
import com.example.storyapp.DataDummy
import com.example.storyapp.MainDispatcherRule
import com.example.storyapp.data.paging.StoryPagingSource
import com.example.storyapp.data.pref.UserModel
import com.example.storyapp.data.repository.UserRepository
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.view.adapter.StoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class MainViewModelTest {

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRules = MainDispatcherRule()

    @Mock private lateinit var userRepository: UserRepository

    @Test
    fun `when get Stories Should Not Null and Return Data`() = runTest {
        val dummyStory = DataDummy.generateDummyStoryResponse()
        val data: PagingData<ListStoryItem> = StoryPagingSource.snapshot(dummyStory)

        val mockUser = UserModel(isLogin = true, name = "aaa", email = "aaa@gmail.com", token = "dummyToken")
        `when`(userRepository.getSession()).thenReturn(flowOf(mockUser))

        `when`(userRepository.getStoriesPaging("Bearer dummyToken")).thenReturn(flowOf(data))

        val mainViewModel = MainViewModel(userRepository)
        val actualStory: PagingData<ListStoryItem> = mainViewModel.storiesPaging.first()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differ.submitData(actualStory)

        Assert.assertNotNull(differ.snapshot())
        Assert.assertEquals(dummyStory.size, differ.snapshot().size)
        Assert.assertEquals(dummyStory[0], differ.snapshot()[0])
    }

    @Test
    fun `when Get Stories Empty Should Return No Data`() = runTest {
        val data: PagingData<ListStoryItem> = PagingData.from(emptyList())
        val expectedStory = MutableLiveData<PagingData<ListStoryItem>>()
        expectedStory.value = data

        val mockUser = UserModel(isLogin = true, name = "aaa", email = "aaa@gmail.com", token = "dummyToken")
        `when`(userRepository.getSession()).thenReturn(flowOf(mockUser))

        `when`(userRepository.getStoriesPaging("Bearer dummyToken")).thenReturn(flowOf(data))

        val mainViewModel = MainViewModel(userRepository)
        val actualQuote: PagingData<ListStoryItem> = mainViewModel.storiesPaging.first()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = noopListUpdateCallback,
            workerDispatcher = Dispatchers.Main,
        )
        differ.submitData(actualQuote)
        Assert.assertEquals(0, differ.snapshot().size)
    }
}

val noopListUpdateCallback = object : ListUpdateCallback {
    override fun onInserted(position: Int, count: Int) {}
    override fun onRemoved(position: Int, count: Int) {}
    override fun onMoved(fromPosition: Int, toPosition: Int) {}
    override fun onChanged(position: Int, count: Int, payload: Any?) {}
}