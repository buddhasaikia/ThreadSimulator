package com.bs.threadsimulator.model

data class StateError(
    val message: String = "",
    val throwable: Throwable? = null,
)
