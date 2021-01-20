package ru.netology.nmedia.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.db.AppDb
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositorySQLiteImp

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
    private val repository: PostRepository = PostRepositorySQLiteImp(
            AppDb.getInstance(application).postDao
    )
    val data = repository.getAll()
    private val edited = MutableLiveData(emptyPost)

    fun save() {
        edited.value?.let {
            if (it.content?.isNotBlank() == true) {
                repository.save(it)
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

    fun likeById(id: Long) = repository.likeById(id)
    fun removeById(id: Long) = repository.removeById(id)
    fun shareById(id: Long) = repository.shareById(id)
}