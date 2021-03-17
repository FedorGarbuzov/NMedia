package ru.netology.nmedia.repository

import androidx.navigation.NavHost
import androidx.navigation.fragment.NavHostFragment
import retrofit2.Call
import ru.netology.nmedia.NewPostFragment
import ru.netology.nmedia.api.PostsApi
import ru.netology.nmedia.post.Post

class PostRepositoryImp : PostRepository {
    override fun getAllAsync(callback: PostRepository.Callback<List<Post>>) {
        PostsApi.retrofitService.getAll().enqueue(object : retrofit2.Callback<List<Post>> {
            override fun onResponse(
                    call: Call<List<Post>>,
                    response: retrofit2.Response<List<Post>>,
            ) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                    return
                }
                callback.onSuccess(response.body() ?: throw RuntimeException("body is null"))
            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                callback.onError(t as Exception)
            }

        })
    }

    override fun saveAsync(post: Post, callback: PostRepository.Callback<Post>) {
        PostsApi.retrofitService.save(post).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(
                    call: Call<Post>,
                    response: retrofit2.Response<Post>,
            ) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                }
                callback.onSuccess(post)
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(t as Exception)
            }
        })
    }

    override fun likedByMeAsync(id: Long, callback: PostRepository.Callback<Post>) {
        PostsApi.retrofitService.likedByMe(id).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(
                    call: Call<Post>,
                    response: retrofit2.Response<Post>,
            ) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                }
                response.body()?.let { callback.onSuccess(it) }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(t as Exception)
            }
        })
    }

    override fun unlikedByMeAsync(id: Long, callback: PostRepository.Callback<Post>) {
        PostsApi.retrofitService.unlikedByMe(id).enqueue(object : retrofit2.Callback<Post> {
            override fun onResponse(
                    call: Call<Post>,
                    response: retrofit2.Response<Post>,
            ) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                }
                response.body()?.let { callback.onSuccess(it) }
            }

            override fun onFailure(call: Call<Post>, t: Throwable) {
                callback.onError(t as Exception)
            }
        })
    }

    override fun likeById(id: Long) {
        //TODO("Not yetimplemented")
    }

    override fun shareById(id: Long) {
        //TODO("Not yet implemented")
    }

    override fun removeByIdAsync(id: Long, callback: PostRepository.Callback<Unit>) {
        PostsApi.retrofitService.removeById(id).enqueue(object : retrofit2.Callback<Unit> {
            override fun onResponse(
                    call: Call<Unit>,
                    response: retrofit2.Response<Unit>,
            ) {
                if (!response.isSuccessful) {
                    callback.onError(RuntimeException(response.message()))
                }
                callback.onSuccess(Unit)
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.onError(t as Exception)
            }
        })
    }
}

