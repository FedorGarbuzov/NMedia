package ru.netology.nmedia

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import ru.netology.nmedia.databinding.ActivityNewPostBinding
import ru.netology.nmedia.post.Post

class NewPostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val binding = ActivityNewPostBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.edit.requestFocus()
        val getIntent = intent
        getIntent.getStringExtra(Intent.EXTRA_TEXT)?.let {
            binding.edit.setText(it)
        }

        binding.ok.setOnClickListener {
            if (binding.edit.text.isNullOrBlank()) {
                setResult(RESULT_CANCELED)
            } else {
                val content = binding.edit.text.toString()
                val intent = Intent()
                intent.putExtra(Intent.EXTRA_TEXT, content)
                setResult(RESULT_OK, intent)
            }
            finish()
        }
    }
}