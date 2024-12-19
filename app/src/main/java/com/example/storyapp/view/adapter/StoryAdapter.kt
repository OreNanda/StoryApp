package com.example.storyapp.view.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.app.ActivityOptionsCompat
import androidx.core.util.Pair
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.storyapp.data.response.ListStoryItem
import com.example.storyapp.databinding.ListStoryBinding
import com.example.storyapp.view.detail.DetailActivity

class StoryAdapter : PagingDataAdapter<ListStoryItem, StoryAdapter.MyViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val binding = ListStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MyViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val data = getItem(position)
        if (data != null) {
            holder.bind(data)
        }
    }

    class MyViewHolder(private val binding: ListStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(data: ListStoryItem) {
            Glide.with(binding.ivItemPhoto.context)
                .load(data.photoUrl)
                .into(binding.ivItemPhoto)
            binding.tvItemName.text = data.name
            binding.tvItemDescription.text = data.description

            itemView.setOnClickListener {
                val context = itemView.context
                val intent = Intent(context, DetailActivity::class.java).apply {
                    putExtra("ID_STORY", data.id)
                }

                val optionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(
                    context as Activity,
                    Pair(binding.ivItemPhoto, "photo"),
                    Pair(binding.tvItemName, "name"),
                    Pair(binding.tvItemDescription, "description")
                )

                context.startActivity(intent, optionsCompat.toBundle())
            }
        }
    }

    companion object {
         val DIFF_CALLBACK = object : DiffUtil.ItemCallback<ListStoryItem>() {
            override fun areItemsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(oldItem: ListStoryItem, newItem: ListStoryItem): Boolean {
                return oldItem == newItem
            }
        }
    }
}