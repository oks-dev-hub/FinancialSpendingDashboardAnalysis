package com.example.financialspendingdashboardanalysis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.view.WindowCompat
import com.example.financialspendingdashboardanalysis.navGraph.DisplayFinancialDashboardNavGraph
import com.example.financialspendingdashboardanalysis.ui.theme.FinancialSpendingDashboardAnalysisTheme

/**
 * The primary entry point for the Financial Spending Dashboard application.
 *
 * This Activity is responsible for setting up the edge-to-edge display and initializing
 * the Jetpack Compose UI hierarchy. It serves as the host for the application's
 * navigation graph.
 *
 * Features:
 * - Edge-to-edge system UI integration.
 * - Theme initialization via [FinancialSpendingDashboardAnalysisTheme].
 * - Root navigation hosting via [DisplayFinancialDashboardNavGraph].
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Disable automatic window insets to allow custom edge-to-edge handling in Compose
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        setContent {
            FinancialSpendingDashboardAnalysisTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                      modifier = Modifier
                          .fillMaxSize()
                          .padding(innerPadding)
                    ) {
                        // Entry point for the application's navigation logic
                        DisplayFinancialDashboardNavGraph()
                    }
                }
            }
        }
    }
}