package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.post.Post

interface PostRepository {
    val data: Flow<List<Post>>
    fun getNewer(id: Long): Flow<List<Post>>
    suspend fun loadNewer()
    suspend fun likeById(id: Long)
    suspend fun shareById(id: Long)
    suspend fun save(post: Post)
    suspend fun saveWithAttachment(post: Post, upload: MediaUpload)
    suspend fun getAll()
    suspend fun removeById(id: Long)
    suspend fun likedByMe(id: Long)
    suspend fun unlikedByMe(id: Long)
    suspend fun upload(upload: MediaUpload): Media
}