package com.open.pin.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.delay

/**
 * Unified interaction state management for Pin UI components.
 * Provides clean, composable state holders that eliminate duplication.
 */

/**
 * Core interaction state that can be used across all interactive components
 */
class PinInteractionState {
    var isHovered by mutableStateOf(false)
    var isPressed by mutableStateOf(false)
    var isFocused by mutableStateOf(false)
    var isEnabled by mutableStateOf(true)
    var isSnapped by mutableStateOf(false)
    
    // Computed states
    val isActive: Boolean get() = isHovered || isPressed || isFocused || isSnapped
    val effectiveHovered: Boolean get() = isHovered || isSnapped
    val effectivePressed: Boolean get() = isPressed
    
    /**
     * Reset all interaction states
     */
    fun reset() {
        isHovered = false
        isPressed = false
        isFocused = false
        isSnapped = false
    }
}

/**
 * Animated interaction state that provides smooth transitions between states
 */
@Composable
fun rememberAnimatedInteractionState(): AnimatedInteractionState {
    return remember { AnimatedInteractionState() }
}

class AnimatedInteractionState {
    private val interactionState = PinInteractionState()
    
    // Expose interaction state properties
    var isHovered by interactionState::isHovered
    var isPressed by interactionState::isPressed
    var isFocused by interactionState::isFocused
    var isEnabled by interactionState::isEnabled
    var isSnapped by interactionState::isSnapped
    
    val isActive by derivedStateOf { interactionState.isActive }
    val effectiveHovered by derivedStateOf { interactionState.effectiveHovered }
    val effectivePressed by derivedStateOf { interactionState.effectivePressed }
    
    /**
     * Animated progress values for smooth state transitions
     */
    @Composable
    fun animatedHoverProgress(): State<Float> {
        return animateFloatAsState(
            targetValue = if (effectiveHovered) 1f else 0f,
            animationSpec = PinAnimationSpecs.Interaction.hover,
            label = "hover_progress"
        )
    }
    
    @Composable
    fun animatedPressProgress(): State<Float> {
        return animateFloatAsState(
            targetValue = if (effectivePressed) 1f else 0f,
            animationSpec = PinAnimationSpecs.Interaction.press,
            label = "press_progress"
        )
    }
    
    @Composable
    fun animatedFocusProgress(): State<Float> {
        return animateFloatAsState(
            targetValue = if (isFocused) 1f else 0f,
            animationSpec = PinAnimationSpecs.Interaction.focus,
            label = "focus_progress"
        )
    }
    
    @Composable
    fun animatedAlpha(): State<Float> {
        return animateFloatAsState(
            targetValue = if (isEnabled) 1f else 0.6f,
            animationSpec = PinAnimationSpecs.Core.standard,
            label = "alpha"
        )
    }
    
    fun reset() = interactionState.reset()
}

/**
 * Magnetic interaction state for components with magnetic effects
 */
@Composable
fun rememberMagneticInteractionState(
    elementId: String? = null,
    resetDelayMs: Long = PinAnimationSpecs.Config.SCROLL_MOMENTUM_DECAY_MS
): MagneticInteractionState {
    return remember(elementId) { MagneticInteractionState(elementId, resetDelayMs) }
}

class MagneticInteractionState(
    private val elementId: String?,
    private val resetDelayMs: Long
) {
    private val baseInteractionState = PinInteractionState()
    
    // Magnetic-specific state
    var magneticOffset by mutableStateOf(IntOffset.Zero)
    var isPointerInBounds by mutableStateOf(false)
    var lastInteractionTime by mutableStateOf(0L)
    
    // Expose base interaction state
    var isHovered by baseInteractionState::isHovered
    var isPressed by baseInteractionState::isPressed
    var isFocused by baseInteractionState::isFocused
    var isEnabled by baseInteractionState::isEnabled
    var isSnapped by baseInteractionState::isSnapped
    
    val isActive by derivedStateOf { baseInteractionState.isActive }
    val effectiveHovered by derivedStateOf { baseInteractionState.effectiveHovered }
    val effectivePressed by derivedStateOf { baseInteractionState.effectivePressed }
    
    /**
     * Update magnetic offset with smooth animation
     */
    @Composable
    fun animatedMagneticOffset(): State<IntOffset> {
        return animateIntOffsetAsState(
            targetValue = magneticOffset,
            animationSpec = tween(
                durationMillis = 100,
                easing = FastOutLinearInEasing
            ),
            label = "magnetic_offset"
        )
    }
    
    /**
     * Calculate magnetic offset from pointer position
     */
    fun calculateMagneticOffset(
        pointerPosition: Offset,
        elementSize: androidx.compose.ui.geometry.Size,
        maxOffset: Float = PinAnimationSpecs.Config.MAGNETIC_MAX_OFFSET.value,
        sensitivity: Float = PinAnimationSpecs.Config.MAGNETIC_SENSITIVITY
    ): IntOffset {
        val centerX = elementSize.width / 2
        val centerY = elementSize.height / 2
        
        val offsetX = ((pointerPosition.x - centerX) / centerX * maxOffset * sensitivity).toInt()
        val offsetY = ((pointerPosition.y - centerY) / centerY * maxOffset * sensitivity).toInt()
        
        return IntOffset(offsetX, offsetY)
    }
    
    /**
     * Reset magnetic state with delay
     */
    suspend fun resetWithDelay() {
        delay(resetDelayMs)
        if (!isPointerInBounds) {
            magneticOffset = IntOffset.Zero
            baseInteractionState.reset()
        }
    }
    
    fun reset() {
        magneticOffset = IntOffset.Zero
        baseInteractionState.reset()
        isPointerInBounds = false
    }
}

/**
 * Scroll momentum state for clean scroll physics management
 */
@Composable
fun rememberScrollMomentumState(): ScrollMomentumState {
    return remember { ScrollMomentumState() }
}

class ScrollMomentumState {
    private var lastScrollTime by mutableStateOf(0L)
    private var momentumLevel by mutableStateOf(0)
    
    private val momentumMultipliers = listOf(1.0f, 1.2f, 1.5f, 2.0f, 2.5f, 3.0f)
    private val maxMomentumLevel = PinAnimationSpecs.Config.SCROLL_MOMENTUM_MAX_LEVEL
    private val momentumDecayMs = PinAnimationSpecs.Config.SCROLL_MOMENTUM_DECAY_MS
    
    /**
     * Get current scroll multiplier based on momentum
     */
    fun getScrollMultiplier(): Float {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastScroll = currentTime - lastScrollTime
        
        // Reset momentum if too much time has passed
        if (timeSinceLastScroll > momentumDecayMs) {
            momentumLevel = 0
        }
        
        // Build momentum up to max level
        momentumLevel = (momentumLevel + 1).coerceAtMost(maxMomentumLevel)
        lastScrollTime = currentTime
        
        return momentumMultipliers.getOrElse(momentumLevel) { momentumMultipliers.last() }
    }
    
    /**
     * Get appropriate animation spec based on current momentum
     */
    fun getAnimationSpec(): AnimationSpec<Float> {
        return when (momentumLevel) {
            0, 1 -> PinAnimationSpecs.Scroll.snap
            in 2..3 -> PinAnimationSpecs.Scroll.momentum
            else -> PinAnimationSpecs.Scroll.windUp
        }
    }
    
    /**
     * Reset momentum state
     */
    fun reset() {
        momentumLevel = 0
        lastScrollTime = 0L
    }
}

/**
 * Utility composable for managing interaction state lifecycle
 */
@Composable
fun InteractionStateEffect(
    state: PinInteractionState,
    onStateChange: ((PinInteractionState) -> Unit)? = null
) {
    LaunchedEffect(state.isActive) {
        onStateChange?.invoke(state)
    }
    
    DisposableEffect(state) {
        onDispose {
            state.reset()
        }
    }
}