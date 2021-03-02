package ru.netology.nmedia.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.post.Post
import java.io.IOException
import java.lang.Exception
import java.util.concurrent.TimeUnit

class PostRepositoryImp : PostRepository {
    private val client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAllAsync(callback: PostRepository.GetAllCallback) {
        val request: Request = Request.Builder()
                .url("${BASE_URL}/api/slow/posts")
                .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: throw RuntimeException("body is null")
                try {
                    callback.onSuccess(gson.fromJson(body, typeToken.type))
                } catch (e: Exception) {
                    callback.onError(e)
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                callback.onError(e)
            }
        })
    }

    override fun saveAsync(post: Post, callback: PostRepository.SaveCallback) {
        val request: Request = Request.Builder()
                .post(gson.toJson(post).toRequestBody(jsonType))
                .url("${BASE_URL}/api/slow/posts")
                .build()

        client.newCall(request)
                .enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body?.string() ?: throw RuntimeException("body is null")
                        try {
                            callback.onSuccess(gson.fromJson(body, typeToken.type))
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        callback.onError(e)
                    }
                })
    }

    override fun likeByMeAsync(id: Long, callback: PostRepository.LikeByMeCallback) {
        val request: Request = Request.Builder()
                .post(gson.toJson(id).toRequestBody(jsonType))
                .url("${BASE_URL}/api/slow/posts/$id/likes")
                .build()

        client.newCall(request)
                .enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body?.string() ?: throw RuntimeException("body is null")
                        try {
                            callback.onSuccess(gson.fromJson(body, typeToken.type))
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        callback.onError(e)
                    }
                })
    }

    override fun unlikeByMeAsync(id: Long, callback: PostRepository.UnlikeByMeCallback) {
        val request: Request = Request.Builder()
                .delete()
                .url("${BASE_URL}/api/slow/posts/$id/likes")
                .build()

        client.newCall(request)
                .enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body?.string() ?: throw RuntimeException("body is null")
                        try {
                            callback.onSuccess(gson.fromJson(body, typeToken.type))
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        callback.onError(e)
                    }
                })
    }

    override fun likeById(id: Long) {
        //TODO("Not yetimplemented")
    }

    override fun shareById(id: Long) {
        //TODO("Not yet implemented")
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.RemoveByIdCallback) {
        val request: Request = Request.Builder()
                .delete()
                .url("${BASE_URL}/api/slow/posts/$id")
                .build()

        client.newCall(request)
                .enqueue(object : Callback {
                    override fun onResponse(call: Call, response: Response) {
                        val body = response.body?.string() ?: throw RuntimeException("body is null")
                        try {
                            callback.onSuccess(gson.fromJson(body, typeToken.type))
                        } catch (e: Exception) {
                            callback.onError(e)
                        }
                    }

                    override fun onFailure(call: Call, e: IOException) {
                        callback.onError(e)
                    }
                })
    }
}

