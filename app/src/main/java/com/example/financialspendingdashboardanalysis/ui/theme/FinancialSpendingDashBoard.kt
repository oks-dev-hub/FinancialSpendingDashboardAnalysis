package com.example.financialspendingdashboardanalysis.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.financialspendingdashboardanalysis.R
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardAction
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardEvents
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardStates
import com.example.financialspendingdashboardanalysis.model.FinancialTransactionData
import com.example.financialmodels.TransactionCategory
import com.example.financialspendingdashboardanalysis.navGraph.ViewFullTransactionDetailsRoute
import com.example.financialspendingdashboardanalysis.viewModel.FinancialAnalyticsDashboardViewModel
import com.example.financialspendingdashboardanalysis.viewModel.FinancialAnalyticsDashboardViewModel.Companion.BAR_GRAPH_ON_CLICK_EVENT
import com.example.financialspendingdashboardanalysis.viewModel.FinancialAnalyticsDashboardViewModel.Companion.PIE_SLICE_ON_CLICK_EVENT
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

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

    //For one time event navigation
    //Unit means that this will only execute once
    LaunchedEffect(Unit) {
        financialAnalyticsDashboardViewModel.sharedEvents.collect { event ->
            when (event) {
                is FinancialDashboardEvents.NavigateToViewTransactionDetails -> navController.navigate(ViewFullTransactionDetailsRoute)
                else -> {}
            }
        }
    }
    //Passing all necessary values collected from the state changes and from the view model
    //Passing data like this help us achieve the stateless part of smart recomposition
    //Not that only values, and object that are stateless are passed
    FinancialSpendingDashboardContent(
        uiState = uiState,
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
        },
        sendEvent = { financialTransactionData ->
            //This is all the way at the bottom of the Dashboard page, under the line graph, where we display a list of all the transaction for the selected TransactionCategory for that selected month.
            //Not this is the onItem click function, where it navigates you to the ViewTransaction Details screen
            financialAnalyticsDashboardViewModel.sendEvent(
                FinancialDashboardEvents.NavigateToViewTransactionDetails(
                    financialTransactionData
                ),
                financialTransactionData
            )
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
    lastSixMonthsFromCurrentMonth: List<String> = emptyList(),
    setSelectedPieItemInChartsState: (Int, TransactionCategory) -> Unit = { _, _ -> },
    onAction: (FinancialDashboardAction) -> Unit = {},
    sendEvent: (FinancialTransactionData) -> Unit = {}
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

    ) {
        //Create the DashBoard's Header Item, to signal which screen we are on.
        TransactionHeaderItem(
            headerItem = stringResource(R.string.spendingFinancialDashboard)
        )

        //This is the PieChart SubHeader
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(R.string.totalSpendingSubHeader, lastSixMonthsFromCurrentMonth.getOrNull(pagerState.currentPage) ?: ""),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        )

        //PieChart brief description
        Text(
            modifier = Modifier
                .padding(horizontal = 16.dp),
            text = stringResource(R.string.totalSpendingDescription),

        )

        Spacer(Modifier.height(16.dp))

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
            if (pagerState.currentPage > 0) {
                IconButton(
                    modifier = Modifier
                        .padding(start = 10.dp)
                        .align(Alignment.CenterStart)
                        .size(40.dp)
                        .border(
                            width = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .background(
                            color = Color.LightGray.copy(alpha = 0.08f),
                            shape = CircleShape
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

            //This once gets displayed only if the page is less than 5, this is for paging up
            if (pagerState.currentPage < 5) {
                IconButton(
                    modifier = Modifier
                        .padding(end = 10.dp)
                        .align(Alignment.CenterEnd)
                        .border(
                            width = 1.dp,
                            color = Color.LightGray.copy(alpha = 0.3f),
                            shape = CircleShape
                        )
                        .background(
                            color = Color.LightGray.copy(alpha = 0.08f),
                            shape = CircleShape
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

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(
                R.string.totalSpendingBarGraphSubHeader,
                uiState.selectedCategory.name.first() + uiState.selectedCategory.name.substring(1).lowercase().replace("_", " "),
                uiState.selectedMonth),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        )

        Text(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 25.dp
                ),
            text = stringResource(
                R.string.totalSpendingBarGraphDescription,
                uiState.selectedCategory.name.first() + uiState.selectedCategory.name.substring(1).lowercase().replace("_", " ")
            ),
        )

        //This is the segment of the UI for drawing the Bar Graph
        //The Bar graph is drawn for past six month for the selected Pie Slice TransactionCategory
        //Same applies for this segment UI, it will only be recreated if uiState values have changes from the previously recoeded.
        //Otherwise, this whole section will be skipped, saving us computing time and resource.
        DisplayFinancialClickableBarGraph(
            lastSixMonthsFromCurrentMonth = lastSixMonthsFromCurrentMonth,
            selectedCategoriesForAllMonth = uiState.fetchSelectedCategoriesForAllMonth,
            selectedBarGraphIndex = uiState.selectedBarGraphIndex,
            barColor = uiState.generateRandomColorsList.getOrNull(uiState.selectedPieAnglePairsIndex) ?: Color.Transparent,
            selectedBarColor = selectedBarColor,
            onAction = { selectedBarGraphIndex ->
                scope.launch {
                    onClickSelectionEvent.value = PIE_SLICE_ON_CLICK_EVENT
                    yield()
                    pagerState.animateScrollToPage(selectedBarGraphIndex)
                }
            }
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 16.dp, top = 25.dp, bottom = 16.dp, end = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(
                R.string.lineGraphSubHeader,
                uiState.selectedCategory.name.first() + uiState.selectedCategory.name.substring(1).lowercase().replace("_", " "),
                ),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        )

        Text(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 25.dp
                ),
            text = stringResource(
                R.string.lineGraphDescription,
                uiState.selectedCategory.name.first() + uiState.selectedCategory.name.substring(1).lowercase().replace("_", " "),
                uiState.selectedMonth
            ),
        )

        Text(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .padding(start = 16.dp, top = 25.dp, bottom = 16.dp, end = 16.dp)
                .align(Alignment.CenterHorizontally),
            text = stringResource(
                R.string.transactionDetailsSubHeader,
                uiState.selectedCategory.name.first() + uiState.selectedCategory.name.substring(1).lowercase().replace("_", " "),
            ),
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
            )
        )

        Text(
            modifier = Modifier
                .padding(
                    start = 16.dp,
                    end = 16.dp,
                    bottom = 25.dp
                ),
            text = stringResource(
                R.string.transactionDetailsDescription,
                uiState.selectedCategory.name.first() + uiState.selectedCategory.name.substring(1).lowercase().replace("_", " "),
                uiState.selectedMonth
            ),
        )

        /*//This one displays a list of all transaction items, one by one, in order by paymentDate.
        //Each item is clickable and displays the full transaction details
        DisplayListOfSelectedMonthTransactionsDetails(
            selectedCategoriesInstancesForAllMonth = uiState.fetchSelectedCxategoriesInstancesForAllMonths.first[uiState.selectedMonth],
            sendEvent = sendEvent,
            onClickSelectionEvent = { onClickSelectionEvent.value = BAR_GRAPH_ON_CLICK_EVENT }
        )*/
    }
}

/**
 * Displays list of transactions for selected month.
 *
 * Sorts transactions chronologically and renders each item.
 *
 * ---
 *
 * ## Complexity
 * - Time: O(N log N) due to sorting
 * - Space: O(1) additional (in-place iteration)
 */
@Composable
private fun DisplayListOfSelectedMonthTransactionsDetails(
   selectedCategoriesInstancesForAllMonth: List<FinancialTransactionData>?,
   sendEvent: (FinancialTransactionData) -> Unit,
   onClickSelectionEvent: () -> Unit
) {
    if (selectedCategoriesInstancesForAllMonth == null) return

    selectedCategoriesInstancesForAllMonth.sortedBy { it.paymentDate.split(" ").first() }

    //This iterated to display all transaction formatted in a list item, where you can view the Transaction full Details
    //This list compromises all transactions of the TransactionCategory for a specific month
    selectedCategoriesInstancesForAllMonth.forEachIndexed { index, selectedCategoryInstance ->
        TransactionDetailsListItem(
            isStartIndex = index == 0,
            isLastIndex = index == selectedCategoriesInstancesForAllMonth.size - 1,
            transactionReceiptMeaning = selectedCategoryInstance.fundAccount,
            transactionDateTimeStamp = selectedCategoryInstance.paymentDate,
            amount = convertValueToAMount(selectedCategoryInstance.amount),
            transactionOnClick = {
                onClickSelectionEvent()
                sendEvent(selectedCategoryInstance)
            }
        )

        if (index != selectedCategoriesInstancesForAllMonth.size -1) {
            //This is to separate neatly the list items
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )
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