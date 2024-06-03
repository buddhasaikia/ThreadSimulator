package com.bs.threadsimulator.ui.screens

import com.bs.threadsimulator.model.Company
import com.bs.threadsimulator.model.StateError
import com.bs.threadsimulator.model.Status

data class UIState(
    val companyList: List<Company> = listOf(),
    val status: Status? = null,
    val error: StateError? = null
)