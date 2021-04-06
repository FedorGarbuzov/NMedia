package ru.netology.nmedia.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.map
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.fromPost
import ru.netology.nmedia.entity.toPost
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.post.Post
import java.io.IOException

class PostRepositoryImp(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll().map(List<PostEntity>::toPost)

    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.fromPost().map {
                it.copy(uploadedToServer = true)
            })
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        val lastPost = data.value?.first()
        val old = data.value?.find { it.content == post.content }
        val edited = data.value?.find { it.id == post.id }
        val postId = lastPost?.id
        if (old?.content == post.content) {
            uploadToServer(old)
        } else if (edited?.id == post.id) {
            editPost(post, edited)
        } else {
            savePost(post, postId, old)
        }
    }

    override suspend fun likedByMe(id: Long) {
        dao.likedByMe(id)
        try {
            val response = PostsApi.retrofitService.likedByMe(id)
            if (!response.isSuccessful) {
                dao.unlikedByMe(id)
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun unlikedByMe(id: Long) {
        dao.unlikedByMe(id)
        try {
            val response = PostsApi.retrofitService.unlikedByMe(id)
            if (!response.isSuccessful) {
                dao.likedByMe(id)
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun likeById(id: Long) {
        //TODO("Not yetimplemented")
    }

    override suspend fun shareById(id: Long) {
        //TODO("Not yet implemented")
    }

    override suspend fun removeById(id: Long) {
        val post = data.value?.last { it.id == id }
        dao.removeById(id)
        try {
            val response = PostsApi.retrofitService.removeById(id)
            if (!response.isSuccessful) {
                if (post != null) dao.insert(PostEntity.fromPost(post))
                throw ApiError(response.code(), response.message())
            }
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun uploadToServer(post: Post) {
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromPost(body.copy(uploadedToServer = true)))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun editPost(post: Post, edited: Post) {
        dao.insert(PostEntity.fromPost(post.copy(uploadedToServer = false)))
        try {
            val response = PostsApi.retrofitService.save(edited.copy(content = post.content))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromPost(body.copy(uploadedToServer = true)))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private suspend fun savePost(post: Post, postId: Long?, old: Post?) {
        dao.insert(PostEntity.fromPost(post.copy(uploadedToServer = false)))
        try {
            val response = PostsApi.retrofitService.save(
                    if (postId != null && postId != old?.id) post.copy(id = postId + 1) else post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body()
                    ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromPost(body.copy(uploadedToServer = true)))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}


