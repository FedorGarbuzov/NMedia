package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.post.Attachment
import ru.netology.nmedia.post.Post

@Entity
data class PostEntity(
        @PrimaryKey(autoGenerate = true)
        val id: Long,
        val author: String,
        val authorAvatar: String,
        val published: String,
        val content: String,
        val share: Int,
        val likes: Int,
        val views: Int,
        val url: String?,
        val likedByMe: Boolean,
        val uploadedToServer: Boolean
) {
    fun toPost() = Post(id, author, authorAvatar, published, content, share, likes, views, url, likedByMe, uploadedToServer)

    companion object {
        fun fromPost(post: Post) =
                PostEntity(post.id, post.author, post.authorAvatar, post.published, post.content, post.share, post.likes, post.views, post.url, post.likedByMe,  post.uploadedToServer)
    }
}

fun List<PostEntity>.toPost(): List<Post> = map(PostEntity::toPost)
fun List<Post>.fromPost(): List<PostEntity> = map(PostEntity::fromPost)
