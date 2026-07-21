package com.example.financialspendingdashboardanalysis.model

import com.example.financialmodels.TransactionCategory

data class MonthlyFinancialInformation(
    val transactionCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val transactionMonth: Int = -1
)