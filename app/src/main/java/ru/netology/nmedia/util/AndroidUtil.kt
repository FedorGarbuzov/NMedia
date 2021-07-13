package ru.netology.nmedia.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import com.bumptech.glide.Glide
import retrofit2.Response
import ru.netology.nmedia.R
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import java.io.IOException

object AndroidUtils {
    fun hideKeyboard(view: View) {
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    fun display(num: Int): String {
        var newString = "$num"
        if (num >= 1_000) {
            val first = Character.getNumericValue(num.toString()[0])
            val second = Character.getNumericValue(num.toString()[1])
            val third = Character.getNumericValue(num.toString()[2])
            when {
                (num in 1_000..9_999) -> newString = "${first}.${second}K"
                (num in 10_000..99_999) -> newString = "${first}${second}K"
                (num in 100_000..999_999) -> newString = "${first}${second}${third}K"
                (num in 1_000_000..9_999_999) -> newString = "${first}.${second}M"
                (num in 10_000_000..99_999_999) -> newString = "${first}${second}.${third}M"
                (num in 100_000_000..999_999_000) -> newString = "${first}${second}${third}M"
            }
        }
        return newString
    }

    fun loadAvatar(imageView: ImageView, url: String) {
        Glide.with(imageView)
            .load(url)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .circleCrop()
            .into(imageView)
    }

    fun loadImage(imageView: ImageView, url: String) {
        Glide.with(imageView)
            .load(url)
            .placeholder(R.drawable.ic_loading_100dp)
            .error(R.drawable.ic_error_100dp)
            .timeout(10_000)
            .into(imageView)
    }

    suspend fun <T, R> makeRequest(
        request: suspend () -> Response<T>,
        onSuccess: suspend (body: T) -> R
    ): R {
        try {
            val response = request()
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body =
                response.body() ?: throw ApiError(response.code(), response.message())
            return onSuccess(body)
        } catch (e: IOException) {
            throw NetworkError
//        } catch (e: Exception) {
//            throw UnknownError
//        }
        }
    }
}