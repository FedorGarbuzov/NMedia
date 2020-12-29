package ru.netology.nmedia.viewModel

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl

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

class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryImpl()
    val data = repository.getAll()
    private val edited = MutableLiveData(emptyPost)

    fun save() {
        edited.value?.let {
            repository.save(it)
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

    fun likeById(id: Long) = repository.likeById(id)
    fun removeById(id: Long) = repository.removeById(id)
    fun shareById(id: Long) = repository.shareById(id)
}