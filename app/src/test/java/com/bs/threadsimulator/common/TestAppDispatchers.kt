package com.bs.threadsimulator.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.test.TestDispatcher

/**
 * Test implementation of [AppDispatchers] that routes all dispatchers
 * through a single [TestDispatcher] for deterministic coroutine testing.
 */
class TestAppDispatchers(
    testDispatcher: TestDispatcher,
) : AppDispatchers {
    override val ioDispatcher: CoroutineDispatcher = testDispatcher
    override val defaultDispatcher: CoroutineDispatcher = testDispatcher
    override val mainDispatcher: CoroutineDispatcher = testDispatcher
}
