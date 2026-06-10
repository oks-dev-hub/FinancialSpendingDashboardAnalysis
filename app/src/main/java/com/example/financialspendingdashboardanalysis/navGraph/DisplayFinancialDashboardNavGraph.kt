package com.example.financialspendingdashboardanalysis.navGraph

import android.content.Context
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.financialspendingdashboardanalysis.ui.theme.FinancialSpendingDashBoard
import com.example.financialspendingdashboardanalysis.ui.theme.ViewFullTransactionDetails
import com.example.financialspendingdashboardanalysis.viewModel.FinancialAnalyticsDashboardViewModel
import timber.log.Timber

@Composable
fun DisplayFinancialDashboardNavGraph() {
    val navController = rememberNavController()
    val context: Context = LocalContext.current
    val financialAnalyticsDashboardViewModel: FinancialAnalyticsDashboardViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = DisplayFinancialDashboardRoute
    ) {
        composable<DisplayFinancialDashboardRoute> {
            FinancialSpendingDashBoard(
                navController = navController,
                financialAnalyticsDashboardViewModel = financialAnalyticsDashboardViewModel
            )
        }

        composable<ViewFullTransactionDetailsRoute> {
            ViewFullTransactionDetails(
                navController = navController,
                financialTransactionData = financialAnalyticsDashboardViewModel.selectedFinancialTransactionData
            )
        }
    }
}