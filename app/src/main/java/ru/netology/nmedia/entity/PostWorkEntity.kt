package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostWorkEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val authorId: Long,
    val authorAvatar: String,
    val published: String,
    val content: String,
    val share: Int,
    val likes: Int,
    val views: Int,
    val likedByMe: Boolean,
    val uploadedToServer: Boolean,
    val read: Boolean = true,
    @Embedded
    var attachment: AttachmentEmbeddable?,
    var uri: String? = null,
) {
    fun toPost() = Post(
        id,
        author,
        authorId,
        authorAvatar,
        published,
        content,
        share,
        likes,
        views,
        likedByMe,
        uploadedToServer,
        read,
        attachment?.toDto()
    )
}

fun Post.toWorkEntity() = PostWorkEntity(
    id,
    author,
    authorId,
    authorAvatar,
    published,
    content,
    share,
    likes,
    views,
    likedByMe,
    uploadedToServer = true,
    read = true,
    AttachmentEmbeddable.fromDto(attachment)
)