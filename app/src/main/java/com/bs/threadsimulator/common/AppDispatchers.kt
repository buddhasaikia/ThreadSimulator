package com.bs.threadsimulator.common

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

class AppDispatchers @Inject constructor() {
    val ioDispatcher: CoroutineDispatcher = Dispatchers.IO

}