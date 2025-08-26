package com.open.pin.ui.components.button

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.open.pin.ui.animation.PinAnimationSpecs
import com.open.pin.ui.animation.rememberAnimatedInteractionState
import com.open.pin.ui.theme.*
import com.open.pin.ui.utils.PinDimensions
import com.open.pin.ui.utils.modifiers.dashedBorder
import com.open.pin.ui.utils.modifiers.magneticEffect
import com.open.pin.ui.utils.modifiers.snappable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.DpSize
import java.util.UUID


/**
 * Defines the visual style of a Pin button.
 * 
 * - [Default]: Standard filled button with solid background on hover
 * - [List]: Outlined button style for list items
 * - [Borderless]: Button with no border or background
 * - [Accent]: Button that uses the accent color scheme
 */
enum class ButtonStyle { Default, List, Borderless, Accent }

/**
 * Defines the border style of a Pin button.
 * 
 * - [Solid]: Standard solid border
 * - [Dashed]: Dashed border for suggestion buttons
 */
enum class ButtonBorderStyle { Solid, Dashed }

/**
 * Defines how hover and snap effects fill the button.
 * 
 * - [Full]: Complete background fill when hovered/snapped
 * - [Partial]: Light tint background when hovered/snapped
 */
enum class ButtonHoverFill { Full, Partial }

/**
 * Defines text styles for button content.
 * 
 * - [BodyLarge]: Standard button text
 * - [TitleLarge]: Larger, more prominent text
 * - [BodyMedium]: Medium-sized text
 * - [LabelLarge]: Large label text
 */
enum class ButtonTextStyle { BodyLarge, TitleLarge, BodyMedium, LabelLarge }


/**
 * Configures the interactive effects for Pin buttons.
 * 
 * @property enableMagnetic Whether the button attracts the cursor with a magnetic effect
 * @property enableSnap Whether the button participates in Voronoi-based cursor snapping
 * @property hoverFill How hover effects should visually fill the button
 * @property snapWeight Weight factor for the button in the Voronoi diagram (higher = larger region)
 * @property animationSpec Animation specification for interactive effects
 */
data class ButtonEffect(
    val enableMagnetic: Boolean = true,
    val enableSnap: Boolean = true,
    val hoverFill: ButtonHoverFill = ButtonHoverFill.Full,
    val snapWeight: Float = 1f,
    val animationSpec: AnimationSpec<Float> = PinAnimationSpecs.Interaction.hover
)




/**
 * Complete style data for a Pin button.
 * 
 * This immutable data class encapsulates all visual styling attributes
 * for a button, making it easy to pass around and apply consistently.
 * 
 * @property buttonTextStyle Text style for the button content
 * @property border Border specification for the button, or null for no border
 * @property containerColor Background color for the button
 * @property contentColor Color for text and icons
 * @property colors Material3 ButtonColors for the underlying implementation
 * @property shape Corner shape for the button
 * @property contentPadding Padding around the button content
 */
@Immutable
data class ButtonStyleData(
    val buttonTextStyle: TextStyle,
    val border: BorderStroke?,
    val containerColor: Color,
    val contentColor: Color,
    val colors: ButtonColors,
    val shape: RoundedCornerShape = Precomputed.RectCornerShape,
    val contentPadding: PaddingValues = Precomputed.ContentPadding
)


/**
 * Precomputed constants for button styling to improve performance.
 * 
 * These values are calculated once and reused across all button instances
 * to minimize object allocations during recomposition.
 */
private object Precomputed {
    val Transparent = Color.Transparent
    val DisabledContentColor = PinColors.primary.copy(alpha = 0.5f)
    val DisabledBorderColor = PinColors.tertiary.copy(alpha = 0.5f)
    val HoverPartialColor = PinColors.primary.copy(alpha = 0.1f)
    val PrimaryButtonColor = PinColors.primary
    val RectCornerShape = RoundedCornerShape(PinDimensions.buttonCornerRadius)
    val CircleShape = RoundedCornerShape(50)
    val ContentPadding = PaddingValues(
        horizontal = PinDimensions.paddingHorizontalMedium,
        vertical = PinDimensions.paddingVerticalMedium
    )
    
    // Pre-calculate color combinations for different button styles
    val AccentBaseColor = PinColors.tertiary
    val DefaultBaseColor = Transparent
    val LaserOffColor = LaserOff
}


/**
 * Creates and remembers a complete style data object for a Pin button.
 * 
 * This function computes all visual aspects of a button based on its configuration
 * and interaction state, using efficient remembering strategies to minimize
 * recomposition costs.
 * 
 * @param style Visual style variant of the button
 * @param enabled Whether the button is enabled
 * @param borderStyle Style of the button border
 * @param borderThickness Thickness of the button border
 * @param textStyle Text style for the button content
 * @param interaction Current interaction state of the button
 * @param effect Effect configuration for the button
 * @return A complete [ButtonStyleData] object for styling the button
 */
@Composable
fun rememberButtonStyleData(
    style: ButtonStyle,
    enabled: Boolean,
    borderStyle: ButtonBorderStyle,
    borderThickness: Dp,
    textStyle: ButtonTextStyle,
    interaction: com.open.pin.ui.animation.AnimatedInteractionState,
    effect: ButtonEffect
): ButtonStyleData {
    val buttonTextStyle = remember(textStyle) {
        when (textStyle) {
            ButtonTextStyle.BodyLarge -> PinTypography.bodyLarge
            ButtonTextStyle.TitleLarge -> PinTypography.titleLarge
            ButtonTextStyle.BodyMedium -> PinTypography.bodyMedium
            ButtonTextStyle.LabelLarge -> PinTypography.labelLarge
        }
    }

    val hoverProgress by interaction.animatedHoverProgress()
    
    // Cache color calculations to reduce expensive operations
    val containerColor by remember(style, enabled, effect.hoverFill) {
        derivedStateOf {
            when {
                !enabled -> Precomputed.Transparent
                else -> {
                    val baseColor = when (style) {
                        ButtonStyle.Accent -> Precomputed.AccentBaseColor
                        else -> Precomputed.DefaultBaseColor
                    }
                    val hoverColor = when (effect.hoverFill) {
                        ButtonHoverFill.Full -> Precomputed.PrimaryButtonColor
                        ButtonHoverFill.Partial -> Precomputed.HoverPartialColor
                    }
                    // Optimize interpolation with early exit for no-change cases
                    if (hoverProgress == 0f) baseColor
                    else if (hoverProgress == 1f) hoverColor
                    else lerp(baseColor, hoverColor, hoverProgress)
                }
            }
        }
    }

    val contentColor by remember(enabled, effect.hoverFill) {
        derivedStateOf {
            when {
                !enabled -> Precomputed.DisabledContentColor
                effect.hoverFill == ButtonHoverFill.Full -> {
                    // Optimize interpolation with early exit conditions
                    if (hoverProgress == 0f) Precomputed.PrimaryButtonColor
                    else if (hoverProgress == 1f) Precomputed.LaserOffColor
                    else lerp(Precomputed.PrimaryButtonColor, Precomputed.LaserOffColor, hoverProgress)
                }
                else -> Precomputed.PrimaryButtonColor
            }
        }
    }

    val border = remember(style, borderStyle, borderThickness, enabled) {
        if (style == ButtonStyle.Borderless || borderStyle == ButtonBorderStyle.Dashed) {
            null
        } else {
            val borderColor = if (enabled) {
                when (style) {
                    ButtonStyle.Default, ButtonStyle.Borderless -> PinColors.primary
                    ButtonStyle.List, ButtonStyle.Accent -> PinColors.tertiary
                }
            } else {
                Precomputed.DisabledContentColor
            }
            BorderStroke(borderThickness, borderColor)
        }
    }

    val colors = when (style) {
        ButtonStyle.Default, ButtonStyle.Accent -> ButtonDefaults.buttonColors(
            containerColor = containerColor, 
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
        ButtonStyle.List -> ButtonDefaults.outlinedButtonColors(
            containerColor = containerColor, 
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
        ButtonStyle.Borderless -> ButtonDefaults.textButtonColors(
            containerColor = containerColor, 
            contentColor = contentColor,
            disabledContainerColor = containerColor,
            disabledContentColor = contentColor
        )
    }

    return ButtonStyleData(buttonTextStyle, border, containerColor, contentColor, colors)
}

/**
 * Configuration for a button's shape and size.
 * 
 * This class allows for flexible button shape customization including
 * rectangular, rounded, circular, or custom-sized buttons.
 * 
 * @property shape Corner shape for the button
 * @property size Explicit size for fixed-size buttons (like circular buttons)
 * @property minSize Minimum size constraints for the button
 * @property padding Content padding inside the button
 */
data class PinButtonShapeConfig(
    val shape: RoundedCornerShape,
    val size: DpSize? = null,          // Optional, for square or circle
    val minSize: DpSize? = null,       // Optional, for buttons like pills
    val padding: PaddingValues = Precomputed.ContentPadding
)


/**
 * Foundation composable for all Pin UI button variants.
 * 
 * This is the core building block for all buttons in the Pin UI system.
 * It handles common styling, interaction effects, and accessibility concerns.
 * Most custom button components should be built on top of this foundation.
 * 
 * @param interaction State holder for button interactions
 * @param modifier Modifier for the button
 * @param enabled Whether the button is enabled
 * @param borderStyle Style of the button border
 * @param borderThickness Thickness of the button border
 * @param style Visual style variant of the button
 * @param textStyle Text style for the button content
 * @param effect Effect configuration for hover and snap behavior
 * @param shapeConfig Shape and size configuration
 * @param snapId Unique ID for Voronoi snap regions
 * @param onClick Action to perform when clicked
 * @param content Content to display inside the button
 */
@Composable
fun PinButtonBase(
    modifier: Modifier = Modifier,
    interaction: com.open.pin.ui.animation.AnimatedInteractionState = rememberAnimatedInteractionState(),
    enabled: Boolean = true,
    borderStyle: ButtonBorderStyle = ButtonBorderStyle.Solid,
    borderThickness: Dp = PinDimensions.borderThickness,
    style: ButtonStyle = ButtonStyle.Default,
    textStyle: ButtonTextStyle = ButtonTextStyle.BodyLarge,
    effect: ButtonEffect = ButtonEffect(),
    shapeConfig: PinButtonShapeConfig = PinButtonShapeConfig(Precomputed.RectCornerShape),
    snapId: String = remember { "button-${randomId()}" },
    onClick: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val styleData = rememberButtonStyleData(style, enabled, borderStyle, borderThickness, textStyle, interaction, effect)

    val finalModifier = modifier
        .then(
            shapeConfig.size?.let { Modifier.size(it) }
                ?: shapeConfig.minSize?.let { Modifier.defaultMinSize(it.width, it.height) }
                ?: Modifier.fillMaxWidth().height(PinDimensions.buttonHeightPrimary)
        )
        .then(
            if (borderStyle == ButtonBorderStyle.Dashed) Modifier.dashedBorder(
                strokeWidth = borderThickness,
                color = if (enabled) PinColors.tertiary else Precomputed.DisabledBorderColor,
                cornerRadiusDp = PinDimensions.buttonCornerRadius
            ) else Modifier
        )
        .then(
            if (effect.enableMagnetic && enabled) {
                Modifier.magneticEffect(
                    enabled = true,
                    elementId = snapId
                ) { hovered -> 
                    if (hovered || !interaction.isSnapped) {
                        interaction.isHovered = hovered
                    }
                }
            } else Modifier
        )
        .then(
            if (effect.enableSnap && enabled) {
                Modifier.snappable(
                    id = snapId,
                    weight = effect.snapWeight,
                    onSnap = { snapped ->
                        interaction.isSnapped = snapped
                        interaction.isHovered = snapped
                    },
                    onActivate = onClick
                )
            } else Modifier
        )

    val interactionSource = remember { MutableInteractionSource() }

    when (style) {
        ButtonStyle.Borderless -> TextButton(
            onClick = onClick, 
            modifier = finalModifier, 
            enabled = enabled, 
            colors = styleData.colors, 
            interactionSource = interactionSource,
            shape = shapeConfig.shape, 
            contentPadding = shapeConfig.padding, 
            content = content
        )
        ButtonStyle.List -> OutlinedButton(
            onClick = onClick, 
            modifier = finalModifier, 
            enabled = enabled, 
            colors = styleData.colors,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            interactionSource = interactionSource,
            border = styleData.border, 
            shape = shapeConfig.shape, 
            contentPadding = shapeConfig.padding, 
            content = content
        )
        else -> Button(
            onClick = onClick, 
            modifier = finalModifier, 
            enabled = enabled, 
            colors = styleData.colors,
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 0.dp,
                pressedElevation = 0.dp,
                disabledElevation = 0.dp,
                hoveredElevation = 0.dp,
                focusedElevation = 0.dp
            ),
            interactionSource = interactionSource,
            border = styleData.border, 
            shape = shapeConfig.shape, 
            contentPadding = shapeConfig.padding, 
            content = content
        )
    }
}


/**
 * Standard Pin UI button with text and optional icons.
 * 
 * This is the main button component for Pin UI. It supports both leading and trailing
 * icons, various styles, and all the interaction effects of the Pin UI system.
 * 
 * @param text Text content of the button
 * @param onClick Action to perform when the button is clicked
 * @param leadingIcon Optional icon to display before the text
 * @param trailingIcon Optional icon to display after the text
 * @param modifier Modifier for the button
 * @param style Visual style variant of the button
 * @param enabled Whether the button is enabled
 * @param borderStyle Style of the button border
 * @param borderThickness Thickness of the button border
 * @param textStyle Text style for the button content
 * @param effect Effect configuration for hover and snap behavior
 * @param snapId Optional unique ID for Voronoi snap regions (generated from text if not provided)
 */
@Composable
fun PinButton(
    modifier: Modifier = Modifier,
    text: String,
    onClick: () -> Unit,
    leadingIcon: ImageVector? = null,
    trailingIcon: ImageVector? = null,
    style: ButtonStyle = ButtonStyle.Default,
    enabled: Boolean = true,
    borderStyle: ButtonBorderStyle = ButtonBorderStyle.Solid,
    borderThickness: Dp = PinDimensions.borderThickness,
    textStyle: ButtonTextStyle = ButtonTextStyle.BodyLarge,
    effect: ButtonEffect = ButtonEffect(),
    snapId: String? = null
) {
    val interaction = rememberAnimatedInteractionState()

    PinButtonBase(
        modifier = modifier,
        interaction = interaction,
        enabled = enabled,
        borderStyle = borderStyle,
        borderThickness = borderThickness,
        style = style,
        textStyle = textStyle,
        effect = effect,
        snapId = snapId ?: "btn-${text.hashCode()}",
        onClick = onClick
    ) {
        if (leadingIcon != null) {
            Icon(leadingIcon, null, modifier = Modifier.size(PinDimensions.iconSizeLarge))
            Spacer(Modifier.width(PinDimensions.paddingHorizontalSmall))
        }
        Text(
            text = text, 
            style = PinTypography.bodyLarge, 
            maxLines = 1,
            overflow = TextOverflow.Ellipsis, 
            modifier = Modifier.weight(1f)
        )
        if (trailingIcon != null) {
            Spacer(Modifier.width(PinDimensions.paddingHorizontalSmall))
            Icon(trailingIcon, null, modifier = Modifier.size(PinDimensions.iconSizeLarge))
        }
    }
}


/**
 * Circular icon button for the Pin UI system.
 * 
 * This button is designed for UI patterns that need circular buttons with icons,
 * such as floating action buttons or icon-only controls.
 * 
 * @param onClick Action to perform when the button is clicked
 * @param icon Icon to display in the button
 * @param contentDescription Accessibility description for the icon
 * @param size Diameter of the circular button
 * @param effect Effect configuration for hover and snap behavior
 * @param enabled Whether the button is enabled
 */
@Composable
fun PinCircularButton(
    onClick: () -> Unit,
    icon: ImageVector,
    contentDescription: String? = null,
    size: Dp = 120.dp,
    effect: ButtonEffect = ButtonEffect(),
    enabled: Boolean = true
) {
    PinButtonBase(
        enabled = enabled,
        effect = effect,
        shapeConfig = PinButtonShapeConfig(
            shape = Precomputed.CircleShape,
            size = DpSize(size, size),
            padding = PaddingValues(0.dp)
        ),
        onClick = onClick
    ) {
        Icon(icon, contentDescription, modifier = Modifier.size(size * 0.5f))
    }
}


/**
 * Preview of various button styles for design review.
 * 
 * Shows different button variants and configurations to help designers
 * and developers see how buttons will appear in the application.
 */
@Preview
@Composable
fun PinButtonSystemPreview() {
    Column(Modifier.padding(16.dp)) {
        PinButton(text = "Default", onClick = {})
        Spacer(Modifier.height(8.dp))
        PinButton(text = "Accent + Snap", onClick = {}, style = ButtonStyle.Accent, effect = ButtonEffect(enableSnap = true))
        Spacer(Modifier.height(8.dp))
        PinButton(text = "Dashed + Magnetic", onClick = {}, borderStyle = ButtonBorderStyle.Dashed, effect = ButtonEffect(enableMagnetic = true))
        Spacer(Modifier.height(8.dp))
        PinButton(text = "Borderless", onClick = {}, style = ButtonStyle.Borderless)
    }
}

/**
 * Generates a random ID for button elements.
 * Used to create unique identifiers for snapping behavior.
 * 
 * @return A random UUID as a string
 */
private fun randomId() = UUID.randomUUID().toString()
