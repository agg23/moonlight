package com.open.pin.ui.components.button

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.open.pin.ui.utils.PinDimensions
import com.open.pin.ui.theme.PinScrollDimensions

/**
 * Direction for scroll button arrows
 */
enum class ScrollDirection {
    UP, DOWN
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
    val interaction = remember { ButtonInteractionState() }
    
    // Animate scale based on hover state (matching web reference behavior)
    val scale by animateFloatAsState(
        targetValue = if (interaction.effectiveHovered) 1.2f else 1f,
        animationSpec = spring(dampingRatio = 0.6f, stiffness = 300f),
        label = "scroll_button_scale"
    )
    
    val icon: ImageVector = when (direction) {
        ScrollDirection.UP -> Icons.Default.KeyboardArrowUp
        ScrollDirection.DOWN -> Icons.Default.KeyboardArrowDown
    }
    
    val description = contentDescription ?: when (direction) {
        ScrollDirection.UP -> "Scroll up"
        ScrollDirection.DOWN -> "Scroll down"
    }
    
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
    interaction: ButtonInteractionState? = null
) {
    val buttonInteraction = interaction ?: remember { ButtonInteractionState() }
    
    // Animate scale based on hover state (matching HTML demo behavior)
    val scale by animateFloatAsState(
        targetValue = if (buttonInteraction.effectiveHovered) 1.15f else 1f,
        animationSpec = spring(dampingRatio = 0.7f, stiffness = 400f),
        label = "compact_scroll_button_scale"
    )
    
    // Peek-out animation: buttons emerge from edges when hovered
    val peekOffset by animateDpAsState(
        targetValue = if (buttonInteraction.effectiveHovered) 0.dp else when (direction) {
            ScrollDirection.UP -> (-20).dp    // Tucked up into top edge
            ScrollDirection.DOWN -> 20.dp     // Tucked down into bottom edge
        },
        animationSpec = spring(dampingRatio = 0.8f, stiffness = 300f),
        label = "compact_scroll_button_peek"
    )
    
    val icon: ImageVector = when (direction) {
        ScrollDirection.UP -> Icons.Default.KeyboardArrowUp
        ScrollDirection.DOWN -> Icons.Default.KeyboardArrowDown
    }
    
    val description = contentDescription ?: when (direction) {
        ScrollDirection.UP -> "Scroll up"
        ScrollDirection.DOWN -> "Scroll down"
    }
    
    PinButtonBase(
        modifier = modifier
            .scale(scale)
            .offset(y = peekOffset),
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
        enableMagnetic = true,  // Disabled to prevent conflict with peek-out animation
        enableSnap = true
    ),
    interaction: ButtonInteractionState? = null
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
        enableMagnetic = true,  // Disabled to prevent conflict with peek-out animation
        enableSnap = true
    ),
    interaction: ButtonInteractionState? = null
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