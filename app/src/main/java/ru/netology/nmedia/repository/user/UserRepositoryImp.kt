package ru.netology.nmedia.repository.user

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.UsersApi
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.post.MediaUpload
import ru.netology.nmedia.post.Token
import ru.netology.nmedia.post.User
import java.io.IOException

class UserRepositoryImp : UserRepository {
    override suspend fun updateUser(login: String, pass: String) {
        try {
            val response = UsersApi.retrofitService.updateUser(login, pass)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            AppAuth.getInstance().setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun createUser(login: String, pass: String, name: String) {
        try {
            val response = UsersApi.retrofitService.createUser(login, pass, name)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            AppAuth.getInstance().setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }

    override suspend fun createWithPhoto(login: RequestBody, pass: RequestBody, name: RequestBody, upload: MediaUpload) {
        try {
            val media = MultipartBody.Part.createFormData(
                    "file", upload.file.name, upload.file.asRequestBody()
            )
            val response = UsersApi.retrofitService.createWithPhoto(login, pass, name, media)
            if (!response.isSuccessful) {
                throw ApiError(response.code(), response.message())
            }
            val body = response.body() ?: throw ApiError(response.code(), response.message())
            AppAuth.getInstance().setAuth(body.id, body.token)
        } catch (e: IOException) {
            throw NetworkError
        } catch (e: Exception) {
            throw UnknownError
        }
    }
}