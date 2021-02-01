package ru.netology.nmedia.dao

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert
    fun insert(post: PostEntity)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    fun updateContentById(content: String, id: Long)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
""")
    fun likeByMe(id: Long)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + 1
        WHERE id = :id
    """)
    fun likeById(id: Long)

    @RequiresApi(Build.VERSION_CODES.O)
    fun save(post: PostEntity) {
        if (post.id == 0L) insert(
            post.copy(author = if(post.author == "") "Me" else post.author,
                published = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd MMMM в HH:mm")
                )
            )
        ) else updateContentById(post.content, post.id)
    }

    @Query("""
        UPDATE PostEntity SET
        share = share + 1
        WHERE id = :id
    """)
    fun shareById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    fun removeById(id: Long)
}