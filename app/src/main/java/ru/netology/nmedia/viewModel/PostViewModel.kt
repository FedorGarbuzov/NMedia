package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.CardFragment
import ru.netology.nmedia.databinding.FragmentCardBinding
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
        _data.postValue(FeedModel(loading = true))
        repository.getAllAsync(object : PostRepository.GetAllCallback {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun onError(e: Exception) {
                FeedModel(error = true)
            }
        })
    }

    fun save() {
        edited.value?.let {
            repository.saveAsync(it, object : PostRepository.SaveCallback {
                override fun onSuccess(post: Post) {
                    _postCreated.postValue(Unit)
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
        repository.removeByIdAsync(id, object : PostRepository.RemoveByIdCallback {})
        loadPosts()
    }

    fun likeByMe(id: Long) {
        repository.likeByMeAsync(id, object : PostRepository.LikeByMeCallback {})
        loadPosts()
    }


    fun unlikeByMe(id: Long) {
        repository.unlikeByMeAsync(id, object : PostRepository.UnlikeByMeCallback {})
        loadPosts()
    }

    fun likeById(id: Long) = thread { repository.likeById(id) }
    fun shareById(id: Long) = thread { repository.shareById(id) }
}
