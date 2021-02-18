package ru.netology.nmedia.repository

import ru.netology.nmedia.post.Post

interface PostRepository {
    fun getAll(): List<Post>
    fun likeByMe(id: Long)
    fun unlikeByMe(id: Long)
    fun likeById(id: Long)
    fun save(post: Post)
    fun shareById(id: Long)
    fun removeById(id: Long)
}