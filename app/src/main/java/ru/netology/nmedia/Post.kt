package ru.netology.nmedia

data class Post (
    val id: Int,
    val author: String,
    val published: String,
    val content: String,
    var share: Int,
    var likes: Int,
    var views: Int,
    var likedByMe: Boolean = false
        )