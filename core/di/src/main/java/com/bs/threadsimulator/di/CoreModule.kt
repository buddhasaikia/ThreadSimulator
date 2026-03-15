package com.bs.threadsimulator.di

import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ChannelConfig
import com.bs.threadsimulator.common.DefaultAppDispatchers
import com.bs.threadsimulator.common.ThreadMonitor
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Core DI module providing common utilities and infrastructure components.
 *
 * Provides:
 * - ThreadMonitor for metrics tracking
 * - AppDispatchers for coroutine dispatchers
 * - ChannelConfig for data stream configuration
 *
 * This module is feature-agnostic and can be used by any feature module.
 */
@Module
@InstallIn(SingletonComponent::class)
object CoreModule {
    @Provides
    @Singleton
    fun provideThreadMonitor(): ThreadMonitor = ThreadMonitor()

    @Provides
    @Singleton
    fun provideAppDispatchers(): AppDispatchers = DefaultAppDispatchers()

    @Provides
    @Singleton
    fun provideChannelConfig(): ChannelConfig = ChannelConfig()
}
