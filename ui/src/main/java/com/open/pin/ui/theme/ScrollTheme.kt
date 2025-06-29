package com.open.pin.ui.theme

import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Scroll-specific dimensions optimized for Pin UI
 */
object PinScrollDimensions {
    // Scroll button dimensions (bigger for better visibility)
    val scrollButtonWidth = 80.dp
    val scrollButtonHeight = 32.dp
    val scrollButtonCornerRadius = 16.dp
    val scrollButtonIconSize = 24.dp
    val scrollButtonPaddingHorizontal = 16.dp

    // Compact scroll button dimensions for overlay use - increased for better visibility
    val compactScrollButtonWidth = 100.dp
    val compactScrollButtonHeight = 40.dp
    val compactScrollButtonCornerRadius = 20.dp
    val compactScrollButtonIconSize = 50.dp
    val compactScrollButtonPaddingHorizontal = 20.dp

    // Gradient overlay dimensions - increased for better coverage
    val gradientOverlayHeight = 80.dp // Increased for larger gradient area
    val gradientOverlayAlpha = 1.0f // Full opacity for clean black fade

    // Scroll amounts and spacing
    val defaultScrollAmount = 240.dp // Doubled for more substantial scrolling
    val scrollButtonSpacing = 8.dp

    // Content padding for scrollable areas - edge-to-edge on all sides
    val scrollContentPadding = PaddingValues(
        top = 0.dp,
        bottom = 0.dp,
        start = 0.dp,
        end = 0.dp
    )

    // Negative margins for seamless gradient integration (matching HTML demo)
    val contentNegativeMarginVertical = 28.dp

    // Item spacing in scrollable lists
    val defaultItemSpacing = 12.dp

    // Clean scroll configuration with theme-centered values
    val baseScrollAmount = 200.dp // Base scroll distance per action
    val momentumMultipliers = listOf(1.0f, 1.2f, 1.5f, 2.0f, 2.5f, 3.0f) // Progressive momentum steps
    val maxMomentumLevel = 5 // Maximum momentum level (index in multipliers list)
    val momentumDecayMs = 600L // Time before momentum resets
}

/**
 * Simple momentum tracker using theme-defined multipliers
 */
class CleanScrollMomentumTracker {
    private var lastScrollTime = 0L
    private var momentumLevel = 0
    
    /**
     * Get scroll multiplier based on current momentum
     */
    fun getScrollMultiplier(): Float {
        val currentTime = System.currentTimeMillis()
        val timeSinceLastScroll = currentTime - lastScrollTime
        
        // Reset momentum if too much time has passed
        if (timeSinceLastScroll > PinScrollDimensions.momentumDecayMs) {
            momentumLevel = 0
        }
        
        // Build momentum up to max level
        momentumLevel = (momentumLevel + 1).coerceAtMost(PinScrollDimensions.maxMomentumLevel)
        lastScrollTime = currentTime
        
        // Return theme-defined multiplier
        return PinScrollDimensions.momentumMultipliers[momentumLevel]
    }
    
    /**
     * Reset momentum
     */
    fun reset() {
        momentumLevel = 0
        lastScrollTime = 0L
    }
}

/**
 * Scroll-specific colors for gradients and overlays
 */
object PinScrollColors {
    // Gradient overlay colors (solid black fading to transparent)
    val gradientOverlayStart = PinColors.background // Solid black
    val gradientOverlayEnd = Color.Transparent
    
    // Scroll button colors (inherits from main theme)
    val scrollButtonNormal = PinColors.primary
    val scrollButtonHover = PinColors.primary
    val scrollButtonPressed = PinColors.tertiary
    val scrollButtonDisabled = PinColors.primary.copy(alpha = 0.3f)
    
    // Scroll indicators and tracks
    val scrollTrack = PinColors.tertiary.copy(alpha = 0.2f)
    val scrollThumb = PinColors.primary.copy(alpha = 0.6f)
    val scrollThumbHover = PinColors.primary.copy(alpha = 0.8f)
}