package com.bs.threadsimulator.di

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ThreadMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideThreadMonitor(): ThreadMonitor {
        return ThreadMonitor()
    }

    @Provides
    @Singleton
    fun provideAppDispatchers(): AppDispatchers {
        return AppDispatchers()
    }
}
