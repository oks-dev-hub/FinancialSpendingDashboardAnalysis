package com.example.financialspendingdashboardanalysis.model

import com.example.financialmodels.TransactionCategory

/**
 * Sealed interface representing all possible user or system actions that can occur
 * on the Financial Spending Dashboard.
 *
 * These actions are dispatched from the UI to the ViewModel's `onAction` method,
 * enabling a clear separation of UI events and business logic.
 */
sealed interface FinancialDashboardAction {
    /**
     * Action to initialize the dashboard data for a specific month page.
     *
     * @property pageIndex The index of the month page to load.
     */
    data class InitializeMonthlyPieChartDashboard(val pageIndex: Int): FinancialDashboardAction

    /**
     * Action triggered when a user taps on a pie slice in the pie chart.
     *
     * @property selectedPieChartPairsIndex The index of the clicked slice.
     * @property selectedCategory The [TransactionCategory] associated with the clicked slice.
     */
    data class SetPieChartClickAction(
        val selectedPieChartPairsIndex: Int,
        val selectedCategory: TransactionCategory = TransactionCategory.UNKNOWN
    ): FinancialDashboardAction

    /**
     * Action triggered when a user taps on a bar in the bar graph.
     *
     * @property selectedBarGraphIndex The index of the clicked month's bar.
     */
    data class SetBarChartClickAction(val selectedBarGraphIndex: Int): FinancialDashboardAction
}
