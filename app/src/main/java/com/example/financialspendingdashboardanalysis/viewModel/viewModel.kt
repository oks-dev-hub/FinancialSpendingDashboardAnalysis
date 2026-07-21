package com.example.financialspendingdashboardanalysis.viewModel

import android.app.Application
import com.example.financialspendingdashboardanalysis.FinancialTransactionRepository
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.AndroidViewModel
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
import kotlin.random.Random

/**
 * ViewModel responsible for managing all business logic and state
 * associated with the Financial Spending Dashboard.
 *
 * This ViewModel acts as the single source of truth for:
 *
 * - Pie chart analytics
 * - Monthly spending summaries
 * - Category-based bar chart data
 * - Transaction trend line graph data
 * - Navigation events
 * - Dashboard state management
 *
 * ---
 *
 * ## Architecture
 *
 * FinancialAnalyticsDashboardViewModel
 * │
 * ├── Transaction Asset Loader
 * │
 * ├── Category Aggregation Engine
 * │
 * ├── Monthly Analytics Engine
 * │   ├── Pie Chart Data
 * │   ├── Bar Chart Data
 * │   └── Line Graph Data
 * │
 * ├── State Management (StateFlow)
 * │
 * └── Navigation Events (Channel)
 *
 * ---
 *
 * ## Data Flow
 *
 * transactions.txt
 *        ↓
 * loadTransactionFromAssets()
 *        ↓
 * generateTransactionPieChartInformation()
 *        ↓
 * populateMonthlyDataForEachFinancialCategory()
 *        ↓
 * User Actions
 *        ↓
 * onAction(...)
 *        ↓
 * uiState.update(...)
 *        ↓
 * Compose UI
 *
 * ---
 *
 * ## State Management
 *
 * UI state is exposed through:
 *
 * - [uiState]
 *
 * One-time events are exposed through:
 *
 * - [sharedEvents]
 *
 * ---
 *
 * ## Performance Analysis
 *
 * Initial Data Loading:
 * O(N)
 *
 * Pie Chart Aggregation:
 * O(N)
 *
 * Bar Graph Aggregation:
 * O(N)
 *
 * Line Graph Aggregation:
 * O(N)
 *
 * State Updates:
 * O(1)
 *
 * Where:
 * N = number of transactions loaded from assets.
 *
 * ---
 *
 * ## Thread Safety
 *
 * State updates occur through MutableStateFlow.
 *
 * Navigation events are dispatched through a buffered Channel.
 *
 * Heavy operations are executed within ViewModel scope and survive
 * configuration changes.
 */
class FinancialAnalyticsDashboardViewModel(
    private val application: Application,
    private val financialTransactionRepository: FinancialTransactionRepository
) : AndroidViewModel(application) {
    var financialTransactionData: MutableList<FinancialTransactionData> = mutableListOf()
    val globalPieChartInfoMap: MutableMap<Int, List<PieChartInfo>> = mutableMapOf()
    var categorizedFinancialTransactionData: MutableMap<TransactionCategory, List<FinancialTransactionData>> = mutableMapOf<TransactionCategory, List<FinancialTransactionData>>()
    var monthlyPieChartData: MutableMap<TransactionCategory, PieChartInfo> = mutableMapOf()
    var monthlyBarGraphData: MutableMap<Int, PieChartInfo> = mutableMapOf()
    val monthlyLineGraphsData: MutableMap<String, List<FinancialTransactionData>> = mutableMapOf()
    var currentMonth: String = ""
    var selectedMonth: String = ""

    var selectedMonthIndex: Int = 0
    var globalSelectedCategory: TransactionCategory = TransactionCategory.UNKNOWN
    val months: List<YearlyMonths> = YearlyMonths.entries
    var lastSixMonthsFromCurrentMonth: List<String> = emptyList()
    val generateRandomColors: MutableList<Color> = mutableListOf()
    var selectedFinancialTransactionData: FinancialTransactionData = FinancialTransactionData()
    var fetchedFinancialTransactionsEntity: List<PieChartInfo> = emptyList()
    private var financialTransactionsDatabase: FinancialTransactionsDatabase =
        FinancialTransactionDatabaseProvider.get(application.applicationContext)
    private var financialTransactionsDao: FinancialTransactionsDao = financialTransactionsDatabase.financialTransactionsDao()

    //This is the recomposition state, that we use to trigger recomposition in the UI
    private val _uiState = MutableStateFlow(FinancialDashboardStates())
    val uiState = _uiState.asStateFlow()

    //This is used to orchestrate navigation events in the app
    private val _sharedEvents = Channel<FinancialDashboardEvents>(Channel.BUFFERED)
    val sharedEvents = _sharedEvents.receiveAsFlow()

    //Initial init, this is where we load data from the text file that contains all the transactions we use in this app.
    //The file transactionsList.txt belong in this package is a local file, where in which it contains all the transactions.
    //Then we generate a map that contains all transaction with the TransactionCategory as the key
    //Then after we populated the data into a new map, there the key not becomes the both TransactionCategory and Month, and the values will be all transaction data belonging to the key.
    //We only do this vey heavy job just this once, then the last Map, we created will serve as a Reference throughout the every data filtering we perform.
    //Also, we had to do this job early before we even get to use the data set we just created, which simplifies the Data, and it is very easy and less costly to infer since we are using Map.
    //Maps have an infer Analysis of O(1), we can infer sophisticated data in real time.
    init {
        lastSixMonthsFromCurrentMonth = getPreviousValues(count = 6).reversed()
        currentMonth = SimpleDateFormat(
            "MMMM",
            Locale.getDefault()
        ).format(Calendar.getInstance().time).trim()
    }

    companion object {
        const val BAR_GRAPH_ON_CLICK_EVENT: String = "BarGraphOnClickEvent"
        const val PIE_SLICE_ON_CLICK_EVENT: String = "PieSliceOnClickEvent"


        object AppViewModelProvider {
            val Factory = viewModelFactory {
                initializer {
                    val application = this[APPLICATION_KEY] as FinancialTransactionApplication

                    FinancialAnalyticsDashboardViewModel(
                        application = application,
                        financialTransactionRepository = application.financialTransactionRepository
                    )
                }
            }
        }
    }

    /**
     * Dispatches a one-time UI event to observers.
     *
     * This method is primarily used for navigation events that should
     * not be stored inside persistent UI state.
     *
     * The currently selected transaction is cached to support detail
     * screen navigation.
     *
     * @param event Event to be dispatched.
     * @param financialTransaction Optional transaction associated
     * with the event.
     *
     * Time Complexity: O(1)
     * Space Complexity: O(1)
     */
    fun sendEvent(
        event: FinancialDashboardEvents,
        financialTransaction: FinancialTransactionData?
    ) {
        selectedFinancialTransactionData = financialTransaction ?: selectedFinancialTransactionData
        viewModelScope.launch(Dispatchers.IO) {
            _sharedEvents.send(event)
        }
    }

    /**
     * Processes dashboard actions originating from the UI layer.
     *
     * Supported actions:
     *
     * - InitializeMonthlyPieChartDashboard
     * - SetPieChartClickAction
     * - SetBarChartClickAction
     *
     * This method serves as the primary reducer for dashboard state.
     *
     * ---
     *
     * State Update Flow
     *
     * UI Interaction
     *      ↓
     * FinancialDashboardAction
     *      ↓
     * onAction(...)
     *      ↓
     * uiState.update(...)
     *
     * Time Complexity:
     * O(N)
     *
     * Space Complexity:
     * O(N)
     *
     * Depending on aggregation work performed.
     *
     * @param action Action emitted by the UI.
     */
    fun onAction(action: FinancialDashboardAction) {
        when (action) {
            is FinancialDashboardAction.InitializeMonthlyPieChartDashboard -> {
                if (action.pageIndex == -1) return

                selectedMonth = lastSixMonthsFromCurrentMonth[action.pageIndex]
                getMonthlyPieData(action.pageIndex)
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
                        selectedPieAnglePairsIndex = 0,
                        monthlyPieData = monthlyPieChartData,
                        selectedCategory = transactionCategory,
                        selectedMonth = selectedMonth,
                        hasDashBoardBeenInitializedBefore = true,
                        selectedBarGraphIndex = lastSixMonthsFromCurrentMonth.indexOf(selectedMonth),
                        selectedBarGraphMonth = lastSixMonthsFromCurrentMonth[action.pageIndex],
                        maxAmountForSelectedCategory = getMaxAmountForSelectedCategory(transactionCategory, action.pageIndex),
                        fetchSelectedCategoriesForAllMonth = fetchSelectedCategoriesForAllMonth(transactionCategory) ,
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
            }

            is FinancialDashboardAction.SetBarChartClickAction -> {
                selectedMonth = lastSixMonthsFromCurrentMonth[action.selectedBarGraphIndex]
                getMonthlyPieData(action.selectedBarGraphIndex)
                _uiState.update { currentState ->
                    currentState.copy(
                        generateRandomColorsList = generateRandomColorsList(monthlyPieChartData.size),
                        monthlyPieData = monthlyPieChartData,
                        selectedMonth = selectedMonth,
                        selectedCategory = globalSelectedCategory,
                        selectedPieAnglePairsIndex = currentState.selectedPieAnglePairsIndex,
                        selectedBarGraphIndex = action.selectedBarGraphIndex,
                        selectedBarGraphMonth = lastSixMonthsFromCurrentMonth[(lastSixMonthsFromCurrentMonth.size - 1) - action.selectedBarGraphIndex],
                    )
                }
            }
        }
    }

    /**
     * Determines the highest transaction amount recorded for a specific transaction category.
     *
     * This function iterates through all categorized monthly transaction records and identifies
     * the maximum monetary value associated with the provided [selectedTransactionCategory].
     *
     * The returned value is used primarily for:
     * - Graph normalization and scaling.
     * - Dynamic axis calculations.
     * - Comparative financial analysis across transactions.
     *
     * @param selectedTransactionCategory The category whose maximum transaction amount
     * should be determined.
     *
     * @return The largest transaction amount found for the category.
     * Returns `0` if no matching transactions exist.
     *
     * Time Complexity: O(n)
     * - Where `n` is the total number of transactions belonging to the selected category.
     *
     * Space Complexity: O(1)
     * - Uses only a single accumulator variable regardless of input size.
     */
    fun getMaxAmountForSelectedCategory(
        selectedTransactionCategory: TransactionCategory,
        enquiryMonth: Int
    ): Long {
        var maxAmount = 0L

        globalPieChartInfoMap[enquiryMonth]?.forEach { (monthlyFinancialData, financialTransactionData) ->
            if (monthlyFinancialData == selectedTransactionCategory) {
                if (financialTransactionData > maxAmount) {
                    maxAmount = financialTransactionData
                }
            }
        }

        return maxAmount
    }

    /**
     * Aggregates transaction data for a specific category across all available months.
     *
     * The resulting dataset is used by the monthly bar chart to display:
     * - Total spending per month.
     * - Number of transactions per month.
     * - Historical spending trends for a selected category.
     *
     * Each month is represented by a [PieChartInfo] object containing:
     * - Transaction count.
     * - Total accumulated spending.
     * - Associated transaction metadata.
     *
     * @param transactionCategory The category whose monthly spending information
     * should be aggregated.
     *
     * @return A map where:
     * - Key = Month name.
     * - Value = Aggregated monthly financial statistics.
     *
     * Algorithm:
     * 1. Clear previously generated bar chart data.
     * 2. Traverse all categorized monthly transaction groups.
     * 3. Select only matching transaction categories.
     * 4. Aggregate totals and transaction counts per month.
     * 5. Return the completed month-to-statistics mapping.
     *
     * Time Complexity: O(n)
     * - Where `n` is the total number of transactions belonging to the selected category.
     *
     * Space Complexity: O(m)
     * - Where `m` is the number of unique months stored in the result map.
     */
    private fun fetchSelectedCategoriesForAllMonth(transactionCategory: TransactionCategory): Map<Int, PieChartInfo> {
        viewModelScope.launch {
            monthlyBarGraphData.clear()
            val monthlyTotalsForCategory = financialTransactionRepository.getMonthlyTotalsForCategory(
                transactionCategory = transactionCategory.value,
                transactionsYear = 2026,
                startMonth = 1,
                endMonth = 6
            )

            monthlyTotalsForCategory.forEach { monthlyTotalsForCategory ->
                monthlyBarGraphData[monthlyTotalsForCategory.month] =
                    PieChartInfo(
                        selectedTransactionCategory = transactionCategory,
                        totalSummationOfAmountValuesInCategory = monthlyTotalsForCategory.total,
                    )
            }
        }
        return monthlyBarGraphData
    }

    /**
     * Generates a collection of visually distinct random colors for chart rendering.
     *
     * The generated colors are primarily used for:
     * - Pie chart slice visualization.
     * - Bar chart category differentiation.
     * - Financial dashboard data representation.
     *
     * RGB values are intentionally constrained between 60 and 196 to:
     * - Avoid excessively dark colors.
     * - Avoid extremely bright colors.
     * - Improve text readability and accessibility
     *
     * @param colorItemsSize The number of colors required.
     *
     * @return A list containing randomly generated colors.
     *
     * Time Complexity: O(n)
     * - Where `n` equals [colorItemsSize].
     *
     * Space Complexity: O(n)
     * - Stores one color object per requested item.
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
     * Generates aggregated pie chart data for a selected month.
     *
     * This function extracts all transactions belonging to the supplied month and
     * groups them by transaction category. For each category, the following
     * statistics are calculated:
     *
     * - Total number of transactions.
     * - Total amount spent.
     * - Associated transaction metadata.
     *
     * The resulting dataset is used by the dashboard's monthly pie chart to
     * visualize spending distribution across categories.
     *
     * Categories are sorted in descending order by total spending amount so that:
     * - The largest spending category appears first.
     * - Pie chart slices remain consistently ordered.
     * - Color assignment remains predictable.
     *
     * @param selectedMonthForDisplay The month whose spending information should
     * be aggregated.
     *
     * Algorithm:
     * 1. Clear previously generated pie chart data.
     * 2. Locate all transactions belonging to the selected month.
     * 3. Group transactions by category.
     * 4. Accumulate transaction count and total amount per category.
     * 5. Sort categories by total spending amount.
     *
     * Time Complexity: O(n log n)
     * - O(n) for aggregation.
     * - O(k log k) for sorting categories.
     * - Where:
     *      n = total transactions in selected month.
     *      k = unique transaction categories.
     *
     * Space Complexity: O(k)
     * - Stores one aggregated entry per category.
     */
    private fun getMonthlyPieData(
        enquiredMonth: Int = 0
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            if (globalPieChartInfoMap[enquiredMonth] == null) {
                fetchedFinancialTransactionsEntity =
                    financialTransactionRepository.getFinancialTransactionsInformationByDate(
                        enquiredMonth
                    )

                populateMonthlyDataForEachFinancialCategory(
                    month = enquiredMonth,
                    pieChartInfo = fetchedFinancialTransactionsEntity
                )
            }

            globalPieChartInfoMap[enquiredMonth]?.forEach { globalPieChartInfo ->
                 monthlyPieChartData[globalPieChartInfo.selectedTransactionCategory] =
                    PieChartInfo(
                        selectedTransactionCategory = globalPieChartInfo.selectedTransactionCategory,
                        totalSummationOfAmountValuesInCategory = globalPieChartInfo.totalSummationOfAmountValuesInCategory,
                    )
            }

            monthlyPieChartData = monthlyPieChartData.entries
                .sortedByDescending  { it.value.totalSummationOfAmountValuesInCategory }
                .associate { it.key to it.value }
                .toMutableMap()
            }
    }

    /**
     * Retrieves a sequence of previous months relative to the current month.
     *
     * This function performs cyclic month navigation, allowing the calculation
     * to wrap around calendar boundaries.
     *
     * This functionality is used to determine the dashboard's rolling six-month
     * reporting window.
     *
     * @param count Number of previous months to retrieve.
     *
     * @return List of previous month names ordered from newest to oldest.
     *
     * Algorithm:
     * 1. Locate the current month index.
     * 2. Move backwards through the month list.
     * 3. Wrap around using modular arithmetic.
     * 4. Return the resulting month sequence.
     *
     * Time Complexity: O(count)
     *
     * Space Complexity: O(count)
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
     * Builds the dashboard's master monthly transaction index.
     *
     * This function converts category-grouped transaction data into a structure
     * optimized for month-based financial analysis.
     *
     * The generated map serves as the primary data source for:
     *
     * - Monthly pie charts.
     * - Monthly bar graphs.
     * - Monthly line graphs.
     * - Category drill-down analytics.
     *
     * Only transactions belonging to the dashboard's rolling six-month period
     * are included.
     *
     * Algorithm:
     * 1. Traverse categorized transaction collections.
     * 2. Extract each transaction's month.
     * 3. Filter transactions outside the reporting window.
     * 4. Group by category and month.
     * 5. Store results in the global lookup map.
     *
     * Time Complexity: O(n)
     * - n = total transaction count.
     *
     * Space Complexity: O(n)
     * - Transactions are indexed by category-month combinations.
     */
    fun populateMonthlyDataForEachFinancialCategory(
        month: Int,
        pieChartInfo: List<PieChartInfo>
    ) {
        globalPieChartInfoMap[month] = pieChartInfo
    }

    /**
     * Converts a Unix epoch timestamp into a human-readable date string.
     *
     * The output format follows:
     *
     * This function is used when loading transaction data from assets to
     * transform raw timestamps into presentation-ready values.
     *
     * @param timestamp Epoch timestamp in milliseconds.
     *
     * @return Formatted date string using the device locale.
     *
     * Time Complexity: O(1)
     *
     * Space Complexity: O(1)
     */
    fun formatTimestamp(timestamp: Long): String {
        return SimpleDateFormat(
            "dd MMMM yyyy",
            Locale.getDefault()
        ).format(Date(timestamp))
    }
}


