package com.example.financialspendingdashboardanalysis.model

data class FiveDayAverageValues(
    val intervalIndex: Int,
    val averageAmount: Double,
    val transactionCount: Int
)