package ru.netology.nmedia.repository.post

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.post.Media
import ru.netology.nmedia.post.MediaUpload
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.fromPost
import ru.netology.nmedia.entity.toPost
import ru.netology.nmedia.post.AttachmentType
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.post.Attachment
import ru.netology.nmedia.post.Post
import java.io.IOException

class PostRepositoryImp(private val dao: PostDao) : PostRepository {
    override val data = dao.getAll()
            .map(List<PostEntity>::toPost)
            .flowOn(Dispatchers.Default)

    override fun getNewer(id: Long): Flow<List<Post>> = flow {
        while (true) {
            delay(10_000L)
            val response = PostsApi.retrofitService.getNewer(id)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.fromPost())
            emit(body)
        }
    }
            .catch { e -> throw AppError.from(e) }
            .flowOn(Dispatchers.Default)

    override suspend fun loadNewer() {
        val newer = dao.getNewer()
        dao.insert(newer.map {
            it.copy(uploadedToServer = true, read = true)
        })
    }

    override suspend fun getAll() {
        try {
            val response = PostsApi.retrofitService.getAll()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(body.fromPost().map {
                it.copy(uploadedToServer = true, read = true)
            })
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun save(post: Post) {
        val old = data
                .first()
                .find { it.content == post.content }
        val edited = data
                .first()
                .find { it.id == post.id }
        if (old?.content == post.content) {
            uploadToServer(old)
        } else if (edited?.id == post.id) {
            editPost(post, edited)
        } else {
            savePost(post)
        }
    }

    override suspend fun saveWithAttachment(post: Post, upload: MediaUpload) {
        try {
            val media = upload(upload)
            val postWithAttachment = post.copy(attachment = Attachment(media.id, AttachmentType.IMAGE))
            save(postWithAttachment)
        } catch (e: AppError) {
            throw e
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = PostsApi.retrofitService.upload(media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }

            return response.body() ?: throw ApiError(response.code(), response.message())
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
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
        //TODO("Not yet implemented")
    }

    override suspend fun shareById(id: Long) {
        //TODO("Not yet implemented")
    }

    override suspend fun removeById(id: Long) {
        val post = data
                .first()
                .find { it.id == id }
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

    private suspend fun uploadToServer(post: Post) {
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromPost(body.copy(uploadedToServer = true, read = true)))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun editPost(post: Post, edited: Post) {
        dao.insert(PostEntity.fromPost(post.copy(uploadedToServer = false, read = true)))
        try {
            val response = PostsApi.retrofitService.save(edited.copy(content = post.content))
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromPost(body.copy(uploadedToServer = true, read = true)))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    private suspend fun savePost(post: Post) {
        dao.insert(PostEntity.fromPost(post.copy(uploadedToServer = false, read = true)))
        try {
            val response = PostsApi.retrofitService.save(post)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body()
                    ?: throw ApiError(response.code(), response.message())
            dao.insert(PostEntity.fromPost(body.copy(uploadedToServer = true, read = true)))
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}