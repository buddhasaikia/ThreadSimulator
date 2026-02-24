package com.bs.threadsimulator.model

/**
 * Sealed class representing the result of an asynchronous operation.
 *
 * [Resource] wraps data along with loading and error states, providing a type-safe way to handle
 * different outcomes of operations that may span multiple threads.
 *
 * @param T The type of data wrapped by this Resource
 * @property data The actual data, if available (null for Loading and Error states)
 * @property message Error message or additional context (empty by default)
 * @property throwable The exception that occurred, if any
 */
sealed class Resource<out T>(
    val data: T? = null,
    val message: String = "",
    val throwable: Throwable? = null,
) {
    /**
     * Represents a successful operation result.
     *
     * @param data The successfully retrieved data
     */
    class Success<out T>(
        data: T?,
    ) : Resource<T>(data)

    /**
     * Represents a failed operation.
     *
     * @param t The exception that caused the failure (optional)
     * @param message A human-readable error message
     */
    class Error<out T>(
        t: Throwable? = null,
        message: String,
    ) : Resource<T>(message = message, throwable = t)

    /**
     * Represents an ongoing asynchronous operation.
     *
     * Emitted to indicate that data is being fetched from a background thread.
     */
    class Loading<out T> : Resource<T>()
}
