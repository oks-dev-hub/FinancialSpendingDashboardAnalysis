package com.example.financialspendingdashboardanalysis.model

/**
 * Data model representing the aggregated financial total for a specific category within a single month.
 *
 * This class is primarily used to store query results from the database that summarize
 * spending behavior over a specific time period. It is essential for populating
 * bar graphs and other trend-based visualizations.
 *
 * @property year The year of the recorded transactions.
 * @property month The month index (1-12) of the recorded transactions.
 * @property total The summation of all transaction amounts (in cents) for the category in this month.
 * @property totalTransactionsCount The number of individual transactions that contribute to the [total].
 */
data class MonthlyCategoryTotal(
    val year: Int,
    val month: Int,
    val total: Long,
    val totalTransactionsCount: Int
)