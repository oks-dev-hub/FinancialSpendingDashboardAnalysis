package com.example.financialspendingdashboardanalysis.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.financialmodels.TransactionCategory

@Entity(tableName = "transactions",
    indices = [
        Index(value = [
            "month",
            "transaction_category",
            "amount"
        ]),
        Index(value = [
            "transaction_category",
            "year",
            "month",
            "amount"
        ])
    ])
class FinancialTransactionDataEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val year: Int,

    val month: Int,

    @ColumnInfo(name = "transaction_category")
    val transactionCategory: String,

    val merchant: String,

    val amount: Int,

    @ColumnInfo(name = "from_account")
    val fromAccount: String,

    @ColumnInfo(name = "fund_account")
    val fundAccount: String,

    @ColumnInfo(name = "payment_type")
    val paymentType: String,

    @ColumnInfo(name = "payment_date")
    val paymentDate: String
)