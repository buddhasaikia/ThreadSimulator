package com.bs.threadsimulator.domain

import com.bs.threadsimulator.data.CompanyList
import javax.inject.Inject

class InitCompanyListUseCase @Inject constructor(){
    suspend fun execute(listSize: Int) {
        CompanyList.initCompanyList(listSize)
    }
}