package com.example.financialspendingdashboardanalysis.model

data class PieChartInfo(
    val selectedTransactionCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val totalNumberOfAmountValuesInCategory: Int = 0,
    val totalSummationOfAmountValuesInCategory: Int = 0,
    val paymentDate: String = ""
)