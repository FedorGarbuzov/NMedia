package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import ru.netology.nmedia.post.Post

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun likeByMe(id: Long)
    fun likeById(id: Long)
    fun save(post: Post)
    fun shareById(id: Long)
    fun removeById(id: Long)
}