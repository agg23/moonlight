package com.open.pin.ui.components.views

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Orientation options for ListView arrangements
 */
enum class ListViewOrientation {
    VERTICAL, HORIZONTAL
}

/**
 * Arranges a list of composables in a linear layout (either vertical or horizontal)
 * with customizable spacing and alignment.
 *
 * @param modifier Modifier for the layout container
 * @param contentPadding Padding around the entire content
 * @param orientation Direction of the list (vertical or horizontal)
 * @param spacing Space between items
 * @param horizontalAlignment Horizontal alignment of items within the layout (for vertical lists)
 * @param verticalAlignment Vertical alignment of items within the layout (for horizontal lists)
 * @param reverseLayout Whether to reverse the order of items
 * @param content The list of composable items to arrange
 */
@Composable
fun ListView(
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(0.dp),
    orientation: ListViewOrientation = ListViewOrientation.VERTICAL,
    spacing: Dp = 16.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    reverseLayout: Boolean = false,
    content: @Composable () -> Unit
) {
    when (orientation) {
        ListViewOrientation.VERTICAL -> {
            Column(
                modifier = modifier.padding(contentPadding),
                verticalArrangement = if (reverseLayout) {
                    // For reverse layouts, we'll use the built-in arrangements
                    Arrangement.Bottom
                } else {
                    // For normal layouts, use spacing with appropriate alignment
                    Arrangement.spacedBy(spacing)
                },
                horizontalAlignment = horizontalAlignment,
                content = { content() }
            )
        }
        ListViewOrientation.HORIZONTAL -> {
            Row(
                modifier = modifier.padding(contentPadding),
                horizontalArrangement = if (reverseLayout) {
                    // For reverse layouts, we'll use the built-in arrangements
                    Arrangement.End
                } else {
                    // For normal layouts, use spacing with appropriate alignment
                    Arrangement.spacedBy(spacing)
                },
                verticalAlignment = verticalAlignment,
                content = { content() }
            )
        }
    }
}
