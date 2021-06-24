package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.BuildConfig.BASE_URL
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.AdCardBinding
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.dto.Ad
import ru.netology.nmedia.dto.FeedItem
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.util.AndroidUtils.display
import ru.netology.nmedia.util.AndroidUtils.loadAvatar
import ru.netology.nmedia.util.AndroidUtils.loadImage

class FeedAdapter(
    private val onInteractionListener: OnInteractionListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(FeedItemDiffCallBack()) {
    private val typeAd = 0
    private val typePost = 1

    interface OnInteractionListener {
        fun onLike(post: Post) {}
        fun onEdit(post: Post) {}
        fun onRemove(post: Post) {}
        fun onShare(post: Post) {}
        fun onPlayMedia(post: Post) {}
        fun onOpenCard(post: Post) {}
        fun onAdClick(ad: Ad) {}
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is Ad -> typeAd
            is Post -> typePost
            null -> throw IllegalArgumentException("unknown item type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            typeAd -> AdViewHolder(
                AdCardBinding.inflate(layoutInflater, parent, false),
                onInteractionListener
            )
            typePost -> PostViewHolder(
                PostCardBinding.inflate(layoutInflater, parent, false),
                onInteractionListener
            )
            else -> throw IllegalArgumentException("unknown view type: $viewType")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        getItem(position)?.let {
            when (it) {
                is Post -> (holder as? PostViewHolder)?.bind(it)
                is Ad -> (holder as? AdViewHolder)?.bind(it)
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
                loadAvatar(postAvatar, "${BASE_URL}/avatars/${post.authorAvatar}")
//                postAvatar.setImageResource(itemViewType)
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


    class AdViewHolder(
        private val binding: AdCardBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(ad: Ad) {
            binding.apply {
                loadImage(image, "${BASE_URL}/media/${ad.image}")
                image.setOnClickListener {
                    onInteractionListener.onAdClick(ad)
                }
            }
        }
    }

    class FeedItemDiffCallBack : DiffUtil.ItemCallback<FeedItem>() {
        override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            if (oldItem::class != newItem::class) {
                return false
            }

            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
            return oldItem == newItem
        }
    }
}