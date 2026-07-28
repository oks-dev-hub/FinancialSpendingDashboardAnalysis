package com.example.financialspendingdashboardanalysis.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.NavigateNext
import androidx.compose.material.icons.filled.NavigateNext
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


/**
 * Displays a single transaction entry within a list, providing a summary of the expense.
 *
 * This component is designed to be used in a vertical list (e.g., in the transaction details
 * screen). It supports custom rounding of corners via [isStartIndex] and [isLastIndex] to
 * allow for grouped appearances within a larger list.
 *
 * ### UI Features:
 * - **Merchant/Recipient**: Displays the name or purpose of the transaction.
 * - **Timestamp**: Shows when the transaction occurred.
 * - **Amount**: Displays the formatted monetary value.
 * - **Navigation Icon**: A visual cue indicating that the item is clickable for more details.
 *
 * ### Interaction:
 * The entire row is interactive. Tapping anywhere on the item or the specific navigation
 * icon will trigger the [transactionOnClick] callback.
 *
 * @param isStartIndex True if this is the first item in a group, applying top rounded corners.
 * @param isLastIndex True if this is the last item in a group, applying bottom rounded corners.
 * @param transactionReceiptMeaning The name of the merchant or recipient.
 * @param transactionDateTimeStamp The formatted date and time string.
 * @param amount The formatted currency string (e.g., "R234.50").
 * @param transactionOnClick Callback triggered when the item is selected.
 */
@Composable
fun TransactionDetailsListItem(
    isStartIndex: Boolean = false,
    isLastIndex: Boolean = false,
    transactionReceiptMeaning: String,
    transactionDateTimeStamp: String,
    amount: String,
    transactionOnClick: () -> Unit = {}
) {
    val topPadding = if (isStartIndex) 16.dp else 0.dp
    val bottomPadding = if (isLastIndex) 16.dp else 0.dp
    val shape = RoundedCornerShape(
        topStart = topPadding,
        topEnd = topPadding,
        bottomStart = bottomPadding,
        bottomEnd = bottomPadding
    )

    Box(
       modifier = Modifier
           .fillMaxSize()
           .wrapContentHeight()
           .clip(shape)
           .background(color = Color.White, shape)
           .padding(horizontal = 16.dp, vertical = 10.dp)
           .clickable { transactionOnClick() }
    ) {
        Column(
            modifier = Modifier.align(Alignment.CenterStart)
        ) {
            Text(text = transactionReceiptMeaning)
            Text(text = transactionDateTimeStamp)
        }

        Row(
            modifier = Modifier
                .align(Alignment.CenterEnd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = amount,
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = transactionOnClick
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.NavigateNext,
                    contentDescription = null
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionDetailsListItemPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            ) {
                TransactionDetailsListItem(
                    transactionReceiptMeaning = "Mama",
                    transactionDateTimeStamp = "20 May 2025",
                    amount = "R234.50"
                )
            }
        }
    }
}