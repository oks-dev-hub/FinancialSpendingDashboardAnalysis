package com.example.financialspendingdashboardanalysis.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.financialmodels.TransactionCategory

/**
 * Data class representing the complete UI state for the Financial Spending Dashboard.
 *
 * This state object is designed for "Smart Recomposition" in Jetpack Compose, using the [@Stable]
 * annotation to hint to the compiler that its properties will notify the UI correctly upon change.
 * It encapsulates all data needed to render the pie charts, bar graphs, line graphs, and
 * transaction details.
 *
 * @property generateRandomColorsList A list of colors used for distinguishing categories in charts.
 * @property selectedPieAnglePairsIndex The index of the currently selected/exploded slice in the pie chart.
 * @property selectedMonth The name of the currently displayed month.
 * @property hasDashBoardBeenInitializedBefore Flag to track if the dashboard has already loaded its initial data.
 * @property monthlyPieData Aggregated spending data per category for the selected month.
 * @property selectedBarGraphIndex The index of the selected month in the multi-month bar graph.
 * @property selectedBarGraphMonth The name of the month corresponding to the [selectedBarGraphIndex].
 * @property selectedCategory The currently active transaction category filter.
 * @property selectedFinancialTransactionData Details of a specifically selected transaction.
 * @property fetchSelectedCategoriesForAllMonth Mapping of month indices to category-specific totals for bar graph rendering.
 * @property monthlyLineGraphData Time-series data points for rendering the month's spending trend.
 */
@Stable
data class FinancialDashboardStates(
    val generateRandomColorsList: List<Color> = emptyList(),
    val selectedPieAnglePairsIndex: Int = 0,
    val selectedMonth: String = "",
    val hasDashBoardBeenInitializedBefore: Boolean = false,
    val monthlyPieData: Map<TransactionCategory, PieChartInfo> = emptyMap(),
    val selectedBarGraphIndex: Int = 0,
    val selectedBarGraphMonth: String = "",
    val selectedCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val selectedFinancialTransactionData: FinancialTransactionData = FinancialTransactionData(),
    val fetchSelectedCategoriesForAllMonth: Map<Int, PieChartInfo> = emptyMap(),
    val monthlyLineGraphData:  List<DayAverageValues> = emptyList()
)