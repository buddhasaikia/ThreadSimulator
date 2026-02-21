package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.CompanyList
import javax.inject.Inject

/**
 * Use case for initializing the company list with a specified size.
 *
 * Creates or refreshes the list of companies available for simulation.
 * Useful for testing different list sizes and their impact on threading behavior.
 */
class InitCompanyListUseCase @Inject constructor(){
    /**
     * Initializes the company list with the specified number of companies.
     *
     * @param listSize The number of companies to generate (e.g., 5, 10, 50, 100)
     */
    suspend fun execute(listSize: Int) {
        CompanyList.initCompanyList(listSize)
    }
}