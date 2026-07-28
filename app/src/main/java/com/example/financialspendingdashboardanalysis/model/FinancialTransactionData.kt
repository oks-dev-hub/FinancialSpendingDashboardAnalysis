package com.example.financialspendingdashboardanalysis.model

import com.example.financialmodels.TransactionCategory

/**
 * Data model representing a single financial transaction for UI display.
 *
 * This class is a lightweight version of the database entity, optimized for use in the
 * presentation layer. It captures all relevant details of an expense.
 *
 * @property transactionCategory The category classification (e.g., Food, Transport).
 * @property transactionType A descriptive type for the transaction.
 * @property amount The monetary value of the transaction in cents.
 * @property fromAccount The source account used for the payment.
 * @property fundAccount The specific fund account associated with the transaction.
 * @property paymentType The method of payment used (e.g., Card, Cash).
 * @property paymentDate A formatted string representing when the transaction occurred.
 */
data class FinancialTransactionData(
    val transactionCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val transactionType: String = "",
    val amount: Long = 0,
    val fromAccount: String = "",
    val fundAccount: String = "",
    val paymentType: String = "",
    val paymentDate: String = "",
)