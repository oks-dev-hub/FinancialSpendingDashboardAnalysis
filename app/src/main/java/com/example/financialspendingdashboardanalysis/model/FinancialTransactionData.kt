package com.example.financialspendingdashboardanalysis.model

data class FinancialTransactionData(
    val transactionCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val transactionType: String = "",
    val amount: Int = 0,
    val fromAccount: String = "",
    val fundAccount: String = "",
    val paymentType: String = "",
    val paymentDate: String = "",
)