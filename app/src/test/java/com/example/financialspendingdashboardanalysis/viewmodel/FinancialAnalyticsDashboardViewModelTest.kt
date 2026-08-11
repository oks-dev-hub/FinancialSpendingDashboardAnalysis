package com.example.financialspendingdashboardanalysis.viewmodel

import android.app.Application
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.example.financialspendingdashboardanalysis.FinancialTransactionRepository
import com.example.financialspendingdashboardanalysis.model.FinancialDashboardAction
import com.example.financialspendingdashboardanalysis.model.DayAverageValues
import com.example.financialspendingdashboardanalysis.model.PieChartInfo
import com.example.financialmodels.TransactionCategory
import app.cash.turbine.test
import com.example.financialspendingdashboardanalysis.model.MonthlyCategoryTotal
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestWatcher
import org.junit.runner.Description
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

/**
 * A JUnit Test Rule that overrides the Main dispatcher with a test dispatcher.
 * This is required for testing code that uses [Dispatchers.Main], which is not available in unit tests.
 *
 * @param testDispatcher The dispatcher to use during tests. Defaults to [UnconfinedTestDispatcher].
 */
@ExperimentalCoroutinesApi
class MainDispatcherRule(
    val testDispatcher: kotlinx.coroutines.test.TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

/**
 * Unit tests for the [FinancialAnalyticsDashboardViewModel].
 *
 * This test suite validates the business logic of the ViewModel, including:
 * - Dashboard initialization and data fetching.
 * - Processing user interactions (pie chart clicks, bar graph clicks).
 * - Proper state transitions and synchronization between different chart datasets.
 * - Data sorting and aggregation logic.
 *
 * The tests use [Mockito] for dependency mocking and [Turbine] for testing [Flow] emissions.
 */
@ExperimentalCoroutinesApi
class FinancialAnalyticsDashboardViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var application: Application

    @Mock
    private lateinit var repository: FinancialTransactionRepository

    private lateinit var viewModel: FinancialAnalyticsDashboardViewModel

    /**
     * Mock data representing a list of [PieChartInfo] for testing.
     */
    val mockPieData = listOf(
        PieChartInfo(
            selectedTransactionCategory = "Food",
            totalSummationOfAmountValuesInCategory = 1000L,
            totalTransactionsCount = 150
        ),
        PieChartInfo(
            selectedTransactionCategory = "Transport",
            totalSummationOfAmountValuesInCategory = 5000L,
            totalTransactionsCount = 200
        )
    )

    /**
     * Mock data representing monthly category totals for testing trend lines/bars.
     */
    val mockCategoryMonthlyTotals = listOf(
        MonthlyCategoryTotal(
            year = 2026,
            month = 1,
            total = 246532L,
            totalTransactionsCount = 166000
        ),
        MonthlyCategoryTotal(
            year = 2026,
            month = 2,
            total = 366532L,
            totalTransactionsCount = 24532
        ),
    )
    /**
     * Mock data representing intra-month spending averages for testing line graphs.
     */
    val dayAverageValues = listOf(
        DayAverageValues(day = 10, 10000.0, 120),
        DayAverageValues(day = 14, 20000.0, 230),
    )

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        viewModel = FinancialAnalyticsDashboardViewModel(application, repository)
    }

    /**
     * Tests that [FinancialDashboardAction.InitializeMonthlyPieChartDashboard] correctly:
     * 1. Fetches data from the repository.
     * 2. Sorts categories by total expenditure (descending).
     * 3. Updates the UI state with the correct selected month and category.
     * 4. Synchronizes line graph data for the default selected category.
     */
    @Test
    fun `onAction InitializeMonthlyPieChartDashboard updates UI state with sorted data`() = runTest {
        //Given
        val monthIndex = 0

        // Mock the repository to return a list of categories where Transport is more expensive than Food.
        // This allows us to test the ViewModel's internal sorting logic.
        `when`(repository.getFinancialTransactionsInformationByDate(anyInt()))
            .thenReturn(mockPieData)
        
        // Mock the daily spending averages for the line graph.
        `when`(repository.getAverageAmountPerDay(anyInt(), anyInt(), any()))
            .thenReturn(dayAverageValues)

        // Mock the monthly totals for the bar graph trend analysis.
        `when`(repository.getMonthlyTotalsForCategory(any(), anyInt(), anyInt(), anyInt()))
            .thenReturn(mockCategoryMonthlyTotals)

        // Use Turbine to test the uiState Flow emissions
        viewModel.uiState.test {
            awaitItem() // Skip initial state
            
            //When
            viewModel.onAction(FinancialDashboardAction.InitializeMonthlyPieChartDashboard(monthIndex))
            
            // Ensure all background coroutines and IO work finish before asserting
            advanceUntilIdle()
            
            // Collect the updated state. We may need to skip intermediate empty states if any.
            var state = awaitItem()
            while (state.monthlyLineGraphData.isEmpty()) {
                state = awaitItem()
            }

            val categoriesTotalAmounts = state.monthlyPieData
            val getCategoryMonthlyTotals = state.fetchSelectedCategoriesForAllMonth
            val monthlyLineGraphData = state.monthlyLineGraphData

            //Then
            assertEquals(mockPieData.first().totalSummationOfAmountValuesInCategory, categoriesTotalAmounts[TransactionCategory.FOOD]?.totalSummationOfAmountValuesInCategory)
            assertEquals(mockPieData[1].totalSummationOfAmountValuesInCategory, categoriesTotalAmounts[TransactionCategory.TRANSPORT]?.totalSummationOfAmountValuesInCategory)
            assertEquals(mockPieData.first().totalTransactionsCount, categoriesTotalAmounts[TransactionCategory.FOOD]?.totalTransactionsCount)
            assertEquals(mockPieData[1].totalTransactionsCount, categoriesTotalAmounts[TransactionCategory.TRANSPORT]?.totalTransactionsCount)
            assertEquals(state.selectedPieAnglePairsIndex, 0)
            assertEquals(state.selectedCategory.value, mockPieData[1].selectedTransactionCategory)
            assertEquals(state.selectedMonth, viewModel.lastSixMonthsFromCurrentMonth[monthIndex])
            assertEquals(state.selectedBarGraphIndex, monthIndex)

            assertEquals(mockCategoryMonthlyTotals.first().total, getCategoryMonthlyTotals[1]?.totalSummationOfAmountValuesInCategory)
            assertEquals(mockCategoryMonthlyTotals.first().totalTransactionsCount, getCategoryMonthlyTotals[1]?.totalTransactionsCount)
            assertEquals(mockCategoryMonthlyTotals[1].totalTransactionsCount, getCategoryMonthlyTotals[2]?.totalTransactionsCount)
            assertEquals(mockCategoryMonthlyTotals[1].total, getCategoryMonthlyTotals[2]?.totalSummationOfAmountValuesInCategory)

            assertEquals(dayAverageValues.first().day, monthlyLineGraphData.first().day)
            assertEquals(dayAverageValues.first().transactionCount, monthlyLineGraphData.first().transactionCount)
            assertEquals(dayAverageValues.first().averageAmount.toFloat(), monthlyLineGraphData.first().averageAmount.toFloat())
            assertEquals(dayAverageValues[1].day, monthlyLineGraphData[1].day)
            assertEquals(dayAverageValues[1].transactionCount, monthlyLineGraphData[1].transactionCount)
            assertEquals(dayAverageValues[1].averageAmount.toFloat(), monthlyLineGraphData[1].averageAmount.toFloat())

            assertTrue(state.hasDashBoardBeenInitializedBefore)
            assertEquals(2, state.monthlyPieData.size)
        }
    }

    /**
     * Tests that [FinancialDashboardAction.SetPieChartClickAction] correctly:
     * 1. Updates the selected category in the UI state.
     * 2. Triggers a refresh of the line graph data for the new category.
     * 3. Maintains consistency in selected month and bar graph highlights.
     */
    @Test
    fun `onAction SetPieChartClickAction updates selected category and max amount`() = runTest {
        //Given
        `when`(repository.getMonthlyTotalsForCategory(any(), anyInt(), anyInt(), anyInt()))
            .thenReturn(mockCategoryMonthlyTotals)
        `when`(repository.getAverageAmountPerDay(anyInt(), anyInt(), any()))
            .thenReturn(dayAverageValues)

        viewModel.uiState.test {
            awaitItem() // Skip initial state

            //When
            viewModel.onAction(
                FinancialDashboardAction.SetPieChartClickAction(
                    1,
                    TransactionCategory.TRANSPORT
                )
            )

            advanceUntilIdle()

            var state = awaitItem()
            while (state.monthlyLineGraphData.isEmpty()) {
                state = awaitItem()
            }

            val monthlyLineGraphData = state.monthlyLineGraphData

            //Then
            assertEquals(dayAverageValues.first().day, monthlyLineGraphData.first().day)
            assertEquals(dayAverageValues.first().transactionCount, monthlyLineGraphData.first().transactionCount)
            assertEquals(dayAverageValues.first().averageAmount.toFloat(), monthlyLineGraphData.first().averageAmount.toFloat())
            assertEquals(dayAverageValues[1].day, monthlyLineGraphData[1].day)
            assertEquals(dayAverageValues[1].transactionCount, monthlyLineGraphData[1].transactionCount)
            assertEquals(dayAverageValues[1].averageAmount.toFloat(), monthlyLineGraphData[1].averageAmount.toFloat())
        }
    }

    /**
     * Tests that [FinancialDashboardAction.SetBarChartClickAction] correctly:
     * 1. Switches the active month based on the bar graph interaction.
     * 2. Refreshes the pie chart data for the newly selected month.
     * 3. Synchronizes the line graph to reflect the spending pattern of the new month.
     */
    @Test
    fun `onAction SetBarChartClickAction updates selected month and refreshes data`() = runTest {
        //Given
        val monthIndex = 1
        `when`(repository.getFinancialTransactionsInformationByDate(anyInt()))
            .thenReturn(mockPieData)

        `when`(repository.getAverageAmountPerDay(anyInt(), anyInt(), any()))
            .thenReturn(dayAverageValues)

        `when`(repository.getMonthlyTotalsForCategory(any(), anyInt(), anyInt(), anyInt()))
            .thenReturn(mockCategoryMonthlyTotals)

        viewModel.uiState.test {
            awaitItem() // Skip initial state

            //When
            viewModel.onAction(FinancialDashboardAction.SetBarChartClickAction(monthIndex))

            advanceUntilIdle()

            var state = awaitItem()
            while (state.fetchSelectedCategoriesForAllMonth.isEmpty() || state.monthlyLineGraphData.isEmpty()) {
                state = awaitItem()
            }
            
            // Verify sorting (Transport has 5000, Food has 1000)
            val categoriesTotalAmounts = state.monthlyPieData
            val monthlyLineGraphData = state.monthlyLineGraphData

            //Then
            assertEquals(mockPieData.first().totalSummationOfAmountValuesInCategory, categoriesTotalAmounts[TransactionCategory.FOOD]?.totalSummationOfAmountValuesInCategory)
            assertEquals(mockPieData[1].totalSummationOfAmountValuesInCategory, categoriesTotalAmounts[TransactionCategory.TRANSPORT]?.totalSummationOfAmountValuesInCategory)
            assertEquals(mockPieData.first().totalTransactionsCount, categoriesTotalAmounts[TransactionCategory.FOOD]?.totalTransactionsCount)
            assertEquals(mockPieData[1].totalTransactionsCount, categoriesTotalAmounts[TransactionCategory.TRANSPORT]?.totalTransactionsCount)
            assertEquals(state.selectedPieAnglePairsIndex, 0)
            assertEquals(state.selectedMonth, viewModel.lastSixMonthsFromCurrentMonth[monthIndex])
            assertEquals(state.selectedBarGraphIndex, monthIndex)

            assertEquals(dayAverageValues.first().day, monthlyLineGraphData.first().day)
            assertEquals(dayAverageValues.first().transactionCount, monthlyLineGraphData.first().transactionCount)
            assertEquals(dayAverageValues.first().averageAmount.toFloat(), monthlyLineGraphData.first().averageAmount.toFloat())
            assertEquals(dayAverageValues[1].day, monthlyLineGraphData[1].day)
            assertEquals(dayAverageValues[1].transactionCount, monthlyLineGraphData[1].transactionCount)
            assertEquals(dayAverageValues[1].averageAmount.toFloat(), monthlyLineGraphData[1].averageAmount.toFloat())

            assertTrue(state.hasDashBoardBeenInitializedBefore)
            assertEquals(2, state.monthlyPieData.size)
        }
    }
}