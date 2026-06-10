package com.example.financialspendingdashboardanalysis.ui.theme

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialspendingdashboardanalysis.model.BarGraphInfo
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import kotlin.math.roundToInt

@Composable
fun DisplayFinancialClickableBarGraph(
    lastSixMonthsFromCurrentMonth: List<String>,
    selectedCategoriesForAllMonth: Map<String, PieChartInfo> = emptyMap(),
    selectedBarGraphIndex: Int = 0,
    barColor: Color = Color.Transparent,
    selectedBarColor: Color = Color.Transparent,
    onAction: (Int) -> Unit = {}
) {
    val textMeasurer = rememberTextMeasurer()
    val barGraphInfoList = MutableList(6) { BarGraphInfo() }

    Column {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .pointerInput(selectedCategoriesForAllMonth.toMap()) {
                    detectTapGestures { tapOffset ->
                        val barWidth = size.width / (selectedCategoriesForAllMonth.size * 2f)

                        selectedCategoriesForAllMonth.entries.forEachIndexed { index, _ ->
                            val trueIndex = selectedCategoriesForAllMonth.size - (index + 1)
                            val left = trueIndex * (barWidth + barWidth) + barWidth / 2.toFloat()
                            val right = left + barWidth

                            if (tapOffset.x in left..right) {
                                onAction(trueIndex)
                            }
                        }
                    }
                }
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            val spacing = 16.dp.toPx()
            val itemCount = selectedCategoriesForAllMonth.size
            val totalSpacing = spacing * (itemCount + 1)

            val barWidth = (canvasWidth - totalSpacing) / itemCount.toFloat()
            val labelArea = 40.dp.toPx()

            val chartHeight = canvasHeight - labelArea
            val maxAmountForSelectionCategory =
                selectedCategoriesForAllMonth.maxOfOrNull {
                    it.value.totalSummationOfAmountValuesInCategory
                } ?: 1
            val maxLabel = (maxAmountForSelectionCategory / 100f / 1000f).roundToInt().toString()

            val gridLineCount = 15

            for (i in 0..gridLineCount) {
                val y = chartHeight - (i * chartHeight / gridLineCount)

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            selectedCategoriesForAllMonth.entries.forEach { (key, entry) ->
                val barHeight = (entry.totalSummationOfAmountValuesInCategory.toFloat() / maxAmountForSelectionCategory.toFloat()) * (chartHeight)
                val correctIndex = lastSixMonthsFromCurrentMonth.indexOf(key[0].uppercase() + key.substring(1).lowercase())

                val barGraphInfo = BarGraphInfo(
                    barHeight = barHeight,
                    month = key,
                    trueIndex = correctIndex,
                    left = spacing + correctIndex * (barWidth + spacing),
                    top = chartHeight - barHeight,
                    color = if (selectedBarGraphIndex == correctIndex) selectedBarColor else barColor
                )
                barGraphInfoList[correctIndex] = barGraphInfo
            }

            barGraphInfoList.forEachIndexed { _, barGraphInfo ->
                val barGraphMonth = barGraphInfo.month.take(3)
                val textLayoutResult = textMeasurer.measure(
                    text = barGraphMonth,
                    style = TextStyle(fontSize = 16.sp)
                )

                val textWidth = textLayoutResult.size.width.toFloat()
                val textX = barGraphInfo.left + ((barWidth - textWidth) / 2f)

                drawRoundRect(
                    color = barGraphInfo.color,
                    topLeft = Offset(barGraphInfo.left, barGraphInfo.top),
                    size = Size(barWidth, barGraphInfo.barHeight),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                if (selectedBarGraphIndex == barGraphInfo.trueIndex) {
                    drawRoundRect(
                        color = Color.Black,
                        topLeft = Offset(barGraphInfo.left, barGraphInfo.top),
                        size = Size(barWidth, barGraphInfo.barHeight),
                        cornerRadius = CornerRadius(16f, 16f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                drawText(
                    textMeasurer = textMeasurer,
                    text = barGraphMonth,
                    topLeft = Offset(textX, chartHeight + 8.dp.toPx()),
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp, // Define font size
                        fontWeight = if (selectedBarGraphIndex == barGraphInfo.trueIndex) FontWeight.ExtraBold else FontWeight.Normal,
                        fontStyle = FontStyle.Normal
                    )
                )
            }

            drawText(
                textMeasurer = textMeasurer,
                text = "${maxLabel}K",
                topLeft = Offset(
                    4.dp.toPx(),
                    0.dp.toPx()
                ),
                style = TextStyle(
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}