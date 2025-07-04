package com.open.pin.ui.components.views

import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.open.pin.ui.scroll.ScrollBehavior
import com.open.pin.ui.scroll.ScrollBehaviorConfig
import com.open.pin.ui.scroll.ScrollConfig
import com.open.pin.ui.scroll.SimpleScrollBehavior
import com.open.pin.ui.scroll.rememberScrollBehaviorState
import com.open.pin.ui.theme.PinScrollDimensions

/**
 * Orientation options for ListView arrangements
 */
enum class ListViewOrientation {
    VERTICAL, HORIZONTAL
}

/**
 * A unified list component that supports both simple and advanced scrolling.
 * Features orientation support, indentation, optional gradient overlays, and scroll buttons.
 *
 * @param modifier Modifier for the container
 * @param leftIndent Left side spacing/indentation for the list
 * @param rightIndent Right side spacing/indentation for the list  
 * @param topPadding Top padding around the content
 * @param bottomPadding Bottom padding around the content
 * @param orientation Direction of the list (vertical or horizontal)
 * @param itemSpacing Space between individual items
 * @param horizontalAlignment Horizontal alignment of items (for vertical lists)
 * @param verticalAlignment Vertical alignment of items (for horizontal lists)
 * @param showScrollButtons Whether to show scroll up/down buttons with gradients
 * @param scrollAmount Amount to scroll per button press in dp
 * @param autoHideButtons Whether to auto-hide scroll buttons at boundaries
 * @param enableSnapToCenter Whether to enable snap-to-center behavior
 * @param listState Optional LazyListState for external scroll control
 * @param content The composable content to arrange
 */
@Composable
fun ListView(
    modifier: Modifier = Modifier,
    leftIndent: Dp = 0.dp,
    rightIndent: Dp = 0.dp,
    topPadding: Dp = 0.dp,
    bottomPadding: Dp = 0.dp,
    orientation: ListViewOrientation = ListViewOrientation.VERTICAL,
    itemSpacing: Dp = 16.dp,
    horizontalAlignment: Alignment.Horizontal = Alignment.CenterHorizontally,
    verticalAlignment: Alignment.Vertical = Alignment.CenterVertically,
    showScrollButtons: Boolean = false,
    scrollAmount: Dp = PinScrollDimensions.baseScrollAmount,
    autoHideButtons: Boolean = true,
    enableSnapToCenter: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    content: @Composable () -> Unit
) {
    // Snap fling behavior for center alignment (only when enabled)
    val snapFlingBehavior = if (enableSnapToCenter) {
        rememberSnapFlingBehavior(lazyListState = listState)
    } else null
    
    // Configure scroll behavior
    val scrollConfig = ScrollBehaviorConfig(
        showScrollButtons = showScrollButtons,
        autoHideButtons = autoHideButtons,
        scrollConfig = ScrollConfig(baseScrollAmount = scrollAmount)
    )
    
    // Use clean scroll behavior system
    when (orientation) {
        ListViewOrientation.VERTICAL -> {
            VerticalListWithScroll(
                modifier = modifier,
                leftIndent = leftIndent,
                rightIndent = rightIndent,
                topPadding = topPadding,
                bottomPadding = bottomPadding,
                itemSpacing = itemSpacing,
                horizontalAlignment = horizontalAlignment,
                snapFlingBehavior = snapFlingBehavior,
                listState = listState,
                scrollConfig = scrollConfig,
                content = content
            )
        }
        ListViewOrientation.HORIZONTAL -> {
            HorizontalListWithScroll(
                modifier = modifier,
                leftIndent = leftIndent,
                rightIndent = rightIndent,
                topPadding = topPadding,
                bottomPadding = bottomPadding,
                itemSpacing = itemSpacing,
                verticalAlignment = verticalAlignment,
                snapFlingBehavior = snapFlingBehavior,
                listState = listState,
                scrollConfig = scrollConfig,
                content = content
            )
        }
    }
}

/**
 * Vertical list with clean scroll behavior
 */
@Composable
private fun VerticalListWithScroll(
    modifier: Modifier,
    leftIndent: Dp,
    rightIndent: Dp,
    topPadding: Dp,
    bottomPadding: Dp,
    itemSpacing: Dp,
    horizontalAlignment: Alignment.Horizontal,
    snapFlingBehavior: androidx.compose.foundation.gestures.FlingBehavior?,
    listState: LazyListState,
    scrollConfig: ScrollBehaviorConfig,
    content: @Composable () -> Unit
) {
    val contentPadding = PaddingValues(
        start = leftIndent,
        end = rightIndent,
        top = topPadding,
        bottom = bottomPadding
    )

    // Apply scroll behavior if needed
    if (scrollConfig.showScrollButtons) {
        ScrollBehavior(
            state = rememberScrollBehaviorState(scrollConfig, listState),
            modifier = modifier
        ) {
            VerticalLazyColumn(
                listState = listState,
                contentPadding = contentPadding,
                itemSpacing = itemSpacing,
                horizontalAlignment = horizontalAlignment,
                snapFlingBehavior = snapFlingBehavior,
                gradientHeight = scrollConfig.gradientHeight,
                content = content
            )
        }
    } else {
        VerticalLazyColumn(
            listState = listState,
            modifier = modifier,
            contentPadding = contentPadding,
            itemSpacing = itemSpacing,
            horizontalAlignment = horizontalAlignment,
            snapFlingBehavior = snapFlingBehavior,
            content = content
        )
    }
}

/**
 * Horizontal list with clean scroll behavior
 */
@Composable
private fun HorizontalListWithScroll(
    modifier: Modifier,
    leftIndent: Dp,
    rightIndent: Dp,
    topPadding: Dp,
    bottomPadding: Dp,
    itemSpacing: Dp,
    verticalAlignment: Alignment.Vertical,
    snapFlingBehavior: androidx.compose.foundation.gestures.FlingBehavior?,
    listState: LazyListState,
    scrollConfig: ScrollBehaviorConfig,
    content: @Composable () -> Unit
) {
    val contentPadding = PaddingValues(
        start = leftIndent,
        end = rightIndent,
        top = topPadding,
        bottom = bottomPadding
    )

    // Horizontal lists typically don't use scroll buttons, but support is here
    HorizontalLazyRow(
        listState = listState,
        modifier = modifier,
        contentPadding = contentPadding,
        itemSpacing = itemSpacing,
        verticalAlignment = verticalAlignment,
        snapFlingBehavior = snapFlingBehavior,
        content = content
    )
}

/**
 * Clean vertical lazy column implementation
 */
@Composable
private fun VerticalLazyColumn(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp,
    horizontalAlignment: Alignment.Horizontal,
    snapFlingBehavior: androidx.compose.foundation.gestures.FlingBehavior? = null,
    gradientHeight: Dp = 0.dp,
    content: @Composable () -> Unit
) {
    val adjustedPadding = PaddingValues(
        start = contentPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
        end = contentPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
        top = contentPadding.calculateTopPadding() + gradientHeight,
        bottom = contentPadding.calculateBottomPadding() + gradientHeight
    )

    if (snapFlingBehavior != null) {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = adjustedPadding,
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            horizontalAlignment = horizontalAlignment,
            flingBehavior = snapFlingBehavior
        ) {
            item { content() }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = adjustedPadding,
            verticalArrangement = Arrangement.spacedBy(itemSpacing),
            horizontalAlignment = horizontalAlignment
        ) {
            item { content() }
        }
    }
}

/**
 * Clean horizontal lazy row implementation
 */
@Composable
private fun HorizontalLazyRow(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    itemSpacing: Dp,
    verticalAlignment: Alignment.Vertical,
    snapFlingBehavior: androidx.compose.foundation.gestures.FlingBehavior? = null,
    content: @Composable () -> Unit
) {
    if (snapFlingBehavior != null) {
        LazyRow(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalAlignment = verticalAlignment,
            flingBehavior = snapFlingBehavior
        ) {
            item { content() }
        }
    } else {
        LazyRow(
            state = listState,
            modifier = modifier.fillMaxSize(),
            contentPadding = contentPadding,
            horizontalArrangement = Arrangement.spacedBy(itemSpacing),
            verticalAlignment = verticalAlignment
        ) {
            item { content() }
        }
    }
}