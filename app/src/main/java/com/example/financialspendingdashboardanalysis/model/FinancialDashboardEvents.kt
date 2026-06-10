package com.example.financialspendingdashboardanalysis.model

sealed interface FinancialDashboardEvents {
    data class NavigateToViewTransactionDetails(val financialTransactionData: FinancialTransactionData): FinancialDashboardEvents
    data object NavigateToBackToFinancialSpendingDashboard: FinancialDashboardEvents
}