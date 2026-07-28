package com.example.financialmodels

/**
 * Represents a business or entity where financial transactions occur.
 *
 * Each merchant is associated with a specific [TransactionCategory] and
 * typically has an expected range of transaction amounts.
 *
 * @property name The display name of the merchant.
 * @property category The financial category this merchant's transactions fall under.
 * @property paymentType The primary method of payment (e.g., "Card", "Cash").
 * @property minAmount The typical minimum transaction value in cents.
 * @property maxAmount The typical maximum transaction value in cents.
 */
data class Merchant(
    val name: String,
    val category: TransactionCategory,
    val paymentType: String,
    val minAmount: Int,
    val maxAmount: Int
)