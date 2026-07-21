package com.example.financialmodels

enum class TransactionCategory(val value: String) {
    PERSONAL("Personal"),
    FAMILY("Family"),
    MEDICAL("Medical"),
    FOOD("Food"),
    CASH_WITHDRAWAL("Cash Withdrawal"),
    ENTERTAINMENT("Entertainment"),
    TRANSPORT("Transport"),
    HOUSEHOLD("Household"),
    TRANSFER_FEES("Transfer Fees"),
    COMMUNICATIONS("Communication"),
    EDUCATION("Education"),
    INSURANCE("Insurance"),
    SAVING_AND_INVESTMENTS("Saving and investments"),
    UNKNOWN("Unknow")
}