package ru.netology.nmedia.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import org.w3c.dom.Entity
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.post.Post

class PostRepositoryImp(
        private val dao: PostDao
) : PostRepository {

    override fun getAll() = Transformations.map(dao.getAll()) { list ->
        list.map { it.toPost() }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun save(post: Post) {
        dao.save(PostEntity.fromPost(post))
    }

    override fun likeById(id: Long) {
        dao.likeById(id)
    }

    override fun removeById(id: Long) {
        dao.removeById(id)
    }

    override fun shareById(id: Long) {
        dao.shareById(id)
    }
}
