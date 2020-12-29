package ru.netology.nmedia

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import ru.netology.nmedia.adapter.OnInterractionListener
import ru.netology.nmedia.adapter.PostAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.post.Post
import ru.netology.nmedia.viewModel.PostViewModel

class MainActivity : AppCompatActivity() {
    private val viewModel: PostViewModel by viewModels()
    private val newPostRequestCode = 1

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val adapter = PostAdapter(object : OnInterractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }

            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plane"
                }.let { Intent.createChooser(it, null) }

                startActivity(intent)
                viewModel.shareById(post.id)
            }

            override fun onEdit(post: Post) {
                val intent = Intent(this@MainActivity, NewPostActivity::class.java).apply {
                        action = Intent.ACTION_SEND
                        putExtra(Intent.EXTRA_TEXT, post.content)
                        type = "text/plane"
                    }
                startActivityForResult(intent, newPostRequestCode)
                viewModel.edit(post)
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
            }

            override fun onPlayMedia(post: Post) {
                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(post.url))
                if (intent.resolveActivity(packageManager) != null) {
                    startActivity(intent)
                } else {
                    Toast.makeText(this@MainActivity, R.string.app_not_found, Toast.LENGTH_SHORT).show()
                }
            }
        })

        binding.postsList.adapter = adapter
        viewModel.data.observe(this, { posts ->
            adapter.submitList(posts)
        })

        binding.add.setOnClickListener {
            val intent = Intent(this@MainActivity, NewPostActivity::class.java)
            startActivityForResult(intent, newPostRequestCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            newPostRequestCode -> {
                if (resultCode != Activity.RESULT_OK) {
                    return
                }
                data?.getStringExtra(Intent.EXTRA_TEXT)?.let {
                    viewModel.changeContent(it)
                    viewModel.save()
                }
            }
        }
    }
}
    






