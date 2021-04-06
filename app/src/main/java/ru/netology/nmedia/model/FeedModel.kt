package ru.netology.nmedia.model

import ru.netology.nmedia.post.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)