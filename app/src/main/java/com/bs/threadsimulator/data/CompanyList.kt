package com.bs.threadsimulator.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object CompanyList {
    private var _generatedCompanies: List<CompanyInfo> = listOf()
    val generatedCompanies: List<CompanyInfo>
        get() = _generatedCompanies

    suspend fun initCompanyList(listSize: Int) {
        withContext(Dispatchers.IO) {
            _generatedCompanies = MockDataSource().generateCompanies(listSize)
            //_generatedCompanies = CopyOnWriteArrayList(MockDataSource().generateCompanies(listSize))
        }
    }
}