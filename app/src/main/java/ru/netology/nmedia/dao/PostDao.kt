package ru.netology.nmedia.dao

import ru.netology.nmedia.post.Post

interface PostDao {
    fun getAll(): List<Post>
    fun likeById(id: Long)
    fun save (post: Post): Post
    fun shareById(id: Long)
    fun removeById(id: Long)
}