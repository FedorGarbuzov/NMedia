package ru.netology.nmedia

import android.annotation.SuppressLint
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.viewModel.PostViewModel

class MainActivity : AppCompatActivity() {
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val viewModel: PostViewModel by viewModels()
        viewModel.data.observe(this, {
            with(binding) {
                userName.text = it.author
                published.text = it.published
                content.text = it.content
                shareTxt.text = display(it.share)
                likes.text = display(it.likes)
                views.text = display(it.views)

                favorite.setImageResource(
                        if (it.likedByMe) R.drawable.ic_baseline_favorite_border_24 else R.drawable.ic_baseline_favorite_24)
                likes.setText(
                        if (it.likedByMe) display(it.likes) else display(it.likes + 1))

                shareTxt.setText(display(it.share))
            }
        })

        binding.favorite.setOnClickListener {
            viewModel.like()
        }

        binding.shareImg.setOnClickListener {
                viewModel.share()
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





