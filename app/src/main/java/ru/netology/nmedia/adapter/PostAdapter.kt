package ru.netology.nmedia.adapter

import android.net.Uri
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.post.Post

interface OnInterractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onPlayMedia(post: Post) {}
}

class PostAdapter(
        private val onInterractionListener: OnInterractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInterractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
        private val binding: PostCardBinding,
        private val onInterractionListener: OnInterractionListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            userName.text = post.author
            published.text = post.published
            content.text = post.content
            share.text = display(post.share)
            favorite.text = display(post.likes)
            views.text = display(post.views)
            if (post.url == null) video.visibility = View.GONE

            favorite.isChecked = post.likedByMe
            favorite.text = display(post.likes)

            share.setText(display(post.share))

            postMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.post_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInterractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInterractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            favorite.setOnClickListener {
                onInterractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInterractionListener.onShare(post)
            }

            video.setOnClickListener {
                onInterractionListener.onPlayMedia(post)
            }
        }
    }

    private fun display(num: Int): String {
        var newString = "$num"
        if (num >= 1_000) {
            val first = Character.getNumericValue(num.toString()[0])
            val second = Character.getNumericValue(num.toString()[1])
            val third = Character.getNumericValue(num.toString()[2])
            when {
                (num in 1_000..9_999) -> newString = "${first}.${second}K"
                (num in 10_000..99_999) -> newString = "${first}${second}K"
                (num in 100_000..999_999) -> newString = "${first}${second}${third}K"
                (num in 1_000_000..9_999_999) -> newString = "${first}.${second}M"
                (num in 10_000_000..99_999_999) -> newString = "${first}${second}.${third}M"
                (num in 100_000_000..999_999_000) -> newString = "${first}${second}${third}M"
            }
        }
        return newString
    }
}

class PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}




