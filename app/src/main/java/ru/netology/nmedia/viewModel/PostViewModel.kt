package ru.netology.nmedia.viewModel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.R
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImp
import ru.netology.nmedia.util.SingleLiveEvent
import kotlin.concurrent.thread

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
        attachment = null
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryImp()
    private val _data = MutableLiveData(FeedModel())
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
        _data.value = FeedModel(loading = true)
        repository.getAllAsync(object : PostRepository.Callback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.value = FeedModel(posts = posts, empty = posts.isEmpty())
            }

            override fun onError(e: Exception) {
                _data.value = FeedModel(error = true)
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.Callback<Post> {
                override fun onSuccess(posts: Post) {
                    _postCreated.value = Unit
                    loadPosts()
                }

                override fun onError(e: Exception) {
                    Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
                }
            })
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

    fun removeById(id: Long) {
        repository.removeByIdAsync(id, object : PostRepository.Callback<Unit> {
            override fun onSuccess(posts: Unit) {
                super.onSuccess(posts)
                loadPosts()
            }

            override fun onError(e: Exception) {
                Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
                loadPosts()
            }
        })
    }

    fun likedByMe(id: Long) {
            repository.likedByMeAsync(id, object : PostRepository.Callback<Post> {
                override fun onSuccess(posts: Post) {
                    super.onSuccess(posts)
                    loadPosts()
                }

                override fun onError(e: Exception) {
                    Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
                    loadPosts()
                }
            })
    }


    fun unlikedByMe(id: Long) {
        repository.unlikedByMeAsync(id, object : PostRepository.Callback<Post> {
            override fun onSuccess(posts: Post) {
                super.onSuccess(posts)
                loadPosts()
            }

            override fun onError(e: Exception) {
                Toast.makeText(getApplication(), R.string.error_loading, Toast.LENGTH_LONG).show()
                loadPosts()
            }
        })
    }

    fun likeById(id: Long) = thread { repository.likeById(id) }
    fun shareById(id: Long) = thread { repository.shareById(id) }
}
