package ru.netology.nmedia.repository

import okhttp3.Callback
import ru.netology.nmedia.post.Post

interface PostRepository {
    fun likeById(id: Long)
    fun shareById(id: Long)
    fun saveAsync(post: Post, callback: Callback<Post>)
    fun getAllAsync(callback: Callback<List<Post>>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
    fun likedByMeAsync(id: Long, callback: Callback<Post>)
    fun unlikedByMeAsync(id: Long, callback: Callback<Post>)

    interface Callback<T> {
        fun onSuccess(posts: T) {}
        fun onError(e: Exception) {}
    }

}