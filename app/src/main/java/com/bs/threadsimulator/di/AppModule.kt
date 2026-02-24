package com.bs.threadsimulator.di

import android.content.Context
import com.bs.threadsimulator.common.AppDispatchers
import com.bs.threadsimulator.common.MetricsExporter
import com.bs.threadsimulator.common.ThreadMonitor
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
    fun provideThreadMonitor(): ThreadMonitor {
        return ThreadMonitor()
    }

    @Provides
    @Singleton
    fun provideAppDispatchers(): AppDispatchers {
        return AppDispatchers()
    }

    @Provides
    @Singleton
    fun provideMetricsExporter(
        @ApplicationContext context: Context,
    ): MetricsExporter {
        return MetricsExporter(context)
    }
}
