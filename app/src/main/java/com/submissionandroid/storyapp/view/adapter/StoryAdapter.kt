package com.submissionandroid.storyapp.view.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.submissionandroid.storyapp.data.ListStoryItem
import com.submissionandroid.storyapp.databinding.ItemStoryBinding
import com.submissionandroid.storyapp.view.detail.DetailStoryActivity

class StoryAdapter(private val stories: List<ListStoryItem>, private val token: String) :
    RecyclerView.Adapter<StoryAdapter.StoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StoryViewHolder {
        val binding = ItemStoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return StoryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StoryViewHolder, position: Int) {
        holder.bind(stories[position])
    }

    override fun getItemCount(): Int = stories.size

    inner class StoryViewHolder(private val binding: ItemStoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(story: ListStoryItem) {
            binding.nameTextView.text = story.name
            binding.descriptionTextView.text = story.description
            Glide.with(binding.photoImageView.context)
                .load(story.photoUrl)
                .into(binding.photoImageView)

            binding.root.setOnClickListener {
                val intent = Intent(binding.root.context, DetailStoryActivity::class.java)
                intent.putExtra(DetailStoryActivity.EXTRA_STORY_ID, story.id)
                intent.putExtra(DetailStoryActivity.EXTRA_TOKEN, token)
                binding.root.context.startActivity(intent)
            }
        }
    }
}
