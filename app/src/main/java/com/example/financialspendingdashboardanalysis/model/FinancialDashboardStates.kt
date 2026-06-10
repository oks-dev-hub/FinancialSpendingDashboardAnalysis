package com.example.financialspendingdashboardanalysis.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color

@Stable
data class FinancialDashboardStates(
    val generateRandomColorsList: List<Color> = emptyList(),
    val generateRandomColorsListForLineGraphs: List<Color> = emptyList(),
    val selectedPieAnglePairsIndex: Int = 0,
    val selectedMonth: String = "",
    val hasDashBoardBeenInitializedBefore: Boolean = false,
    val monthlyPieData: Map<TransactionCategory, PieChartInfo> = emptyMap(),
    val selectedBarGraphIndex: Int = 0,
    val selectedBarGraphMonth: String = "",
    val maxAmountForSelectedCategory: Int = 0,
    val selectedCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val selectedFinancialTransactionData: FinancialTransactionData = FinancialTransactionData(),
    val fetchSelectedCategoriesForAllMonth: Map<String, PieChartInfo> = emptyMap(),
    val fetchSelectedCategoriesInstancesForAllMonths: Pair<Map<String, List<FinancialTransactionData>>, Int> = Pair(emptyMap(), 0),
)