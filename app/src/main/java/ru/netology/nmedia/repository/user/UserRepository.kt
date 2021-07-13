package ru.netology.nmedia.repository.user

import okhttp3.RequestBody
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Token

interface UserRepository {
    suspend fun updateUser(login: String, pass: String): Token
    suspend fun createUser(login: String, pass: String, name: String): Token
    suspend fun createWithPhoto(login: RequestBody, pass: RequestBody, name: RequestBody, upload: MediaUpload): Token
}