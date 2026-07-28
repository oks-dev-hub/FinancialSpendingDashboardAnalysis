package com.example.financialspendingdashboardanalysis.model

/**
 * Sealed interface representing one-time events dispatched from the ViewModel to the UI.
 *
 * Unlike state updates, events represent transient occurrences like navigation or
 * displaying temporary UI elements (e.g., Snackbars) that should be handled only once.
 */
sealed interface FinancialDashboardEvents {
    /**
     * Event to trigger navigation to the detailed transaction view.
     *
     * @property financialTransactionData The specific transaction data to display in the detail view.
     */
    data class NavigateToViewTransactionDetails(val financialTransactionData: FinancialTransactionData): FinancialDashboardEvents

    /**
     * Event to trigger navigation back to the main financial spending dashboard.
     */
    data object NavigateToBackToFinancialSpendingDashboard: FinancialDashboardEvents
}