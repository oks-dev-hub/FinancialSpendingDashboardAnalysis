package com.example.financialspendingdashboardanalysis.room

import androidx.room.TypeConverter
import com.example.financialmodels.TransactionCategory

class RoomConverters {
    @TypeConverter
    fun fromTransactionCategory(category: TransactionCategory): String {
        return category.name
    }

    @TypeConverter
    fun toTransactionCategory(category: String): TransactionCategory {
        return try {
            TransactionCategory.valueOf(category)
        } catch (e: IllegalArgumentException) {
            TransactionCategory.UNKNOWN
        }
    }
}
