package com.indisparte.pothole.di.module

import com.indisparte.pothole.data.network.PotholeRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideRepository(): PotholeRepository {
        return PotholeRepository.getInstance()
    }

}