package ru.netology.nmedia.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import ru.netology.nmedia.R
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImp
import ru.netology.nmedia.util.SingleLiveEvent

val emptyPost = Post(
    id = 0L,
    author = "",
    authorAvatar = "",
    published = "",
    content = "",
    share = 0,
    likes = 0,
    views = 0,
    url = null,
    likedByMe = false,
    uploadedToServer = false,
    attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository =
        PostRepositoryImp(AppDb.getInstance(context = application).postDao())
    val data: LiveData<FeedModel> = repository.data.map(::FeedModel)
    private val _dataState = MutableLiveData<FeedModelState>()
    val dataState: LiveData<FeedModelState>
        get() = _dataState
    private val edited = MutableLiveData(emptyPost)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

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
                    repository.save(it)
                    _dataState.value = FeedModelState()
                    loadPosts()
                } catch (e: Exception) {
                    _dataState.value = FeedModelState(errorSaving = true)
                }
            }
            _postCreated.value = Unit
        }
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
