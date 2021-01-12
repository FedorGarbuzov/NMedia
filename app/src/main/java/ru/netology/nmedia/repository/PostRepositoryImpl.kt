package ru.netology.nmedia.repository

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.R
import ru.netology.nmedia.post.Post
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PostRepositoryImpl(
        private val context: Context
) : PostRepository {
    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val fileName = "posts.json"
    private var nextId = 1L
    private var posts = emptyList<Post>()
    private val data = MutableLiveData(posts)

    init {
        val file = context.filesDir.resolve(fileName)
        if (file.exists()) {
            context.openFileInput(fileName).bufferedReader().use {
                posts = gson.fromJson(it, type)
                data.value = posts
            }
        } else {
            sync()
        }
    }


    override fun getAll(): LiveData<List<Post>> = data

    @RequiresApi(Build.VERSION_CODES.O)
    override fun save(post: Post) {
        if (post.id == 0L) {
            posts = listOf(
                    post.copy(
                            id = nextId++,
                            author = "Me",
                            published = LocalDateTime.now()
                                    .format(DateTimeFormatter.ofPattern("dd MMMM в HH:MM"))
                    )
            ) + posts
            data.value = posts
            sync()
            return
        }

        posts = posts.map {
            if (it.id != post.id) it else it.copy(content = post.content)
        }
        data.value = posts
        sync()
    }

    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(
                    likedByMe = !it.likedByMe,
                    likes = if (!it.likedByMe) it.likes + 1 else it.likes - 1
            )
        }
        data.value = posts
        sync()
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
        sync()
    }

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(share = it.share + 1)
        }
        data.value = posts
        sync()
    }

    private fun sync() {
        context.openFileOutput(fileName, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
    }
}

