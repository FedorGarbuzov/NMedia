package ru.netology.nmedia.repository

import okhttp3.Callback
import ru.netology.nmedia.post.Post

interface PostRepository {
//    fun getAll(): List<Post>
//    fun likeByMe(id: Long)
//    fun unlikeByMe(id: Long)
    fun likeById(id: Long)
//    fun save(post: Post)
    fun shareById(id: Long)
//    fun removeById(id: Long)

    fun saveAsync(post: Post, callback: SaveCallback)

    interface SaveCallback {
        fun onSuccess(post: Post) {}
        fun onError(e: Exception) {}
    }

    fun getAllAsync(callback: GetAllCallback)

    interface GetAllCallback {
        fun onSuccess(posts: List<Post>) {}
        fun onError(e: Exception) {}
    }

    fun removeByIdAsync(id: Long, callback: RemoveByIdCallback)

    interface RemoveByIdCallback {
        fun onSuccess(id: Long) {}
        fun onError(e: Exception) {}
    }

    fun likeByMeAsync(id: Long, callback: LikeByMeCallback)

    interface LikeByMeCallback {
        fun onSuccess(id: Long) {}
        fun onError(e: Exception) {}
    }

    fun unlikeByMeAsync(id: Long, callback: UnlikeByMeCallback)

    interface UnlikeByMeCallback {
        fun onSuccess(id: Long) {}
        fun onError(e: Exception) {}
    }
}