package ru.netology.nmedia.api

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.auth.AppAuth
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ApiServiceModule {
    @Provides
    @Singleton
    fun providePostApiService(auth: AppAuth): PostApiService {
        return retrofit(okhttp(loggingInterceptor(), authInterceptor(auth)))
            .create(PostApiService::class.java)
    }

    @Provides
    @Singleton
    fun provideUserApiService(auth: AppAuth): UserApiService {
        return retrofit(okhttp(loggingInterceptor(), authInterceptor(auth)))
            .create(UserApiService::class.java)
    }
}