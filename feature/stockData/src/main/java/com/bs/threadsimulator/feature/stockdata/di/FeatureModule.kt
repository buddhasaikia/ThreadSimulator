package com.bs.threadsimulator.feature.stockdata.di

import com.bs.threadsimulator.feature.stockdata.data.DataRepository
import com.bs.threadsimulator.feature.stockdata.data.repository.StockRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt DI module for the stock data feature.
 *
 * Provides dependencies for the feature module's use cases, repositories, and services.
 * Dependencies are auto-provided through constructor injection.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class FeatureModule {
    @Binds
    @Singleton
    abstract fun bindStockRepository(impl: DataRepository): StockRepository
}
