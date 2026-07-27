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
 */
class FinancialAnalyticsDashboardViewModel(
    private val application: Application,
    private val financialTransactionRepository: FinancialTransactionRepository
) : AndroidViewModel(application) {
    val globalPieChartInfoMap: MutableMap<Int, List<PieChartInfo>> = mutableMapOf()
    var monthlyPieChartData: MutableMap<TransactionCategory, PieChartInfo> = mutableMapOf()
    var monthlyBarGraphData: MutableMap<Int, PieChartInfo> = mutableMapOf()
    val monthlyLineGraphsData: Map<Int, FiveDayAverageValues> = emptyMap()
    var currentMonth: String = ""
    var selectedMonth: String = ""

    var selectedMonthIndex: Int = 0
    var globalSelectedCategory: TransactionCategory = TransactionCategory.UNKNOWN
    val months: List<YearlyMonths> = YearlyMonths.entries
    var lastSixMonthsFromCurrentMonth: List<String> = emptyList()
    val generateRandomColors: MutableList<Color> = mutableListOf()
    var selectedFinancialTransactionData: FinancialTransactionData = FinancialTransactionData()
    var fetchedFinancialTransactionsEntity: List<PieChartInfo> = emptyList()
    var fetchedAverageMonthlySummation: List<FiveDayAverageValues> = emptyList()

    private val _uiState = MutableStateFlow(FinancialDashboardStates())
    val uiState = _uiState.asStateFlow()

    private val _sharedEvents = Channel<FinancialDashboardEvents>(Channel.BUFFERED)
    val sharedEvents = _sharedEvents.receiveAsFlow()

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

    fun sendEvent(
        event: FinancialDashboardEvents,
        financialTransaction: FinancialTransactionData?
    ) {
        selectedFinancialTransactionData = financialTransaction ?: selectedFinancialTransactionData
        viewModelScope.launch(Dispatchers.IO) {
            _sharedEvents.send(event)
        }
    }

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

    fun convertValueToTransactionCategory(transactionCategory: String): TransactionCategory {
        return TransactionCategory.entries.find { it.value == transactionCategory } ?: TransactionCategory.UNKNOWN
    }

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
                    )
            }
        }
        return monthlyBarGraphData
    }

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
                val convertValueToTransactionCategory = convertValueToTransactionCategory(globalPieChartInfo.selectedTransactionCategory)

                if (convertValueToTransactionCategory != TransactionCategory.UNKNOWN) {
                    monthlyPieChartData[convertValueToTransactionCategory] =
                        PieChartInfo(
                            selectedTransactionCategory = globalPieChartInfo.selectedTransactionCategory,
                            totalSummationOfAmountValuesInCategory = globalPieChartInfo.totalSummationOfAmountValuesInCategory,
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

    fun getPreviousValues(count: Int): List<String> {
        val currentIndex = YearlyMonths.entries.indexOfFirst {
            it.month.equals(currentMonth, ignoreCase = true)
        }

        return (1..count).map { offset ->
            val index = (currentIndex - offset + months.size) % months.size
            months[index].month
        }
    }

    fun populateMonthlyDataForEachFinancialCategory(
        month: Int,
        pieChartInfo: List<PieChartInfo>
    ) {
        globalPieChartInfoMap[month] = pieChartInfo
    }

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

    fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}
