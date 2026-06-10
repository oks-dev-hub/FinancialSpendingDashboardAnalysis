package com.example.financialspendingdashboardanalysis.model

data class MonthlyFinancialInformation(
    val transactionCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val transactionMonth: String = ""
)