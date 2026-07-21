package com.example.financialmodels

data class Merchant(
    val name: String,
    val category: TransactionCategory,
    val paymentType: String,
    val minAmount: Int,
    val maxAmount: Int
)