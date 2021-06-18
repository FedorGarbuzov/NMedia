package ru.netology.nmedia.repository.user

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class UserRepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserRepository(imp: UserRepositoryImp): UserRepository
}