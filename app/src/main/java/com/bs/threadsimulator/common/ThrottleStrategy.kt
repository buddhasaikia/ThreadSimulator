package com.bs.threadsimulator.common

/**
 * Provides predefined throttling strategies for controlling update frequency.
 *
 * These constants represent different update intervals for balancing performance and responsiveness.
 * Use with [throttleUpdates] to limit how frequently UI updates occur from background data streams.
 *
 * Strategies are designed around frame rates:
 * - RAPID: ~60 FPS (good for fast-moving data)
 * - NORMAL: ~30 FPS (balanced performance and updates)
 * - RELAXED: ~15 FPS (minimal updates, low CPU usage)
 */
object ThrottleStrategy {
    /**
     * Rapid update throttling (~60 FPS).
     *
     * Use for data that changes frequently and requires responsive updates.
     * Best for current price tracking and similar high-frequency metrics.
     */
    const val RAPID = 16L

    /**
     * Normal update throttling (~30 FPS).
     *
     * Balanced default for most scenarios. Suitable for moderate update frequencies
     * like high/low price tracking.
     */
    const val NORMAL = 32L

    /**
     * Relaxed update throttling (~15 FPS).
     *
     * Use for low-frequency updates or when minimizing CPU usage is important.
     * Appropriate for metrics like PE ratio that change infrequently.
     */
    const val RELAXED = 64L
    
    /**
     * Determines the throttling strategy for a specific update type.
     *
     * @param updateType The type of update ("current_price", "high_low", "PE", etc.)
     * @return The throttling interval in milliseconds
     */
    fun forUpdateType(updateType: String): Long {
        return when(updateType) {
            "current_price" -> RAPID
            "high_low" -> NORMAL
            "PE" -> RELAXED
            else -> NORMAL
        }
    }
}