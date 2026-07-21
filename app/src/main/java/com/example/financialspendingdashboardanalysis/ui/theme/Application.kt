package com.example.financialspendingdashboardanalysis.ui.theme

import android.app.Application
import com.example.financialspendingdashboardanalysis.FinancialTransactionRepository
import com.example.financialspendingdashboardanalysis.room.FinancialTransactionDatabaseProvider

class FinancialTransactionApplication : Application() {

    val database by lazy { FinancialTransactionDatabaseProvider.get(this) }

    val financialTransactionRepository by lazy {
        FinancialTransactionRepository(database.financialTransactionsDao())
    }
}