package com.bs.threadsimulator.utils

object InputValidator {
    private const val MIN_INTERVAL_MS = 1L
    private const val MAX_INTERVAL_MS = 3600000L // 1 hour
    private const val MIN_LIST_SIZE = 1
    private const val MAX_LIST_SIZE = 1000

    /**
     * Validates an update interval input.
     *
     * @param value The input string to validate
     * @param minMs Minimum allowed value in milliseconds (default: 1)
     * @param maxMs Maximum allowed value in milliseconds (default: 1 hour)
     * @return Result with the Long value or an exception with error message
     */
    fun validateInterval(
        value: String,
        minMs: Long = MIN_INTERVAL_MS,
        maxMs: Long = MAX_INTERVAL_MS
    ): Result<Long> {
        return try {
            when {
                value.isBlank() -> Result.failure(
                    IllegalArgumentException("Interval cannot be empty")
                )

                else -> {
                    val interval = value.trim().toLong()
                    when {
                        interval < minMs -> Result.failure(
                            IllegalArgumentException("Interval must be at least $minMs ms")
                        )

                        interval > maxMs -> Result.failure(
                            IllegalArgumentException("Interval cannot exceed $maxMs ms (1 hour)")
                        )

                        else -> Result.success(interval)
                    }
                }
            }
        } catch (e: NumberFormatException) {
            Result.failure(
                IllegalArgumentException("Please enter a valid number")
            )
        }
    }

    /**
     * Validates a list size input.
     *
     * @param value The input string to validate
     * @param minSize Minimum allowed size (default: 1)
     * @param maxSize Maximum allowed size (default: 1000)
     * @return Result with the Int value or an exception with error message
     */
    fun validateListSize(
        value: String,
        minSize: Int = MIN_LIST_SIZE,
        maxSize: Int = MAX_LIST_SIZE
    ): Result<Int> {
        return try {
            when {
                value.isBlank() -> Result.failure(
                    IllegalArgumentException("List size cannot be empty")
                )

                else -> {
                    val size = value.trim().toInt()
                    when {
                        size < minSize -> Result.failure(
                            IllegalArgumentException("List size must be at least $minSize")
                        )

                        size > maxSize -> Result.failure(
                            IllegalArgumentException("List size cannot exceed $maxSize")
                        )

                        else -> Result.success(size)
                    }
                }
            }
        } catch (e: NumberFormatException) {
            Result.failure(
                IllegalArgumentException("Please enter a valid number")
            )
        }
    }
}
