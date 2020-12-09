package ru.netology.nmedia.post

data class Post (
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val share: Int,
    val likes: Int,
    val views: Int,
    val likedByMe: Boolean = false
        )