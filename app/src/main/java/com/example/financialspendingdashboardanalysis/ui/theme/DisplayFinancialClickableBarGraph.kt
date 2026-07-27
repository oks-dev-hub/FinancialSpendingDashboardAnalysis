package com.example.financialspendingdashboardanalysis.ui.theme

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
import androidx.room.TypeConverter
import com.example.financialmodels.TransactionCategory
import com.example.financialspendingdashboardanalysis.model.BarGraphInfo
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import kotlin.math.absoluteValue
import kotlin.math.roundToInt

/**
 * Displays an interactive financial bar chart for the last six months.
 * NB: This is a clickable bar graph, i,e, each bar is clickable, and when clicked it triggers a recomposition, of the whole screen.
 * Firstly, each bar represents the total amount of all transaction of the selected Transaction Category for the selected month in the Pie Chart.
 * To put it into context, there will always be six bar, meaning that each we show the total summation amount of transaction for the selected category for the past six month.
 * So, when a user, clicks/taps on an unselected bar, the screen recomposes, and we then show the selected Transaction Category for that selected month in the Bar Graph.
 * This means that in the Pie Chart, we need to Page to the that selected month, and show the Pie Chart for the that month with the selected Transaction Category pie slice out of the Pie Chart,
 * Moreover, show the bar that is currently selected as selected in the Bra Graph, then finally, we display the information for the new selected month alongside it selected Transaction Category in the Line Graph.
 *
 *
 * Features:
 * - Interactive bar selection via touch input
 * - Highlighted selected bar
 * - Dynamic scaling based on maximum dataset value
 * - Grid background for readability
 * - Month labels under each bar
 *
 * @param lastSixMonthsFromCurrentMonth Ordered list of month labels (e.g., Jan–Jun)
 * @param selectedCategoriesForAllMonth Map of month → financial summary data
 * @param selectedBarGraphIndex Currently selected bar index
 * @param barColor Default bar color
 * @param selectedBarColor Highlight color for selected bar
 * @param onAction Callback triggered when a bar is tapped (returns index)
 *
 * ---
 *
 * ## Performance Analysis
 *
 * - **Time Complexity: O(N)** per recomposition
 *   - N = number of months/bars (typically constant = 6)
 *
 * - **Space Complexity: O(N)**
 *   - Temporary BarGraphInfo list used for rendering layout calculations
 *
 * Note:
 * Canvas rendering is GPU-accelerated; performance cost mainly comes from
 * recomposition frequency and data recalculation.
 */
@Composable
fun DisplayFinancialClickableBarGraph(
    lastSixMonthsFromCurrentMonth: List<String>,
    selectedCategoriesForAllMonth: Map<Int, PieChartInfo> = emptyMap(),
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
                        //this is the lowest level of determining a click in a Canvas area
                        if (selectedCategoriesForAllMonth.isEmpty()) return@detectTapGestures

                        val barWidth = size.width / (selectedCategoriesForAllMonth.size * 2f)

                        /**
                         * Handles user tap detection on bars.
                         *
                         * Converts tap X position into a bar index.
                         *
                         * Basically, we're trying to identify the tap using the x-axis only
                         */
                        selectedCategoriesForAllMonth.entries.forEachIndexed { index, _ ->
                            val trueIndex = selectedCategoriesForAllMonth.size - (index + 1)
                            //left bound of the associated bar width + padding
                            //start from right to left
                            val left = trueIndex * (barWidth + barWidth) + barWidth / 2.toFloat()
                            //determine the righ bound on the associated bar
                            val right = left + barWidth

                            // Detect if tap is within bar bounds by checking if the tap x-coordinate, i,e, tapOffset.x
                            if (tapOffset.x in left..right) {
                                //this sends an event to do a very heavy Job, because the UI's Job is only to display UI, nothing more.
                                //we need to delegate this Job to the viewModel, so we so by sending en event such as this one
                                //this heavy lifting will be done by the viewModel, on a separate thread, non-Main, this is the power of Kotlin.
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
            if (itemCount == 0) return@Canvas

            val totalSpacing = spacing * (itemCount + 1)

            //determines the barWidth for each bar in the bar graph
            val barWidth = (canvasWidth - totalSpacing) / itemCount.toFloat()
            val labelArea = 40.dp.toPx()

            //determined the bar graph's height, adding space for labeling the month on the bottom
            val chartHeight = canvasHeight - labelArea

            val maxAmountForSelectionCategory =
                selectedCategoriesForAllMonth.maxOfOrNull {
                    it.value.totalSummationOfAmountValuesInCategory
                } ?: 1

            //determines the highest value on the bra graph, to indicate the estimation of the bra's height in relation to one another.
            val maxLabel = (maxAmountForSelectionCategory / 100f / 1000f).roundToInt().toString()

            //Control the number of horizontal grid line drawn onf the bar graph
            val gridLineCount = 15

            //Draws the horizontal grid lines on the graph, this is just a UI advancement
            for (i in 0..gridLineCount) {
                val y = chartHeight - (i * chartHeight / gridLineCount)

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(canvasWidth, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            //In this code block, the intention is to sort this Map below in ascending order based of the payment date
            //This is so that when we want to display them we can be sure about the order's consistency
            selectedCategoriesForAllMonth.entries.forEach { (key, entry) ->
                //determine the bra's height via the ration of the summation of all transaction for this category associated with the selected month by total summation all transaction for that Transaction Category
                val barHeight =
                    (entry.totalSummationOfAmountValuesInCategory.toFloat() / maxAmountForSelectionCategory.toFloat()) * (chartHeight)
                val correctIndex = key - 1

                if (correctIndex in 0 until 6) {
                    val barGraphInfo = BarGraphInfo(
                        barHeight = barHeight,
                        month = lastSixMonthsFromCurrentMonth.getOrNull(correctIndex) ?: "",
                        trueIndex = correctIndex,
                        left = spacing + (correctIndex) * (barWidth + spacing),
                        top = chartHeight - barHeight,
                        color = if (selectedBarGraphIndex == correctIndex) selectedBarColor else barColor
                    )
                    barGraphInfoList[correctIndex] = barGraphInfo
                }
            }

            //this is the resultant sorted list, we use to display the bar's orderly so.
            barGraphInfoList.forEachIndexed { _, barGraphInfo ->
                if (barGraphInfo.month.isEmpty()) return@forEachIndexed

                //will be displaced the bottom associated with its corresponding bar
                val barGraphMonth = barGraphInfo.month.take(3)

                //This is used to center the text at the bottom of its corresponding bar
                val textLayoutResult = textMeasurer.measure(
                    text = barGraphMonth,
                    style = TextStyle(fontSize = 16.sp)
                )

                val textWidth = textLayoutResult.size.width.toFloat()
                //determine the x-coordinate for the Offsetting of mont label text
                val textX = barGraphInfo.left + ((barWidth - textWidth).absoluteValue / 2f)

                //This where we draw each bar, placed them using the function Offset
                //The size, that is width and height is determined by Size
                drawRoundRect(
                    color = barGraphInfo.color,
                    topLeft = Offset(barGraphInfo.left, barGraphInfo.top),
                    size = Size(barWidth, barGraphInfo.barHeight),
                    cornerRadius = CornerRadius(16f, 16f)
                )

                //This is to indicated that a bra is selected, here we uniquely mark a bar to signal that it is selected.
                if (selectedBarGraphIndex == barGraphInfo.trueIndex) {
                    drawRoundRect(
                        color = Color.Companion.Black,
                        topLeft = Offset(barGraphInfo.left, barGraphInfo.top),
                        size = Size(barWidth, barGraphInfo.barHeight),
                        cornerRadius = CornerRadius(16f, 16f),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }

                //This is where we actually draw each month's label
                drawText(
                    textMeasurer = textMeasurer,
                    text = barGraphMonth,
                    topLeft = Offset(textX, (chartHeight + 8.dp.toPx()).absoluteValue),
                    style = TextStyle(
                        color = Color.Black,
                        fontSize = 16.sp, // Define font size
                        //The text should be bold for the selected bar's label month
                        fontWeight = if (selectedBarGraphIndex == barGraphInfo.trueIndex) FontWeight.ExtraBold else FontWeight.Normal,
                        fontStyle = FontStyle.Normal
                    )
                )
            }

            //This is where we draw the maximum amount indicator that is used to estimate the relation between the bars.
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