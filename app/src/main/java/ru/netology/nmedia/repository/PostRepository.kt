package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import okhttp3.Callback
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.post.Post

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewer(id: Long): Flow<List<Post>>
    suspend fun loadNewer()
    suspend fun likeById(id: Long)
    suspend fun shareById(id: Long)
    suspend fun save(post: Post)
    suspend fun getAll()
    suspend fun removeById(id: Long)
    suspend fun likedByMe(id: Long)
    suspend fun unlikedByMe(id: Long)
}