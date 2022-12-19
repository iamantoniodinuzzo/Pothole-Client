package com.indisparte.pothole.di.module;

import com.indisparte.pothole.data.network.network.PotholeRepository;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;
import dagger.hilt.InstallIn;
import dagger.hilt.components.SingletonComponent;

/**
 * @author Antonio Di Nuzzo (Indisparte)
 */
@Module
@InstallIn(SingletonComponent.class)
public class RepositoryModule {
    @Singleton
    @Provides
    public PotholeRepository provideRepository() {
        return PotholeRepository.getInstance();
    }
}
