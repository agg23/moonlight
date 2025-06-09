package com.open.pin.ui.utils.modifiers

import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import com.open.pin.ui.utils.PinDimensions

fun Modifier.dashedBorder(
    strokeWidth: Dp,
    color: Color,
    cornerRadiusDp: Dp,
    dashLength: Float = PinDimensions.paddingHorizontalSmall.value,
    gapLength: Float = PinDimensions.paddingHorizontalSmall.value
) = composed(
    factory = {
        val density = LocalDensity.current
        val strokeWidthPx = density.run { strokeWidth.toPx() }
        val cornerRadiusPx = density.run { cornerRadiusDp.toPx() }

        this.then(
            Modifier.drawWithCache {
                onDrawBehind {
                    val rect = size
                    val width = rect.width
                    val height = rect.height

                    // Calculate exact perimeter of a rounded rectangle
                    // Straight segments length
                    val straightSegments = (2 * width) + (2 * height) - (8 * cornerRadiusPx)
                    // Corner segments (approximating arc length)
                    val cornerSegments = 2 * Math.PI.toFloat() * cornerRadiusPx
                    val perimeter = straightSegments + cornerSegments

                    // Calculate pattern count and adjust lengths
                    val patternLength = dashLength + gapLength
                    val count = (perimeter / patternLength).toInt()

                    // Adjust dash and gap to fit perimeter evenly
                    val adjustedPatternLength = perimeter / count
                    val ratio = dashLength / patternLength
                    val adjustedDashLength = adjustedPatternLength * ratio
                    val adjustedGapLength = adjustedPatternLength * (1 - ratio)

                    val stroke = Stroke(
                        width = strokeWidthPx,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(adjustedDashLength, adjustedGapLength),
                            0f
                        )
                    )

                    drawRoundRect(
                        color = color,
                        style = stroke,
                        cornerRadius = CornerRadius(cornerRadiusPx)
                    )
                }
            }
        )
    }
)