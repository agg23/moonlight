package com.open.pin.ui.animation

import androidx.compose.animation.core.*
import androidx.compose.ui.unit.dp

/**
 * Centralized animation specifications for consistent behavior across Pin UI.
 * Provides a clean, maintainable way to define and reuse animations.
 */
object PinAnimationSpecs {
    
    /**
     * Standard durations following Pin UI design principles
     */
    object Duration {
        const val INSTANT = 0
        const val QUICK = 150
        const val STANDARD = 250
        const val RELAXED = 350
        const val SLOW = 500
    }
    
    /**
     * Core animation specifications for common interactions
     */
    object Core {
        /**
         * Quick, responsive animations for immediate feedback
         */
        val quick: AnimationSpec<Float> = tween(
            durationMillis = Duration.QUICK,
            easing = FastOutSlowInEasing
        )
        
        /**
         * Standard animation for most UI transitions
         */
        val standard: AnimationSpec<Float> = tween(
            durationMillis = Duration.STANDARD,
            easing = FastOutSlowInEasing
        )
        
        /**
         * Relaxed animation for complex state changes
         */
        val relaxed: AnimationSpec<Float> = tween(
            durationMillis = Duration.RELAXED,
            easing = FastOutSlowInEasing
        )
        
        /**
         * Smooth spring animation for natural movement
         */
        val spring: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMedium
        )
        
        /**
         * Bouncy spring for playful interactions
         */
        val springBouncy: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }
    
    /**
     * Specialized animations for scroll interactions
     */
    object Scroll {
        /**
         * Smooth momentum-based scrolling with natural deceleration
         */
        val momentum: AnimationSpec<Float> = tween(
            durationMillis = Duration.STANDARD,
            easing = FastOutSlowInEasing
        )
        
        /**
         * Quick snap-to-position for precise scrolling
         */
        val snap: AnimationSpec<Float> = tween(
            durationMillis = Duration.QUICK,
            easing = LinearOutSlowInEasing
        )
        
        /**
         * Wind-up/wind-down effect for enhanced scroll feel
         */
        val windUp: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessHigh
        )
    }
    
    /**
     * Button and interaction-specific animations
     */
    object Interaction {
        /**
         * Hover state transitions
         */
        val hover: AnimationSpec<Float> = tween(
            durationMillis = Duration.QUICK,
            easing = FastOutSlowInEasing
        )
        
        /**
         * Press/release feedback
         */
        val press: AnimationSpec<Float> = tween(
            durationMillis = Duration.QUICK,
            easing = LinearOutSlowInEasing
        )
        
        /**
         * Magnetic attraction effect
         */
        val magnetic: AnimationSpec<Float> = tween(
            durationMillis = 100,
            easing = FastOutLinearInEasing
        )
        
        /**
         * Focus state animations
         */
        val focus: AnimationSpec<Float> = tween(
            durationMillis = Duration.STANDARD,
            easing = FastOutSlowInEasing
        )
    }
    
    /**
     * Layout and structural animations
     */
    object Layout {
        /**
         * Size changes and expansion
         */
        val resize: AnimationSpec<Float> = tween(
            durationMillis = Duration.STANDARD,
            easing = FastOutSlowInEasing
        )
        
        /**
         * Position changes and movement
         */
        val move: AnimationSpec<Float> = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        )
        
        /**
         * Fade in/out transitions
         */
        val fade: AnimationSpec<Float> = tween(
            durationMillis = Duration.STANDARD,
            easing = LinearEasing
        )
    }
    
    /**
     * Configuration values for animation behaviors
     */
    object Config {
        // Magnetic effect sensitivity
        const val MAGNETIC_SENSITIVITY = 0.8f
        val MAGNETIC_MAX_OFFSET = 6.dp
        
        // Scroll momentum configuration
        const val SCROLL_MOMENTUM_DECAY_MS = 600L
        const val SCROLL_MOMENTUM_MAX_LEVEL = 5
        
        // Hover effect configuration
        const val HOVER_ALPHA_ACTIVE = 0.08f
        const val HOVER_ALPHA_INACTIVE = 0.0f
        
        // Spring animation constants
        const val SPRING_DAMPING_BOUNCY = Spring.DampingRatioLowBouncy
        const val SPRING_DAMPING_SMOOTH = Spring.DampingRatioNoBouncy
        const val SPRING_STIFFNESS_LOW = Spring.StiffnessLow
        const val SPRING_STIFFNESS_MEDIUM = Spring.StiffnessMedium
        const val SPRING_STIFFNESS_HIGH = Spring.StiffnessHigh
        
        // Scroll icons visibility
        const val SCROLL_ICON_BASE_ALPHA = 0.3f  // Always visible when scrollable
        const val SCROLL_ICON_HOVER_ALPHA = 1.0f // Full opacity on hover
    }
    
    /**
     * Builder functions for creating configurable animations with custom parameters
     */
    object Builders {
        
        /**
         * Create a custom spring animation with adjustable stiffness and damping
         */
        fun customSpring(
            dampingRatio: Float = Spring.DampingRatioNoBouncy,
            stiffness: Float = Spring.StiffnessMedium
        ): AnimationSpec<Float> = spring(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        )
        
        /**
         * Create a custom tween animation with adjustable duration and easing
         */
        fun customTween(
            durationMs: Int = Duration.STANDARD,
            easing: Easing = FastOutSlowInEasing
        ): AnimationSpec<Float> = tween(
            durationMillis = durationMs,
            easing = easing
        )
        
        /**
         * Smooth scroll physics spring with configurable responsiveness
         */
        fun smoothScrollSpring(
            dampingRatio: Float = Spring.DampingRatioNoBouncy,
            stiffness: Float = Spring.StiffnessMedium
        ): AnimationSpec<Float> = spring(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        )
        
        /**
         * Wind-up/wind-down scroll animation with configurable bounce
         */
        fun windUpSpring(
            dampingRatio: Float = Spring.DampingRatioLowBouncy,
            stiffness: Float = Spring.StiffnessHigh
        ): AnimationSpec<Float> = spring(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        )
        
        /**
         * Magnetic attraction animation with configurable responsiveness
         */
        fun magneticSpring(
            dampingRatio: Float = Spring.DampingRatioNoBouncy,
            stiffness: Float = Spring.StiffnessHigh
        ): AnimationSpec<Float> = spring(
            dampingRatio = dampingRatio,
            stiffness = stiffness
        )
    }
}

