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
 * Displays a reusable screen header component with an optional
 * back navigation button.
 *
 * This composable is intended to provide a consistent header across
 * application screens while supporting optional backward navigation.
 *
 * The header consists of:
 * - A centered title
 * - An optional back button aligned to the start of the container
 * - A rounded rectangular background
 *
 * ---
 *
 * ## UI Layout Structure
 *
 * Box
 * ├── Text (Centered)
 * │   └── Screen title
 * │
 * └── IconButton (Optional)
 *     └── Back Arrow Icon
 *
 * Layout Characteristics:
 * - Fixed height: 72.dp
 * - Full screen width
 * - Rounded corners (16.dp)
 * - Light gray background
 *
 * ## Behavior
 *
 * When [backButtonOnClick] is supplied:
 * - A back arrow icon is displayed.
 * - Pressing the icon invokes the callback.
 *
 * When [backButtonOnClick] is null:
 * - No navigation control is rendered.
 * - The title remains centered.
 *
 * ---
 *
 * ## Performance Analysis
 *
 * Time Complexity: O(1)
 *
 * The composable renders a fixed number of UI elements
 * regardless of input size.
 *
 * Space Complexity: O(1)
 *
 * No dynamic collections or additional memory allocations
 * proportional to input size are required.
 *
 * ---
 *
 * ## Recomposition Notes
 *
 * Recomposition occurs when:
 * - [headerItem] changes
 * - [backButtonOnClick] changes
 *
 * Since the composable contains a small and fixed UI hierarchy,
 * recomposition cost is negligible.
 *
 * @param headerItem Text displayed as the screen title.
 * @param backButtonOnClick Optional callback invoked when the
 * back button is pressed. If null, the back button is hidden.
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