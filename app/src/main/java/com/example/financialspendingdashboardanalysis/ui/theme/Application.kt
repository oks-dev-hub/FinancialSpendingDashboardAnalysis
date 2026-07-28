package com.example.financialspendingdashboardanalysis.ui.theme

import android.app.Application
import com.example.financialspendingdashboardanalysis.FinancialTransactionRepository
import com.example.financialspendingdashboardanalysis.room.FinancialTransactionDatabaseProvider
import com.example.financialspendingdashboardanalysis.room.FinancialTransactionsDatabase

/**
 * Custom [Application] class for the Financial Spending Dashboard app.
 *
 * This class serves as the entry point for the application and is responsible for
 * initializing global dependencies. It uses a lazy initialization pattern for the
 * database and repository to ensure they are only created when first accessed,
 * optimizing the application's startup time.
 */
class FinancialTransactionApplication : Application() {

    /**
     * Lazy-initialized instance of the [FinancialTransactionsDatabase].
     * Provides access to the underlying Room database.
     */
    val database by lazy { FinancialTransactionDatabaseProvider.get(this) }

    /**
     * Lazy-initialized instance of the [FinancialTransactionRepository].
     * Acts as the single source of truth for transaction data throughout the app.
     */
    val financialTransactionRepository by lazy {
        FinancialTransactionRepository(database.financialTransactionsDao())
    }
}