package com.open.pin.ui.scroll

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.gestures.animateScrollBy
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import com.open.pin.ui.animation.PinAnimationSpecs
import com.open.pin.ui.animation.ScrollMomentumState
import kotlinx.coroutines.CoroutineScope

/**
 * Clean interface for scroll physics behavior.
 * Separates scroll logic from UI implementation for better maintainability.
 */
interface ScrollPhysics {
    /**
     * Calculate scroll distance based on base amount and current momentum
     */
    fun calculateScrollDistance(baseAmount: Float, momentumState: ScrollMomentumState): Float
    
    /**
     * Get animation spec for current scroll action
     */
    fun getScrollAnimation(momentumState: ScrollMomentumState): AnimationSpec<Float>
    
    /**
     * Perform scroll action with momentum
     */
    suspend fun performScroll(
        listState: LazyListState,
        direction: Int,
        baseAmount: Float,
        momentumState: ScrollMomentumState
    )
}

/**
 * Default Pin UI scroll physics implementation
 */
class PinScrollPhysics : ScrollPhysics {
    
    override fun calculateScrollDistance(
        baseAmount: Float, 
        momentumState: ScrollMomentumState
    ): Float {
        val multiplier = momentumState.getScrollMultiplier()
        return baseAmount * multiplier
    }
    
    override fun getScrollAnimation(momentumState: ScrollMomentumState): AnimationSpec<Float> {
        return momentumState.getAnimationSpec()
    }
    
    override suspend fun performScroll(
        listState: LazyListState,
        direction: Int,
        baseAmount: Float,
        momentumState: ScrollMomentumState
    ) {
        val scrollDistance = calculateScrollDistance(baseAmount, momentumState)
        val animationSpec = getScrollAnimation(momentumState)
        
        listState.animateScrollBy(
            value = direction * scrollDistance,
            animationSpec = animationSpec
        )
    }
}

/**
 * Smooth scroll physics with enhanced momentum curves
 */
class SmoothScrollPhysics : ScrollPhysics {
    
    private val smoothingFactor = 0.85f
    
    override fun calculateScrollDistance(
        baseAmount: Float, 
        momentumState: ScrollMomentumState
    ): Float {
        val multiplier = momentumState.getScrollMultiplier()
        // Apply smoothing for more gradual acceleration
        return baseAmount * (1f + (multiplier - 1f) * smoothingFactor)
    }
    
    override fun getScrollAnimation(momentumState: ScrollMomentumState): AnimationSpec<Float> {
        // Always use smooth momentum animation for consistency
        return PinAnimationSpecs.Scroll.momentum
    }
    
    override suspend fun performScroll(
        listState: LazyListState,
        direction: Int,
        baseAmount: Float,
        momentumState: ScrollMomentumState
    ) {
        val scrollDistance = calculateScrollDistance(baseAmount, momentumState)
        val animationSpec = getScrollAnimation(momentumState)
        
        listState.animateScrollBy(
            value = direction * scrollDistance,
            animationSpec = animationSpec
        )
    }
}

/**
 * Configurable scroll physics with custom damping and stiffness
 */
class ConfigurableScrollPhysics(
    private val dampingRatio: Float = Spring.DampingRatioNoBouncy,
    private val stiffness: Float = Spring.StiffnessMedium,
    private val smoothingFactor: Float = 0.85f
) : ScrollPhysics {
    
    override fun calculateScrollDistance(
        baseAmount: Float, 
        momentumState: ScrollMomentumState
    ): Float {
        val multiplier = momentumState.getScrollMultiplier()
        return baseAmount * (1f + (multiplier - 1f) * smoothingFactor)
    }
    
    override fun getScrollAnimation(momentumState: ScrollMomentumState): AnimationSpec<Float> {
        // Use custom spring animation with specified damping and stiffness
        return PinAnimationSpecs.Builders.customSpring(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        )
    }
    
    override suspend fun performScroll(
        listState: LazyListState,
        direction: Int,
        baseAmount: Float,
        momentumState: ScrollMomentumState
    ) {
        val scrollDistance = calculateScrollDistance(baseAmount, momentumState)
        val animationSpec = getScrollAnimation(momentumState)
        
        listState.animateScrollBy(
            value = direction * scrollDistance,
            animationSpec = animationSpec
        )
    }
}

/**
 * Configuration for scroll behavior
 */
data class ScrollConfig(
    val baseScrollAmount: Dp,
    val physics: ScrollPhysics = ConfigurableScrollPhysics(
        dampingRatio = Spring.DampingRatioNoBouncy,
        stiffness = Spring.StiffnessLow
    ),
    val enableMomentum: Boolean = true,
    val autoHideButtons: Boolean = true
)

/**
 * Scroll controller that encapsulates scroll logic and state
 */
@Composable
fun rememberScrollController(
    config: ScrollConfig,
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): ScrollController {
    val density = LocalDensity.current
    return remember(config) { 
        ScrollController(config, density, coroutineScope) 
    }
}

class ScrollController(
    private val config: ScrollConfig,
    private val density: Density,
    private val coroutineScope: CoroutineScope
) {
    private val upMomentumState = ScrollMomentumState()
    private val downMomentumState = ScrollMomentumState()
    
    private val baseScrollAmountPx = with(density) { config.baseScrollAmount.toPx() }
    
    /**
     * Scroll up with momentum
     */
    suspend fun scrollUp(listState: LazyListState) {
        config.physics.performScroll(
            listState = listState,
            direction = -1,
            baseAmount = baseScrollAmountPx,
            momentumState = if (config.enableMomentum) upMomentumState else createEmptyMomentumState()
        )
    }
    
    /**
     * Scroll down with momentum
     */
    suspend fun scrollDown(listState: LazyListState) {
        config.physics.performScroll(
            listState = listState,
            direction = 1,
            baseAmount = baseScrollAmountPx,
            momentumState = if (config.enableMomentum) downMomentumState else createEmptyMomentumState()
        )
    }
    
    /**
     * Reset momentum for both directions
     */
    fun resetMomentum() {
        upMomentumState.reset()
        downMomentumState.reset()
    }
    
    /**
     * Reset momentum with delay (for hover exit scenarios)
     */
    suspend fun resetMomentumWithDelay(delayMs: Long = PinAnimationSpecs.Config.SCROLL_MOMENTUM_DECAY_MS) {
        kotlinx.coroutines.delay(delayMs)
        resetMomentum()
    }
    
    private fun createEmptyMomentumState(): ScrollMomentumState {
        return ScrollMomentumState().apply { reset() }
    }
}

/**
 * Scroll position utilities for boundary detection
 */
object ScrollPositionUtils {
    
    /**
     * Check if list is at the top
     */
    fun LazyListState.isAtTop(): Boolean {
        return firstVisibleItemIndex == 0 && firstVisibleItemScrollOffset == 0
    }
    
    /**
     * Check if list is at the bottom
     */
    fun LazyListState.isAtBottom(): Boolean {
        val lastVisibleItem = layoutInfo.visibleItemsInfo.lastOrNull()
        return lastVisibleItem?.let { item ->
            val info = layoutInfo
            item.index == info.totalItemsCount - 1 && 
            item.offset + item.size <= info.viewportEndOffset
        } ?: false
    }
    
    /**
     * Get scroll progress as a value between 0f and 1f
     */
    fun LazyListState.getScrollProgress(): Float {
        val totalItems = layoutInfo.totalItemsCount
        if (totalItems <= 1) return 0f
        
        val currentIndex = firstVisibleItemIndex
        val itemProgress = if (layoutInfo.visibleItemsInfo.isNotEmpty()) {
            val firstItemSize = layoutInfo.visibleItemsInfo.first().size
            if (firstItemSize > 0) firstVisibleItemScrollOffset.toFloat() / firstItemSize else 0f
        } else 0f
        
        return ((currentIndex + itemProgress) / (totalItems - 1)).coerceIn(0f, 1f)
    }
}

/**
 * Composable for observing scroll state changes
 */
@Composable
fun ScrollStateObserver(
    listState: LazyListState,
    onScrollStateChanged: (isAtTop: Boolean, isAtBottom: Boolean, progress: Float) -> Unit
) {
    val isAtTop by remember {
        derivedStateOf { ScrollPositionUtils.run { listState.isAtTop() } }
    }
    
    val isAtBottom by remember {
        derivedStateOf { ScrollPositionUtils.run { listState.isAtBottom() } }
    }
    
    val scrollProgress by remember {
        derivedStateOf { ScrollPositionUtils.run { listState.getScrollProgress() } }
    }
    
    LaunchedEffect(isAtTop, isAtBottom, scrollProgress) {
        onScrollStateChanged(isAtTop, isAtBottom, scrollProgress)
    }
}