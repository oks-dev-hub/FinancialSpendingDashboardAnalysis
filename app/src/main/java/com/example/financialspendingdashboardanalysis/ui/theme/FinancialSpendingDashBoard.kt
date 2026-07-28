/**
 * Financial Spending Dashboard Module
 *
 * This file contains the primary UI components for the Financial Spending Dashboard.
 * It utilizes Jetpack Compose with a focus on "Smart Recomposition" to optimize performance.
 *
 * The dashboard provides:
 * - Interactive Pie Charts for category distribution.
 * - Synchronized Bar Graphs for trend analysis.
 * - Detailed Line Graphs for historical data visualization.
 *
 * Last Updated: October 2025
 */

package com.example.financialspendingdashboardanalysis.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.financialspendingdashboardanalysis.R
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardAction
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardStates
import com.example.financialmodels.TransactionCategory
import com.example.financialspendingdashboardanalysis.viewmodel.FinancialAnalyticsDashboardViewModel
import com.example.financialspendingdashboardanalysis.viewmodel.FinancialAnalyticsDashboardViewModel.Companion.BAR_GRAPH_ON_CLICK_EVENT
import com.example.financialspendingdashboardanalysis.viewmodel.FinancialAnalyticsDashboardViewModel.Companion.PIE_SLICE_ON_CLICK_EVENT
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlin.String

/**
 * Main entry composable for the Financial Spending Dashboard screen.
 * The most important principle about this specific choice of pattern formerly known as smart recomposition.
 * As the term says it, it is the most affordable way to display UI in Kotlin Compose
 * The best this about it is that it only execute the UI that has the state change and not the entire screen.
 * This is very important because for low memory devices this is very important or else we may run too many recomposition at many instance at the same time.
 * Recomposition is more art than principle, recomposing this way not only save you on memory and thread space.
 * But help you order your UI segment, apply separation of concerns, independent dividend maintain of code, code re-usability etc.
 *
 * This function is the first part of smart recomposition, and is known as the parent composable function.
 * This is the stateful section of compose, meaning all states changes and state classes are initialized and state-changes listening happens here
 *
 *
 * This screen visualizes financial data across multiple dimensions:
 * - Pie chart (category distribution per month)
 * - Bar chart (monthly totals per category)
 * - Line graph (trend across months)
 * - Transaction list (detailed entries)
 *
 * It also manages navigation events and connects UI state
 * from [FinancialAnalyticsDashboardViewModel].
 *
 * ---
 *
 * ## Architecture Flow
 *
 * ViewModel → uiState → Composables → User Interaction → Actions → ViewModel
 *
 * ---
 *
 * ## Key Responsibilities
 *
 * - Collect UI state using lifecycle-aware flows
 * - Forward user actions to ViewModel
 * - Handle navigation events
 */
@Composable
fun FinancialSpendingDashBoard(
    navController: NavController,
    financialAnalyticsDashboardViewModel: FinancialAnalyticsDashboardViewModel
) {
    //this is where most recomposition are triggered from, here we are listening to state changes and collecting the updated state
    val uiState by financialAnalyticsDashboardViewModel.uiState.collectAsStateWithLifecycle()

    //Passing all necessary values collected from the state changes and from the view model
    //Passing data like this help us achieve the stateless part of smart recomposition
    //Not that only values, and object that are stateless are passed
    FinancialSpendingDashboardContent(
        uiState = uiState,
        totalTransactionsCount = financialAnalyticsDashboardViewModel.totalTransactionsCount,
        setSelectedPieItemInChartsState = { selectedPieItemInChartIndex, selectedCategory ->
            //This delegated the PieChart Pie Slice click action to the viewModel
            //This is triggered everytime a new Pie Slice is selected
            financialAnalyticsDashboardViewModel.onAction(
                FinancialDashboardAction.SetPieChartClickAction(selectedPieItemInChartIndex, selectedCategory)
            )
        },
        lastSixMonthsFromCurrentMonth = financialAnalyticsDashboardViewModel.lastSixMonthsFromCurrentMonth,
        onAction = { action ->
            //A very important function, this function helps us achieve the stateless part of smart recomposition
            //This is because it helps use delegate all the heavy lifting, data manipulation away from the UI to the viewModel
            //The view model check the event, begin work and when done pass output data to UI via recomposition
            //The cycle repeats, this means that we are sending command very cheap on the UI side, and we are sending the output very cheap back to the UI.
            financialAnalyticsDashboardViewModel.onAction(action)
        }
    )
}

/**
 * Main entry point for the Financial Spending Dashboard screen.
 *
 * This composable acts as the root container for the entire analytics dashboard,
 * coordinating multiple financial visualizations and user interactions.
 *
 * In respect to the smart recomposition, this class is called the sub-parent composable.
 * This is the stateless part of compose, and the most important part of smart recomposition.
 * This is because Compose, uses the uiState parameter, mainly, to determine which part of the segment UI it should execute and which one it should not.
 * Careful implementation of this class reaps optimal performance, at a very low functioning cost.
 *
 * It connects:
 * - Pie chart (category breakdown per month)
 * - Bar chart (monthly category totals)
 * - Line graph (trend over time)
 * - Transaction list (detailed records)
 *
 * It also bridges UI state from [FinancialAnalyticsDashboardViewModel]
 * and handles navigation events.
 *
 * ---
 *
 * ## UI Layout Structure
 *
 * Column (Scrollable)
 * ├── TransactionHeaderItem (Screen title)
 * ├── Monthly Summary Text (selected month)
 * ├── Description Text
 * ├── Pie Chart (inside HorizontalPager)
 * │   ├── Left navigation button
 * │   ├── Right navigation button
 * │   └── DisplayFinancialPieChart
 * ├── Bar Chart Header Text
 * ├── Bar Chart Description Text
 * ├── DisplayFinancialClickableBarGraph
 * ├── Line Graph Header Text
 * ├── Line Graph Description Text
 * ├── DisplayAllMonthlyLineGraphs
 * ├── Transaction Details Header
 * ├── Transaction Details Description
 * └── Transaction List (scrollable items)
 *
 * ---
 *
 * ## Architecture Flow
 *
 * ViewModel → uiState → UI Composables → User Actions → ViewModel → State update
 *
 * ---
 *
 * ## Key Responsibilities
 *
 * - Render full analytics dashboard UI
 * - Synchronize pager, bar chart, and pie chart interactions
 * - Handle navigation events to transaction details screen
 * - Maintain interaction source state (pie vs bar click)
 *
 * ---
 *
 * ## Performance Analysis
 *
 * - Pie chart rendering: O(N)
 * - Bar chart rendering: O(N)
 * - Line graph rendering: O(N * M)
 * - Transaction list: O(N log N) due to sorting
 *
 * Note:
 * Canvas-based rendering is GPU-accelerated; main performance cost
 * comes from recomposition and data transformations.
 */
@Composable
fun FinancialSpendingDashboardContent(
    uiState: FinancialDashboardStates,
    totalTransactionsCount: Int,
    lastSixMonthsFromCurrentMonth: List<String> = emptyList(),
    setSelectedPieItemInChartsState: (Int, TransactionCategory) -> Unit = { _, _ -> },
    onAction: (FinancialDashboardAction) -> Unit = {}
) {

    //Number of pages in pager (one per month).
    val pageCount = lastSixMonthsFromCurrentMonth.size
    //Coroutine scope tied to composable lifecycle.
    val scope = rememberCoroutineScope()
    //Determines highlight color for selected bar.
    val selectedBarColor = if (uiState.generateRandomColorsList.isEmpty()) Color.Transparent else brightenColor(color = uiState.generateRandomColorsList[uiState.selectedPieAnglePairsIndex])

    //Default starting page (latest month)
    val startPage = (pageCount - 1).coerceAtLeast(0)

    //Tracks whether interaction originated from bar or pie chart
    val onClickSelectionEvent = rememberSaveable { mutableStateOf(BAR_GRAPH_ON_CLICK_EVENT) }

    val pagerState = rememberPagerState(
        initialPage = pageCount,
        pageCount = { pageCount }
    )

    //This run initially, to instantiate all the views
    LaunchedEffect(Unit) {
        if (startPage > 0 && !uiState.hasDashBoardBeenInitializedBefore) {
            onAction(
                FinancialDashboardAction.InitializeMonthlyPieChartDashboard(startPage)
            )
        }
    }

    /**
     * Observes pager page changes in a lifecycle-safe way.
     *
     * snapshotFlow ensures:
     * - Only emits when settledPage changes
     * - Avoids intermediate scroll states
     */
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                /**
                 * Routes update based on interaction source:
                 * - BAR click → update pie chart
                 * - PIE click → update bar chart
                 */
                onAction(
                    if (onClickSelectionEvent.value == BAR_GRAPH_ON_CLICK_EVENT) {
                        FinancialDashboardAction.InitializeMonthlyPieChartDashboard(page)
                    } else {
                        FinancialDashboardAction.SetBarChartClickAction(page)
                    }
                )
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        //Create the DashBoard's Header Item, to signal which screen we are on.
        TransactionHeaderItem(
            headerItem = stringResource(R.string.spendingFinancialDashboard)
        )

        AddHorizontalPagerEffect(
            pagerState = pagerState,
            onClickSelectionEvent = onClickSelectionEvent,
            selectedMonth = uiState.selectedMonth
        )

        PagerIndicator(
            pageCount = pageCount,
            currentPage = pagerState.currentPage
        )

        val transactionsCount =  "%,d".format(totalTransactionsCount).replace(',', ' ')

        TitleAndDescriptionContainer(
            title = stringResource(R.string.totalSpendingSubHeader, lastSixMonthsFromCurrentMonth.getOrNull(pagerState.currentPage) ?: ""),
            description = stringResource(R.string.totalSpendingDescription, transactionsCount, uiState.selectedMonth)
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
        ) {
            //This is the Horizontal Pager, that is responsible for paging all the PieCharts for every month.
            //Simply put, each page represents a month, and each PieChart displayed is an indication of the total spending activity for each TransactionCategory.
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
            ) { page ->
                key(page) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .wrapContentHeight(),
                    ) {
                        //This is the reason why we had to write this class in its own file, because now we get to tell Compose to whether or not to execute this function or not.
                        //In some case where the UI state did not change this function will skipped by Compose, and it will use the cached resultant view of this function.
                        //Not only that but whole becomes a segment of UI, that has UI dimension boundary in this page.
                        DisplayFinancialPieChart(
                            modifier = Modifier.align(Alignment.Center),
                            generateRandomColorsList = uiState.generateRandomColorsList,
                            selectedPieAnglePairsIndex = uiState.selectedPieAnglePairsIndex,
                            setSelectedPieItemInChartState = { selectedPieItemInChartIndex, selectedCategory ->
                                setSelectedPieItemInChartsState(selectedPieItemInChartIndex, selectedCategory)
                            },
                            monthlyPieData = uiState.monthlyPieData
                        )
                    }
                }
            }

            //This is to add navigation icons for paging the Horizontal PieChart Pager
            //The user has the option to drag slide the view or to press on these two below icons.
            //This once only get visible if the current page index is not zero, this is the icons to page backwards, and is displayed on the left hand side.

            //This once gets displayed only if the page is less than 5, this is for paging up
        }

        val totalCountForSelectedCategory = uiState.monthlyPieData[uiState.selectedCategory]?.totalTransactionsCount ?: 0
        val countSelectedCategory =  "%,d".format(totalCountForSelectedCategory).replace(',', ' ')

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.selectedCategoryDetail, countSelectedCategory, uiState.selectedCategory.value),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.bodySmall
        )

        TitleAndDescriptionContainer(
            title = stringResource(
                R.string.totalSpendingBarGraphSubHeader,
                uiState.selectedCategory.name.first() + uiState.selectedCategory.name.substring(1).lowercase().replace("_", " "),
                uiState.selectedMonth),
            description = stringResource(
                R.string.totalSpendingBarGraphDescription,
                uiState.selectedCategory.name.first() + uiState.selectedCategory.name.substring(1).lowercase().replace("_", " ")
            )
        )

        //This is the segment of the UI for drawing the Bar Graph
        //The Bar graph is drawn for past six month for the selected Pie Slice TransactionCategory
        //Same applies for this segment UI, it will only be recreated if uiState values have changes from the previously recoeded.
        //Otherwise, this whole section will be skipped, saving us computing time and resource.
        DisplayFinancialClickableBarGraph(
            lastSixMonthsFromCurrentMonth = lastSixMonthsFromCurrentMonth,
            selectedCategoriesForAllMonth = uiState.fetchSelectedCategoriesForAllMonth,
            selectedBarGraphIndex = uiState.selectedBarGraphIndex,
            barColor = uiState.generateRandomColorsList.getOrNull(uiState.selectedPieAnglePairsIndex)
                ?: Color.Transparent,
            selectedBarColor = selectedBarColor,
            onAction = { selectedBarGraphIndex ->
                scope.launch {
                    onClickSelectionEvent.value = PIE_SLICE_ON_CLICK_EVENT
                    yield()
                    pagerState.animateScrollToPage(selectedBarGraphIndex)
                }
            }
        )

        TitleAndDescriptionContainer(
            title = stringResource(
                R.string.lineGraphSubHeader,
                uiState.selectedCategory.value,
            ),
            description = stringResource(
                R.string.lineGraphDescription,
                uiState.selectedMonth
            )
        )

        DisplayAllMonthlyLineGraphs(
            monthlyLineGraphData = uiState.monthlyLineGraphData,
            selectedBarColor = selectedBarColor
        )
    }
}

/**
 * A container for displaying a bold title and a description text.
 * Used consistently across the dashboard to provide context for each visualization.
 *
 * @param title The header text to display.
 * @param description The supporting description text.
 */
@Composable
private fun TitleAndDescriptionContainer(
    title: String,
    description: String
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(horizontal = 16.dp),
        text = title,
        fontWeight = FontWeight.Bold,
        style = MaterialTheme.typography.titleMedium
    )

    Text(
        modifier = Modifier
            .padding(horizontal = 16.dp),
        text = description,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Adds side-to-side navigation buttons and a month/year indicator for the pager.
 * This component allows users to navigate through different months of financial data.
 *
 * @param pagerState The state of the [PagerState] used by the horizontal pager.
 * @param onClickSelectionEvent Tracks the source of the navigation to prevent conflicting updates between chart clicks and pager swipes.
 * @param selectedMonth The name of the month currently being viewed.
 */
@Composable
private fun AddHorizontalPagerEffect(
    pagerState: PagerState,
    onClickSelectionEvent: MutableState<String>,
    selectedMonth: String
) {
    val scope = rememberCoroutineScope()

    Box(modifier = Modifier.fillMaxWidth()) {
        if (pagerState.currentPage > 0) {
            FilledTonalIconButton(
                modifier = Modifier
                    .padding(start = 10.dp)
                    .align(Alignment.CenterStart)
                    .size(24.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                onClick = {
                    /**
                     * Navigates to previous month in pager.
                     *
                     * yield() ensures coroutine cooperatively suspends
                     * before animation starts (prevents UI jank).
                     */
                    scope.launch {
                        onClickSelectionEvent.value = BAR_GRAPH_ON_CLICK_EVENT
                        //this function waits for the state change to complete before it continue execution
                        yield()
                        pagerState.animateScrollToPage(pagerState.currentPage - 1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = null
                )
            }
        }

        Text(
            modifier = Modifier.align(Alignment.Center),
            text = "$selectedMonth 2026",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold
        )

        //This once gets displayed only if the page is less than 5, this is for paging up
        if (pagerState.currentPage < 5) {
            FilledTonalIconButton(
                modifier = Modifier
                    .padding(end = 10.dp)
                    .align(Alignment.CenterEnd)
                    .size(24.dp),
                colors = IconButtonDefaults.filledTonalIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerHighest
                ),
                onClick = {
                    /**
                     * Navigates to next month in pager.
                     */
                    scope.launch {
                        onClickSelectionEvent.value = BAR_GRAPH_ON_CLICK_EVENT
                        //this function waits for the state change to complete before it continue execution
                        yield()
                        pagerState.animateScrollToPage(pagerState.currentPage + 1)
                    }
                }
            ) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = null
                )
            }
        }
    }
}

/**
 * A visual indicator for the horizontal pager, showing the current page relative to total pages.
 * Displays a row of dots where the active page is highlighted with the primary theme color.
 *
 * @param pageCount The total number of pages in the pager.
 * @param currentPage The index of the currently active page.
 */
@Composable
fun PagerIndicator(
    pageCount: Int,
    currentPage: Int,
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
    ) {
        Row(
            modifier = Modifier.align(Alignment.Center),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(pageCount) { page ->
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .then(
                            if (page == currentPage) {
                                Modifier.background(MaterialTheme.colorScheme.primary)
                            } else {
                                Modifier.border(
                                    width = 1.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                            }
                        )
                )
            }
        }
    }
}

/**
 * Brightens a color by increasing its HSV value component.
 *
 * Used to highlight selected chart elements dynamically.
 *
 * ---
 *
 * ## Algorithm
 * - Convert Color → HSV
 * - Increase brightness (V channel)
 * - Clamp to max 1.0
 * - Convert back to Color
 *
 * ---
 *
 * ## Complexity
 * - Time: O(1)
 * - Space: O(1)
 */
fun brightenColor(color: Color, factor: Float = 1.3f): Color {
    val brightenHSV = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), brightenHSV)
    brightenHSV[2] = (brightenHSV[2] * factor).coerceAtMost(1f) // brightness
    return Color(android.graphics.Color.HSVToColor(brightenHSV))
}