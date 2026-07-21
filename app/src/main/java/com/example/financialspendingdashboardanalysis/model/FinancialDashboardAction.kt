package com.example.financialspendingdashboardanalysis.model

import com.example.financialmodels.TransactionCategory

sealed interface FinancialDashboardAction {
    data class InitializeMonthlyPieChartDashboard(val pageIndex: Int): FinancialDashboardAction

    data class SetPieChartClickAction(val selectedPieChartPairsIndex: Int, val selectedCategory: TransactionCategory = TransactionCategory.UNKNOWN): FinancialDashboardAction

    data class SetBarChartClickAction(val selectedBarGraphIndex: Int): FinancialDashboardAction
}