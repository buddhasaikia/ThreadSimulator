package com.bs.threadsimulator.common

import kotlinx.coroutines.channels.BufferOverflow

/**
 * Configuration for the channel used to aggregate concurrent stock data updates.
 *
 * Extracted from hard-coded values in [HomeViewModel] to allow injection and testing
 * of different channel strategies.
 *
 * @property capacity Maximum number of buffered elements before the overflow strategy kicks in.
 * @property onBufferOverflow Strategy when the buffer is full (e.g. [BufferOverflow.DROP_OLDEST],
 *           [BufferOverflow.SUSPEND]).
 */
data class ChannelConfig(
    val capacity: Int = DEFAULT_CAPACITY,
    val onBufferOverflow: BufferOverflow = DEFAULT_OVERFLOW,
) {
    companion object {
        /** Default buffer size — matches the original hard-coded value. */
        const val DEFAULT_CAPACITY = 15_000

        /** Default overflow strategy — matches the original hard-coded value. */
        val DEFAULT_OVERFLOW: BufferOverflow = BufferOverflow.DROP_OLDEST
    }
}
