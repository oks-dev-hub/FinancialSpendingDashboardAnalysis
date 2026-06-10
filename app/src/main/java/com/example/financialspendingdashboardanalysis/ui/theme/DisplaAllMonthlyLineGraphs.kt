package com.example.financialspendingdashboardanalysis.ui.theme

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.financialspendingdashboardanalysis.R
import com.example.financialspendingdashboardanalysis.model.FinancialTransactionData
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToInt

@Composable
fun DisplayAllMonthlyLineGraphs(
    selectedCategoriesInstancesForAMonth: Pair<Map<String, List<FinancialTransactionData>>, Int> = Pair(emptyMap(), 0),
    selectedBarColor: Color = Color.Transparent
) {
    val maxValue = selectedCategoriesInstancesForAMonth.second
    val maxLabel = (maxValue / 100f / 1000f).roundToInt().toString()
    val textMeasurer = rememberTextMeasurer()
    val gridLineCount = 15

    Column {
        Canvas(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.White)
        ) {
            val width = size.width
            val height = size.height

            drawLine(
                color = Color.Gray,
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 3f
            )

            drawLine(
                color = Color.Gray,
                start = Offset(0f, 0f),
                end = Offset(0f, height),
                strokeWidth = 3f
            )

            for (i in 0..gridLineCount) {
                val y = height - (i * height / gridLineCount)

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
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

            selectedCategoriesInstancesForAMonth.first.entries.forEachIndexed { _, (key, _) ->
                val values = selectedCategoriesInstancesForAMonth.first[key]
                val lineGraphStroke = Stroke(
                    width = 3f
                )

                if (values != null) {
                    val xStep = width / (values.size - 1).coerceAtLeast(1).toFloat()
                    val path = Path()

                    values.forEachIndexed { pointIndex, data ->
                        val normalized = data.amount.toFloat() / calculateAxisMax(maxValue.toFloat())
                        val horizontalValue = pointIndex * xStep
                        val verticalValue = height - (normalized * height)

                        if (pointIndex == 0) {
                            path.moveTo(horizontalValue, verticalValue)
                        } else {
                            path.lineTo(horizontalValue, verticalValue)
                        }

                        drawCircle(
                            color = Color.Gray,
                            radius = 6f,
                            center = Offset(horizontalValue, verticalValue)
                        )
                    }

                    drawPath(
                        path = path,
                        color = selectedBarColor,
                        style = lineGraphStroke
                    )
                }
            }
        }

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp, top = 4.dp),
            textAlign = TextAlign.Center,
            text = stringResource(R.string.lineGraphXAxisDescription),
            style = TextStyle(
                color = Color.Black,
                fontSize = 16.sp,
                fontWeight =  FontWeight.Normal
            )
        )
    }
}

fun calculateAxisMax(value: Float): Float {
    if (value <= 0f) return 1f

    val magnitude = 10.0.pow(
        floor(log10(value.toDouble()))
    ).toFloat()
    return ceil(value / magnitude) * magnitude
}
