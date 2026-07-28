package com.example.financialspendingdashboardanalysis.model

import androidx.room.ColumnInfo

/**
 * Data model representing aggregated financial data for a specific category.
 * This class is used to populate pie charts and bar graphs.
 *
 * @property selectedTransactionCategory The name of the transaction category.
 * @property totalSummationOfAmountValuesInCategory The total amount spent in this category (in cents).
 * @property totalTransactionsCount The total number of transactions that contribute to the summation.
 */
data class PieChartInfo(
    @ColumnInfo(name = "selectedTransactionCategory")
    val selectedTransactionCategory: String = "Unknown",
    @ColumnInfo(name = "totalSummationOfAmountValuesInCategory")
    val totalSummationOfAmountValuesInCategory: Long = 0,
    @ColumnInfo(name = "totalTransactionsCount")
    val totalTransactionsCount: Int = 0
)