package com.bs.threadsimulator.data

object CompanyList {
    private var _generatedCompanies: List<CompanyInfo> = listOf()
    val generatedCompanies: List<CompanyInfo>
        get() = _generatedCompanies

    suspend fun initCompanyList(listSize: Int) {
        _generatedCompanies = MockDataSource().generateCompanies(listSize)
        //_generatedCompanies = CopyOnWriteArrayList(MockDataSource().generateCompanies(listSize))
    }
}