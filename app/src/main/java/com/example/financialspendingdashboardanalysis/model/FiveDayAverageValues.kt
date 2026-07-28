package com.example.financialspendingdashboardanalysis.model

/**
 * Data model for aggregated transaction averages calculated over five-day intervals.
 *
 * This structure is primarily used for rendering trend lines in line graphs, where
 * data is smoothed to show spending patterns across a month rather than individual
 * daily spikes.
 *
 * @property intervalIndex The index of the five-day period (0-5, where 0 is days 1-5).
 * @property averageAmount The mean transaction value for this period.
 * @property transactionCount The number of transactions occurring within this interval.
 */
data class FiveDayAverageValues(
    val intervalIndex: Int,
    val averageAmount: Double,
    val transactionCount: Int
)