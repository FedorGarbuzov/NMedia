package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.PostCardBinding
import ru.netology.nmedia.post.Post

typealias OnLikeListener = (post: Post) -> Unit
typealias OnShareListener = (post: Post) -> Unit

class PostAdapter(
        private val onLikeListener: OnLikeListener,
        private val onShareListener: OnShareListener
) : ListAdapter<Post, PostViewHolder>(PostDiffCallBack()) {
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onLikeListener, onShareListener)
    }

    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
    }
}

class PostViewHolder(
        private val binding: PostCardBinding,
        private val onLikeListener: OnLikeListener,
        private val onShareListener: OnShareListener
) : RecyclerView.ViewHolder(binding.root) {
    fun bind(post: Post) {
        binding.apply {
            userName.text = post.author
            published.text = post.published
            content.text = post.content
            shareTxt.text = display(post.share)
            likes.text = display(post.likes)
            views.text = display(post.views)

            favorite.setImageResource(
                    if (!post.likedByMe) R.drawable.ic_baseline_favorite_border_24 else R.drawable.ic_baseline_favorite_24)
            likes.setText(
                    if (!post.likedByMe) display(post.likes) else display(post.likes + 1))

            shareTxt.setText(display(post.share))

            favorite.setOnClickListener {
                onLikeListener(post)
            }

            shareImg.setOnClickListener {
                onShareListener(post)
            }
        }
    }

//class PostShareAdapter(
//        private val onShareListener: OnShareListener
//) : ListAdapter<Post, PostShareViewHolder>(PostDiffCallBack()) {
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostShareViewHolder {
//        val binding = PostCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
//        return PostShareViewHolder(binding, onShareListener)
//    }
//
//    override fun onBindViewHolder(holder: PostShareViewHolder, position: Int) {
//        val post = getItem(position)
//        holder.bind(post)
//    }
//}
//
//class PostShareViewHolder(
//        private val binding: PostCardBinding,
//        private val onShareListener: OnShareListener
//) : RecyclerView.ViewHolder(binding.root) {
//    fun bind(post: Post) {
//        binding.apply {
//            userName.text = post.author
//            published.text = post.published
//            content.text = post.content
//            shareTxt.text = display(post.share)
//            likes.text = display(post.likes)
//            views.text = display(post.views)
//
//            shareTxt.setText(display(post.share))
//
//            shareImg.setOnClickListener {
//                onShareListener(post)
//            }
//        }
//    }
//}

    private fun display(num: Int): String {
        val first = Character.getNumericValue(num.toString()[0])
        val second = Character.getNumericValue(num.toString()[1])
        val third = Character.getNumericValue(num.toString()[2])
        var newString = ""
        when {
            (num < 1_000) -> newString = num.toString()
            (num in 1_000..9_999) -> newString = "${first}.${second}K"
            (num in 10_000..99_999) -> newString = "${first}${second}K"
            (num in 100_000..999_999) -> newString = "${first}${second}${third}K"
            (num in 1_000_000..9_999_999) -> newString = "${first}.${second}M"
            (num in 10_000_000..99_999_999) -> newString = "${first}${second}.${third}M"
            (num in 100_000_000..999_999_000) -> newString = "${first}${second}${third}M"
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




