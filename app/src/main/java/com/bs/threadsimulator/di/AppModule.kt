package com.bs.threadsimulator.di

import android.content.Context
import com.bs.threadsimulator.common.MetricsExporter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import com.bs.threadsimulator.model.MetricsExporter as MetricsExporterInterface

/**
 * Application-specific DI module providing utilities specific to the app.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideMetricsExporter(
        @ApplicationContext context: Context,
    ): MetricsExporterInterface = MetricsExporter(context)
}
