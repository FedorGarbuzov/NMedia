package ru.netology.nmedia.repository.post

import android.net.Uri
import androidx.core.net.toFile
import androidx.lifecycle.map
import androidx.paging.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.PostApiService
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.dao.PostWorkDao
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.*
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.AppError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.util.AndroidUtils.makeRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostRepositoryImp @Inject constructor(
    appDb: AppDb,
    private val postDao: PostDao,
    private val postWorkDao: PostWorkDao,
    private val postApi: PostApiService,
    private val postRemoteKeyDao: PostRemoteKeyDao
) : PostRepository {

    private val pageSize = 20

    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize),
        remoteMediator = PostRemoteMediator(postApi, appDb, postDao, postRemoteKeyDao),
        pagingSourceFactory = postDao::pagingSource,
    ).flow.map { pagingData ->
        pagingData.map(PostEntity::toPost)
    }

    override val dbPosts = postDao.getAll()
        .map(List<PostEntity>::toPost)

    override fun getNewer(id: Long): Flow<List<Post>> = flow {
        while (true) {
            delay(10_000L)
            makeRequest(
                request = { postApi.getNewer(id) },
                onSuccess = { body ->
                    postDao.insert(body.toEntity().map { postEntity ->
                        postEntity.copy(read = false)
                    })
                    emit(body)
                }
            )
        }
    }
        .catch { e -> throw AppError.from(e) }
        .flowOn(Dispatchers.Default)

    override suspend fun loadNewer(): List<PostEntity> {
        val newer = postDao.getNewer()
        postDao.insert(newer.map { postEntity ->
            postEntity.copy(read = true)
        })
        return newer
    }

    override suspend fun getLatest(): List<Post> = makeRequest(
        request = { postApi.getLatest(pageSize) },
        onSuccess = { body ->
            postDao.insert(body.toEntity())
            body
        }
    )

    override suspend fun getAfter(): List<Post> {
        val id = postRemoteKeyDao.max()
        id ?: return emptyList()
        return makeRequest(
            request = { postApi.getAfter(id, pageSize) },
            onSuccess = { body ->
                postDao.insert(body.toEntity())
                body
            }
        )
    }

    override suspend fun upload(upload: MediaUpload): Media {
        val media = MultipartBody.Part.createFormData(
            "file", upload.file.name, upload.file.asRequestBody()
        )
        return makeRequest(
            request = { postApi.upload(media) },
            onSuccess = { body ->
                body
            }
        )
    }

    override suspend fun likeByMe(id: Long): Post = makeRequest(
        request = { postApi.likeByMe(id) },
        onSuccess = { body ->
            postDao.insert(body.toEntity())
            body
        }
    )

    override suspend fun unlikeByMe(id: Long): Post = makeRequest(
        request = { postApi.unlikeByMe(id) },
        onSuccess = { body ->
            postDao.insert(body.toEntity())
            body
        }
    )

    override suspend fun likeById(id: Long) {
        try {
            return postDao.likeById(id)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun shareById(id: Long) {
        try {
            return postDao.shareById(id)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun saveWork(post: Post, upload: MediaUpload?): Long {
        try {
            val entity = post.toWorkEntity().apply {
                upload ?: return@apply
                this.uri = upload.file.toURI().toString()
            }
            return postWorkDao.insert(entity)
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun processWork(id: Long) {
        try {
            val entity = postWorkDao.getById(id)
            var post = entity.toPost()
            if (entity.uri != null) {
                val upload = MediaUpload(Uri.parse(entity.uri).toFile())
                post = post.copy(
                    attachment = Attachment(upload(upload).id, AttachmentType.IMAGE)
                )
            }
            val edited = dbPosts.value?.find { it.id == post.id && it.authorId == post.authorId }
            val old = dbPosts.value?.find { it.content == post.content && it.attachment == post.attachment }

            when {
                old != null -> uploadToServer(old)
                edited != null -> editPost(post, edited.copy(attachment = post.attachment))
                else -> {
                    post = post.copy(id = 0)
                    savePost(post)
                    postWorkDao.removeById(id)
                }
            }
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun removeById(id: Long) = makeRequest(
        request = { postApi.removeById(id) },
        onSuccess = { postDao.removeById(id) }
    )

    private suspend fun uploadToServer(post: Post): Post = makeRequest(
        request = { postApi.save(post) },
        onSuccess = { body ->
            postDao.insert(body.toEntity())
            body
        }
    )

    private suspend fun editPost(post: Post, edited: Post): Post = makeRequest(
        request = { postApi.save(edited.copy(content = post.content)) },
        onSuccess = { body ->
            postDao.insert(body.toEntity())
            body
        }
    )

    private suspend fun savePost(post: Post): Post {
        postDao.insert(post.copy(uploadedToServer = false, read = true).toEntity())
        return makeRequest(
            request = { postApi.save(post) },
            onSuccess = { body ->
                postDao.insert(body.toEntity())
                body
            }
        )
    }
}