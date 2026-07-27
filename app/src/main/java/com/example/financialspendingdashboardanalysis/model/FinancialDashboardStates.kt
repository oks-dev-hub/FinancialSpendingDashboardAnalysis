package com.example.financialspendingdashboardanalysis.model

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Color
import com.example.financialmodels.TransactionCategory

@Stable
data class FinancialDashboardStates(
    val generateRandomColorsList: List<Color> = emptyList(),
    val selectedPieAnglePairsIndex: Int = 0,
    val selectedMonth: String = "",
    val hasDashBoardBeenInitializedBefore: Boolean = false,
    val monthlyPieData: Map<TransactionCategory, PieChartInfo> = emptyMap(),
    val selectedBarGraphIndex: Int = 0,
    val selectedBarGraphMonth: String = "",
    val maxAmountForSelectedCategory: Long = 0,
    val selectedCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val selectedFinancialTransactionData: FinancialTransactionData = FinancialTransactionData(),
    val fetchSelectedCategoriesForAllMonth: Map<Int, PieChartInfo> = emptyMap(),
    val monthlyLineGraphData:  List<FiveDayAverageValues> = emptyList()
)