package com.example.storyapp.view.main

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.storyapp.R
import com.example.storyapp.databinding.ActivityMainBinding
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.view.adapter.LoadingStateAdapter
import com.example.storyapp.view.adapter.StoryAdapter
import com.example.storyapp.view.add.AddActivity
import com.example.storyapp.view.maps.MapsActivity
import com.example.storyapp.view.welcome.WelcomeActivity
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    private lateinit var binding: ActivityMainBinding
    private lateinit var storyAdapter: StoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecommendedRecyclerView()
        setupSwipeRefresh()
        setupAction()

        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                startActivity(Intent(this, WelcomeActivity::class.java))
                finish()
            } else {
                loadData()
            }
        }
    }

    private fun setupRecommendedRecyclerView() {
        storyAdapter = StoryAdapter()
        binding.rvStory.apply {
            layoutManager = LinearLayoutManager(this@MainActivity, LinearLayoutManager.VERTICAL, false)
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }

        lifecycleScope.launch {
            storyAdapter.loadStateFlow.collectLatest { loadState ->
                binding.progressBar.visibility =
                    if (loadState.refresh is LoadState.Loading) View.VISIBLE
                    else View.GONE

                val errorState = loadState.source.append as? LoadState.Error
                    ?: loadState.source.prepend as? LoadState.Error
                    ?: loadState.append as? LoadState.Error
                    ?: loadState.prepend as? LoadState.Error

                errorState?.let {
                    Toast.makeText(
                        this@MainActivity,
                        "Error: ${it.error.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun loadData() {
        lifecycleScope.launch {
            viewModel.fetchStoriesPaging()
            viewModel.storiesPaging.collectLatest { pagingData ->
                storyAdapter.submitData(pagingData)
            }
        }
    }


    private fun setupAction() {
        binding.btnAdd.setOnClickListener {
            startActivity(Intent(this, AddActivity::class.java))
        }
    }

    private fun setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener {
            loadData()
            binding.swipeRefresh.isRefreshing = false
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.item_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_map -> {
                val intent = Intent(this, MapsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_exit -> {
                viewModel.logout()
                val intent = Intent(this, WelcomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                }
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        viewModel.getSession().observe(this) { user ->
            if (!user.isLogin) {
                finishAffinity()
            } else {
                super.onBackPressed()
            }
        }
    }
}