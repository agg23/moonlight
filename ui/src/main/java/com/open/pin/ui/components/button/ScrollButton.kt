package com.open.pin.ui.components.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.runtime.State
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.open.pin.ui.animation.PinAnimationSpecs
import com.open.pin.ui.animation.rememberAnimatedInteractionState
import com.open.pin.ui.utils.PinDimensions
import com.open.pin.ui.theme.PinScrollDimensions

/**
 * Direction for scroll button arrows
 */
enum class ScrollDirection {
    UP, DOWN
}

/**
 * Shared utilities for scroll button implementation
 */
private object ScrollButtonUtils {
    
    fun getIconForDirection(direction: ScrollDirection): ImageVector {
        return when (direction) {
            ScrollDirection.UP -> Icons.Default.KeyboardArrowUp
            ScrollDirection.DOWN -> Icons.Default.KeyboardArrowDown
        }
    }
    
    fun getContentDescription(direction: ScrollDirection, custom: String?): String {
        return custom ?: when (direction) {
            ScrollDirection.UP -> "Scroll up"
            ScrollDirection.DOWN -> "Scroll down"
        }
    }
    
    @Composable
    fun animatedScale(
        isHovered: Boolean,
        targetScale: Float = 1.2f
    ): State<Float> {
        return animateFloatAsState(
            targetValue = if (isHovered) targetScale else 1f,
            animationSpec = PinAnimationSpecs.Interaction.hover,
            label = "scroll_button_scale"
        )
    }
    
}

/**
 * Specialized scroll button component that matches the Pin UI web reference.
 * Features compact styling optimized for scroll controls with proper magnetic effects.
 *
 * @param direction Direction of the scroll (UP or DOWN)
 * @param onClick Action to perform when clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param effect Effect configuration for hover and snap behavior
 * @param contentDescription Accessibility description for the button
 */
@Composable
fun PinScrollButton(
    direction: ScrollDirection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    effect: ButtonEffect = ButtonEffect(
        enableMagnetic = true,
        enableSnap = true
    ),
    contentDescription: String? = null
) {
    val interaction = rememberAnimatedInteractionState()
    
    // Use shared animation utilities
    val scale by ScrollButtonUtils.animatedScale(
        isHovered = interaction.effectiveHovered,
        targetScale = 1.2f
    )
    
    val icon = ScrollButtonUtils.getIconForDirection(direction)
    val description = ScrollButtonUtils.getContentDescription(direction, contentDescription)
    
    PinButtonBase(
        modifier = modifier.scale(scale),
        interaction = interaction,
        enabled = enabled,
        style = ButtonStyle.Borderless,
        effect = effect,
        shapeConfig = PinButtonShapeConfig(
            shape = RoundedCornerShape(PinScrollDimensions.scrollButtonCornerRadius),
            size = DpSize(
                width = PinScrollDimensions.scrollButtonWidth, 
                height = PinScrollDimensions.scrollButtonHeight
            ),
            padding = PaddingValues(
                horizontal = PinScrollDimensions.scrollButtonPaddingHorizontal, 
                vertical = 0.dp
            )
        ),
        snapId = "scroll-${direction.name.lowercase()}",
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier.size(PinScrollDimensions.scrollButtonIconSize)
        )
    }
}

/**
 * Convenience composable for scroll up button
 */
@Composable
fun PinScrollUpButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    effect: ButtonEffect = ButtonEffect(
        enableMagnetic = true,
        enableSnap = true
    )
) {
    PinScrollButton(
        direction = ScrollDirection.UP,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        effect = effect
    )
}

/**
 * Convenience composable for scroll down button
 */
@Composable
fun PinScrollDownButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    effect: ButtonEffect = ButtonEffect(
        enableMagnetic = true,
        enableSnap = true
    )
) {
    PinScrollButton(
        direction = ScrollDirection.DOWN,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        effect = effect
    )
}

/**
 * Compact scroll button designed for overlay use, matching Humane AI demo styling.
 * Features ultra-minimal size with horizontal icon stretching and smooth scaling on hover.
 * Magnetic and snap effects work properly without position-based animations.
 *
 * @param direction Direction of the scroll (UP or DOWN)
 * @param onClick Action to perform when clicked
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param effect Effect configuration for hover and snap behavior
 * @param contentDescription Accessibility description for the button
 */
@Composable
fun PinCompactScrollButton(
    direction: ScrollDirection,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    effect: ButtonEffect = ButtonEffect(
        enableMagnetic = true,
        enableSnap = true
    ),
    contentDescription: String? = null,
    interaction: com.open.pin.ui.animation.AnimatedInteractionState? = null
) {
    val buttonInteraction = interaction ?: rememberAnimatedInteractionState()
    
    // Use shared animation utilities with compact-specific values
    val scale by ScrollButtonUtils.animatedScale(
        isHovered = buttonInteraction.effectiveHovered,
        targetScale = 1.15f  // Slightly less scale for compact buttons
    )
    
    val icon = ScrollButtonUtils.getIconForDirection(direction)
    val description = ScrollButtonUtils.getContentDescription(direction, contentDescription)
    
    PinButtonBase(
        modifier = modifier
            .scale(scale),
        interaction = buttonInteraction,
        enabled = enabled,
        style = ButtonStyle.Borderless,
        effect = effect,
        shapeConfig = PinButtonShapeConfig(
            shape = RoundedCornerShape(PinScrollDimensions.compactScrollButtonCornerRadius),
            size = DpSize(
                width = PinScrollDimensions.compactScrollButtonWidth, 
                height = PinScrollDimensions.compactScrollButtonHeight
            ),
            padding = PaddingValues(
                horizontal = PinScrollDimensions.compactScrollButtonPaddingHorizontal, 
                vertical = 0.dp
            )
        ),
        snapId = "compact-scroll-${direction.name.lowercase()}",
        onClick = onClick
    ) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier
                .size(PinScrollDimensions.compactScrollButtonIconSize)
                .graphicsLayer {
                    // Enhanced stretch and bold effect
                    scaleX = 2.2f  // Slightly more horizontal stretch
                    scaleY = 1.2f  // Vertical stretch to make thicker/bolder
                }
        )
    }
}

/**
 * Convenience composable for compact scroll up button
 */
@Composable
fun PinCompactScrollUpButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    effect: ButtonEffect = ButtonEffect(
        enableMagnetic = true,
        enableSnap = true
    ),
    interaction: com.open.pin.ui.animation.AnimatedInteractionState? = null
) {
    PinCompactScrollButton(
        direction = ScrollDirection.UP,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        effect = effect,
        interaction = interaction
    )
}

/**
 * Convenience composable for compact scroll down button
 */
@Composable
fun PinCompactScrollDownButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    effect: ButtonEffect = ButtonEffect(
        enableMagnetic = true,
        enableSnap = true
    ),
    interaction: com.open.pin.ui.animation.AnimatedInteractionState? = null
) {
    PinCompactScrollButton(
        direction = ScrollDirection.DOWN,
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        effect = effect,
        interaction = interaction
    )
}