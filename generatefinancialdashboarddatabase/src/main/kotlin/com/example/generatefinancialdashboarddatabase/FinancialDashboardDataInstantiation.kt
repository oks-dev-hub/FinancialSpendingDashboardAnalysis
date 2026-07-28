/**
 * Financial Dashboard Data Instantiation Module
 *
 * This module is responsible for generating a mock SQLite database for the Financial Spending Dashboard.
 * It populates a `financial.db` file with one million synthetic transaction records spanning a 
 * six-month period.
 *
 * The generated data includes:
 * - Transaction categories (Food, Transport, Entertainment, etc.)
 * - Merchant details with randomized amounts within realistic ranges.
 * - Payment types (Card, Debit Order, EFT, etc.)
 * - Randomized dates within the first half of 2026.
 *
 * The module also optimizes the database for performance by:
 * - Using batch insertions and manual transaction commits.
 * - Configuring SQLite pragmas for speed (Journal Mode DELETE, Synchronous OFF).
 * - Creating specialized indices to support common dashboard queries.
 */

package com.example.generatefinancialdashboarddatabase

import com.example.financialmodels.Merchant
import com.example.financialmodels.TransactionCategory
import java.io.File
import java.sql.Connection
import java.sql.DriverManager
import java.util.Random
import kotlin.random.asKotlinRandom
import java.time.LocalDate
import java.time.temporal.ChronoUnit

//Generate a MILLION randomized transactions
const val TOTAL_ROWS = 1_000_000
val random = Random().asKotlinRandom()
val accountTypes = listOf("Debit Account", "Cheque Account", "Credit Account")
val merchants = listOf(
    // FOOD
    Merchant("Checkers", TransactionCategory.FOOD, "CARD", 80, 15000),
    Merchant("Shoprite", TransactionCategory.FOOD, "CARD", 60, 12000),
    Merchant("Pick n Pay", TransactionCategory.FOOD, "CARD", 80, 14000),
    Merchant("Woolworths", TransactionCategory.FOOD, "CARD", 120, 18000),
    Merchant("Spar", TransactionCategory.FOOD, "CARD", 50, 8000),
    Merchant("Food Lovers Market", TransactionCategory.FOOD, "CARD", 90, 7000),
    Merchant("Makro", TransactionCategory.FOOD, "CARD", 250, 20000),
    Merchant("Nandos", TransactionCategory.FOOD, "CARD", 90, 500),
    Merchant("KFC", TransactionCategory.FOOD, "CARD", 60, 400),
    Merchant("McDonalds", TransactionCategory.FOOD, "CARD", 50, 350),
    Merchant("Burger King", TransactionCategory.FOOD, "CARD", 60, 400),
    Merchant("Steers", TransactionCategory.FOOD, "CARD", 70, 500),
    Merchant("Debonairs", TransactionCategory.FOOD, "CARD", 90, 600),
    Merchant("Vida e Caffe", TransactionCategory.FOOD, "CARD", 40, 250),
    Merchant("Starbucks", TransactionCategory.FOOD, "CARD", 50, 300),

    // TRANSPORT
    Merchant("Shell", TransactionCategory.TRANSPORT, "CARD", 300, 3000),
    Merchant("Engen", TransactionCategory.TRANSPORT, "CARD", 300, 3500),
    Merchant("BP", TransactionCategory.TRANSPORT, "CARD", 300, 3500),
    Merchant("Sasol", TransactionCategory.TRANSPORT, "CARD", 300, 3500),
    Merchant("Uber", TransactionCategory.TRANSPORT, "CREDIT_CARD", 40, 800),
    Merchant("Bolt", TransactionCategory.TRANSPORT, "CARD", 40, 700),
    Merchant("Gautrain", TransactionCategory.TRANSPORT, "CARD", 50, 500),
    Merchant("Uber Eats Delivery", TransactionCategory.TRANSPORT, "CARD", 20, 120),

    // ENTERTAINMENT
    Merchant("Netflix", TransactionCategory.ENTERTAINMENT, "CARD", 199, 400),
    Merchant("Disney+", TransactionCategory.ENTERTAINMENT, "CARD", 120, 250),
    Merchant("Spotify", TransactionCategory.ENTERTAINMENT, "CARD", 60, 120),
    Merchant("Apple Music", TransactionCategory.ENTERTAINMENT, "CARD", 70, 150),
    Merchant("Showmax", TransactionCategory.ENTERTAINMENT, "CARD", 100, 250),
    Merchant("Ster Kinekor", TransactionCategory.ENTERTAINMENT, "CARD", 120, 600),
    Merchant("Nu Metro", TransactionCategory.ENTERTAINMENT, "CARD", 120, 600),
    Merchant("Exclusive Books", TransactionCategory.ENTERTAINMENT, "CARD", 100, 2500),
    Merchant("Steam", TransactionCategory.ENTERTAINMENT, "CARD", 100, 3000),
    Merchant("PlayStation Store", TransactionCategory.ENTERTAINMENT, "CARD", 100, 3000),

    // PERSONAL
    Merchant("Clicks", TransactionCategory.PERSONAL, "CARD", 40, 2500),
    Merchant("Dis-Chem", TransactionCategory.PERSONAL, "CARD", 50, 3000),
    Merchant("Barber Shop", TransactionCategory.PERSONAL, "CARD", 100, 800),
    Merchant("Sorbet", TransactionCategory.PERSONAL, "CARD", 150, 1500),
    Merchant("Virgin Active", TransactionCategory.PERSONAL, "DEBIT_ORDER", 500, 1500),
    Merchant("Planet Fitness", TransactionCategory.PERSONAL, "DEBIT_ORDER", 400, 1200),
    Merchant("Cotton On", TransactionCategory.PERSONAL, "CARD", 200, 3000),
    Merchant("Mr Price", TransactionCategory.PERSONAL, "CARD", 150, 2500),
    Merchant("Sportscene", TransactionCategory.PERSONAL, "CARD", 200, 3500),
    Merchant("Totalsports", TransactionCategory.PERSONAL, "CARD", 250, 4000),

    // MEDICAL
    Merchant("Mediclinic", TransactionCategory.MEDICAL, "CARD", 300, 10000),
    Merchant("Life Healthcare", TransactionCategory.MEDICAL, "CARD", 500, 15000),
    Merchant("Netcare", TransactionCategory.MEDICAL, "CARD", 500, 15000),
    Merchant("Lancet", TransactionCategory.MEDICAL, "CARD", 200, 4000),
    Merchant("Ampath", TransactionCategory.MEDICAL, "CARD", 200, 4000),

    // HOUSEHOLD
    Merchant("Builders Warehouse", TransactionCategory.HOUSEHOLD, "CARD", 150, 15000),
    Merchant("Game", TransactionCategory.HOUSEHOLD, "CARD", 200, 10000),
    Merchant("Makro Home", TransactionCategory.HOUSEHOLD, "CARD", 200, 15000),
    Merchant("Takealot", TransactionCategory.HOUSEHOLD, "CARD", 100, 12000),
    Merchant("Mr Price Home", TransactionCategory.HOUSEHOLD, "CARD", 100, 5000),
    Merchant("Sheet Street", TransactionCategory.HOUSEHOLD, "CARD", 100, 4000),
    Merchant("Pep Home", TransactionCategory.HOUSEHOLD, "CARD", 50, 2500),

    // COMMUNICATION
    Merchant("Vodacom", TransactionCategory.COMMUNICATIONS, "DEBIT_ORDER", 100, 2500),
    Merchant("MTN", TransactionCategory.COMMUNICATIONS, "DEBIT_ORDER", 100, 2500),
    Merchant("Cell C", TransactionCategory.COMMUNICATIONS, "DEBIT_ORDER", 100, 2000),
    Merchant("Telkom", TransactionCategory.COMMUNICATIONS, "DEBIT_ORDER", 150, 3000),
    Merchant("Rain", TransactionCategory.COMMUNICATIONS, "DEBIT_ORDER", 300, 1000),

    // EDUCATION
    Merchant("University Fees", TransactionCategory.EDUCATION, "EFT", 5000, 60000),
    Merchant("School Fees", TransactionCategory.EDUCATION, "DEBIT_ORDER", 1000, 12000),
    Merchant("Udemy", TransactionCategory.EDUCATION, "CARD", 200, 3000),
    Merchant("Coursera", TransactionCategory.EDUCATION, "CARD", 300, 5000),
    Merchant("Pearson", TransactionCategory.EDUCATION, "CARD", 200, 4000),

    // INSURANCE
    Merchant("Discovery", TransactionCategory.INSURANCE, "DEBIT_ORDER", 900, 2500),
    Merchant("Momentum", TransactionCategory.INSURANCE, "DEBIT_ORDER", 800, 3000),
    Merchant("Old Mutual", TransactionCategory.INSURANCE, "DEBIT_ORDER", 600, 5000),
    Merchant("Santam", TransactionCategory.INSURANCE, "DEBIT_ORDER", 500, 4000),
    Merchant("OUTsurance", TransactionCategory.INSURANCE, "DEBIT_ORDER", 500, 3500),

    // FAMILY
    Merchant("Spur", TransactionCategory.FAMILY, "CARD", 150, 1200),
    Merchant("Toy Kingdom", TransactionCategory.FAMILY, "CARD", 150, 4000),
    Merchant("Hamleys", TransactionCategory.FAMILY, "CARD", 150, 4000),
    Merchant("Kids Emporium", TransactionCategory.FAMILY, "CARD", 200, 5000),
    Merchant("Bounce", TransactionCategory.FAMILY, "CARD", 100, 1200),

    // CASH WITHDRAWAL
    Merchant("ATM Withdrawal", TransactionCategory.CASH_WITHDRAWAL, "ATM", 100, 5000),
    Merchant("Branch Withdrawal", TransactionCategory.CASH_WITHDRAWAL, "ATM", 200, 10000),

    // TRANSFER FEES
    Merchant("EFT Fee", TransactionCategory.TRANSFER_FEES, "BANK_FEE", 2, 50),
    Merchant("Immediate Payment Fee", TransactionCategory.TRANSFER_FEES, "BANK_FEE", 10, 150),
    Merchant("Monthly Account Fee", TransactionCategory.TRANSFER_FEES, "BANK_FEE", 80, 350),
    Merchant("Card Replacement Fee", TransactionCategory.TRANSFER_FEES, "BANK_FEE", 100, 350),

    // SAVINGS & INVESTMENTS
    Merchant("Investment Deposit", TransactionCategory.SAVING_AND_INVESTMENTS, "DEBIT_ORDER", 500, 15000),
    Merchant("EasyEquities", TransactionCategory.SAVING_AND_INVESTMENTS, "EFT", 200, 50000),
    Merchant("Satrix", TransactionCategory.SAVING_AND_INVESTMENTS, "EFT", 500, 30000),
    Merchant("Allan Gray", TransactionCategory.SAVING_AND_INVESTMENTS, "DEBIT_ORDER", 1000, 50000),
    Merchant("Ninety One", TransactionCategory.SAVING_AND_INVESTMENTS, "DEBIT_ORDER", 1000, 50000),
)

val startDate: LocalDate = LocalDate.of(2026, 1, 1)
val endDate: LocalDate = LocalDate.of(2026, 6, 30)
val daysRange = ChronoUnit.DAYS.between(startDate, endDate).toInt()

/**
 * Generates a random date between January 1, 2026, and June 30, 2026.
 *
 * @return A [LocalDate] within the specified six-month range.
 */
fun randomDate(): LocalDate =
    startDate.plusDays(random.nextInt(daysRange).toLong())

/**
 * Entry point for the database generation process.
 *
 * This function performs the following steps:
 * 1. Sets up the target database folder and file.
 * 2. Establishes a JDBC connection to SQLite.
 * 3. Configures performance pragmas and creates the `transactions` table schema.
 * 4. Iteratively generates and batch-inserts [TOTAL_ROWS] records.
 * 5. Creates performance-critical indices for month-based and category-based aggregation.
 * 6. Executes `ANALYZE` to optimize the SQLite query planner.
 */
fun main() {

    Class.forName("org.sqlite.JDBC")
    val dbFolder = File(
        "C:/Users/User/AndroidStudioProjects/FinancialSpendingDashboardAnalysis/app/src/main/assets/database"
    )

    if (!dbFolder.exists()) {
        dbFolder.mkdirs()
    }

    val dbFile = File(dbFolder, "financial.db")

// Delete old database if you want a fresh generation
    if (dbFile.exists()) {
        dbFile.delete()
    }

    val conn = DriverManager.getConnection("jdbc:sqlite:${dbFile.absolutePath}")

    val stmt = conn.createStatement()

    // SPEED PRAGMAS
    stmt.execute("PRAGMA journal_mode = DELETE;")
    stmt.execute("PRAGMA synchronous = OFF;")
    stmt.execute("PRAGMA temp_store = MEMORY;")
    stmt.execute("PRAGMA cache_size = -100000;")

    conn.autoCommit = false

    // TABLE
    stmt.execute(
        """
        CREATE TABLE IF NOT EXISTS transactions (
            id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
            year INTEGER NOT NULL,
            month INTEGER NOT NULL,
            transaction_category TEXT NOT NULL,
            merchant TEXT NOT NULL,
            amount INTEGER NOT NULL,
            from_account TEXT NOT NULL,
            fund_account TEXT NOT NULL,
            payment_type TEXT NOT NULL,
            payment_date TEXT NOT NULL
        );
        """.trimIndent()
    )

    val sql = """
        INSERT INTO transactions (
            year, month,
            transaction_category,
            merchant,
            amount,
            from_account,
            fund_account,
            payment_type,
            payment_date
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """.trimIndent()

    val ps = conn.prepareStatement(sql)

    var batchCount = 0

    for (i in 1..TOTAL_ROWS) {

        val date = randomDate()

        val merchant = merchants.random(random)
        val paymentType = merchant.paymentType
        val amount = random.nextInt(merchant.minAmount, merchant.maxAmount + 1)
        val category = merchant.category

        val fromAccount = accountTypes.random(random)

        val paymentDate = date.toString()
        val year = date.year
        val month = date.monthValue

        ps.setInt(1, year)
        ps.setInt(2, month)
        ps.setString(3, category.value)
        ps.setString(4, merchant.name)
        ps.setInt(5, amount)
        ps.setString(6, fromAccount)
        ps.setString(7, fromAccount)
        ps.setString(8, paymentType)
        ps.setString(9, paymentDate)

        ps.addBatch()
        batchCount++

        if (batchCount == 50_000) {
            ps.executeBatch()
            conn.commit()
            batchCount = 0
            println("Inserted: $i rows")
        }
    }

    ps.executeBatch()
    ps.clearBatch()
    conn.commit()

    // Optimized for:
    // SELECT transaction_category, SUM(amount)
    // FROM transactions
    // WHERE month = ?
    // GROUP BY transaction_category
    stmt.execute("""
        CREATE INDEX IF NOT EXISTS
        index_transactions_month_transaction_category_amount
        ON transactions(month, transaction_category, amount);
    """.trimIndent())

    // Optimized for:
    // SELECT year, month, SUM(amount)
    // FROM transactions
    // WHERE transaction_category = ?
    //   AND year = ?
    //   AND month BETWEEN ? AND ?
    // GROUP BY year, month
    stmt.execute("""
        CREATE INDEX IF NOT EXISTS
        index_transactions_transaction_category_year_month_amount
        ON transactions(transaction_category, year, month, amount);
    """.trimIndent())

    conn.commit()

    stmt.execute("ANALYZE")

    ps.close()
    stmt.close()
    conn.close()
}