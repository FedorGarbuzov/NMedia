package ru.netology.nmedia.repository.user

import okhttp3.RequestBody
import ru.netology.nmedia.post.MediaUpload

interface UserRepository {
    suspend fun updateUser(login: String, pass: String)
    suspend fun createUser(login: String, pass: String, name: String)
    suspend fun createWithPhoto(login: RequestBody, pass: RequestBody, name: RequestBody, upload: MediaUpload)
}