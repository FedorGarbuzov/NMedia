package ru.netology.nmedia.viewModel

import android.app.Application
import android.net.Uri
import android.widget.Toast
import androidx.core.net.toFile
import androidx.lifecycle.*
import androidx.work.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.post.PostRepository
import ru.netology.nmedia.repository.post.PostRepositoryImp
import ru.netology.nmedia.util.SingleLiveEvent
import ru.netology.nmedia.work.SavePostWorker
import java.io.File

val emptyPost = Post(
    id = 0L,
    author = "",
    authorId = 0,
    authorAvatar = "",
    published = "",
    content = "",
    share = 0,
    likes = 0,
    views = 0,
    likedByMe = false,
    uploadedToServer = false,
    attachment = null,
    read = true
)

private val noPhoto = PhotoModel()

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImp(
            AppDb.getInstance(context = application).postDao(),
            AppDb.getInstance(context = application).postWorkDao()
        )

    private val workManager: WorkManager =
        WorkManager.getInstance(application)

    @ExperimentalCoroutinesApi
    val data: LiveData<FeedModel> = AppAuth.getInstance()
        .authStateFlow
        .flatMapLatest { (myId, _) ->
            repository.data
                .map { posts ->
                    FeedModel(
                        posts.map { it.copy(ownedByMe = it.authorId == myId) },
                        posts.isEmpty()
                    )
                }
        }.asLiveData(Dispatchers.Default)

    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState

    private val edited = MutableLiveData(emptyPost)

    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    val getNewer: LiveData<List<Post>> = data.switchMap {
        repository.getNewer(it.posts.firstOrNull()?.id ?: 0L)
            .catch { e -> e.printStackTrace() }
            .asLiveData()
    }

    fun loadNewer() = viewModelScope.launch {
        repository.loadNewer()
    }

    private val _photo = MutableLiveData(noPhoto)
    val photo: LiveData<PhotoModel>
        get() = _photo

    init {
        loadPosts()
    }

    fun loadPosts() = viewModelScope.launch {
        try {
            _dataState.value = FeedModelState(loading = true)
            repository.getAll()
            _dataState.value = FeedModelState()
        } catch (e: Exception) {
            _dataState.value = FeedModelState(errorLoading = true)
        }
    }

    fun save() {
        edited.value?.let {
            viewModelScope.launch {
                try {
                    val id = repository.saveWork(
                            it, _photo.value?.uri?.let { MediaUpload(it.toFile()) }
                    )
                    val data = workDataOf(SavePostWorker.postKey to id)
                    val constraints = Constraints.Builder()
                            .setRequiredNetworkType(NetworkType.CONNECTED)
                            .build()
                    val request = OneTimeWorkRequestBuilder<SavePostWorker>()
                            .setInputData(data)
                            .setConstraints(constraints)
                            .build()
                    workManager.enqueue(request)

                    _dataState.value = FeedModelState()
                    loadPosts()
                } catch (e: Exception) {
                    e.printStackTrace()
                    _dataState.value = FeedModelState(errorSaving = true)
                }
            }
            _postCreated.value = Unit
        }
        edited.value = emptyPost
        _photo.value = noPhoto
    }

    fun edit(post: Post) {
        edited.value = post
    }

    fun changeContent(content: String) {
        val text = content.trim()
        if (edited.value?.content == text) {
            return
        }
        edited.value = edited.value?.copy(content = text)
    }

    fun changePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun removeById(id: Long) {
        viewModelScope.launch {
            try {
                repository.removeById(id)
                _dataState.value = FeedModelState()
                loadPosts()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
                loadPosts()
            }
        }
    }

    fun likedByMe(id: Long) {
        viewModelScope.launch {
            try {
                repository.likedByMe(id)
                _dataState.value = FeedModelState()
                loadPosts()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
                loadPosts()
            }
        }
    }

    fun unlikedByMe(id: Long) {
        viewModelScope.launch {
            try {
                repository.unlikedByMe(id)
                _dataState.value = FeedModelState()
                loadPosts()
            } catch (e: Exception) {
                Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
                loadPosts()
            }
        }
    }

    fun likeById(id: Long) = viewModelScope.launch { repository.likeById(id) }
    fun shareById(id: Long) = viewModelScope.launch { repository.shareById(id) }
}
