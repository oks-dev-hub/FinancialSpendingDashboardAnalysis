package com.example.financialspendingdashboardanalysis.ui.theme

import android.graphics.Paint
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import com.example.financialmodels.TransactionCategory
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Displays an interactive financial pie chart representing spending distribution per category.
 * NB: This is a clickable pie chart, that shows a monthly total spending distribution in terms of percentages.
 * At the very top of this function is a display of Legend of the Pie Chart associated with the total summation amount of each Transaction Category for that month
 * Each spending transaction is categorized into a TransactionCategory, the Pie Chart displays the total monthly expenditure for all categories the user has spent on for that specific month.
 * The user can track what exactly did they spend on and by how much.
 * Each pie is clickable, a clicked pie is demonstrated by the clicked piece to be slightly our of the pie and border black all around.
 * The mechanics is that when a user taps on an unselected pie, then the bar, get recreated entire now to display contents pertaining to the new clicked TransactionCategory still on the same mont though.
 * So even if the bar graph get recreated with new data input for the total TransactionCategory for the last past six month, the selected bar remain the same, so we still remain in that selected bar but for a new TransactionCategory total summation amounts.
 * Moreover, the line graph will also get recreated to display all transaction point relation for the new TransactionCategory still for the same month, since the mont did not change.
 *
 *
 * This composable renders:
 * - Pie chart slices proportional to category totals
 * - Percentage labels inside slices
 * - Click/tap interaction for selecting slices
 * - Highlighted "exploded" selected slice
 *
 * The chart uses Canvas for custom rendering and manual angle calculations.
 *
 * @param modifier Layout modifier for external positioning
 * @param generateRandomColorsList List of colors mapped to pie slices
 * @param selectedPieAnglePairsIndex Currently selected slice index
 * @param setSelectedPieItemInChartState Callback when a slice is tapped
 * @param monthlyPieData Map of transaction category → aggregated values
 *
 * ---
 *
 * ## Performance Analysis
 *
 * - **Time Complexity: O(N)**
 *   - N = number of categories (slices)
 *   - Each recomposition iterates through all slices once
 *
 * - **Space Complexity: O(N)**
 *   - Stores angle constraints and category name mappings
 *
 * Note:
 * Canvas rendering is GPU-accelerated; main cost comes from recomposition
 * and angle/geometry calculations, not drawing itself.
 */
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
    var overallAmountForAllCategory = 0L

    LaunchedEffect(Unit) {
        animationPlayed.value = true
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        //This display the Legend of the Pie Chart each entry associated with the total summation amount of all transaction for that TransactionCategory
        PieChartLabelIndexDisplayed(
            generateRandomColorsList = generateRandomColorsList,
            getMonthlyPieData = monthlyPieData.entries.associate { monthlyPieDataEntry ->
                //get the overall summation amount for all categories' total summation amount
                overallAmountForAllCategory += monthlyPieDataEntry.value.totalSummationOfAmountValuesInCategory
                categoryNames.add(monthlyPieDataEntry.key.name)
                //Change the map relation, to be the Transaction Category mapping to the total summation amount of all transaction for that TransactionCategory
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
                        //Here below pie chart tap detection logic, this the lowest-level in Kotlin to determine click/tap events

                        detectTapGestures { tapOffset ->
                            //This "if" block dismisses when the data is not yet ready to be displayed
                            //Prevent crashes and unnecessary code executions
                            if (diameter == 0.toFloat() && pieCenter == Offset.Zero && pieSliceAngleConstraints.isEmpty()) {
                                return@detectTapGestures
                            }

                            //determine the pie's horizontal distance
                            val horizontalDistance = tapOffset.x - pieCenter.x
                            //determine the pie's vertical distance
                            val verticalDistance = tapOffset.y - pieCenter.y

                            //The idea is that we need to calculate the distance from where we tapped in respect to the center of the pie chart.
                            val distance = sqrt(horizontalDistance * horizontalDistance + verticalDistance * verticalDistance)
                            val radius = diameter / 2f

                            //If the distance is greater than the radius then the click is out of bounds
                            if (distance > radius) {
                                return@detectTapGestures
                            }

                            //If not, then we can find the angle from the using the center and the distance to check if is falls within which pie slice
                            val rawAngle = Math.toDegrees(
                                atan2(-verticalDistance, horizontalDistance).toDouble()
                            ).toFloat()

                            val pieAngle = (360f - rawAngle + 360f) % 360f

                            //Here we are checking from the derived angle to where it fits within angle make-up of the Pie Chart
                            pieSliceAngleConstraints.forEachIndexed { index, pieSliceAngleConstraint ->
                                if (pieAngle >= pieSliceAngleConstraint.first && pieAngle < pieSliceAngleConstraint.first + pieSliceAngleConstraint.second) {
                                    val transactionCategory = TransactionCategory.entries.find { it.name.equals(categoryNames[index], ignoreCase = true) }
                                    //Once we found a match we then send an event to be processed in the vieeModel and once that is done the viewModel sends the results asd data to be displayed once more
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
                    //this is the angle calculated for each category in relation to the overall summation amount of all TransactionCategories
                    sliceAngle =
                        ((entry.totalSummationOfAmountValuesInCategory.toFloat() / overallAmountForAllCategory.toFloat()) * 360f)
                    //Calculated the percentage of the TransactionCategory in relation to the overall TransactionCategories
                    val percentage =
                        ((entry.totalSummationOfAmountValuesInCategory.toFloat() / overallAmountForAllCategory.toFloat()) * 100)

                    //Convert angle to Radians for simplicity and ease of calculation, but we could use degrees, it's like cm and mm.
                    val angleInRadian =
                        Math.toRadians((startAngle + (sliceAngle / 2.toFloat())).toDouble())
                    //This calculated the x-axis value for the pie slice line drawn for separating each pie from another
                    val horizontalTranslation =
                        pieCenter.x + ((diameter / 4.toFloat()) * cos(angleInRadian)).toFloat()
                    //Calculated the y-axis value for the same as above ^
                    val verticalTranslation =
                        pieCenter.y + ((diameter / 4.toFloat()) * sin(angleInRadian)).toFloat()

                    scale(
                        scaleX = 1f,
                        scaleY = 1f
                    ) {
                        //This is the case where we are drawing the clicked pie slice instance
                        //After we have identified the clicked position we have mapped the slices by index, therefore we can simply filter by index
                        if (index == selectedPieAnglePairsIndex) {
                            val middleAngle = startAngle + (sliceAngle / 2f)
                            val radians = Math.toRadians(middleAngle.toDouble())
                            val explodeDistance = 30f

                            //Here in these two lines, we are trying to shift the position of the slice a bit more out of the pie chart
                            //We are generally offsetting this pie instance out of the pie
                            //So here we are generic the calculation for the coordinates
                            val offsetX = cos(radians).toFloat() * explodeDistance
                            val offsetY = sin(radians).toFloat() * explodeDistance

                            val pieCenterX = offsetX + diameter / 2f
                            val pieCenterY = offsetY + diameter / 2f
                            val textRadius = diameter * 0.25f

                            //Final coordinates calculation for the slight offsetting out of the selected pie slice
                            val textX = pieCenterX + cos(radians).toFloat() * textRadius
                            val textY = pieCenterY + sin(radians).toFloat() * textRadius

                            //Here we are drawing the pie slice with above calculated offsetting coordinates
                            //Not that startAngle always begins where the last drawn pie slice was last drawn
                            //In essence, startAngle is the summation of all angle of the pie slice that currently drawn and is always summed up to 360 degrees.
                            drawArc(
                                color = generateRandomColorsList[index],
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(offsetX, offsetY),
                                size = Size(diameter, diameter)
                            )

                            //Here we draw a black borderline for the around the selected pie slice, to show that it is selected
                            drawArc(
                                color = Color.Black,
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                topLeft = Offset(offsetX, offsetY),
                                size = Size(diameter, diameter),
                                style = Stroke(width = 4f)
                            )

                            //Here we are adding the percentage in the center of the selected pie slice
                            drawContext.canvas.nativeCanvas.drawText(
                                "${percentage.toInt()}%",
                                textX,
                                textY - textHeightOffset,
                                paint
                            )
                        } else {
                            //draw all unselected pie slice at the center of the pie chart
                            // The sliceAngle is the angle of the TransactionCategory calculated of as the ratio of the total summation amount of the TransactionCategory for that specific month by the overall amount of all TransactionCategory.
                           // The startAngle is the angle where the last sliceAngle was drawn, it is the summation of all sliceAngle.
                            drawArc(
                                color = generateRandomColorsList[index],
                                startAngle = startAngle,
                                sweepAngle = sliceAngle,
                                useCenter = true,
                                size = Size(diameter, diameter)
                            )

                            //This is where we draw the percentage for unselected Pie Slices, at hte center of the pie slice.
                            drawContext.canvas.nativeCanvas.drawText(
                                "${percentage.toInt()}%",
                                horizontalTranslation,
                                verticalTranslation - textHeightOffset,
                                paint
                            )
                        }

                        //Here we are populating the angle bounds of each pie slice in relation to the pie chart
                        //Notice that we are using index to abstract these pairs
                        //This is used when checking which pie slice is clicked
                        if (pieSliceAngleConstraints.size < monthlyPieData.size) {
                            pieSliceAngleConstraints.add(Pair(startAngle, sliceAngle))
                        }

                        //As mentioned startAngle is the summation of all currently added sliceAngle
                        if (startAngle < 360f) {
                            startAngle += sliceAngle
                        }
                    }
                }
            }
        }
    }
}

/**
 * Displays legend mapping pie categories to colors and total summation amount of all transaction within the respective TransactionCategories for the associated month of display.
 *
 * Time Complexity: O(N)
 * Space Complexity: O(N)
 */
@Composable
fun PieChartLabelIndexDisplayed(
    generateRandomColorsList: List<Color>,
    getMonthlyPieData: Map<String, Long>
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

/**
 * Formats raw integer cents into human-readable currency string.
 *
 * Time Complexity: O(1)
 * Space Complexity: O(1)
 */
fun convertValueToAMount(inputAmount: Long): String {
    val formatter = java.text.DecimalFormat("#,##0.00").apply {
        decimalFormatSymbols = decimalFormatSymbols.apply {
            groupingSeparator = ' '
        }
    }

    return formatter.format(inputAmount / 100.0)
}

/**
 * Renders a single row item in the pie chart legend header.
 *
 * This composable acts as a wrapper that formats and displays:
 * - A colored indicator
 * - A category title
 * - An optional total amount value
 *
 * It delegates actual rendering to [CreatePieChartDescription].
 *
 * ## Performance Analysis
 *
 * - Time Complexity: O(1)
 *   - Only renders a single UI row without iteration
 *
 * - Space Complexity: O(1)
 *   - No collections or state allocations
 *
 * Note:
 * This composable is lightweight and fully UI-bound.
 */
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


/**
 * Displays a single pie chart legend entry with:
 *
 * - A colored square indicator representing a category
 * - A formatted category name
 * - An optional total amount value (if provided)
 **
 * ## Behavior
 * - If [color] or [pieTitle] is null → function exits early (no UI rendered)
 * - Formats category name to capitalize first letter
 * - Displays:
 *   - "CategoryName" OR
 *   - "CategoryName R123.45" (if amount exists)
 *
 * ---
 *
 * ## UI Layout
 * Row:
 * ├── Colored Box (legend indicator)
 * └── Text label (category + optional amount)
 *
 * ---
 *
 * ## Performance Analysis
 *
 * - Time Complexity: O(1)
 *   - Single row render with constant-time string operations
 *
 * - Space Complexity: O(1)
 *   - No allocations beyond local formatting variables
 *
 * Note:
 * This composable is highly efficient and safe for frequent recomposition.
 */
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
        //Formatting the value to sentence Capitals
        val pieTitleFormatted = pieTitle.toCharArray()[0] + pieTitle.substring(1).lowercase()
        Text(text = if (totalAmount.isNullOrEmpty()) pieTitle else "$pieTitleFormatted $totalAmount")
    }
}