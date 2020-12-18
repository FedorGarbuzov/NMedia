package ru.netology.nmedia.repository

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.post.Post
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class PostRepositoryImpl : PostRepository {
    private var nextId = 1L
    private var posts = listOf(
            Post(
                    id = nextId++,
                    author = "Нетология. Университет интернет-профессий будущего",
                    published = "19 сентября в 10:24",
                    content = "Языков программирования много, и выбрать какой-то один бывает нелегко. Собрали подборку статей, которая поможет вам начать, если вы остановили свой выбор на JavaScript.",
                    share = 9_999_999,
                    likes = 9_999,
                    views = 999,
                    likedByMe = false
            ),
            Post(
                    id = nextId++,
                    author = "Нетология. Университет интернет-профессий будущего",
                    published = "18 сентября в 10:12",
                    content = "Знаний хватит на всех: на следующей неделе разбираемся с разработкой мобильных приложений, учимся рассказывать истории и составлять PR-стратегию прямо на бесплатных занятиях \uD83D\uDC47",
                    share = 10_999_999,
                    likes = 10_999,
                    views = 9_999,
                    likedByMe = false
            ),
            Post(
                    id = nextId++,
                    author = "Нетология. Университет интернет-профессий будущего",
                    published = "21 мая в 18:36",
                    content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
                    share = 89_999_999,
                    likes = 100_999,
                    views = 99_999,
                    likedByMe = false
            )
    )

    private val data = MutableLiveData(posts)

    override fun getAll(): LiveData<List<Post>> = data

    @RequiresApi(Build.VERSION_CODES.O)
    override fun save(post: Post) {
        if (post.id == 0L) {
            posts = listOf(
                    post.copy(
                            id = nextId++,
                            author = "Me",
                            published = LocalDateTime.now().format(DateTimeFormatter.ofPattern("dd MMMM в HH:MM"))
                    )
            ) + posts
            data.value = posts
            return
        }

        posts = posts.map {
            if (it.id != post.id) it else it.copy(content = post.content)
        }
        data.value = posts
    }

    override fun likeById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(likedByMe = !it.likedByMe)
        }
        data.value = posts
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    override fun shareById(id: Long) {
        posts = posts.map {
            if (it.id != id) it else it.copy(share = it.share + 1)
        }
        data.value = posts
    }
}
