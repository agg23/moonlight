package com.open.pin.ui.scroll

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.zIndex
import com.open.pin.ui.animation.rememberAnimatedInteractionState
import com.open.pin.ui.components.button.PinCompactScrollUpButton
import com.open.pin.ui.components.button.PinCompactScrollDownButton
import com.open.pin.ui.theme.PinScrollColors
import com.open.pin.ui.theme.PinScrollDimensions
import kotlinx.coroutines.launch

/**
 * Configuration for scroll behavior appearance and functionality
 */
data class ScrollBehaviorConfig(
    val showScrollButtons: Boolean = true,
    val autoHideButtons: Boolean = true,
    val gradientHeight: Dp = PinScrollDimensions.gradientOverlayHeight,
    val scrollConfig: ScrollConfig = ScrollConfig(
        baseScrollAmount = PinScrollDimensions.baseScrollAmount
    )
)

/**
 * State holder for scroll behavior that encapsulates all scroll-related state
 */
@Composable
fun rememberScrollBehaviorState(
    config: ScrollBehaviorConfig = ScrollBehaviorConfig(),
    listState: LazyListState
): ScrollBehaviorState {
    val coroutineScope = rememberCoroutineScope()
    val scrollController = rememberScrollController(config.scrollConfig, coroutineScope)
    
    return remember(config, listState) {
        ScrollBehaviorState(config, listState, scrollController, coroutineScope)
    }
}

class ScrollBehaviorState(
    val config: ScrollBehaviorConfig,
    val listState: LazyListState,
    private val scrollController: ScrollController,
    private val coroutineScope: kotlinx.coroutines.CoroutineScope
) {
    // Scroll position state
    var isAtTop by mutableStateOf(false)
        private set
    var isAtBottom by mutableStateOf(false)
        private set
    var scrollProgress by mutableStateOf(0f)
        private set
    
    // Button visibility
    val showUpButton: Boolean get() = if (config.autoHideButtons) !isAtTop else config.showScrollButtons
    val showDownButton: Boolean get() = if (config.autoHideButtons) !isAtBottom else config.showScrollButtons
    
    /**
     * Update scroll position state
     */
    internal fun updateScrollState(atTop: Boolean, atBottom: Boolean, progress: Float) {
        isAtTop = atTop
        isAtBottom = atBottom
        scrollProgress = progress
    }
    
    /**
     * Perform scroll up action
     */
    fun scrollUp() {
        coroutineScope.launch {
            scrollController.scrollUp(listState)
        }
    }
    
    /**
     * Perform scroll down action
     */
    fun scrollDown() {
        coroutineScope.launch {
            scrollController.scrollDown(listState)
        }
    }
    
    /**
     * Reset scroll momentum
     */
    fun resetMomentum() {
        scrollController.resetMomentum()
    }
    
    /**
     * Reset momentum with delay
     */
    fun resetMomentumWithDelay() {
        coroutineScope.launch {
            scrollController.resetMomentumWithDelay()
        }
    }
}

/**
 * Reusable scroll behavior composable that can be applied to any scrollable content
 */
@Composable
fun ScrollBehavior(
    state: ScrollBehaviorState,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Observe scroll state changes
    ScrollStateObserver(
        listState = state.listState,
        onScrollStateChanged = state::updateScrollState
    )
    
    if (state.config.showScrollButtons) {
        Box(modifier = modifier, contentAlignment = Alignment.Center) {
            // Main content
            content()
            
            // Scroll controls overlay
            ScrollControlsOverlay(
                state = state,
                modifier = Modifier.fillMaxSize()
            )
        }
    } else {
        // Just the content without scroll controls
        Box(modifier = modifier) {
            content()
        }
    }
}

/**
 * Internal scroll controls overlay with gradients and buttons
 */
@Composable
private fun ScrollControlsOverlay(
    state: ScrollBehaviorState,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Top scroll area with gradient and up button
        if (state.showUpButton) {
            ScrollArea(
                direction = ScrollDirection.UP,
                onScroll = { state.scrollUp() },
                onResetMomentum = { state.resetMomentumWithDelay() },
                gradientHeight = state.config.gradientHeight,
                modifier = Modifier.fillMaxWidth()
            )
        }
        
        // Middle spacer
        Spacer(modifier = Modifier.weight(1f))
        
        // Bottom scroll area with gradient and down button
        if (state.showDownButton) {
            ScrollArea(
                direction = ScrollDirection.DOWN,
                onScroll = { state.scrollDown() },
                onResetMomentum = { state.resetMomentumWithDelay() },
                gradientHeight = state.config.gradientHeight,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

/**
 * Direction enum for scroll areas
 */
private enum class ScrollDirection { UP, DOWN }

/**
 * Individual scroll area with gradient background and scroll button
 */
@Composable
private fun ScrollArea(
    direction: ScrollDirection,
    onScroll: () -> Unit,
    onResetMomentum: () -> Unit,
    gradientHeight: Dp,
    modifier: Modifier = Modifier
) {
    val interactionState = rememberAnimatedInteractionState()
    
    // Gradient colors based on direction
    val gradientColors = when (direction) {
        ScrollDirection.UP -> listOf(
            PinScrollColors.gradientOverlayStart,
            PinScrollColors.gradientOverlayEnd
        )
        ScrollDirection.DOWN -> listOf(
            PinScrollColors.gradientOverlayEnd,
            PinScrollColors.gradientOverlayStart
        )
    }
    
    Box(
        modifier = modifier
            .height(gradientHeight)
            .background(
                brush = Brush.verticalGradient(colors = gradientColors)
            )
            .pointerInput(Unit) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        when (event.type) {
                            PointerEventType.Enter, PointerEventType.Move -> {
                                interactionState.isHovered = true
                            }
                            PointerEventType.Exit -> {
                                interactionState.isHovered = false
                                onResetMomentum()
                            }
                            PointerEventType.Press -> {
                                onScroll()
                            }
                        }
                    }
                }
            }
            .zIndex(1f),
        contentAlignment = Alignment.Center
    ) {
        // Scroll button
        when (direction) {
            ScrollDirection.UP -> {
                PinCompactScrollUpButton(
                    onClick = onScroll,
                    modifier = Modifier.alpha(if (interactionState.effectiveHovered) 1f else 0f),
                    interaction = rememberAnimatedInteractionState().apply {
                        isHovered = interactionState.isHovered
                    }
                )
            }
            ScrollDirection.DOWN -> {
                PinCompactScrollDownButton(
                    onClick = onScroll,
                    modifier = Modifier.alpha(if (interactionState.effectiveHovered) 1f else 0f),
                    interaction = rememberAnimatedInteractionState().apply {
                        isHovered = interactionState.isHovered
                    }
                )
            }
        }
    }
}

/**
 * Simplified scroll behavior for basic use cases
 */
@Composable
fun SimpleScrollBehavior(
    listState: LazyListState,
    modifier: Modifier = Modifier,
    showScrollButtons: Boolean = true,
    autoHideButtons: Boolean = true,
    content: @Composable () -> Unit
) {
    val config = ScrollBehaviorConfig(
        showScrollButtons = showScrollButtons,
        autoHideButtons = autoHideButtons
    )
    val state = rememberScrollBehaviorState(config, listState)
    
    ScrollBehavior(
        state = state,
        modifier = modifier,
        content = content
    )
}

/**
 * Extension function to add scroll behavior to any composable
 */
@Composable
fun Modifier.scrollBehavior(
    listState: LazyListState,
    config: ScrollBehaviorConfig = ScrollBehaviorConfig()
): Modifier {
    val state = rememberScrollBehaviorState(config, listState)
    
    // Set up scroll state observation
    ScrollStateObserver(
        listState = listState,
        onScrollStateChanged = state::updateScrollState
    )
    
    return this
}

/**
 * Preset configurations for common scroll scenarios
 */
object ScrollBehaviorPresets {
    
    fun minimal() = ScrollBehaviorConfig(
        showScrollButtons = false,
        autoHideButtons = true
    )
    
    fun standard() = ScrollBehaviorConfig(
        showScrollButtons = true,
        autoHideButtons = true
    )
    
    fun alwaysVisible() = ScrollBehaviorConfig(
        showScrollButtons = true,
        autoHideButtons = false
    )
    
    fun smoothScroll() = ScrollBehaviorConfig(
        showScrollButtons = true,
        autoHideButtons = true,
        scrollConfig = ScrollConfig(
            baseScrollAmount = PinScrollDimensions.baseScrollAmount,
            physics = SmoothScrollPhysics()
        )
    )
}