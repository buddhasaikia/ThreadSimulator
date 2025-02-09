package com.bs.threadsimulator.common

import com.bs.threadsimulator.model.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

fun <T> Flow<Resource<T>>.throttleUpdates(windowMs: Long = 16L): Flow<Resource<T>> = flow {
    var lastEmissionTime = 0L
    collect { value ->
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEmissionTime >= windowMs || value is Resource.Error) {
            emit(value)
            lastEmissionTime = currentTime
        }
    }
}