package ru.netology.nmedia

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import ru.netology.nmedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val post = Post(
                id = 1,
                author = "Нетология. Университет интернет-профессий будущего",
                published = "21 мая в 18:36",
                content = "Привет, это новая Нетология! Когда-то Нетология начиналась с интенсивов по онлайн-маркетингу. Затем появились курсы по дизайну, разработке, аналитике и управлению. Мы растём сами и помогаем расти студентам: от новичков до уверенных профессионалов. Но самое важное остаётся с нами: мы верим, что в каждом уже есть сила, которая заставляет хотеть больше, целиться выше, бежать быстрее. Наша миссия — помочь встать на путь роста и начать цепочку перемен → http://netolo.gy/fyb",
                share = 100_999_999,
                likes = 100_999,
                views = 999,
                likedByMe = false
        )

        with(binding) {
            userName.text = post.author
            published.text = post.published
            content.text = post.content
            shareTxt.text = display(post.share)
            likes.text = display(post.likes)
            views.text = display(post.views)
            if (post.likedByMe) {
                favorite?.setImageResource(R.drawable.ic_baseline_favorite_24)
            }

            favorite?.setOnClickListener {
                post.likedByMe = !post.likedByMe
                favorite.setImageResource(
                        if (post.likedByMe) R.drawable.ic_baseline_favorite_border_24 else R.drawable.ic_baseline_favorite_24
                )
                var likesQuantity = post.likes
                likes.setText(
                        if (post.likedByMe) display(likesQuantity) else display(likesQuantity + 1))
            }

            shareImg?.setOnClickListener {
                var shareQuantity = post.share + 1
                shareTxt.setText(display(shareQuantity))
                post.share++
            }
        }
    }

    private fun display(num: Int): String {
        val first = Character.getNumericValue(num.toString()[0])
        val second = Character.getNumericValue(num.toString()[1])
        val third = Character.getNumericValue(num.toString()[2])
        var newString = ""
        when {
            (num < 1_000) -> newString = num.toString()
            (num in 1_000..9_999) -> newString = "${first}.${second}K"
            (num in 10_000..99_999) -> newString = "${first}${second}K"
            (num in 100_000..999_999) -> newString = "${first}${second}${third}K"
            (num in 1_000_000..9_999_999) -> newString = "${first}.${second}M"
            (num in 10_000_000..99_999_999) -> newString = "${first}${second}.${third}M"
            (num in 100_000_000..999_999_000) -> newString = "${first}${second}${third}M"
        }
            return newString
    }
}





