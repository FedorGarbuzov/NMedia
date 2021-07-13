package ru.netology.nmedia.repository.post

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

interface PostRepository {
    val data: Flow<PagingData<Post>>
    val dbPosts: LiveData<List<Post>>
    fun getNewer(id: Long): Flow<List<Post>>
    suspend fun loadNewer(): List<PostEntity>
    suspend fun likeById(id: Long)
    suspend fun shareById(id: Long)
    suspend fun getAfter(): List<Post>
    suspend fun getLatest(): List<Post>
    suspend fun removeById(id: Long)
    suspend fun likeByMe(id: Long): Post
    suspend fun unlikeByMe(id: Long): Post
    suspend fun upload(upload: MediaUpload): Media
    suspend fun saveWork(post: Post, upload: MediaUpload?): Long
    suspend fun processWork(id: Long)
}