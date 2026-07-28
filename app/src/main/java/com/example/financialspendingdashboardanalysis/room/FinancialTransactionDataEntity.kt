package com.example.financialspendingdashboardanalysis.room

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.financialmodels.TransactionCategory

/**
 * Represents a single financial transaction record in the Room database.
 *
 * This entity defines the schema for the "transactions" table, which stores detailed information
 * about every expense, including the date, amount, category, and merchant involved.
 *
 * The table includes multiple indices to optimize performance for common dashboard queries,
 * specifically those filtering by month, category, and amount for trend analysis and chart generation.
 *
 * @property id The unique identifier for the transaction (auto-generated).
 * @property year The year the transaction occurred.
 * @property month The month the transaction occurred (1-12).
 * @property transactionCategory The category classification (e.g., Food, Transport).
 * @property merchant The name of the entity where the transaction took place.
 * @property amount The monetary value of the transaction in cents.
 * @property fromAccount The source account used for the payment.
 * @property fundAccount The specific fund account associated with the transaction.
 * @property paymentType The method of payment used (e.g., Card, Cash).
 * @property paymentDate The exact date and time of the transaction in string format.
 */
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