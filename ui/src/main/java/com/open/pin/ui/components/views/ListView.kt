package com.open.pin.ui.components.views

import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.PointerEventType
import com.open.pin.ui.components.button.ButtonInteractionState
import com.open.pin.ui.components.button.PinCompactScrollUpButton
import com.open.pin.ui.components.button.PinCompactScrollDownButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.open.pin.ui.theme.PinScrollDimensions
import com.open.pin.ui.theme.PinScrollColors
import com.open.pin.ui.theme.CleanScrollMomentumTracker
import kotlinx.coroutines.launch

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
    scrollAmount: Dp = PinScrollDimensions.defaultScrollAmount,
    autoHideButtons: Boolean = true,
    enableSnapToCenter: Boolean = false,
    listState: LazyListState = rememberLazyListState(),
    content: @Composable () -> Unit
) {
    // Unified scroll implementation using LazyColumn/LazyRow
    val coroutineScope = rememberCoroutineScope()
    val density = androidx.compose.ui.platform.LocalDensity.current
    val scrollAmountPx = with(density) { scrollAmount.toPx() }
    
    // Snap fling behavior for center alignment (only when enabled)
    val snapFlingBehavior = if (enableSnapToCenter) {
        rememberSnapFlingBehavior(lazyListState = listState)
    } else null
    
    // Track scroll position for auto-hiding buttons (only when needed)
    val isAtTop by remember {
        derivedStateOf {
            if (showScrollButtons && autoHideButtons) {
                listState.firstVisibleItemIndex == 0 && listState.firstVisibleItemScrollOffset == 0
            } else false
        }
    }
    
    val isAtBottom by remember {
        derivedStateOf {
            if (showScrollButtons && autoHideButtons) {
                val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
                lastVisibleItem?.let { item ->
                    val info = listState.layoutInfo
                    item.index == info.totalItemsCount - 1 && 
                    item.offset + item.size <= info.viewportEndOffset
                } ?: false
            } else false
        }
    }
    
    // Unified scroll view
    UnifiedScrollView(
        modifier = modifier,
        leftIndent = leftIndent,
        rightIndent = rightIndent,
        topPadding = topPadding,
        bottomPadding = bottomPadding,
        orientation = orientation,
        itemSpacing = itemSpacing,
        horizontalAlignment = horizontalAlignment,
        verticalAlignment = verticalAlignment,
        showScrollButtons = showScrollButtons,
        autoHideButtons = autoHideButtons,
        listState = listState,
        snapFlingBehavior = snapFlingBehavior,
        isAtTop = isAtTop,
        isAtBottom = isAtBottom,
        scrollAmountPx = scrollAmountPx,
        coroutineScope = coroutineScope,
        content = content
    )
}

@Composable
private fun UnifiedScrollView(
    modifier: Modifier,
    leftIndent: Dp,
    rightIndent: Dp,
    topPadding: Dp,
    bottomPadding: Dp,
    orientation: ListViewOrientation,
    itemSpacing: Dp,
    horizontalAlignment: Alignment.Horizontal,
    verticalAlignment: Alignment.Vertical,
    showScrollButtons: Boolean,
    autoHideButtons: Boolean,
    listState: LazyListState,
    snapFlingBehavior: androidx.compose.foundation.gestures.FlingBehavior?,
    isAtTop: Boolean,
    isAtBottom: Boolean,
    scrollAmountPx: Float,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    content: @Composable () -> Unit
) {
    when (orientation) {
        ListViewOrientation.VERTICAL -> {
            if (showScrollButtons) {
                // Vertical with scroll buttons (Box + LazyColumn + gradient overlays)
                VerticalScrollWithButtons(
                    modifier = modifier,
                    leftIndent = leftIndent,
                    rightIndent = rightIndent,
                    topPadding = topPadding,
                    bottomPadding = bottomPadding,
                    itemSpacing = itemSpacing,
                    horizontalAlignment = horizontalAlignment,
                    autoHideButtons = autoHideButtons,
                    listState = listState,
                    snapFlingBehavior = snapFlingBehavior,
                    isAtTop = isAtTop,
                    isAtBottom = isAtBottom,
                    scrollAmountPx = scrollAmountPx,
                    coroutineScope = coroutineScope,
                    content = content
                )
            } else {
                // Simple vertical (just LazyColumn)
                if (snapFlingBehavior != null) {
                    LazyColumn(
                        state = listState,
                        modifier = modifier
                            .fillMaxSize()
                            .padding(
                                start = leftIndent,
                                end = rightIndent,
                                top = topPadding,
                                bottom = bottomPadding
                            ),
                        verticalArrangement = Arrangement.spacedBy(itemSpacing),
                        horizontalAlignment = horizontalAlignment,
                        flingBehavior = snapFlingBehavior
                    ) {
                        item {
                            content()
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = modifier
                            .fillMaxSize()
                            .padding(
                                start = leftIndent,
                                end = rightIndent,
                                top = topPadding,
                                bottom = bottomPadding
                            ),
                        verticalArrangement = Arrangement.spacedBy(itemSpacing),
                        horizontalAlignment = horizontalAlignment
                    ) {
                        item {
                            content()
                        }
                    }
                }
            }
        }
        ListViewOrientation.HORIZONTAL -> {
            // Horizontal (LazyRow - no scroll buttons supported yet)
            if (snapFlingBehavior != null) {
                LazyRow(
                    state = listState,
                    modifier = modifier
                        .fillMaxSize()
                        .padding(
                            start = leftIndent,
                            end = rightIndent,
                            top = topPadding,
                            bottom = bottomPadding
                        ),
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                    verticalAlignment = verticalAlignment,
                    flingBehavior = snapFlingBehavior
                ) {
                    item {
                        content()
                    }
                }
            } else {
                LazyRow(
                    state = listState,
                    modifier = modifier
                        .fillMaxSize()
                        .padding(
                            start = leftIndent,
                            end = rightIndent,
                            top = topPadding,
                            bottom = bottomPadding
                        ),
                    horizontalArrangement = Arrangement.spacedBy(itemSpacing),
                    verticalAlignment = verticalAlignment
                ) {
                    item {
                        content()
                    }
                }
            }
        }
    }
}

@Composable
private fun VerticalScrollWithButtons(
    modifier: Modifier,
    leftIndent: Dp,
    rightIndent: Dp,
    topPadding: Dp,
    bottomPadding: Dp,
    itemSpacing: Dp,
    horizontalAlignment: Alignment.Horizontal,
    autoHideButtons: Boolean,
    listState: LazyListState,
    snapFlingBehavior: androidx.compose.foundation.gestures.FlingBehavior?,
    isAtTop: Boolean,
    isAtBottom: Boolean,
    scrollAmountPx: Float,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    content: @Composable () -> Unit
) {
    val showUpButton = if (autoHideButtons) !isAtTop else true
    val showDownButton = if (autoHideButtons) !isAtBottom else true
    val gradientHeight = PinScrollDimensions.gradientOverlayHeight
    
    // Shared interaction states for gradient area and scroll buttons
    val upButtonInteraction = remember { ButtonInteractionState() }
    val downButtonInteraction = remember { ButtonInteractionState() }
    
    // Clean momentum trackers with simple multiplier system
    val upMomentumTracker = remember { CleanScrollMomentumTracker() }
    val downMomentumTracker = remember { CleanScrollMomentumTracker() }
    
    // Simple scroll helper using theme-defined values with smooth spring animation
    val localDensity = androidx.compose.ui.platform.LocalDensity.current
    val performCleanScroll = { direction: Int, momentumTracker: CleanScrollMomentumTracker ->
        coroutineScope.launch {
            val multiplier = momentumTracker.getScrollMultiplier()
            val baseAmount = with(localDensity) { PinScrollDimensions.baseScrollAmount.toPx() }
            val scrollDistance = baseAmount * multiplier
            
            // Wind-up/wind-down scrolling with pronounced spring effect
            listState.animateScrollBy(
                value = direction * scrollDistance,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy, // More wind-up/overshoot effect
                    stiffness = Spring.StiffnessLow // Shorter, snappier animation
                )
            )
        }
    }
    
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // LazyColumn with scrolling
        if (snapFlingBehavior != null) {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = topPadding,
                        bottom = bottomPadding,
                        start = leftIndent,
                        end = rightIndent
                    ),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                horizontalAlignment = horizontalAlignment,
                flingBehavior = snapFlingBehavior
            ) {
                item {
                    content()
                }
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        top = topPadding,
                        bottom = bottomPadding,
                        start = leftIndent,
                        end = rightIndent
                    ),
                verticalArrangement = Arrangement.spacedBy(itemSpacing),
                horizontalAlignment = horizontalAlignment
            ) {
                item {
                    content()
                }
            }
        }
        
        // Top scroll button and gradient overlay
        if (showUpButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxWidth()
                    .height(gradientHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PinScrollColors.gradientOverlayStart, // Solid black at very top
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.95f),
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.8f),
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.6f),
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.35f),
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.15f),
                                PinScrollColors.gradientOverlayEnd    // Fully transparent at bottom
                            )
                        )
                    )
                    .pointerInput(Unit) { 
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Enter,
                                    PointerEventType.Move -> {
                                        upButtonInteraction.isHovered = true
                                    }
                                    PointerEventType.Exit -> {
                                        upButtonInteraction.isHovered = false
                                        // Reset momentum when leaving the scroll area
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(PinScrollDimensions.momentumDecayMs)
                                            upMomentumTracker.reset()
                                        }
                                    }
                                    PointerEventType.Press -> {
                                        performCleanScroll(-1, upMomentumTracker)
                                    }
                                }
                            }
                        }
                    }
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                PinCompactScrollUpButton(
                    onClick = {
                        performCleanScroll(-1, upMomentumTracker)
                    },
                    modifier = Modifier.alpha(if (showUpButton) 1f else 0f),
                    interaction = upButtonInteraction
                )
            }
        }
        
        // Bottom scroll button and gradient overlay
        if (showDownButton) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .height(gradientHeight)
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                PinScrollColors.gradientOverlayEnd,   // Fully transparent at top
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.15f),
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.35f),
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.6f),
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.8f),
                                PinScrollColors.gradientOverlayStart.copy(alpha = 0.95f),
                                PinScrollColors.gradientOverlayStart  // Solid black at bottom
                            )
                        )
                    )
                    .pointerInput(Unit) { 
                        awaitPointerEventScope {
                            while (true) {
                                val event = awaitPointerEvent()
                                when (event.type) {
                                    PointerEventType.Enter,
                                    PointerEventType.Move -> {
                                        downButtonInteraction.isHovered = true
                                    }
                                    PointerEventType.Exit -> {
                                        downButtonInteraction.isHovered = false
                                        // Reset momentum when leaving the scroll area
                                        coroutineScope.launch {
                                            kotlinx.coroutines.delay(PinScrollDimensions.momentumDecayMs)
                                            downMomentumTracker.reset()
                                        }
                                    }
                                    PointerEventType.Press -> {
                                        performCleanScroll(1, downMomentumTracker)
                                    }
                                }
                            }
                        }
                    }
                    .zIndex(1f),
                contentAlignment = Alignment.Center
            ) {
                PinCompactScrollDownButton(
                    onClick = {
                        performCleanScroll(1, downMomentumTracker)
                    },
                    modifier = Modifier.alpha(if (showDownButton) 1f else 0f),
                    interaction = downButtonInteraction
                )
            }
        }
    }
}