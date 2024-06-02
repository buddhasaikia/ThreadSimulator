package com.bs.threadsimulator.model

sealed class Resource<out T>(
    val data: T? = null,
    val message: String = "",
    val throwable: Throwable? = null
) {
    class Success<out T>(data: T?) : Resource<T>(data)
    class Error<out T>(t: Throwable? = null, message: String) :
        Resource<T>(message = message, throwable = t)

    class Loading<out T> : Resource<T>()
}