package ru.netology.nmedia.repository.post

import android.net.Uri
import androidx.core.net.toFile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostApi
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostWorkDao
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostWorkEntity
import ru.netology.nmedia.entity.fromPost
import ru.netology.nmedia.entity.toPost
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException

class PostRepositoryImp(
    private val dao: PostDao,
    private val postWorkDao: PostWorkDao,
) : PostRepository {
    override val data = dao.getAll()
        .map(List<PostEntity>::toPost)
        .flowOn(Dispatchers.Default)

    override fun getNewer(id: Long): Flow<List<Post>> = flow {
        while (true) {
            delay(10_000L)
            val response = PostApi.retrofitService.getNewer(id)
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
            delay(1000)
            val response = PostApi.retrofitService.getAll()
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

    override suspend fun upload(upload: MediaUpload): Media {
        try {
            val media = MultipartBody.Part.createFormData(
                "file", upload.file.name, upload.file.asRequestBody()
            )

            val response = PostApi.retrofitService.upload(media)
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
            val response = PostApi.retrofitService.likedByMe(id)
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
            val response = PostApi.retrofitService.unlikedByMe(id)
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

    override suspend fun saveWork(post: Post, upload: MediaUpload?): Long {
        try {
            val entity = PostWorkEntity.fromPost(post).apply {
                if (upload != null) {
                    this.uri = upload.file.toURI().toString()
                }
            }
            return postWorkDao.insert(entity)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun processWork(id: Long) {
        try {
            val entity = postWorkDao.getById(id)
            val post = entity.toPost().copy(id = 0)
            val old = data.first()
                .find { it.content == post.content && it.attachment == post.attachment }
            val edited = data.first()
                .find { it.id == post.id }

            when {
                old != null -> {
                    uploadToServer(old)
                }
                edited != null -> {
                    editPost(post, edited)
                }
                else -> {
                    if (entity.uri != null) {
                        val upload = MediaUpload(Uri.parse(entity.uri).toFile())
                        val postWithAttachment = post.copy(
                            id = 0,
                            attachment = Attachment(upload(upload).id, AttachmentType.IMAGE)
                        )
                        savePost(postWithAttachment)
                    } else {
                        savePost(post)
                    }
                    postWorkDao.removeById(id)
                }
            }
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeByIdWork(id: Long) {
        val post = data.first()
            .find { it.id == id }
        dao.removeById(id)
        try {
            val response = PostApi.retrofitService.removeById(id)
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
            val response = PostApi.retrofitService.save(post)
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
            val response = PostApi.retrofitService.save(edited.copy(content = post.content))
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
            val response = PostApi.retrofitService.save(post)
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