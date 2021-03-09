package ru.netology.nmedia.adapter

import android.net.Uri
import android.opengl.Visibility
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.util.AndroidUtils.display

interface OnInterractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onPlayMedia(post: Post) {}
    fun onOpenCard(post: Post) {}
}

class PostAdapter(
        private val onInterractionListener: OnInterractionListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallBack()) {
    private val urls = listOf("tcs.jpg", "sber.jpg", "netology.jpg", "404.png")
    private var index = 0
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        if (index == urls.size) {
            index = 0
        }

        val url = "http://10.0.2.2:9999/avatars/${urls[index++]}"
        Glide.with(binding.postAvatar)
                .load(url)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.postAvatar)

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
            video.text = post.url
            if (post.url == null) video.visibility = View.GONE

            favorite.isChecked = post.likedByMe
            favorite.text = display(post.likes)

            share.text = display(post.share)

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

            binding.root.setOnClickListener {
                onInterractionListener.onOpenCard(post)
            }
        }
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




