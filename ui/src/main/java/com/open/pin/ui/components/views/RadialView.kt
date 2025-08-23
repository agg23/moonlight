package com.open.pin.ui.components.views

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.layout.layout
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.open.pin.ui.components.button.PinCircularButton
import com.open.pin.ui.debug.AiPinPreview
import com.open.pin.ui.debug.PinPreviewView
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
    val offsetY: Dp? = null,
    val offsetX: Dp? = null,
    val padding: Dp = 0.dp
)

/**
 * Arranges composable items in a circular pattern around a center point.
 *
 * @param modifier Modifier for the container
 * @param itemCount Number of items to arrange
 * @param params Configuration parameters for the radial layout
 * @param itemContent Lambda providing composable content for each position
 */
@Composable
fun RadialView(
    modifier: Modifier = Modifier,
    itemCount: Int,
    params: RadialViewParams = RadialViewParams(),
    itemContent: @Composable (Int) -> Unit
) {
    Box(
        modifier = modifier
            .padding(params.padding)
            .layout { measurable, constraints ->
                val placeable = measurable.measure(constraints)
                
                layout(placeable.width, placeable.height) {
                    // Place the Box at the center of available space
                    placeable.placeRelative(0, 0)
                }
            }
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

            // Bizarrely the Dp operations have strict ordering, so I can't put the trig functions first
            val x = params.radius * cos(angleInRadians) + (params.offsetX ?: 0.dp)
            val y = params.radius * sin(angleInRadians) + (params.offsetY ?: 0.dp)
            
            // Calculate rotation if needed
            val rotation = if (params.rotateItems) {
                Math.toDegrees(angleInRadians.toDouble()).toFloat() + 90f
            } else 0f
            
            Box(
                modifier = Modifier
                    .layout { measurable, constraints ->
                        // Calculate position relative to center
                        val placeable = measurable.measure(constraints)
                        
                        layout(placeable.width, placeable.height) {
                            val centerX = constraints.maxWidth / 2
                            val centerY = constraints.maxHeight / 2
                            
                            placeable.placeRelative(
                                x = centerX + x.roundToPx() - placeable.width / 2,
                                y = centerY + y.roundToPx() - placeable.height / 2
                            )
                        }
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
 * @param modifier Modifier for the container
 * @param params Configuration parameters for the radial layout
 * @param list List of items to arrange
 * @param itemContent Renderer for each item
 */
@Composable
fun <T>RadialView(
    modifier: Modifier = Modifier,
    params: RadialViewParams = RadialViewParams(),
    list: List<T>,
    itemContent: @Composable (T) -> Unit
) {
    RadialView(
        modifier = modifier,
        params = params,
        itemCount = list.size
    ) { index ->
        itemContent(list[index])
    }
}

@AiPinPreview
@Composable
fun RadialViewPreview() {
    val icons = listOf(
        Icons.Default.Home,
        Icons.Default.Email,
        Icons.Default.Call,
        Icons.Default.Notifications,
        Icons.Default.Settings
    )

    PinPreviewView {
        Box(Modifier.fillMaxSize()) {
            RadialView(Modifier, RadialViewParams(), icons) { icon ->
                PinCircularButton({}, icon = icon)
            }
        }
    }
}
