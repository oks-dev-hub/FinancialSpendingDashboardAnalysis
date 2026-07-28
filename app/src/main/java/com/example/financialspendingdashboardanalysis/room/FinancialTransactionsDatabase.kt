package com.example.financialspendingdashboardanalysis.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

/**
 * The Room database for the Financial Spending Dashboard application.
 *
 * This database stores financial transaction data and serves as the local source of truth.
 * It uses [FinancialTransactionDataEntity] as its primary data structure and provides
 * access to data operations through the [FinancialTransactionsDao].
 *
 * The database is pre-populated from an asset file during its initial creation via the
 * [FinancialTransactionDatabaseProvider].
 */
@Database(
    entities = [FinancialTransactionDataEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FinancialTransactionsDatabase : RoomDatabase() {

    /**
     * Provides access to the Data Access Object (DAO) for financial transaction operations.
     *
     * @return An instance of [FinancialTransactionsDao].
     */
    abstract fun financialTransactionsDao(): FinancialTransactionsDao
}
