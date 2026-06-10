package com.example.financialspendingdashboardanalysis.ui.theme

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.financialspendingdashboardanalysis.R
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardAction
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardEvents
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardStates
import com.example.financialspendingdashboardanalysis.model.FinancialTransactionData
import com.example.financialspendingdashboardanalysis.model.TransactionCategory
import com.example.financialspendingdashboardanalysis.navGraph.ViewFullTransactionDetailsRoute
import com.example.financialspendingdashboardanalysis.viewModel.FinancialAnalyticsDashboardViewModel
import com.example.financialspendingdashboardanalysis.viewModel.FinancialAnalyticsDashboardViewModel.Companion.BAR_GRAPH_ON_CLICK_EVENT
import com.example.financialspendingdashboardanalysis.viewModel.FinancialAnalyticsDashboardViewModel.Companion.PIE_SLICE_ON_CLICK_EVENT
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@Composable
fun FinancialSpendingDashBoard(
    navController: NavController,
    financialAnalyticsDashboardViewModel: FinancialAnalyticsDashboardViewModel
) {
    val uiState by financialAnalyticsDashboardViewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        financialAnalyticsDashboardViewModel.sharedEvents.collect { event ->
            when (event) {
                is FinancialDashboardEvents.NavigateToViewTransactionDetails -> navController.navigate(ViewFullTransactionDetailsRoute)
                else -> {}
            }
        }
    }

    FinancialSpendingDashboardContent(
        uiState = uiState,
        setSelectedPieItemInChartsState = { selectedPieItemInChartIndex, selectedCategory ->
            financialAnalyticsDashboardViewModel.onAction(
                FinancialDashboardAction.SetPieChartClickAction(selectedPieItemInChartIndex, selectedCategory)
            )
        },
        lastSixMonthsFromCurrentMonth = financialAnalyticsDashboardViewModel.lastSixMonthsFromCurrentMonth,
        onAction = { action -> financialAnalyticsDashboardViewModel.onAction(action) },
        sendEvent = { financialTransactionData ->
            financialAnalyticsDashboardViewModel.sendEvent(
                FinancialDashboardEvents.NavigateToViewTransactionDetails(
                    financialTransactionData
                ),
                financialTransactionData
            )
        }
    )
}

@Composable
fun FinancialSpendingDashboardContent(
    uiState: FinancialDashboardStates,
    lastSixMonthsFromCurrentMonth: List<String> = emptyList(),
    setSelectedPieItemInChartsState: (Int, TransactionCategory) -> Unit = { _, _ -> },
    onAction: (FinancialDashboardAction) -> Unit = {},
    sendEvent: (FinancialTransactionData) -> Unit = {}
) {
    val pageCount = lastSixMonthsFromCurrentMonth.size
    val scope = rememberCoroutineScope()
    val selectedBarColor = if (uiState.generateRandomColorsList.isEmpty()) Color.Transparent else brightenColor(color = uiState.generateRandomColorsList[uiState.selectedPieAnglePairsIndex])

    val startPage = (pageCount - 1).coerceAtLeast(0)
    val onClickSelectionEvent = rememberSaveable { mutableStateOf(BAR_GRAPH_ON_CLICK_EVENT) }

    val pagerState = rememberPagerState(
        initialPage = pageCount,
        pageCount = { pageCount }
    )

    LaunchedEffect(Unit) {
        if (startPage > 0 && !uiState.hasDashBoardBeenInitializedBefore) {
            onAction(
                FinancialDashboardAction.InitializeMonthlyPieChartDashboard(startPage)
            )
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
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
        TransactionHeaderItem(
            headerItem = stringResource(R.string.spendingFinancialDashboard)
        )

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
                        scope.launch {
                            onClickSelectionEvent.value = BAR_GRAPH_ON_CLICK_EVENT
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
                        scope.launch {
                            onClickSelectionEvent.value = BAR_GRAPH_ON_CLICK_EVENT
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

        DisplayFinancialClickableBarGraph(
            lastSixMonthsFromCurrentMonth = lastSixMonthsFromCurrentMonth,
            selectedCategoriesForAllMonth = uiState.fetchSelectedCategoriesForAllMonth,
            selectedBarGraphIndex = uiState.selectedBarGraphIndex,
            barColor = uiState.generateRandomColorsList.getOrNull(uiState.selectedPieAnglePairsIndex) ?: Color.Transparent,
            selectedBarColor = selectedBarColor,
            onAction = { selectedBarGraphIndex ->
                val stableSelectedBarGraphIndex = selectedBarGraphIndex
                scope.launch {
                    onClickSelectionEvent.value = PIE_SLICE_ON_CLICK_EVENT
                    yield()
                    pagerState.animateScrollToPage(stableSelectedBarGraphIndex)
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

        DisplayAllMonthlyLineGraphs(
            selectedCategoriesInstancesForAMonth = uiState.fetchSelectedCategoriesInstancesForAllMonths,
            selectedBarColor = selectedBarColor
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

        DisplayListOfSelectedMonthTransactionsDetails(
            selectedCategoriesInstancesForAllMonth = uiState.fetchSelectedCategoriesInstancesForAllMonths.first[uiState.selectedMonth],
            sendEvent = sendEvent,
            onClickSelectionEvent = { onClickSelectionEvent.value = BAR_GRAPH_ON_CLICK_EVENT }
        )
    }
}

@Composable
private fun DisplayListOfSelectedMonthTransactionsDetails(
   selectedCategoriesInstancesForAllMonth: List<FinancialTransactionData>?,
   sendEvent: (FinancialTransactionData) -> Unit,
   onClickSelectionEvent: () -> Unit
) {
    if (selectedCategoriesInstancesForAllMonth == null) return

    selectedCategoriesInstancesForAllMonth.sortedBy { it.paymentDate.split(" ").first() }

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
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                color = Color.LightGray,
                thickness = 1.dp
            )
        }
    }
}


fun brightenColor(color: Color, factor: Float = 1.3f): Color {
    val brightenHSV = FloatArray(3)
    android.graphics.Color.colorToHSV(color.toArgb(), brightenHSV)
    brightenHSV[2] = (brightenHSV[2] * factor).coerceAtMost(1f) // brightness
    return Color(android.graphics.Color.HSVToColor(brightenHSV))
}