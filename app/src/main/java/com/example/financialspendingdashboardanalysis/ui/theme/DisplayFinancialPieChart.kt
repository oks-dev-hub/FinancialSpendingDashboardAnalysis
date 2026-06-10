package com.example.financialspendingdashboardanalysis.ui.theme

import android.graphics.Paint
import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.dp
import com.example.financialspendingdashboardanalysis.model.BarGraphInfo
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import com.example.financialspendingdashboardanalysis.model.TransactionCategory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun DisplayFinancialPieChart(
    modifier: Modifier,
    generateRandomColorsList: List<Color>,
    selectedPieAnglePairsIndex: Int = 0,
    setSelectedPieItemInChartState: (Int, TransactionCategory) -> Unit = { _, _ -> },
    monthlyPieData: Map<TransactionCategory, PieChartInfo>
) {
    val animationPlayed = remember { mutableStateOf(true) }
    val pieSliceAngleConstraints: MutableList<Pair<Float, Float>> = remember { mutableListOf() }
    val categoryNames: MutableList<String> = remember { mutableListOf() }
    var pieCenter: Offset = Offset.Zero
    var diameter: Float = 0.toFloat()
    var overallAmountForAllCategory = 0

    LaunchedEffect(Unit) {
        animationPlayed.value = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        PieChartLabelIndexDisplayed(
            generateRandomColorsList = generateRandomColorsList,
            getMonthlyPieData = monthlyPieData.entries.associate { monthlyPieDataEntry ->
                overallAmountForAllCategory += monthlyPieDataEntry.value.totalSummationOfAmountValuesInCategory
                categoryNames.add(monthlyPieDataEntry.key.name)
                monthlyPieDataEntry.key.name.replace("_", " ") to monthlyPieDataEntry.value.totalSummationOfAmountValuesInCategory
            }
        )

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .padding(30.dp)
                    .align(Alignment.Center)
                    .fillMaxWidth()
                    .height(350.dp)
                    .pointerInput(Unit) {
                        detectTapGestures { tapOffset ->
                            if (diameter == 0.toFloat() && pieCenter == Offset.Zero && pieSliceAngleConstraints.isEmpty()) {
                                return@detectTapGestures
                            }

                            val horizontalDistance = tapOffset.x - pieCenter.x
                            val verticalDistance = tapOffset.y - pieCenter.y
                            val distance = sqrt(horizontalDistance * horizontalDistance + verticalDistance * verticalDistance)
                            val radius = diameter / 2f

                            if (distance > radius) {
                                return@detectTapGestures
                            }

                            val rawAngle = Math.toDegrees(
                                atan2(-verticalDistance, horizontalDistance).toDouble()
                            ).toFloat()

                            val pieAngle = (360f - rawAngle + 360f) % 360f

                            pieSliceAngleConstraints.forEachIndexed { index, pieSliceAngleConstraint ->
                                if (pieAngle >= pieSliceAngleConstraint.first && pieAngle < pieSliceAngleConstraint.first + pieSliceAngleConstraint.second) {
                                    val transactionCategory = TransactionCategory.entries.find { it.name.equals(categoryNames[index], ignoreCase = true) }
                                    setSelectedPieItemInChartState(index, transactionCategory ?: TransactionCategory.UNKNOWN)
                                    return@detectTapGestures
                                }
                            }
                        }
                    }
            ) {
                diameter = min(size.width, size.height)
                var startAngle = 0f
                var sliceAngle = 0f
                pieCenter = Offset(x = diameter / 2.toFloat(), y = diameter / 2.toFloat())
                val paint = Paint().apply {
                    color = android.graphics.Color.BLACK
                    textSize = 30f
                    textAlign = Paint.Align.CENTER
                    isFakeBoldText = true
                }
                val textHeightOffset = (paint.descent() + paint.ascent()) / 2f

                monthlyPieData.entries.forEachIndexed { index, (_, entry) ->
                    sliceAngle =
                        ((entry.totalSummationOfAmountValuesInCategory.toFloat() / overallAmountForAllCategory.toFloat()) * 360f)
                    val percentage =
                        ((entry.totalSummationOfAmountValuesInCategory.toFloat() / overallAmountForAllCategory.toFloat()) * 100)
                    val angleInRadian =
                        Math.toRadians((startAngle + (sliceAngle / 2.toFloat())).toDouble())
                    val horizontalTranslation =
                        pieCenter.x + ((diameter / 4.toFloat()) * cos(angleInRadian)).toFloat()
                    val verticalTranslation =
                        pieCenter.y + ((diameter / 4.toFloat()) * sin(angleInRadian)).toFloat()

                    scale(
                        scaleX = 1f,
                        scaleY = 1f
                    ) {
                        if (index == selectedPieAnglePairsIndex) {
                            val middleAngle = startAngle + (sliceAngle / 2f)
                            val radians = Math.toRadians(middleAngle.toDouble())
                            val explodeDistance = 30f
                            val offsetX = cos(radians).toFloat() * explodeDistance
                            val offsetY = sin(radians).toFloat() * explodeDistance

                            val pieCenterX = offsetX + diameter / 2f
                            val pieCenterY = offsetY + diameter / 2f
                            val textRadius = diameter * 0.25f
                            val textX = pieCenterX + cos(radians).toFloat() * textRadius
                            val textY = pieCenterY + sin(radians).toFloat() * textRadius

                            drawArc(
                                color = generateRandomColorsList[index],
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(offsetX, offsetY),
                                size = Size(diameter, diameter)
                            )

                            drawArc(
                                color = Color.Black,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(offsetX, offsetY),
                                size = Size(diameter, diameter),
                                style = Stroke(width = 4f)
                            )

                            drawContext.canvas.nativeCanvas.drawText(
                                "${percentage.toInt()}%",
                                textX,
                                textY - textHeightOffset,
                                paint
                            )
                        } else {
                            drawArc(
                                color = generateRandomColorsList[index],
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                size = Size(diameter, diameter)
                            )

                            drawContext.canvas.nativeCanvas.drawText(
                                "${percentage.toInt()}%",
                                horizontalTranslation,
                                verticalTranslation - textHeightOffset,
                                paint
                            )
                        }

                        if (pieSliceAngleConstraints.size < monthlyPieData.size) {
                            pieSliceAngleConstraints.add(Pair(startAngle, sliceAngle))
                        }

                        if (startAngle < 360f) {
                            startAngle += sliceAngle
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PieChartLabelIndexDisplayed(
    generateRandomColorsList: List<Color>,
    getMonthlyPieData: Map<String, Int>
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        getMonthlyPieData.entries.forEachIndexed { index, (key, entry) ->
            CreatePieChartTopHeaderDetails(
                modifier = Modifier,
                pieTitle = key,
                color = generateRandomColorsList[index],
                totalAmount = "R${convertValueToAMount(entry)}"
            )
        }
    }
}

fun convertValueToAMount(inputAmount: Int): String {
    val formatter = java.text.DecimalFormat("#,##0.00").apply {
        decimalFormatSymbols = decimalFormatSymbols.apply {
            groupingSeparator = ' '
        }
    }

    return formatter.format(inputAmount / 100.0)
}

@Composable
private fun CreatePieChartTopHeaderDetails(
    modifier: Modifier,
    pieTitle: String,
    color: Color,
    totalAmount: String = ""
) {
   Row(modifier = modifier
       .fillMaxWidth()
       .wrapContentHeight()
   ) {
       CreatePieChartDescription(
           modifier = modifier.fillMaxWidth(1f),
           color = color,
           pieTitle = pieTitle.trim(),
           totalAmount = totalAmount
       )
   }
}

@Composable
private fun CreatePieChartDescription(
    modifier: Modifier,
    color: Color?,
    pieTitle: String?,
    totalAmount: String?
) {
    if (color == null || pieTitle == null) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 8.dp),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .padding(8.dp)
                .size(16.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(color = color, RoundedCornerShape(4.dp)),
        )
        val pieTitleFormatted = pieTitle.toCharArray()[0] + pieTitle.substring(1).lowercase()
        Text(text = if (totalAmount.isNullOrEmpty()) pieTitle else "$pieTitleFormatted $totalAmount")
    }
}