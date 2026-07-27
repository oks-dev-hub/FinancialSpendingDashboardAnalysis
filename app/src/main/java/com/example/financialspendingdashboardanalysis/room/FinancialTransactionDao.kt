package com.example.financialspendingdashboardanalysis.room

import androidx.room.Dao
import androidx.room.Query
import com.example.financialspendingdashboardanalysis.model.FiveDayAverageValues
import com.example.financialspendingdashboardanalysis.model.MonthlyCategoryTotal
import com.example.financialspendingdashboardanalysis.model.PieChartInfo

@Dao
interface FinancialTransactionsDao {
    @Query("""
        SELECT
            transaction_category AS selectedTransactionCategory,
            SUM(amount) AS totalSummationOfAmountValuesInCategory
        FROM transactions
        WHERE month = :month
        GROUP BY transaction_category
        ORDER BY totalSummationOfAmountValuesInCategory DESC
    """)
        suspend fun getPieChartInfo(month: Int): List<PieChartInfo>

    @Query("""
        SELECT
            year,
            month,
            SUM(amount) AS total
        FROM transactions
        WHERE transaction_category = :transactionCategory
          AND year = :transactionsYear
          AND month BETWEEN :startMonth AND :endMonth
        GROUP BY year, month
        ORDER BY year ASC, month ASC
    """)
    suspend fun getMonthlyTotalsForCategory(
        transactionCategory: String,
        transactionsYear: Int,
        startMonth: Int,
        endMonth: Int
    ): List<MonthlyCategoryTotal>

    @Query("""
        SELECT
            ((CAST(strftime('%d', payment_date) AS INTEGER) - 1) / 5) AS intervalIndex,
            AVG(amount) AS averageAmount,
            COUNT(id) AS transactionCount
        FROM transactions
        WHERE year = :year
          AND month = :month
          AND transaction_category = :transactionCategory
        GROUP BY intervalIndex
        ORDER BY intervalIndex
    """)
    suspend fun getAverageAmountPerFiveDayInterval(
        year: Int,
        month: Int,
        transactionCategory: String
    ): List<FiveDayAverageValues>
}