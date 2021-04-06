package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import okhttp3.Callback
import ru.netology.nmedia.post.Post

interface PostRepository {
    val data: LiveData<List<Post>>
    suspend fun likeById(id: Long)
    suspend fun shareById(id: Long)
    suspend fun save(post: Post)
    suspend fun getAll()
    suspend fun removeById(id: Long)
    suspend fun likedByMe(id: Long)
    suspend fun unlikedByMe(id: Long)
}