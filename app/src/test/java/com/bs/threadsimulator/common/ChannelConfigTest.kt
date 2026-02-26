package com.bs.threadsimulator.common

import kotlinx.coroutines.channels.BufferOverflow
import org.junit.Assert.assertEquals
import org.junit.Test

class ChannelConfigTest {
    @Test
    fun defaultCapacityMatchesOriginalHardCodedValue() {
        val config = ChannelConfig()
        assertEquals(15_000, config.capacity)
    }

    @Test
    fun defaultOverflowMatchesOriginalHardCodedValue() {
        val config = ChannelConfig()
        assertEquals(BufferOverflow.DROP_OLDEST, config.onBufferOverflow)
    }

    @Test
    fun customCapacityIsApplied() {
        val config = ChannelConfig(capacity = 500)
        assertEquals(500, config.capacity)
    }

    @Test
    fun customOverflowIsApplied() {
        val config = ChannelConfig(onBufferOverflow = BufferOverflow.SUSPEND)
        assertEquals(BufferOverflow.SUSPEND, config.onBufferOverflow)
    }

    @Test
    fun companionConstantsAreCorrect() {
        assertEquals(15_000, ChannelConfig.DEFAULT_CAPACITY)
        assertEquals(BufferOverflow.DROP_OLDEST, ChannelConfig.DEFAULT_OVERFLOW)
    }
}
