package ru.netology.nmedia.repository.post

import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

interface PostRepository {
    val data: Flow<PagingData<Post>>
    val dbPosts: Flow<List<Post>>
    fun getNewer(id: Long): Flow<List<Post>>
    suspend fun loadNewer()
    suspend fun likeById(id: Long)
    suspend fun shareById(id: Long)
//    suspend fun getAll()
    suspend fun getLatest()
    suspend fun removeByIdWork(id: Long)
    suspend fun likedByMe(id: Long)
    suspend fun unlikedByMe(id: Long)
    suspend fun upload(upload: MediaUpload): Media
    suspend fun saveWork(post: Post, upload: MediaUpload?): Long
    suspend fun processWork(id: Long)
}