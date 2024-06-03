package com.bs.threadsimulator.model

sealed class Status {
    data object Loading : Status()
    data object Success : Status()
    data object Error : Status()
}