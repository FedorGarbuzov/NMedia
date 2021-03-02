package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImp
import ru.netology.nmedia.util.SingleLiveEvent
import java.io.IOException
import kotlin.concurrent.thread

val emptyPost = Post(
        id = 0L,
        author = "",
        published = "",
        content = "",
        share = 0,
        likes = 0,
        views = 0,
        url = null,
        likedByMe = false
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImp()
    private val _data = MutableLiveData<FeedModel>()
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(emptyPost)
    private val _postCreated = SingleLiveEvent<Unit>()
    val postCreated: LiveData<Unit>
        get() = _postCreated

    init {
        loadPosts()
    }

    fun loadPosts() {
        thread {
            _data.postValue(FeedModel(loading = true))
            try {
                val posts = repository.getAll()
                FeedModel(posts = posts, empty = posts.isEmpty())
            } catch (e: IOException) {
                FeedModel(error = true)
            }.also(_data::postValue)
        }
    }

    fun save() {
        edited.value?.let {
            thread {
                repository.save(it)
                _postCreated.postValue(Unit)
            }
        }
        edited.value = emptyPost
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

    fun removeById(id: Long) = thread {
        val old = _data.value?.posts.orEmpty()
        _data.postValue(
                _data.value?.copy(posts = _data.value?.posts.orEmpty()
                        .filter { it.id != id }
                )
        )
        try {
            repository.removeById(id)
        } catch (e: IOException) {
            _data.postValue(_data.value?.copy(posts = old))
        }
    }

    fun likeByMe(id: Long) = thread {
        repository.likeByMe(id)
        loadPosts()
    }

    fun unlikeByMe(id: Long) = thread {
        repository.unlikeByMe(id)
        loadPosts()
    }

    fun likeById(id: Long) = thread { repository.likeById(id) }
    fun shareById(id: Long) = thread { repository.shareById(id) }
}
