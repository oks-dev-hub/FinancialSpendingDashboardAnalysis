package com.example.financialspendingdashboardanalysis.room

import android.content.Context
import androidx.room.Room

/**
 * A provider object responsible for managing the singleton instance of [FinancialTransactionsDatabase].
 *
 * This provider ensures that only one instance of the database is created and used throughout
 * the application's lifecycle. It handles the initial creation of the database, including
 * pre-populating it from an asset file.
 */
object FinancialTransactionDatabaseProvider {

    @Volatile
    private var INSTANCE: FinancialTransactionsDatabase? = null

    /**
     * Retrieves the singleton instance of the [FinancialTransactionsDatabase].
     *
     * If the instance does not already exist, it is created using a thread-safe
     * double-checked locking pattern. The database is initialized by building it
     * from the "database/financial.db" asset file.
     *
     * @param context The application context used to initialize the Room database.
     * @return The singleton [FinancialTransactionsDatabase] instance.
     */
    fun get(context: Context): FinancialTransactionsDatabase {

        return INSTANCE ?: synchronized(this) {

            Room.databaseBuilder(
                context,
                FinancialTransactionsDatabase::class.java,
                "financial.db"
            )
            .createFromAsset("database/financial.db")
            .build()
            .also {
                INSTANCE = it
            }
        }
    }
}