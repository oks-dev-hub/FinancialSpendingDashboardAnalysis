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

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            FinancialSpendingDashboardAnalysisTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Column(
                      modifier = Modifier
                          .fillMaxSize()
                          .padding(innerPadding)
                    ) {
                        DisplayFinancialDashboardNavGraph()
                    }
                }
            }
        }
    }
}