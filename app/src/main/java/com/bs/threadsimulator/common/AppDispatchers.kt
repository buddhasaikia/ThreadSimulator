package com.bs.threadsimulator.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

/**
 * Provides configured coroutine dispatchers for the application.
 *
 * Centralizes dispatcher configuration, making it easy to swap implementations for testing
 * or to apply custom dispatcher policies application-wide.
 */
class AppDispatchers @Inject constructor() {
    /**
     * Dispatcher for I/O operations.
     *
     * Used for network requests, database operations, and other I/O-heavy tasks.
     * Runs on a pool of threads optimized for blocking I/O operations.
     */
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

}