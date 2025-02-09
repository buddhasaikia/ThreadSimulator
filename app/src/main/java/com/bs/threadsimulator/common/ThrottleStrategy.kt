package com.bs.threadsimulator.common

// 3. You can also create different throttling strategies
object ThrottleStrategy {
    const val RAPID = 16L    // ~60 FPS
    const val NORMAL = 32L   // ~30 FPS
    const val RELAXED = 64L  // ~15 FPS
    
    fun forUpdateType(updateType: String): Long {
        return when(updateType) {
            "current_price" -> RAPID    // Price updates need to be quick
            "high_low" -> NORMAL        // High/Low can be a bit slower
            "PE" -> RELAXED             // PE ratio can update less frequently
            else -> NORMAL
        }
    }
}