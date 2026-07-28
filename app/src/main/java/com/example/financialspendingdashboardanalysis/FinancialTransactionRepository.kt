package com.example.financialspendingdashboardanalysis

import com.example.financialspendingdashboardanalysis.model.FiveDayAverageValues
import com.example.financialspendingdashboardanalysis.model.MonthlyCategoryTotal
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import com.example.financialspendingdashboardanalysis.room.FinancialTransactionsDao

/**
 * Repository class that abstracts access to financial transaction data.
 *
 * This class acts as a mediator between the data source (Room database via [FinancialTransactionsDao])
 * and the rest of the application, providing high-level methods to retrieve aggregated
 * financial information for visualization.
 *
 * @property financialTransactionsDao The Data Access Object used to perform database operations.
 */
class FinancialTransactionRepository(
    val financialTransactionsDao: FinancialTransactionsDao
) {
    /**
     * Retrieves aggregated spending data per category for a specific month.
     * This data is typically used to populate pie charts.
     *
     * @param enquiryMonth The index of the month to query (1-based or 0-based depending on implementation, 
     *                     check [FinancialTransactionsDao.getPieChartInfo]).
     * @return A list of [PieChartInfo] objects containing category-wise totals.
     */
    suspend fun getFinancialTransactionsInformationByDate(enquiryMonth: Int): List<PieChartInfo> {
        return financialTransactionsDao.getPieChartInfo(enquiryMonth)
    }

    /**
     * Retrieves total spending for a specific category across a range of months.
     * This data is typically used to populate bar graphs for trend analysis.
     *
     * @param transactionCategory The string name of the category to filter by.
     * @param transactionsYear The year of the transactions.
     * @param startMonth The beginning month of the range.
     * @param endMonth The ending month of the range.
     * @return A list of [MonthlyCategoryTotal] objects representing monthly sums for the category.
     */
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

    /**
     * Retrieves average spending amounts calculated in five-day intervals for a given month and category.
     * This data is typically used for line graph visualizations to show spending trends within a month.
     *
     * @param year The year of interest.
     * @param month The month of interest.
     * @param transactionCategory The category to analyze.
     * @return A list of [FiveDayAverageValues] representing smoothed spending data over the month.
     */
    suspend fun getAverageAmountPerFiveDayInterval(
        year: Int,
        month: Int,
        transactionCategory: String
    ): List<FiveDayAverageValues> {
        return financialTransactionsDao.getAverageAmountPerFiveDayInterval(
            year = year,
            month = month,
            transactionCategory = transactionCategory
        )
    }
}