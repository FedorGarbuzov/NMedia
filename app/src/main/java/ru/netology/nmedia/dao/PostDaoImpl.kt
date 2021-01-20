package ru.netology.nmedia.dao

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Build
import androidx.annotation.RequiresApi
import ru.netology.nmedia.post.Post
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PostDaoImpl(private val db: SQLiteDatabase) : PostDao {
    companion object {
        val DDL = """
                    CREATE TABLE ${PostColumns.TABLE} (
                    ${PostColumns.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                    ${PostColumns.COLUMN_AUTHOR} TEXT NOT NULL,
                    ${PostColumns.COLUMN_PUBLISHED} TEXT NOT NULL,
                    ${PostColumns.COLUMN_CONTENT} TEXT NOT NULL,
                    ${PostColumns.COLUMN_SHARE} INTEGER NOT NULL DEFAULT 0,
                    ${PostColumns.COLUMN_LIKES} INTEGER NOT NULL DEFAULT 0,
                    ${PostColumns.COLUMN_VIEWS} INTEGER NOT NULL DEFAULT 0,
                    ${PostColumns.COLUMN_URL} TEXT DEFAULT NULL,
                    ${PostColumns.COLUMN_LIKED_BY_ME} BOOLEAN NOT NULL DEFAULT 0
                    );
                """.trimIndent()
    }

    object PostColumns {
        const val TABLE = "posts"
        const val COLUMN_ID = "id"
        const val COLUMN_AUTHOR = "author"
        const val COLUMN_PUBLISHED = "published"
        const val COLUMN_CONTENT = "content"
        const val COLUMN_SHARE = "share"
        const val COLUMN_LIKES = "likes"
        const val COLUMN_VIEWS = "views"
        const val COLUMN_URL = "url"
        const val COLUMN_LIKED_BY_ME = "likedByMe"
        val ALL_COLUMNS = arrayOf(
                COLUMN_ID,
                COLUMN_AUTHOR,
                COLUMN_PUBLISHED,
                COLUMN_CONTENT,
                COLUMN_SHARE,
                COLUMN_LIKES,
                COLUMN_VIEWS,
                COLUMN_URL,
                COLUMN_LIKED_BY_ME
        )
    }

    override fun getAll(): List<Post> {
        val posts = mutableListOf<Post>()
        db.query(
                PostColumns.TABLE,
                PostColumns.ALL_COLUMNS,
                null,
                null,
                null,
                null,
                "${PostColumns.COLUMN_ID} DESC"
        ).use {
            while (it.moveToNext()) {
                posts.add(map(it))
            }
        }
        return posts
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun save(post: Post): Post {
        val values = ContentValues().apply {
            if (post.id != 0L) {
                put(PostColumns.COLUMN_ID, post.id)
            }
            put(PostColumns.COLUMN_AUTHOR, "Me")
            put(PostColumns.COLUMN_CONTENT, post.content)
            put(PostColumns.COLUMN_PUBLISHED, LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("dd MMMM в HH:MM")))
        }
        val id = db.replace(PostColumns.TABLE, null, values)
        db.query(
                PostColumns.TABLE,
                PostColumns.ALL_COLUMNS,
                "${PostColumns.COLUMN_ID} = ?",
                arrayOf(id.toString()),
                null,
                null,
                null
        ).use {
            it.moveToNext()
            return map(it)
        }
    }

    override fun likeById(id: Long) {
        db.execSQL(
                """
                    UPDATE posts SET
                    likes = likes + CASE WHEN likedByMe THEN 1 ELSE -1 END,
                    likedByMe = CASE WHEN likedByMe THEN 1 ELSE 0 END
                    WHERE id = ?;
                """.trimIndent(), arrayOf(id)
        )
    }

    override fun shareById(id: Long) {
        db.execSQL(
                """
                    UPDATE posts SET
                    share = share + 1
                    WHERE id = ?
                """.trimIndent()
        )
    }

    override fun removeById(id: Long) {
        db.delete(
                PostColumns.TABLE,
                "${PostColumns.COLUMN_ID} = ?",
                arrayOf(id.toString())
        )
    }

    private fun map(cursor: Cursor): Post {
        with(cursor) {
            return Post(
                    id = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_ID)),
                    author = getString(getColumnIndexOrThrow(PostColumns.COLUMN_AUTHOR)),
                    published = getString(getColumnIndexOrThrow(PostColumns.COLUMN_PUBLISHED)),
                    content = getString(getColumnIndexOrThrow(PostColumns.COLUMN_CONTENT)),
                    share = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_SHARE)),
                    likes = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKES)),
                    views = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_VIEWS)),
                    url = getString(getColumnIndexOrThrow(PostColumns.COLUMN_URL)),
                    likedByMe = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKED_BY_ME)) != 0
            )
        }
    }
}