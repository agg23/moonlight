package com.open.pin.ui.debug

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import com.open.pin.ui.utils.modifiers.SnapManager

/**
 * A debug tool that visualizes the Voronoi/Power diagram regions of snappable elements.
 * Add this on top of your UI to see how the regions are calculated.
 */
@Composable
fun VoronoiVisualizer(
    modifier: Modifier = Modifier,
    alpha: Float = 0.3f,
    alphaSelected: Float = 0.8f
) {
    val elements by SnapManager.elements.collectAsState()
    val activeId by SnapManager.activeElementId.collectAsState()
    val cursorPosition by SnapManager.cursorPosition.collectAsState()
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // For each pixel on screen (sampled)
            val stepSize = 15 // Sample fewer pixels for performance
            val colors = listOf(
                Color.Red, Color.Green, Color.Blue, 
                Color.Yellow, Color.Cyan, Color.Magenta
            )
            
            for (x in 0 until size.width.toInt() step stepSize) {
                for (y in 0 until size.height.toInt() step stepSize) {
                    val point = Offset(x.toFloat(), y.toFloat())
                    val closestElement = elements.minByOrNull { element ->
                        val distanceSquared = calculateDistanceSquared(point, element.center)
                        distanceSquared / element.weight
                    }
                    
                    // Draw a dot with the color corresponding to the element
                    closestElement?.let { element ->
                        // Get color based on element ID
                        val colorIndex = element.id.hashCode().rem(colors.size).let { 
                            if (it < 0) it + colors.size else it 
                        }
                        val color = colors[colorIndex]
                        
                        // Highlight active region
                        val isActive = element.id == activeId
                        val finalColor = if (isActive) color.copy(alpha = alphaSelected) else color.copy(alpha = alpha)
                        
                        drawRect(
                            color = finalColor,
                            topLeft = Offset(x.toFloat(), y.toFloat()),
                            size = Size(stepSize.toFloat(), stepSize.toFloat())
                        )
                    }
                }
            }
            
            // Draw cursor position
            drawCircle(
                color = Color.White,
                radius = 15f,
                center = cursorPosition
            )
        }
    }
}

private fun calculateDistanceSquared(a: Offset, b: Offset): Float {
    val dx = a.x - b.x
    val dy = a.y - b.y
    return dx * dx + dy * dy
} 