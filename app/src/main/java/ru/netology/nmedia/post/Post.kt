package ru.netology.nmedia.post

import android.net.Uri

data class Post (
    val id: Long,
    val author: String,
    val published: String,
    val content: String,
    val share: Int,
    val likes: Int,
    val views: Int,
    val url: String?,
    val likedByMe: Boolean = false
        )