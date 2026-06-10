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

@Composable
fun setUpUnitTransactionDetail(detailTitle: Int, detailValue: String): AnnotatedString {
    return buildAnnotatedString {
        withStyle(
            style = SpanStyle(
                fontSynthesis = FontSynthesis.All,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp
            )
        ) {
            append(stringResource(detailTitle))
        }

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