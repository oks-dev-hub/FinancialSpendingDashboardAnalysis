package com.example.financialspendingdashboardanalysis.room

import android.content.Context
import androidx.room.Room

object FinancialTransactionDatabaseProvider {

    @Volatile
    private var INSTANCE: FinancialTransactionsDatabase? = null

    fun get(context: Context): FinancialTransactionsDatabase {

        return INSTANCE ?: synchronized(this) {

            Room.databaseBuilder(
                context,
                FinancialTransactionsDatabase::class.java,
                "financial.db"
            )
                .createFromAsset("financial.db")
                .fallbackToDestructiveMigration()
                .build()
                .also {
                    INSTANCE = it
                }
        }
    }
}