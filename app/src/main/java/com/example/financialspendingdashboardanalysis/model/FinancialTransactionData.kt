package com.example.financialspendingdashboardanalysis.model

import com.example.financialmodels.TransactionCategory

data class FinancialTransactionData(
    val transactionCategory: TransactionCategory = TransactionCategory.UNKNOWN,
    val transactionType: String = "",
    val amount: Long = 0,
    val fromAccount: String = "",
    val fundAccount: String = "",
    val paymentType: String = "",
    val paymentDate: String = "",
)