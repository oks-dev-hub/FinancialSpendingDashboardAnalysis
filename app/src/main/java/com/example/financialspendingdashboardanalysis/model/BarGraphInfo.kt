package com.example.financialspendingdashboardanalysis.model

import androidx.compose.ui.graphics.Color

data class BarGraphInfo(
    val barHeight: Float = 0.toFloat(),
    val month: String = "",
    val trueIndex: Int = 0,
    val left: Float = 0.toFloat(),
    val top: Float = 0.toFloat(),
    val color: Color = Color.Transparent
)