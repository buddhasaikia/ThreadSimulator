package com.bs.threadsimulator.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

/**
 * Provides configured coroutine dispatchers for the application.
 *
 * Centralizes dispatcher configuration, making it easy to swap implementations for testing
 * or to apply custom dispatcher policies application-wide.
 */
interface AppDispatchers {
    /** Dispatcher for I/O operations (network, database, file I/O). */
    val ioDispatcher: CoroutineDispatcher

    /** Dispatcher for CPU-intensive computations (sorting, parsing, calculations). */
    val defaultDispatcher: CoroutineDispatcher

    /** Dispatcher for UI operations that must run on the main thread. */
    val mainDispatcher: CoroutineDispatcher
}

/**
 * Production implementation of [AppDispatchers] backed by the standard Android dispatchers.
 */
class DefaultAppDispatchers : AppDispatchers {
    override val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
    override val defaultDispatcher: CoroutineDispatcher = Dispatchers.Default
    override val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
}
