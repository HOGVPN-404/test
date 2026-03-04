package com.example.coba.di

import com.example.coba.data.repository.FileRepositoryImpl
import com.example.coba.domain.repository.FileRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindFileRepository(
        impl: FileRepositoryImpl
    ): FileRepository
}
