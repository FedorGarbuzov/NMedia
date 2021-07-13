package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils.display
import ru.netology.nmedia.util.AndroidUtils.loadAvatar
import ru.netology.nmedia.util.AndroidUtils.loadImage

interface OnInteractionListener {
    fun onLike(post: Post) {}
    fun onShare(post: Post) {}
    fun onEdit(post: Post) {}
    fun onRemove(post: Post) {}
    fun onPlayMedia(post: Post) {}
    fun onOpenCard(post: Post) {}
}

class PostAdapter(
        private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<Post, PostViewHolder>(PostDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        return PostViewHolder(binding, onInteractionListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        getItem(position)?.let { post ->
            holder.bind(post)
        }
    }
}

class PostViewHolder(
        private val binding: PostCardBinding,
        private val onInteractionListener: OnInteractionListener,
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            userName.text = post.author
            postAvatar.setImageResource(itemViewType)
            published.text = post.published
            content.text = post.content
            share.text = display(post.share)
            favorite.text = display(post.likes)
            views.text = display(post.views)
            if (post.attachment?.url == null) attachment.visibility = View.GONE

            favorite.isChecked = post.likedByMe
            favorite.text = display(post.likes)

            postMenu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            postMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.post_options)
                    setOnMenuItemClickListener { item ->
                        when (item.itemId) {
                            R.id.remove -> {
                                onInteractionListener.onRemove(post)
                                true
                            }
                            R.id.edit -> {
                                onInteractionListener.onEdit(post)
                                true
                            }
                            else -> false
                        }
                    }
                }.show()
            }

            val url = "$BASE_URL/avatars/${post.authorAvatar}"
            loadAvatar(binding.postAvatar, url)

            val myUrl = "$BASE_URL/media/${post.attachment?.url}"
            loadImage(binding.attachment, myUrl)

            favorite.setOnClickListener {
                    onInteractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInteractionListener.onShare(post)
            }

            attachment.setOnClickListener {
                onInteractionListener.onPlayMedia(post)
            }

            binding.root.setOnClickListener {
                onInteractionListener.onOpenCard(post)
            }
            done?.isVisible = post.ownedByMe
            done?.isEnabled = post.uploadedToServer

            if (done?.isEnabled == false) {
                favorite.isClickable = false
                share.isClickable = false
            }
        }
    }
}

class PostDiffCallBack : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }
}