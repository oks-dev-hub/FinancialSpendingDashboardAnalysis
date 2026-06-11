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

/**
 * Displays a multi-line financial chart representing monthly spending trends per category.
 * Each dot represents a transaction, and the connecting line shows how transactions progress sequentially over time.
 * This graph is particularly for the selected Transaction Category in the Pie Chart, for that Selected or Displayed Month.
 * For example, Food, is one of the Transaction Categories, so the Pie Chart is for example, the month of May, then this line graphs will display all transactions performed for May under Food.
 *
 * This composable renders:
 * - X/Y axis grid
 * - Multiple category line graphs
 * - Data points per category
 * - Axis label description
 *
 * The chart is drawn using a low-level Canvas API for maximum performance and flexibility.
 *
 *
 * @param selectedCategoriesInstancesForAMonth
 * This comes already sorted for the Transaction Category that is selected.
 * where the key is associated Month, and values are the list of all transaction performed in that month.
 *
 * A pair containing:
 *  - Map of category name → list of financial transactions
 *  - Maximum transaction value for scaling the Y-axis
 *
 * @param selectedBarColor
 * Color used to draw the line graph paths
 *
 * ---
 *
 * ### Performance Analysis
 *
 * - **Time Complexity: O(C × N)**
 *   - C = number of categories
 *   - N = average number of data points per category
 *
 * - **Space Complexity: O(N) per category**
 *   - Path construction + intermediate drawing objects
 *
 * Note: Canvas rendering is GPU-accelerated; complexity mainly affects recomposition cost.
 */
@Composable
fun DisplayAllMonthlyLineGraphs(
    selectedCategoriesInstancesForAMonth: Pair<Map<String, List<FinancialTransactionData>>, Int> = Pair(emptyMap(), 0),
    selectedBarColor: Color = Color.Transparent
) {
    val maxValue = selectedCategoriesInstancesForAMonth.second

    // Convert max value into human-readable axis label (e.g. 120000 → "120K")
    // Used to gives a rough mathematical measured of the max value displayed on the graph
    val maxLabel = (maxValue / 100f / 1000f).roundToInt().toString()
    val textMeasurer = rememberTextMeasurer()
    //This is adding horizontal grid on the graph for better UI
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

            //This line draws the horizontal axis for the line graph
            drawLine(
                color = Color.Gray,
                start = Offset(0f, height),
                end = Offset(width, height),
                strokeWidth = 3f
            )

            /*
            * Side Note: To place views inside Compose we make use of Offset(x-coordinate: Px, y-coordinate: Px)
            * This function is very important and widely used in this class
            */

            //This line draws the horizontal axis for the line graph
            drawLine(
                color = Color.Gray,
                start = Offset(0f, 0f),
                end = Offset(0f, height),
                strokeWidth = 3f
            )

            //this loop is responsible for drawing the horizontal grid lines
            for (i in 0..gridLineCount) {
                //the spacing in between the horizontal grid lines
                val y = height - (i * height / gridLineCount)

                drawLine(
                    color = Color.LightGray.copy(alpha = 0.4f),
                    start = Offset(0f, y),
                    end = Offset(width, y),
                    strokeWidth = 1.dp.toPx()
                )
            }

            //This where we draw the highest value on the graph, drawn in the top-left corner of the graph
            // This indicates the estimation for numerical value of the dots in general
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

            //below reads all the transactions for the selected Transaction Category along with the selected month
            //from then we then map out the path based on the consecutiveness of each transaction
            selectedCategoriesInstancesForAMonth.first.entries.forEachIndexed { _, (key, _) ->
                val values = selectedCategoriesInstancesForAMonth.first[key]
                val lineGraphStroke = Stroke(
                    width = 3f
                )

                if (values != null) {
                    //X-axis spacing between data points, i.e, spacing between the dots is directly proportional to frequency of values
                    //Avoid division by zero using coerceAtLeast(1)
                    val xStep = width / (values.size - 1).coerceAtLeast(1).toFloat()
                    val path = Path()

                    values.forEachIndexed { pointIndex, data ->
                        //Normalize value between 0 and 1 based on axis max
                        //will be used to determine the dot's height on the line graph
                        val normalized = data.amount.toFloat() / calculateAxisMax(maxValue.toFloat())
                        //horizontal positioning for the dot, in terms of its index
                        val horizontalValue = pointIndex * xStep
                        //determine vertical positioning, using normalized value to remove the upperbound positioning
                        val verticalValue = height - (normalized * height)

                        //In this "if" block we are gathering all the paths from each dot to another one by one
                        if (pointIndex == 0) {
                            path.moveTo(horizontalValue, verticalValue)
                        } else {
                            path.lineTo(horizontalValue, verticalValue)
                        }

                        //This is where we draw each dot on the line graph, each dot represent the amount of each transaction
                        drawCircle(
                            color = Color.Gray,
                            radius = 6f,
                            center = Offset(horizontalValue, verticalValue)
                        )
                    }

                    //this paints the actual path we gathered while we were drawing the transaction dots on the line graphs
                    //this completed the display of the relation of transaction spent for that selected specific Transaction Category along-side the selected month
                    drawPath(
                        path = path,
                        color = selectedBarColor,
                        style = lineGraphStroke
                    )
                }
            }
        }

        //This is the horizontal axis label for the line graph to give a user context
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

/**
 * Calculates a rounded axis maximum for chart scaling.
 *
 * This ensures that Y-axis values are clean multiples of magnitude
 * (e.g. 1340 → 2000 instead of 1340 for better UI readability).
 *
 * @param value Raw maximum dataset value
 * @return Rounded axis-friendly maximum value
 *
 * ---
 *
 * ### Performance Analysis
 * - Time Complexity: O(1) — only mathematical operations
 * - Space Complexity: O(1) — constant memory usage
 */
fun calculateAxisMax(value: Float): Float {
    if (value <= 0f) return 1f

    // Determine magnitude (e.g. 10, 100, 1000)
    val magnitude = 10.0.pow(
        floor(log10(value.toDouble()))
    ).toFloat()
    // Round up value to nearest magnitude
    return ceil(value / magnitude) * magnitude
}
