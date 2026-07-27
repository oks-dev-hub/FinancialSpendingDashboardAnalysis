package com.example.financialspendingdashboardanalysis.room

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [FinancialTransactionDataEntity::class],
    version = 1,
    exportSchema = true
)
abstract class FinancialTransactionsDatabase : RoomDatabase() {

    abstract fun financialTransactionsDao(): FinancialTransactionsDao
}