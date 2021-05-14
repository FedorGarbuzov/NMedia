package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Post
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
    private val onInterractionListener: OnInterractionListener,
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
    private val onInterractionListener: OnInterractionListener,
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

            share.text = display(post.share)
            postMenu.visibility = if (post.ownedByMe) View.VISIBLE else View.INVISIBLE

            postMenu.setOnClickListener {
                PopupMenu(it.context, it).apply {
                    inflate(R.menu.post_options)
//                    menu.setGroupVisible(R.id.owned, post.ownedByMe)
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

            val url = "$BASE_URL/avatars/${post.authorAvatar}"
            Glide.with(binding.postAvatar)
                .load(url)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .circleCrop()
                .into(binding.postAvatar)

            val attUrl = "$BASE_URL/images/${post.attachment?.url}"
            Glide.with(binding.attachment)
                .load(attUrl)
                .placeholder(R.drawable.ic_loading_100dp)
                .error(R.drawable.ic_error_100dp)
                .timeout(10_000)
                .into(binding.attachment)

            val myUrl = "$BASE_URL/media/${post.attachment?.url}"
            Glide.with(binding.attachment)
                    .load(myUrl)
                    .placeholder(R.drawable.ic_loading_100dp)
                    .error(R.drawable.ic_error_100dp)
                    .timeout(10_000)
                    .into(binding.attachment)

            favorite.setOnClickListener {
                onInterractionListener.onLike(post)
            }

            share.setOnClickListener {
                onInterractionListener.onShare(post)
            }

            attachment.setOnClickListener {
                onInterractionListener.onPlayMedia(post)
            }

            binding.root.setOnClickListener {
                onInterractionListener.onOpenCard(post)
            }
            done?.isVisible = post.ownedByMe
            done?.isEnabled = post.uploadedToServer

            if(done?.isEnabled == false) {
                favorite.isClickable = false
                share.isClickable = false
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




