package com.bs.threadsimulator.data

import com.bs.threadsimulator.common.AppDispatchers

object CompanyList {
    private var _generatedCompanies: List<CompanyInfo> = listOf()
    val generatedCompanies: List<CompanyInfo>
        get() = _generatedCompanies

    suspend fun initCompanyList(listSize: Int, appDispatchers: AppDispatchers) {
        _generatedCompanies = MockDataSource(appDispatchers).generateCompanies(listSize)
        //_generatedCompanies = CopyOnWriteArrayList(MockDataSource(appDispatchers).generateCompanies(listSize))
    }
}