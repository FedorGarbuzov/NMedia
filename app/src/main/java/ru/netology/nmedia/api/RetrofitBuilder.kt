package ru.netology.nmedia.api

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.auth.AppAuth

private const val BASE_URL = "${BuildConfig.BASE_URL}/api/slow/"

class RetrofitBuilder {
    private val loggin = HttpLoggingInterceptor().apply {
        if (BuildConfig.DEBUG) {
            level = HttpLoggingInterceptor.Level.BODY
        }
    }

    private val okhttp = OkHttpClient.Builder()
            .addInterceptor(loggin)
            .addInterceptor { chain ->
                AppAuth.getInstance().authStateFlow.value.token?.let { token ->
                    val newRequest = chain.request().newBuilder()
                            .addHeader("Authorization", token)
                            .build()
                    return@addInterceptor chain.proceed(newRequest)
                }
                chain.proceed(chain.request())
            }
            .build()

    private val _retrofit = Retrofit.Builder()
            .addConverterFactory(GsonConverterFactory.create())
            .baseUrl(BASE_URL)
            .client(okhttp)
            .build()

    val retfofit
        get() = _retrofit
}