package com.open.pin.ui.components.views

import androidx.compose.foundation.layout.Box
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
 * Describes where items should be positioned on the polygon
 */
enum class PolyItemPlacement {
    VERTICES,  // Place items at the vertices/corners of the polygon
    EDGES      // Place items centered on the edges of the polygon
}

/**
 * Parameters for configuring a PolyView layout
 *
 * @property sides Number of sides for the polygon (minimum 3)
 * @property radius Distance from center to vertices
 * @property rotation Rotation of the polygon in degrees
 * @property itemPlacement Whether to place items at vertices or on edges
 * @property rotateItems Whether to rotate items to face outward
 * @property offsetX Horizontal offset from center
 * @property offsetY Vertical offset from center
 * @property padding Padding around the entire layout
 */
data class PolyViewParams(
    val sides: Int = 4,
    val radius: Dp = 150.dp,
    val rotation: Float = 0f,
    val itemPlacement: PolyItemPlacement = PolyItemPlacement.VERTICES,
    val rotateItems: Boolean = false,
    val offsetX: Dp = 0.dp,
    val offsetY: Dp = 0.dp,
    val padding: Dp = 0.dp
)

/**
 * Arranges composable items in a polygonal formation (triangle, square, pentagon, etc.)
 *
 * @param itemCount Number of items to arrange
 * @param params Configuration parameters for the polygon layout
 * @param modifier Modifier for the container
 * @param itemContent Lambda providing composable content for each position
 */
@Composable
fun PolyView(
    itemCount: Int,
    params: PolyViewParams = PolyViewParams(),
    modifier: Modifier = Modifier,
    itemContent: @Composable (Int) -> Unit
) {
    // Ensure we have a valid polygon (minimum 3 sides)
    val effectiveSides = if (params.itemPlacement == PolyItemPlacement.VERTICES) {
        // For vertices placement, use max of sides param and item count
        maxOf(3, maxOf(params.sides, itemCount))
    } else {
        // For edge placement, sides = itemCount (as each edge gets one item)
        maxOf(3, itemCount)
    }
    
    Box(
        modifier = modifier.padding(params.padding),
        contentAlignment = Alignment.Center
    ) {
        if (itemCount <= 0) return@Box
        
        // Calculate positions for each item
        for (i in 0 until itemCount) {
            // Only place as many items as we have content for
            if (i >= itemCount) break
            
            val angleInRadians = calculateAngleForPolygon(
                itemIndex = i,
                sides = effectiveSides,
                rotation = params.rotation,
                // For edge placement, offset angle to center items on edges
                isEdgePlacement = params.itemPlacement == PolyItemPlacement.EDGES
            )
            
            val x = (cos(angleInRadians) * params.radius.value).toInt()
            val y = (sin(angleInRadians) * params.radius.value).toInt()
            
            // Calculate rotation angle if needed
            val itemRotation = if (params.rotateItems) {
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
                    .rotate(itemRotation),
                contentAlignment = Alignment.Center
            ) {
                itemContent(i)
            }
        }
    }
}

/**
 * Calculates the angle in radians for an item in a polygon layout
 */
private fun calculateAngleForPolygon(
    itemIndex: Int,
    sides: Int,
    rotation: Float,
    isEdgePlacement: Boolean
): Float {
    // Base angle for each vertex
    val angleIncrement = 2.0 * Math.PI / sides
    
    // Convert rotation to radians, adjusting for standard coordinate system
    // In standard math, 0° is at the positive x-axis (3 o'clock) and increases counter-clockwise
    val rotationRadians = Math.toRadians((rotation - 90).toDouble())
    
    // Calculate angle for this item
    val baseAngle = rotationRadians + (angleIncrement * itemIndex)
    
    // For edge placement, add half the increment to center on edges
    return if (isEdgePlacement) {
        (baseAngle + angleIncrement / 2).toFloat()
    } else {
        baseAngle.toFloat()
    }
}

/**
 * Variant of PolyView that accepts direct composable content for convenience
 * 
 * @param params Configuration parameters for the polygon layout
 * @param modifier Modifier for the container
 * @param content Variable argument list of composable content items to arrange in a polygon
 */
@Composable
fun PolyView(
    params: PolyViewParams = PolyViewParams(),
    modifier: Modifier = Modifier,
    vararg content: @Composable () -> Unit
) {
    PolyView(
        itemCount = content.size,
        params = params,
        modifier = modifier
    ) { index ->
        content[index]()
    }
}

