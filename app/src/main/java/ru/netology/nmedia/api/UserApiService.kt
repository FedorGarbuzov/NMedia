package ru.netology.nmedia.api

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth
import ru.netology.nmedia.dto.PushToken
import ru.netology.nmedia.dto.Token

private val retrofit = RetrofitBuilder().retfofit

interface UsersApiService {
    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(@Field("login") login: String, @Field("pass") pass: String): Response<Token>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun createUser(@Field("login") login: String, @Field("pass") pass: String, @Field("name") name: String): Response<Token>

    @POST("users/push-tokens")
    suspend fun savePushToken(@Body pushToken: PushToken): Response<Unit>

    @Multipart
    @POST("users/registration")
    suspend fun createWithPhoto(
            @Part("login") login: RequestBody,
            @Part("pass") pass: RequestBody,
            @Part("name") name: RequestBody,
            @Part media: MultipartBody.Part,
    ): Response<Token>
}

object UsersApi {
    val retrofitService: UsersApiService by lazy {
        retrofit.create(UsersApiService::class.java)
    }
}