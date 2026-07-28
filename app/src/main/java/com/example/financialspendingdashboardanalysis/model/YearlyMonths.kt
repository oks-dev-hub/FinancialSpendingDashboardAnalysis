package com.example.financialspendingdashboardanalysis.model

/**
 * Enum representing the twelve months of the year.
 *
 * Each entry maps to its human-readable string representation, facilitating
 * consistent display and data filtering across the dashboard and database queries.
 *
 * @property month The full name of the month (e.g., "January").
 */
enum class YearlyMonths(val month: String) {
    JANUARY("January"),
    FEBRUARY("February"),
    MARCH("March"),
    APRIL("April"),
    MAY("May"),
    JUNE("June"),
    JULY("July"),
    AUGUST("August"),
    SEPTEMBER("September"),
    OCTOBER("October"),
    NOVEMBER("November"),
    DECEMBER("December")
}