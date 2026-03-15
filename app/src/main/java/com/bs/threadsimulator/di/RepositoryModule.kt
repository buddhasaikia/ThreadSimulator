package com.bs.threadsimulator.di

import com.bs.threadsimulator.data.DataRepository
import com.bs.threadsimulator.data.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Binding module for repository interfaces specific to the application.
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
