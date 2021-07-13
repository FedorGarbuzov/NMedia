package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import ru.netology.nmedia.entity.PostEntity


@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE read == 1 ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Query("SELECT * FROM PostEntity WHERE read == 1 ORDER BY id DESC")
    fun pagingSource(): PagingSource<Int, PostEntity>

    @Query("SELECT * FROM PostEntity WHERE read == 0 ORDER BY id DESC")
    suspend fun getNewer(): List<PostEntity>

    @Query("SELECT COUNT(*) == 0 FROM PostEntity")
    suspend fun isEmpty(): Boolean

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(posts: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content WHERE id = :id")
    suspend fun updateContentById(content: String, id: Long)

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

    @Query("""
        UPDATE PostEntity SET
        share = share + 1
        WHERE id = :id
    """)
    suspend fun shareById(id: Long)

    @Query("DELETE FROM PostEntity WHERE id = :id")
    suspend fun removeById(id: Long)

    @Query("DELETE FROM PostEntity")
    suspend fun removeAll()
}