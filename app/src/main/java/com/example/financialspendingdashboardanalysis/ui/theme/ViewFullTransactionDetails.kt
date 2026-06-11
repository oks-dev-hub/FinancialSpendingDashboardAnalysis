package com.example.financialspendingdashboardanalysis.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontSynthesis
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.financialspendingdashboardanalysis.R
import com.example.financialspendingdashboardanalysis.model.FinancialTransactionData
import com.example.financialspendingdashboardanalysis.navGraph.DisplayFinancialDashboardRoute

/**
 * Displays the complete details of a selected financial transaction.
 *
 * This screen is responsible for presenting all available information
 * associated with a single [FinancialTransactionData] instance in a
 * structured and user-friendly format.
 *
 * Information displayed includes:
 * - Transaction category
 * - Transaction type
 * - Transaction amount
 * - Source account
 * - Destination account
 * - Transaction date
 * - Payment method
 *
 * The screen also provides navigation back to the Financial Dashboard.
 *
 * ---
 *
 * ## UI Layout Structure
 *
 * Column
 * ├── TransactionHeaderItem
 * │   └── Back navigation button
 * │
 * ├── Spacer
 * │
 * ├── Category Card
 * │   └── Category Name
 * │
 * ├── Spacer
 * │
 * └── Details Card
 *     ├── Transaction Type
 *     ├── Amount
 *     ├── From Account
 *     ├── Fund Account
 *     ├── Transaction Date
 *     └── Payment Type
 *
 * ---
 *
 * ## Navigation Flow
 *
 * Back Button
 *      ↓
 * Financial Dashboard
 *
 * The dashboard screen is restored using:
 * - popUpTo(...)
 * - launchSingleTop = true
 *
 * This prevents duplicate dashboard destinations from being added
 * to the navigation back stack.
 *
 * ---
 *
 * ## Performance Analysis
 *
 * Time Complexity: O(1)
 *
 * A fixed number of UI elements are rendered regardless of
 * transaction contents.
 *
 * Space Complexity: O(1)
 *
 * No additional collections or dynamic structures are created.
 *
 * ---
 *
 * ## Recomposition Notes
 *
 * Recomposition occurs only when:
 * - [financialTransactionData] changes
 * - Navigation state changes
 *
 * Since the screen contains a fixed amount of content,
 * recomposition cost is minimal.
 *
 * @param navController Navigation controller used for screen transitions.
 * @param financialTransactionData Transaction whose details are displayed.
 */
@Composable
fun ViewFullTransactionDetails(
    navController: NavController,
    financialTransactionData: FinancialTransactionData
) {
    Column(
       modifier = Modifier
           .fillMaxSize()
           .background(Color.LightGray.copy(alpha = 0.2f))
    ) {
        //This is the Header part of the screen that uniquely identifies the screen
        TransactionHeaderItem(
            headerItem = stringResource(R.string.transactionDetails),
            backButtonOnClick = {
                navController.navigate(DisplayFinancialDashboardRoute) {
                    popUpTo(DisplayFinancialDashboardRoute) { inclusive = true }
                    launchSingleTop = true
                }
            }
        )

        Spacer(modifier = Modifier
            .height(20.dp)
            .background(Color.LightGray.copy(alpha = 0.2f))
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            Text(
                text = setUpUnitTransactionDetail(
                    detailTitle = R.string.category,
                    detailValue = financialTransactionData.transactionCategory.name
                )
            )
        }

        Spacer(modifier = Modifier
            .height(30.dp)
            .background(Color.LightGray.copy(alpha = 0.2f))
        )

        //All the Text below represent the Header part and UI part for each item
        //It displays all the fields in the TransactionData item.
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceEvenly
        ) {
            Text(
                text = setUpUnitTransactionDetail(
                    detailTitle = R.string.transactionType,
                    detailValue = financialTransactionData.transactionType,
                )
            )


            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = setUpUnitTransactionDetail(
                    detailTitle = R.string.amount,
                    detailValue = convertValueToAMount(inputAmount = financialTransactionData.amount),
                )
            )

            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = setUpUnitTransactionDetail(
                    detailTitle = R.string.fromAccount,
                    detailValue = financialTransactionData.fromAccount,
                )
            )

            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = setUpUnitTransactionDetail(
                    detailTitle = R.string.fundAccount,
                    detailValue = financialTransactionData.fundAccount,
                )
            )

            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = setUpUnitTransactionDetail(
                    detailTitle = R.string.transactionDate,
                    detailValue = financialTransactionData.paymentDate
                )
            )

            Text(
                modifier = Modifier.padding(top = 16.dp),
                text = setUpUnitTransactionDetail(
                    detailTitle = R.string.paymentType,
                    detailValue = financialTransactionData.paymentType
                )
            )
        }
    }
}


/**
 * Creates a styled transaction detail entry using an [AnnotatedString].
 *
 * This helper function formats a transaction detail into a two-line
 * text representation:
 *
 * Example:
 *
 * Category
 * Food & Dining
 *
 * The title and value are rendered using different text styles to
 * improve visual hierarchy and readability.
 *
 * Styling:
 * - Title:
 *   - Normal weight
 *   - 16sp
 *
 * - Value:
 *   - Medium weight
 *   - 18sp
 *
 * ---
 *
 * ## Visual Output
 *
 * Transaction Type
 * Debit Card Purchase
 *
 * Amount
 * R1 250.00
 *
 * ---
 *
 * ## Performance Analysis
 *
 * Time Complexity: O(n)
 *
 * Where:
 * - n = length of generated text.
 *
 * Building an [AnnotatedString] requires appending all characters once.
 *
 * Space Complexity: O(n)
 *
 * The resulting [AnnotatedString] stores a copy of the formatted text
 * and associated styling spans.
 *
 * ---
 *
 * ## Usage
 *
 * This function is intended to standardize transaction detail formatting
 * throughout the application and reduce duplicate styling code.
 *
 * @param detailTitle String resource identifier representing the detail label.
 * @param detailValue Value associated with the detail label.
 *
 * @return An [AnnotatedString] containing styled title and value text.
 */
@Composable
fun setUpUnitTransactionDetail(detailTitle: Int, detailValue: String): AnnotatedString {
    return buildAnnotatedString {

        //This is the Header part of the Item
        withStyle(
            style = SpanStyle(
                fontSynthesis = FontSynthesis.All,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )
        ) {
            append(stringResource(detailTitle))
        }

        //This is the value part of the Item
        withStyle(
            style = SpanStyle(
                fontSynthesis = FontSynthesis.All,
                fontWeight = FontWeight.Medium,
                fontSize = 18.sp
            )
        ) {
            append("\n$detailValue")
        }
    }
}