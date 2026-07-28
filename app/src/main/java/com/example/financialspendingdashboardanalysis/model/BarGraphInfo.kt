package com.example.financialspendingdashboardanalysis.model

import androidx.compose.ui.graphics.Color

/**
 * Data model used for drawing and interacting with individual bars in a bar graph.
 *
 * This class captures the geometric and metadata properties of a bar, enabling
 * precise rendering on a canvas and facilitating click detection by mapping
 * screen coordinates back to specific data points.
 *
 * @property barHeight The normalized height of the bar based on the data value.
 * @property month The month name associated with this bar.
 * @property trueIndex The actual index of this data point in the original data set.
 * @property left The horizontal start coordinate (x) of the bar on the canvas.
 * @property top The vertical start coordinate (y) of the bar on the canvas.
 * @property color The color used to render this specific bar.
 */
data class BarGraphInfo(
    val barHeight: Float = 0.toFloat(),
    val month: String = "",
    val trueIndex: Int = 0,
    val left: Float = 0.toFloat(),
    val top: Float = 0.toFloat(),
    val color: Color = Color.Transparent
)