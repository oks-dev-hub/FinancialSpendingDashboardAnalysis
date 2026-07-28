package com.example.financialspendingdashboardanalysis.room

import androidx.room.Dao
import androidx.room.Query
import com.example.financialspendingdashboardanalysis.model.FiveDayAverageValues
import com.example.financialspendingdashboardanalysis.model.MonthlyCategoryTotal
import com.example.financialspendingdashboardanalysis.model.PieChartInfo

/**
 * Data Access Object (DAO) for the financial transactions table.
 *
 * This interface defines the database operations required for the Financial Spending Dashboard.
 * It provides complex SQL queries to aggregate data for various visualizations:
 * - Pie Charts (Category distribution per month)
 * - Bar Graphs (Monthly totals per category)
 * - Line Graphs (Spending trends via five-day averages)
 */
@Dao
interface FinancialTransactionsDao {
    /**
     * Aggregates transaction data for a specific month to provide a breakdown of spending by category.
     * Used primarily for rendering the monthly Pie Chart.
     *
     * @param month The month index (1-12) to query.
     * @return A list of [PieChartInfo] representing the total amount and count for each category.
     */
    @Query("""
        SELECT
            transaction_category AS selectedTransactionCategory,
            SUM(amount) AS totalSummationOfAmountValuesInCategory,
            COUNT(*) AS totalTransactionsCount
        FROM transactions
        WHERE month = :month
        GROUP BY transaction_category
        ORDER BY totalSummationOfAmountValuesInCategory DESC
    """)
        suspend fun getPieChartInfo(month: Int): List<PieChartInfo>

    /**
     * Calculates monthly total spending for a specific category over a specified range of months.
     * Used primarily for rendering the category trend Bar Graph.
     *
     * @param transactionCategory The category string (e.g., "Food").
     * @param transactionsYear The year to filter by.
     * @param startMonth The starting month index (inclusive).
     * @param endMonth The ending month index (inclusive).
     * @return A list of [MonthlyCategoryTotal] representing the totals for each month in the range.
     */
    @Query("""
        SELECT
            year,
            month,
            SUM(amount) AS total,
            COUNT(*) AS totalTransactionsCount
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

    /**
     * Calculates the average transaction amount for a category within a month, grouped into five-day intervals.
     * Used primarily for rendering the intra-month trend Line Graph.
     *
     * @param year The year to filter by.
     * @param month The month index to filter by.
     * @param transactionCategory The category string (e.g., "Food").
     * @return A list of [FiveDayAverageValues] representing the spending trend across the month.
     */
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
