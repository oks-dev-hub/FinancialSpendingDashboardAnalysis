package com.example.financialspendingdashboardanalysis.viewmodel

import android.app.Application
import android.util.Log
import com.example.financialspendingdashboardanalysis.FinancialTransactionRepository
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardAction
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardEvents
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardStates
import com.example.financialspendingdashboardanalysis.model.FinancialTransactionData
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import com.example.financialmodels.TransactionCategory
import com.example.financialspendingdashboardanalysis.model.FiveDayAverageValues
import com.example.financialspendingdashboardanalysis.model.YearlyMonths
import com.example.financialspendingdashboardanalysis.room.FinancialTransactionDatabaseProvider
import com.example.financialspendingdashboardanalysis.room.FinancialTransactionsDao
import com.example.financialspendingdashboardanalysis.room.FinancialTransactionsDatabase
import com.example.financialspendingdashboardanalysis.ui.theme.FinancialTransactionApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.collections.set
import kotlin.random.Random

/**
 * ViewModel responsible for managing all business logic and state
 * associated with the Financial Spending Dashboard.
 *
 * This class coordinates data fetching from the [FinancialTransactionRepository],
 * manages UI state through [FinancialDashboardStates], and processes user actions
 * via [FinancialDashboardAction].
 *
 * It maintains several internal caches for performance:
 * - [globalPieChartInfoMap]: Monthly breakdowns per category.
 * - [monthlyPieChartData]: Aggregated data for the currently selected month.
 * - [monthlyBarGraphData]: Six-month trends for a specific category.
 */
class FinancialAnalyticsDashboardViewModel(
    private val application: Application,
    private val financialTransactionRepository: FinancialTransactionRepository
) : AndroidViewModel(application) {
    /**
     * Internal cache mapping month indices to their respective list of [PieChartInfo].
     */
    val globalPieChartInfoMap: MutableMap<Int, List<PieChartInfo>> = mutableMapOf()

    /**
     * Aggregated pie chart data for the currently selected month, indexed by category.
     */
    var monthlyPieChartData: MutableMap<TransactionCategory, PieChartInfo> = mutableMapOf()

    /**
     * Aggregated bar graph data representing trends for a specific category across months.
     */
    var monthlyBarGraphData: MutableMap<Int, PieChartInfo> = mutableMapOf()

    /**
     * The total number of transactions processed for the current context.
     */
    var totalTransactionsCount: Int = 0

    /**
     * The name of the current calendar month.
     */
    var currentMonth: String = ""

    /**
     * The name of the month currently selected in the UI.
     */
    var selectedMonth: String = ""

    /**
     * The index of the selected month relative to the data set.
     */
    var selectedMonthIndex: Int = 0

    /**
     * The currently selected transaction category across the entire dashboard.
     */
    var globalSelectedCategory: TransactionCategory = TransactionCategory.UNKNOWN

    /**
     * A list of all months in a year.
     */
    val months: List<YearlyMonths> = YearlyMonths.entries

    /**
     * A list of the last six month names, ending with the current month.
     */
    var lastSixMonthsFromCurrentMonth: List<String> = emptyList()

    /**
     * A list of generated colors for chart segments.
     */
    val generateRandomColors: MutableList<Color> = mutableListOf()

    /**
     * Data object for the currently selected single transaction.
     */
    var selectedFinancialTransactionData: FinancialTransactionData = FinancialTransactionData()

    /**
     * Raw list of pie chart data entities fetched from the repository.
     */
    var fetchedFinancialTransactionsEntity: List<PieChartInfo> = emptyList()

    /**
     * List of five-day average values for line graph visualization.
     */
    var fetchedAverageMonthlySummation: List<FiveDayAverageValues> = emptyList()

    private val _uiState = MutableStateFlow(FinancialDashboardStates())

    /**
     * UI state flow consumed by the Compose layer.
     */
    val uiState = _uiState.asStateFlow()

    private val _sharedEvents = Channel<FinancialDashboardEvents>(Channel.BUFFERED)

    init {
        currentMonth = SimpleDateFormat(
            "MMMM",
            Locale.getDefault()
        ).format(Calendar.getInstance().time).trim()

        lastSixMonthsFromCurrentMonth = getPreviousValues(count = 6).reversed()
    }

    companion object {
        const val BAR_GRAPH_ON_CLICK_EVENT: String = "BarGraphOnClickEvent"
        const val PIE_SLICE_ON_CLICK_EVENT: String = "PieSliceOnClickEvent"


        /**
         * Factory for creating [FinancialAnalyticsDashboardViewModel] instances.
         */
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[APPLICATION_KEY] as FinancialTransactionApplication

                FinancialAnalyticsDashboardViewModel(
                    application = application,
                    financialTransactionRepository = application.financialTransactionRepository
                )
            }
        }
    }

    /**
     * Processes incoming user or system actions and updates the UI state accordingly.
     *
     * @param action The [FinancialDashboardAction] to be performed.
     */
    fun onAction(action: FinancialDashboardAction) {
        when (action) {
            is FinancialDashboardAction.InitializeMonthlyPieChartDashboard -> {
                if (action.pageIndex == -1) return

                selectedMonth = lastSixMonthsFromCurrentMonth[action.pageIndex]
                getMonthlyPieData(action.pageIndex) { monthlyPieChartDataObject ->
                    var transactionCategory: TransactionCategory = TransactionCategory.UNKNOWN

                    monthlyPieChartDataObject.entries.forEachIndexed { index, (transactionData, _) ->
                        if (index == 0) {
                            transactionCategory = transactionData
                            return@forEachIndexed
                        }
                    }

                    _uiState.update { currentState ->
                        globalSelectedCategory = transactionCategory
                        currentState.copy(
                            generateRandomColorsList = generateRandomColorsList(monthlyPieChartData.size),
                            selectedPieAnglePairsIndex = 0,
                            monthlyPieData = monthlyPieChartDataObject,
                            selectedCategory = transactionCategory,
                            selectedMonth = selectedMonth,
                            hasDashBoardBeenInitializedBefore = true,
                            selectedBarGraphIndex = action.pageIndex,
                            selectedBarGraphMonth = lastSixMonthsFromCurrentMonth[action.pageIndex],
                            maxAmountForSelectedCategory = getMaxAmountForSelectedCategory(
                                transactionCategory,
                                action.pageIndex
                            ),
                            fetchSelectedCategoriesForAllMonth = fetchSelectedCategoriesForAllMonth(
                                transactionCategory
                            )
                        )
                    }

                    populateFiveDayAMountAverageMonthly(
                        month = action.pageIndex,
                        selectedTransactionCategory = transactionCategory
                    )
                }
            }

            is FinancialDashboardAction.SetPieChartClickAction -> {
                globalSelectedCategory = action.selectedCategory
                _uiState.update { currentState ->
                    currentState.copy(
                        selectedPieAnglePairsIndex = action.selectedPieChartPairsIndex,
                        selectedCategory = action.selectedCategory,
                        selectedBarGraphIndex = lastSixMonthsFromCurrentMonth.indexOf(selectedMonth),
                        selectedBarGraphMonth = lastSixMonthsFromCurrentMonth[(lastSixMonthsFromCurrentMonth.size - 1) - currentState.selectedBarGraphIndex],
                        fetchSelectedCategoriesForAllMonth = buildMap {
                            putAll(fetchSelectedCategoriesForAllMonth(action.selectedCategory))
                        },
                        maxAmountForSelectedCategory = getMaxAmountForSelectedCategory(
                            globalSelectedCategory,
                            selectedMonthIndex
                        ),
                    )
                }

                populateFiveDayAMountAverageMonthly(
                    month = lastSixMonthsFromCurrentMonth.indexOf(selectedMonth),
                    selectedTransactionCategory = globalSelectedCategory
                )
            }

            is FinancialDashboardAction.SetBarChartClickAction -> {
                selectedMonth = lastSixMonthsFromCurrentMonth[action.selectedBarGraphIndex]
                getMonthlyPieData(action.selectedBarGraphIndex) { monthlyPieChartDataObject ->
                    _uiState.update { currentState ->
                        currentState.copy(
                            generateRandomColorsList = generateRandomColorsList(monthlyPieChartData.size),
                            monthlyPieData = monthlyPieChartDataObject,
                            selectedMonth = selectedMonth,
                            selectedCategory = globalSelectedCategory,
                            selectedPieAnglePairsIndex = currentState.selectedPieAnglePairsIndex,
                            selectedBarGraphIndex = action.selectedBarGraphIndex,
                            selectedBarGraphMonth = lastSixMonthsFromCurrentMonth[(lastSixMonthsFromCurrentMonth.size - 1) - action.selectedBarGraphIndex],
                        )
                    }

                    populateFiveDayAMountAverageMonthly(
                        month = action.selectedBarGraphIndex,
                        selectedTransactionCategory = globalSelectedCategory
                    )
                }
            }
        }
    }

    /**
     * Fetches the maximum transaction amount for a specific category and month.
     * Used for scaling charts and graphs.
     *
     * @param selectedTransactionCategory The category to inspect.
     * @param enquiryMonth The month index to search in.
     * @return The highest transaction value (in cents) found.
     */
    fun getMaxAmountForSelectedCategory(
        selectedTransactionCategory: TransactionCategory,
        enquiryMonth: Int
    ): Long {
        var maxAmount = 0L

        globalPieChartInfoMap[enquiryMonth]?.forEach { (monthlyFinancialData, financialTransactionData) ->
            val convertValueToTransactionCategory = convertValueToTransactionCategory(monthlyFinancialData)
            if (convertValueToTransactionCategory == selectedTransactionCategory) {
                if (financialTransactionData > maxAmount) {
                    maxAmount = financialTransactionData
                }
            }
        }

        return maxAmount
    }

    /**
     * Converts a string value to its corresponding [TransactionCategory] enum.
     *
     * @param transactionCategory The string representation of the category.
     * @return The matching [TransactionCategory] or [TransactionCategory.UNKNOWN].
     */
    fun convertValueToTransactionCategory(transactionCategory: String): TransactionCategory {
        return TransactionCategory.entries.find { it.value == transactionCategory } ?: TransactionCategory.UNKNOWN
    }

    /**
     * Fetches monthly totals for a specific category over a 6-month period.
     *
     * @param transactionCategory The category to fetch data for.
     * @return A map of month indices to [PieChartInfo] containing totals.
     */
    private fun fetchSelectedCategoriesForAllMonth(transactionCategory: TransactionCategory): Map<Int, PieChartInfo> {
        viewModelScope.launch(Dispatchers.IO) {
            val monthlyTotalsForCategory = financialTransactionRepository.getMonthlyTotalsForCategory(
                transactionCategory = transactionCategory.value,
                transactionsYear = 2026,
                startMonth = 1,
                endMonth = 6
            )

            monthlyTotalsForCategory.forEach { monthlyTotalsForCategory ->
                monthlyBarGraphData[monthlyTotalsForCategory.month] =
                    PieChartInfo(
                        selectedTransactionCategory = transactionCategory.value,
                        totalSummationOfAmountValuesInCategory = monthlyTotalsForCategory.total,
                        totalTransactionsCount = monthlyTotalsForCategory.totalTransactionsCount
                    )
            }
        }
        return monthlyBarGraphData
    }

    /**
     * Generates a list of random colors for chart visualizations.
     *
     * @param colorItemsSize The number of colors to generate.
     * @return A list of [Color] objects.
     */
    private fun generateRandomColorsList(colorItemsSize: Int): List<Color> {
        generateRandomColors.clear()
        repeat(colorItemsSize) {
            val randomColor = Color(
            Random.nextInt(60, 196),
            Random.nextInt(60, 196),
            Random.nextInt(60, 196)
            )
            generateRandomColors.add(randomColor)
        }
        return generateRandomColors
    }

    /**
     * Fetches and processes pie chart data for a specific month.
     *
     * @param enquiredMonth The index of the month to fetch data for.
     * @param onUpdateRecomposition Callback triggered when the data is ready for the UI.
     */
    private fun getMonthlyPieData(
        enquiredMonth: Int = 0,
        onUpdateRecomposition: (MutableMap<TransactionCategory, PieChartInfo>) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            fetchedFinancialTransactionsEntity =
                financialTransactionRepository.getFinancialTransactionsInformationByDate(
                    enquiryMonth = enquiredMonth,
                )

            populateMonthlyDataForEachFinancialCategory(
                month = enquiredMonth,
                pieChartInfo = fetchedFinancialTransactionsEntity
            )

            fetchedFinancialTransactionsEntity.forEach { globalPieChartInfo ->
                totalTransactionsCount += globalPieChartInfo.totalTransactionsCount
                val convertValueToTransactionCategory = convertValueToTransactionCategory(globalPieChartInfo.selectedTransactionCategory)

                if (convertValueToTransactionCategory != TransactionCategory.UNKNOWN) {
                    monthlyPieChartData[convertValueToTransactionCategory] =
                        PieChartInfo(
                            selectedTransactionCategory = globalPieChartInfo.selectedTransactionCategory,
                            totalSummationOfAmountValuesInCategory = globalPieChartInfo.totalSummationOfAmountValuesInCategory,
                            totalTransactionsCount = globalPieChartInfo.totalTransactionsCount
                        )
                }
            }

            monthlyPieChartData = monthlyPieChartData.entries
                .sortedByDescending  { it.value.totalSummationOfAmountValuesInCategory }
                .associate { it.key to it.value }
                .toMutableMap()

            onUpdateRecomposition(monthlyPieChartData)
        }
    }

    /**
     * Retrieves the list of month names preceding the current month.
     *
     * @param count The number of previous months to retrieve.
     * @return A list of month names in reverse chronological order.
     */
    fun getPreviousValues(count: Int): List<String> {
        val currentIndex = YearlyMonths.entries.indexOfFirst {
            it.month.equals(currentMonth, ignoreCase = true)
        }

        return (1..count).map { offset ->
            val index = (currentIndex - offset + months.size) % months.size
            months[index].month
        }
    }

    /**
     * Caches monthly pie chart information in the [globalPieChartInfoMap].
     *
     * @param month The month index.
     * @param pieChartInfo The data to cache.
     */
    fun populateMonthlyDataForEachFinancialCategory(
        month: Int,
        pieChartInfo: List<PieChartInfo>
    ) {
        globalPieChartInfoMap[month] = pieChartInfo
    }

    /**
     * Fetches average spending values in five-day intervals for line graph display.
     * Updates the UI state with the results.
     *
     * @param month The month index.
     * @param selectedTransactionCategory The category to analyze.
     */
    fun populateFiveDayAMountAverageMonthly(
        month: Int,
        selectedTransactionCategory: TransactionCategory
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            fetchedAverageMonthlySummation = financialTransactionRepository.getAverageAmountPerFiveDayInterval(
                year = 2026,
                month = month + 1,
                transactionCategory = selectedTransactionCategory.value
            )

            Log.i("Okuhle", "did we get to this state $month ${selectedTransactionCategory.value} $fetchedAverageMonthlySummation")
            _uiState.update { currentState ->
                currentState.copy(
                    monthlyLineGraphData = fetchedAverageMonthlySummation
                )
            }
        }
    }

    /**
     * Formats a Unix timestamp into a readable date string.
     *
     * @param timestamp The timestamp in milliseconds.
     * @return A formatted string (e.g., "12 October 2025").
     */
    fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}
