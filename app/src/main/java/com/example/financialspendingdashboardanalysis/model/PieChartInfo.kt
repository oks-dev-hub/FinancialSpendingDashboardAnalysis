package com.example.financialspendingdashboardanalysis.model

import androidx.room.ColumnInfo

data class PieChartInfo(
    @ColumnInfo(name = "selectedTransactionCategory")
    val selectedTransactionCategory: String = "Unknown",
    @ColumnInfo(name = "totalSummationOfAmountValuesInCategory")
    val totalSummationOfAmountValuesInCategory: Long = 0
)