package ru.netology.nmedia.dao

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.entity.PostEntity
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE read == 1 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(content: String, id: Long)

    @Query("SELECT MAX(id) FROM PostEntity")
    fun getId(): Long

    @Query("""
        UPDATE PostEntity SET
        likes = likes +1,
        likedByMe = 1
        WHERE id = :id
""")
    suspend fun likedByMe(id: Long)

    @Query("""
        UPDATE PostEntity SET
        likes = likes -1,
        likedByMe = 0
        WHERE id = :id
""")
    suspend fun unlikedByMe(id: Long)

    @Query("""
        UPDATE PostEntity SET
        likes = likes + 1
        WHERE id = :id
    """)
    suspend fun likeById(id: Long)

    @RequiresApi(Build.VERSION_CODES.O)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(post: PostEntity) {
        insert(
            post.copy(author = if(post.author == "") "Me" else post.author,
                published = LocalDateTime.now().format(
                    DateTimeFormatter.ofPattern("dd MMMM в HH:mm")
                )
            )
        )
    }

    @Query("""
        UPDATE PostEntity SET
        share = share + 1
        WHERE id = :id
    """)
    suspend fun shareById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)
}