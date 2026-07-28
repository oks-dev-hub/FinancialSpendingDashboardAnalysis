package com.example.financialspendingdashboardanalysis.navGraph

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.financialspendingdashboardanalysis.ui.theme.FinancialSpendingDashBoard
import com.example.financialspendingdashboardanalysis.viewmodel.FinancialAnalyticsDashboardViewModel

/**
 * Root navigation graph for the Financial Spending Dashboard application.
 *
 * This composable serves as the application's navigation entry point and
 * defines all available destinations within the dashboard feature.
 *
 * Responsibilities:
 * - Creates and owns the application's [NavController].
 * - Provides a shared instance of [FinancialAnalyticsDashboardViewModel]
 *   across navigation destinations.
 * - Defines navigation routes and their associated screen composables.
 * - Coordinates navigation between dashboard and transaction detail screens.
 *
 * Navigation Flow:
 *
 * ```
 * Financial Dashboard
 *          │
 *          ▼
 * Transaction Details
 * ```
 *
 * Route Definitions:
 *
 * - [DisplayFinancialDashboardRoute]
 *     Displays the primary financial analytics dashboard containing:
 *     - Monthly spending pie charts.
 *     - Category spending bar graphs.
 *     - Transaction trend line graphs.
 *     - Transaction history.
 *
 * - [ViewFullTransactionDetailsRoute]
 *     Displays detailed information for a selected financial transaction.
 *
 * Shared State:
 *
 * A single instance of [FinancialAnalyticsDashboardViewModel] is created
 * and shared between all destinations. This ensures:
 *
 * Navigation Strategy:
 *
 * The dashboard screen acts as the application's start destination.
 * Transaction detail navigation occurs when a transaction list item
 * is selected from the dashboard.
 *
 * Recomposition Characteristics:
 *
 * Navigation destinations are composed lazily and only rendered when
 * active within the navigation back stack.
 */
@Composable
fun DisplayFinancialDashboardNavGraph() {
    // Navigation controller responsible for managing the application's
    // navigation back stack.
    val navController = rememberNavController()

    // Shared ViewModel used across all navigation destinations.
    val financialAnalyticsDashboardViewModel: FinancialAnalyticsDashboardViewModel = viewModel(
        factory = FinancialAnalyticsDashboardViewModel.Factory
    )

    NavHost(
        navController = navController,
        // Initial screen displayed when the application launches.
        startDestination = DisplayFinancialDashboardRoute
    ) {
        // Dashboard analytics screen.
        composable<DisplayFinancialDashboardRoute> {
            FinancialSpendingDashBoard(
                navController = navController,
                financialAnalyticsDashboardViewModel = financialAnalyticsDashboardViewModel
            )
        }
    }
}