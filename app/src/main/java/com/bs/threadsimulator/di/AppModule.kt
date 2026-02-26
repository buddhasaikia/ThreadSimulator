package com.bs.threadsimulator.di

import android.content.Context
import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.ChannelConfig
import com.bs.threadsimulator.common.DefaultAppDispatchers
import com.bs.threadsimulator.common.MetricsExporter
import com.bs.threadsimulator.common.ThreadMonitor
import com.bs.threadsimulator.data.DataRepository
import com.bs.threadsimulator.data.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideThreadMonitor(): ThreadMonitor = ThreadMonitor()

    @Provides
    @Singleton
    fun provideAppDispatchers(): AppDispatchers = DefaultAppDispatchers()

    @Provides
    @Singleton
    fun provideChannelConfig(): ChannelConfig = ChannelConfig()

    @Provides
    @Singleton
    fun provideMetricsExporter(
        @ApplicationContext context: Context,
    ): MetricsExporter = MetricsExporter(context)
}

/**
 * Binding module for repository interfaces.
 *
 * Uses Hilt's @Binds annotation for cleaner dependency injection of interface implementations.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindStockRepository(dataRepository: DataRepository): StockRepository
}
