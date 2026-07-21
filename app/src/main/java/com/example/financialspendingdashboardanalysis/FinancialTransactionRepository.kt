package com.example.financialspendingdashboardanalysis

import com.example.financialspendingdashboardanalysis.model.MonthlyCategoryTotal
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import com.example.financialspendingdashboardanalysis.room.FinancialTransactionsDao

class FinancialTransactionRepository(
    val financialTransactionsDao: FinancialTransactionsDao
) {
    suspend fun getFinancialTransactionsInformationByDate(enquiryMonth: Int): List<PieChartInfo> {
        return financialTransactionsDao.getPieChartInfo(enquiryMonth)
    }

    suspend fun getMonthlyTotalsForCategory(
        transactionCategory: String,
        transactionsYear: Int,
        startMonth: Int,
        endMonth: Int
    ): List<MonthlyCategoryTotal> {
        return financialTransactionsDao.getMonthlyTotalsForCategory(
            transactionCategory = transactionCategory,
            transactionsYear = transactionsYear,
            startMonth = startMonth,
            endMonth = endMonth
        )
    }
}