package com.example.financialspendingdashboardanalysis.viewModel

import android.app.Application
import android.util.Log
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.application
import androidx.lifecycle.viewModelScope
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardAction
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardEvents
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardStates
import com.example.financialspendingdashboardanalysis.model.FinancialTransactionData
import com.example.financialspendingdashboardanalysis.model.MonthlyFinancialInformation
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import com.example.financialspendingdashboardanalysis.model.TransactionCategory
import com.example.financialspendingdashboardanalysis.model.YearlyMonths
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
import kotlin.random.Random

class FinancialAnalyticsDashboardViewModel(
    application: Application
) : AndroidViewModel(application) {
    var financialTransactionData: MutableList<FinancialTransactionData> = mutableListOf()
    val globalPieChartInfoMap: MutableMap<MonthlyFinancialInformation, List<FinancialTransactionData>> = mutableMapOf()
    var categorizedFinancialTransactionData: MutableMap<TransactionCategory, List<FinancialTransactionData>> = mutableMapOf()
    var monthlyPieChartData: MutableMap<TransactionCategory, PieChartInfo> = mutableMapOf()
    var monthlyBarGraphData: MutableMap<String, PieChartInfo> = mutableMapOf()
    val monthlyLineGraphsData: MutableMap<String, List<FinancialTransactionData>> = mutableMapOf()
    var currentMonth: String = ""
    var selectedMonth: String = ""
    var globalSelectedCategory: TransactionCategory = TransactionCategory.UNKNOWN
    val months: List<YearlyMonths> = YearlyMonths.entries
    var lastSixMonthsFromCurrentMonth: List<String> = emptyList()
    val generateRandomColors: MutableList<Color> = mutableListOf()
    var selectedFinancialTransactionData: FinancialTransactionData = FinancialTransactionData()

    private val _uiState = MutableStateFlow(FinancialDashboardStates())
    val uiState = _uiState.asStateFlow()

    private val _sharedEvents = Channel<FinancialDashboardEvents>(Channel.BUFFERED)
    val sharedEvents = _sharedEvents.receiveAsFlow()

    init {
        loadTransactionFromAssets(fileName = "transactionsList.txt")
        generateTransactionPieChartInformation()
        populateMonthlyDataForEachFinancialCategory()
    }

    companion object {
        const val BAR_GRAPH_ON_CLICK_EVENT: String = "BarGraphOnClickEvent"
        const val PIE_SLICE_ON_CLICK_EVENT: String = "PieSliceOnClickEvent"
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
                Log.i("Okuhle", "why do we always end up here?")


                selectedMonth = lastSixMonthsFromCurrentMonth[action.pageIndex]
                getMonthlyPieData(selectedMonth)
                var transactionCategory: TransactionCategory = TransactionCategory.UNKNOWN

                monthlyPieChartData.entries.forEachIndexed { index, (transactionData, _) ->
                    if (index == 0) {
                        transactionCategory = transactionData
                        return@forEachIndexed
                    }
                }

                _uiState.update { currentState ->
                    globalSelectedCategory = transactionCategory
                    currentState.copy(
                        generateRandomColorsList = generateRandomColorsList(monthlyPieChartData.size),
                        generateRandomColorsListForLineGraphs = generateRandomColorsList(monthlyPieChartData.maxOfOrNull { it.value.totalNumberOfAmountValuesInCategory } ?: 1),
                        selectedPieAnglePairsIndex = 0,
                        monthlyPieData = monthlyPieChartData,
                        selectedCategory = transactionCategory,
                        selectedMonth = selectedMonth,
                        hasDashBoardBeenInitializedBefore = true,
                        selectedBarGraphIndex = lastSixMonthsFromCurrentMonth.indexOf(selectedMonth),
                        selectedBarGraphMonth = lastSixMonthsFromCurrentMonth[action.pageIndex],
                        maxAmountForSelectedCategory = getMaxAmountForSelectedCategory(transactionCategory),
                        fetchSelectedCategoriesForAllMonth = fetchSelectedCategoriesForAllMonth(transactionCategory) ,
                        fetchSelectedCategoriesInstancesForAllMonths = fetchSelectedCategoriesInstancesForAllMonths(transactionCategory, selectedMonth)
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
                        maxAmountForSelectedCategory = getMaxAmountForSelectedCategory(globalSelectedCategory),
                        fetchSelectedCategoriesInstancesForAllMonths = fetchSelectedCategoriesInstancesForAllMonths(action.selectedCategory, selectedMonth)
                    )
                }
            }

            is FinancialDashboardAction.SetBarChartClickAction -> {
                selectedMonth = lastSixMonthsFromCurrentMonth[action.selectedBarGraphIndex]
                getMonthlyPieData(selectedMonth)
                _uiState.update { currentState ->
                    currentState.copy(
                        generateRandomColorsList = generateRandomColorsList(monthlyPieChartData.size),
                        generateRandomColorsListForLineGraphs = generateRandomColorsList(monthlyPieChartData.maxOfOrNull { it.value.totalNumberOfAmountValuesInCategory } ?: 1),
                        monthlyPieData = monthlyPieChartData,
                        selectedMonth = selectedMonth,
                        selectedCategory = globalSelectedCategory,
                        selectedPieAnglePairsIndex = currentState.selectedPieAnglePairsIndex,
                        selectedBarGraphIndex = action.selectedBarGraphIndex,
                        selectedBarGraphMonth = lastSixMonthsFromCurrentMonth[(lastSixMonthsFromCurrentMonth.size - 1) - action.selectedBarGraphIndex],
                        fetchSelectedCategoriesInstancesForAllMonths = fetchSelectedCategoriesInstancesForAllMonths(globalSelectedCategory, selectedMonth)
                    )
                }
            }
        }
    }

    private fun fetchSelectedCategoriesInstancesForAllMonths(transactionCategory: TransactionCategory, selectedMonth: String): Pair<Map<String, List<FinancialTransactionData>>, Int> {
        monthlyLineGraphsData.clear()
        var highestValue = 0
        globalPieChartInfoMap.forEach { globalPieChartInfo ->
            if (globalPieChartInfo.key.transactionCategory == transactionCategory && selectedMonth == globalPieChartInfo.key.transactionMonth) {
                globalPieChartInfo.value.forEach { globalPieChartInfoValue ->
                    val previousPieChartInfo = monthlyLineGraphsData[globalPieChartInfo.key.transactionMonth]
                    if (previousPieChartInfo == null) {
                        monthlyLineGraphsData[globalPieChartInfo.key.transactionMonth] = listOf(globalPieChartInfoValue)
                        highestValue = globalPieChartInfoValue.amount
                    } else {
                        if (globalPieChartInfoValue.amount > highestValue) {
                           highestValue = globalPieChartInfoValue.amount
                        }
                        monthlyLineGraphsData[globalPieChartInfo.key.transactionMonth] = previousPieChartInfo + listOf(globalPieChartInfoValue)
                    }
                }
            }
        }
        return Pair(
            buildMap {
                putAll(monthlyLineGraphsData)
            },
            highestValue
        )
    }

    fun getMaxAmountForSelectedCategory(selectedTransactionCategory: TransactionCategory): Int {
        var maxAmount = 0
        globalPieChartInfoMap.entries.forEach { (monthlyFinancialData, financialTransactionData) ->
            if (monthlyFinancialData.transactionCategory == selectedTransactionCategory) {
                financialTransactionData.forEach { transactionData ->
                    if (transactionData.amount > maxAmount) {
                        maxAmount = transactionData.amount
                    }
                    return@forEach
                }
            }
        }
        return maxAmount
    }

    private fun fetchSelectedCategoriesForAllMonth(transactionCategory: TransactionCategory): Map<String, PieChartInfo> {
        monthlyBarGraphData.clear()
        globalPieChartInfoMap.entries.forEach { globalPieChartInfo ->
            if (globalPieChartInfo.key.transactionCategory == transactionCategory) {
                globalPieChartInfo.value.forEach { globalPieChartInfoValue ->
                    if (globalPieChartInfoValue.paymentDate.isNotEmpty()) {
                        val previousPieChartInfo = monthlyBarGraphData[globalPieChartInfo.key.transactionMonth]
                        if (previousPieChartInfo == null) {
                            monthlyBarGraphData[globalPieChartInfo.key.transactionMonth] =
                                PieChartInfo(
                                    selectedTransactionCategory = globalPieChartInfoValue.transactionCategory,
                                    totalNumberOfAmountValuesInCategory = 1,
                                    totalSummationOfAmountValuesInCategory = globalPieChartInfoValue.amount,
                                    paymentDate = globalPieChartInfoValue.paymentDate
                                )
                        } else {
                            monthlyBarGraphData[globalPieChartInfo.key.transactionMonth] =
                                PieChartInfo(
                                    selectedTransactionCategory = previousPieChartInfo.selectedTransactionCategory,
                                    totalNumberOfAmountValuesInCategory = (previousPieChartInfo.totalNumberOfAmountValuesInCategory) + 1,
                                    totalSummationOfAmountValuesInCategory = (previousPieChartInfo.totalSummationOfAmountValuesInCategory) + globalPieChartInfoValue.amount,
                                    paymentDate = previousPieChartInfo.paymentDate
                                )
                        }
                    }
                }
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

    private fun getMonthlyPieData(selectedMonthForDisplay: String) {
        monthlyPieChartData.clear()
        globalPieChartInfoMap.forEach { globalPieChartInfo ->
            if (globalPieChartInfo.key.transactionMonth.equals(selectedMonthForDisplay, ignoreCase = true)) {
                globalPieChartInfo.value.forEach { globalPieChartInfoValue ->
                    if (monthlyPieChartData[globalPieChartInfo.key.transactionCategory] == null) {
                         monthlyPieChartData[globalPieChartInfo.key.transactionCategory] =
                            PieChartInfo(
                                selectedTransactionCategory = globalPieChartInfoValue.transactionCategory,
                                totalNumberOfAmountValuesInCategory = 1,
                                totalSummationOfAmountValuesInCategory = globalPieChartInfoValue.amount,
                                paymentDate = globalPieChartInfoValue.paymentDate
                            )
                    } else {
                        val previousPieChartInfo = monthlyPieChartData[globalPieChartInfo.key.transactionCategory]
                        monthlyPieChartData[globalPieChartInfo.key.transactionCategory] =
                            PieChartInfo(
                                selectedTransactionCategory = globalPieChartInfoValue.transactionCategory,
                                totalNumberOfAmountValuesInCategory = (previousPieChartInfo?.totalNumberOfAmountValuesInCategory ?: 0) + 1,
                                totalSummationOfAmountValuesInCategory = (previousPieChartInfo?.totalSummationOfAmountValuesInCategory ?: 0) + globalPieChartInfoValue.amount,
                                paymentDate = globalPieChartInfoValue.paymentDate
                            )
                    }
                }

                monthlyPieChartData = monthlyPieChartData.entries
                    .sortedByDescending  { it.value.totalSummationOfAmountValuesInCategory }
                    .associate { it.key to it.value }
                    .toMutableMap()
            }
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

    fun generateTransactionPieChartInformation() {
        currentMonth = SimpleDateFormat(
            "MMMM",
            Locale.getDefault()
        ).format(Calendar.getInstance().time).trim()
        lastSixMonthsFromCurrentMonth = getPreviousValues(count = 6).reversed()

        financialTransactionData.forEach { financialTransactionInstance ->
            val transactionMonth = financialTransactionInstance.paymentDate.split(" ")[1].trim()
            val monthEnum = YearlyMonths.entries.first {
                it.month == transactionMonth
            }

            val isTransactionWithinTheAcceptableTimeFrame = monthEnum in months
            if (isTransactionWithinTheAcceptableTimeFrame) {
                val doesTransactionCategoryExist =
                    categorizedFinancialTransactionData[financialTransactionInstance.transactionCategory]

                if (doesTransactionCategoryExist == null) {
                    categorizedFinancialTransactionData[financialTransactionInstance.transactionCategory] =
                        listOf(financialTransactionInstance)
                } else {
                    val triplePieChartData =
                        categorizedFinancialTransactionData[financialTransactionInstance.transactionCategory]
                            ?: emptyList()
                    categorizedFinancialTransactionData[financialTransactionInstance.transactionCategory] =
                        triplePieChartData + listOf(financialTransactionInstance)
                }
            }
        }
    }

    fun populateMonthlyDataForEachFinancialCategory() {
        categorizedFinancialTransactionData.forEach { (category, pieChartInfoMap) ->
            pieChartInfoMap.forEach { pieChartInfo ->
                val paymentMonth = pieChartInfo.paymentDate.split(" ")[1].trim()
                if (paymentMonth.isNotEmpty() && paymentMonth in lastSixMonthsFromCurrentMonth) {
                    val fetchPieChartData =
                        globalPieChartInfoMap[MonthlyFinancialInformation(category, paymentMonth)]
                    if (fetchPieChartData == null) {
                        globalPieChartInfoMap[MonthlyFinancialInformation(category, paymentMonth)] =
                            listOf(pieChartInfo)
                    } else {
                        globalPieChartInfoMap[MonthlyFinancialInformation(category, paymentMonth)] =
                            fetchPieChartData + listOf(pieChartInfo)
                    }
                }
            }
        }
    }

    fun loadTransactionFromAssets(fileName: String) {
        financialTransactionData = application.assets.open(fileName)
            .bufferedReader()
            .useLines { lines ->
                lines
                    .filter { it.isNotEmpty() }
                    .mapNotNull { line ->
                        val fields = line.split(";")

                        if (fields.size != 7) {
                            return@mapNotNull null
                        }

                        FinancialTransactionData(
                            transactionCategory = TransactionCategory.valueOf(fields.first()),
                            transactionType = fields[1],
                            amount = fields[2].toInt(),
                            fromAccount = fields[3],
                            fundAccount = fields[4],
                            paymentType = fields[5],
                            paymentDate = formatTimestamp(timestamp = fields[6].toLong())
                        )
                    }.toMutableList()
        }
    }

    fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}


