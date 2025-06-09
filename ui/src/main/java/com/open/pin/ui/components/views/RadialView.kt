package com.open.pin.ui.components.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlin.math.cos
import kotlin.math.sin

/**
 * Parameters for configuring a RadialView layout
 *
 * @property radius Distance from center to the items
 * @property startAngle Angle where the first item is placed (in degrees, 0° is at 3 o'clock)
 * @property sweepAngle How much of the circle to use (in degrees, 360° is a full circle)
 * @property rotateItems Whether to rotate the items to face the center
 * @property offsetY Vertical offset from center (can adjust the center position)
 * @property offsetX Horizontal offset from center (can adjust the center position)
 * @property padding Padding around the entire layout
 */
data class RadialViewParams(
    val radius: Dp = 150.dp,
    val startAngle: Float = 0f,
    val sweepAngle: Float = 360f,
    val rotateItems: Boolean = false,
    val offsetY: Dp = 0.dp,
    val offsetX: Dp = 0.dp,
    val padding: Dp = 0.dp
)

/**
 * Arranges composable items in a circular pattern around a center point.
 *
 * @param itemCount Number of items to arrange
 * @param params Configuration parameters for the radial layout
 * @param modifier Modifier for the container
 * @param itemContent Lambda providing composable content for each position
 */
@Composable
fun RadialView(
    itemCount: Int,
    params: RadialViewParams = RadialViewParams(),
    modifier: Modifier = Modifier,
    itemContent: @Composable (Int) -> Unit
) {
    Box(
        modifier = modifier.padding(params.padding),
        contentAlignment = Alignment.Center
    ) {
        if (itemCount <= 0) return@Box
        
        // Place each item
        for (i in 0 until itemCount) {
            val angleInRadians = calculateAngleForItem(
                itemIndex = i,
                itemCount = itemCount,
                startAngle = params.startAngle,
                sweepAngle = params.sweepAngle
            )
            
            val x = (cos(angleInRadians) * params.radius.value).toInt()
            val y = (sin(angleInRadians) * params.radius.value).toInt()
            
            // Calculate rotation if needed
            val rotation = if (params.rotateItems) {
                Math.toDegrees(angleInRadians.toDouble()).toFloat() + 90f
            } else 0f
            
            // Place the item
            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = x + params.offsetX.roundToPx(),
                            y = y + params.offsetY.roundToPx()
                        )
                    }
                    .rotate(rotation),
                contentAlignment = Alignment.Center
            ) {
                itemContent(i)
            }
        }
    }
}

/**
 * Calculates the angle in radians for an item in the radial layout
 */
private fun calculateAngleForItem(
    itemIndex: Int,
    itemCount: Int,
    startAngle: Float,
    sweepAngle: Float
): Float {
    // Convert start angle to radians and adjust for standard coordinate system
    val startAngleRadians = Math.toRadians(startAngle.toDouble() - 90).toFloat()
    
    // Calculate angle per item
    val anglePerItem = if (itemCount > 0) {
        Math.toRadians((sweepAngle / itemCount).toDouble()).toFloat()
    } else 0f
    
    // Return the angle for this specific item
    return startAngleRadians + (anglePerItem * itemIndex)
}

/**
 * Variant of RadialView that accepts direct composable content for convenience
 * 
 * @param params Configuration parameters for the radial layout
 * @param modifier Modifier for the container
 * @param content Variable argument list of composable content items to arrange radially
 */
@Composable
fun RadialView(
    params: RadialViewParams = RadialViewParams(),
    modifier: Modifier = Modifier,
    vararg content: @Composable () -> Unit
) {
    RadialView(
        itemCount = content.size,
        params = params,
        modifier = modifier
    ) { index ->
        content[index]()
    }
}

