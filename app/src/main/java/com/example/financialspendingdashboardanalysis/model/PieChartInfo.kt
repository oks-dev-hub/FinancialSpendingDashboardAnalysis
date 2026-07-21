package com.example.financialspendingdashboardanalysis.model

import com.example.financialmodels.TransactionCategory

data class PieChartInfo(
    val selectedTransactionCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val totalSummationOfAmountValuesInCategory: Long = 0
)