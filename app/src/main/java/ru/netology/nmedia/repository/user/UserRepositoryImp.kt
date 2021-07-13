package ru.netology.nmedia.repository.user

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.api.UserApiService
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.dto.Token
import ru.netology.nmedia.error.ApiError
import ru.netology.nmedia.error.NetworkError
import ru.netology.nmedia.error.UnknownError
import ru.netology.nmedia.util.AndroidUtils.makeRequest
import java.io.IOException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserRepositoryImp @Inject constructor(
    private val userApi: UserApiService,
    private val auth: AppAuth
) : UserRepository {

    override suspend fun updateUser(login: String, pass: String): Token = makeRequest(
        request = { userApi.updateUser(login, pass) },
        onSuccess = { body ->
            auth.setAuth(body.id, body.token)
            body
        }
    )

    override suspend fun createUser(login: String, pass: String, name: String): Token = makeRequest(
        request = { userApi.createUser(login, pass, name) },
        onSuccess = { body ->
            auth.setAuth(body.id, body.token)
            body
        }
    )

    override suspend fun createWithPhoto(
        login: RequestBody,
        pass: RequestBody,
        name: RequestBody,
        upload: MediaUpload
    ): Token {
        val media = MultipartBody.Part.createFormData(
            "file", upload.file.name, upload.file.asRequestBody()
        )
        return makeRequest(
            request = { userApi.createWithPhoto(login, pass, name, media) },
            onSuccess = { body ->
                auth.setAuth(body.id, body.token)
                body
            }
        )
    }
}