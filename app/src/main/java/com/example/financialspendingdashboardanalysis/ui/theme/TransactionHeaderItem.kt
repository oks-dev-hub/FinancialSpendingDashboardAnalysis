package com.example.financialspendingdashboardanalysis.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * A reusable header component for the financial dashboard and detail screens.
 *
 * This composable provides a consistent top bar appearance, featuring a centered title
 * and an optional back navigation button. It uses the theme's primary container color
 * to establish a clear visual hierarchy.
 *
 * ### UI Features:
 * - **Centered Title**: Displays the [headerItem] text using the `titleLarge` typography.
 * - **Optional Navigation**: If [backButtonOnClick] is provided, an "Arrow Back" icon
 *   is displayed on the leading edge.
 * - **Elevation**: Uses a slight tonal elevation (3.dp) to distinguish the header from the content.
 *
 * @param headerItem The title text to be displayed in the center of the header.
 * @param backButtonOnClick Optional callback for the back button. If null, the button is hidden.
 */
@Composable
fun TransactionHeaderItem(
    headerItem: String,
    backButtonOnClick: (() -> Unit)? = null
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        color = MaterialTheme.colorScheme.primaryContainer,
        tonalElevation = 3.dp,
        shadowElevation = 0.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = headerItem,
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.surface
            )

            if (backButtonOnClick != null) {
                IconButton(
                    modifier = Modifier.align(Alignment.CenterStart),
                    onClick = backButtonOnClick
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primaryContainer
                    )
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun TransactionHeaderItemPreview() {
    MaterialTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White)
            )
        }
    }
}