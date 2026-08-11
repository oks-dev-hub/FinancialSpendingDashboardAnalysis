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
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardStates
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import com.example.financialmodels.TransactionCategory
import com.example.financialspendingdashboardanalysis.model.DayAverageValues
import com.example.financialspendingdashboardanalysis.model.YearlyMonths
import com.example.financialspendingdashboardanalysis.ui.theme.FinancialTransactionApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
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
    private val globalPieChartInfoMap: MutableMap<Int, List<PieChartInfo>> = mutableMapOf()

    /**
     * Aggregated pie chart data for the currently selected month, indexed by category.
     */
    private var monthlyPieChartData: MutableMap<TransactionCategory, PieChartInfo> = mutableMapOf()

    /**
     * Aggregated bar graph data representing trends for a specific category across months.
     */
    private var monthlyBarGraphData: MutableMap<Int, PieChartInfo> = mutableMapOf()

    /**
     * The total number of transactions processed for the current context.
     */
    var totalTransactionsCount: Int = 0

    /**
     * The name of the current calendar month.
     */
    private var currentMonth: String = ""

    /**
     * The name of the month currently selected in the UI.
     */
     private var selectedMonth: String = ""

    /**
     * The currently selected transaction category across the entire dashboard.
     */
    private var globalSelectedCategory: TransactionCategory = TransactionCategory.UNKNOWN

    /**
     * A list of all months in a year.
     */
    private val months: List<YearlyMonths> = YearlyMonths.entries

    /**
     * A list of the last six-month names, ending with the current month.
     */
    var lastSixMonthsFromCurrentMonth: List<String> = emptyList()

    /**
     * A list of generated colors for chart segments.
     */
    private val generateRandomColors: MutableList<Color> = mutableListOf()

    /**
     * Raw list of pie chart data entities fetched from the repository.
     */
    private var fetchedFinancialTransactionsEntity: List<PieChartInfo> = emptyList()

    /**
     * List of five-day average values for line graph visualization.
     */
    private var fetchedAverageMonthlySummation: List<DayAverageValues> = emptyList()

    private val _uiState = MutableStateFlow(FinancialDashboardStates())

    /**
     * UI state flow consumed by the Compose layer.
     */
    val uiState = _uiState.asStateFlow()

    init {
        currentMonth = SimpleDateFormat(
            "MMMM",
            Locale.getDefault()
        ).format(Calendar.getInstance().time).trim()

        lastSixMonthsFromCurrentMonth = getPreviousValues().reversed()
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
                            fetchSelectedCategoriesForAllMonth = fetchSelectedCategoriesForAllMonth(
                                transactionCategory
                            )
                        )
                    }

                    populateDayAmountAverageMonthly(
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
                    )
                }

                populateDayAmountAverageMonthly(
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

                    populateDayAmountAverageMonthly(
                        month = action.selectedBarGraphIndex,
                        selectedTransactionCategory = globalSelectedCategory
                    )
                }
            }
        }
    }

    /**
     * Converts a string value to its corresponding [TransactionCategory] enum.
     *
     * @param transactionCategory The string representation of the category.
     * @return The matching [TransactionCategory] or [TransactionCategory.UNKNOWN].
     */
    private fun convertValueToTransactionCategory(transactionCategory: String): TransactionCategory {
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
            totalTransactionsCount = 0
            fetchedFinancialTransactionsEntity =
                financialTransactionRepository.getFinancialTransactionsInformationByDate(
                    enquiryMonth = enquiredMonth,
                )

            populateMonthlyDataForEachFinancialCategory(
                month = enquiredMonth,
                pieChartInfo = fetchedFinancialTransactionsEntity
            )

            Log.i("Okuhle", "fetchedFinancialTransactionsEntity $enquiredMonth $fetchedFinancialTransactionsEntity")
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
    private fun getPreviousValues(): List<String> {
        val currentIndex = YearlyMonths.entries.indexOfFirst {
            it.month.equals(currentMonth, ignoreCase = true)
        }

        return (1..6).map { offset ->
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
    private fun populateMonthlyDataForEachFinancialCategory(
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
    private fun populateDayAmountAverageMonthly(
        month: Int,
        selectedTransactionCategory: TransactionCategory
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            fetchedAverageMonthlySummation = financialTransactionRepository.getAverageAmountPerDay(
                year = 2026,
                month = month + 1,
                transactionCategory = selectedTransactionCategory.value
            )

            _uiState.update { currentState ->
                currentState.copy(
                    monthlyLineGraphData = fetchedAverageMonthlySummation
                )
            }
        }
    }
}
