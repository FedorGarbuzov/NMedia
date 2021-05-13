package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.AttachmentType
import ru.netology.nmedia.dto.Attachment
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
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

    companion object {
        fun fromPost(post: Post) =
                PostEntity(
                        post.id,
                        post.author,
                        post.authorId,
                        post.authorAvatar,
                        post.published,
                        post.content,
                        post.share,
                        post.likes,
                        post.views,
                        post.likedByMe,
                        post.uploadedToServer,
                        post.read,
                        AttachmentEmbeddable.fromDto(post.attachment)
                )
    }
}

data class AttachmentEmbeddable(
        var url: String,
        var type: AttachmentType,
) {
    fun toDto() = Attachment(url, type)

    companion object {
        fun fromDto(dto: Attachment?) = dto?.let {
            AttachmentEmbeddable(it.url, it.type)
        }
    }
}

fun List<PostEntity>.toPost(): List<Post> = map(PostEntity::toPost)
fun List<Post>.fromPost(): List<PostEntity> = map(PostEntity::fromPost)