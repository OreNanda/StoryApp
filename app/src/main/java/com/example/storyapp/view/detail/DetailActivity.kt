package com.example.storyapp.view.detail

import android.os.Bundle
import android.transition.TransitionInflater
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.storyapp.databinding.ActivityDetailBinding
import com.example.storyapp.view.ViewModelFactory
import com.example.storyapp.view.main.MainViewModel

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding
    private val viewModel by viewModels<MainViewModel> {
        ViewModelFactory.getInstance(this)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        window.sharedElementEnterTransition = TransitionInflater.from(this)
            .inflateTransition(android.R.transition.move)

        val storyId=intent.getStringExtra("ID_STORY")
        Log.d("ID STORY","$storyId")
        viewModel.getSession().observe(this) { user ->
            val token = "Bearer " + user.token
            storyId?.let { viewModel.getDetailStories(token, it) }
            viewModel.detailStories.observe(this) { detail ->
                binding.tvItemName.text = detail.story?.name
                binding.tvItemDescription.text = detail.story?.description
                Glide.with(this)
                    .load(detail.story?.photoUrl)
                    .into(binding.ivItemPhoto)
            }
        }
        binding.btnBack.setOnClickListener {
            finishAfterTransition()
        }
    }
}