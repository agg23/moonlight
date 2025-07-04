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
import com.open.pin.ui.utils.VoronoiCalculations
import com.open.pin.ui.utils.modifiers.rememberSnapCoordinator

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
    val coordinator = rememberSnapCoordinator()
    val elements by coordinator.elements.collectAsState()
    val activeId by coordinator.activeElementId.collectAsState()
    val cursorPosition by coordinator.cursorPosition.collectAsState()
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            // For each pixel on screen (sampled)
            val stepSize = 15 // Sample fewer pixels for performance
            val colors = listOf(
                Color.Red, Color.Green, Color.Blue, 
                Color.Yellow, Color.Cyan, Color.Magenta
            )
            
            // Use efficient grid calculation from pure functions
            val voronoiGrid = VoronoiCalculations.calculateVoronoiGrid(
                width = size.width.toInt(),
                height = size.height.toInt(), 
                stepSize = stepSize,
                elements = elements
            )
            
            // Render the grid
            voronoiGrid.forEach { (coordinate, elementId) ->
                val (x, y) = coordinate
                
                // Find the element by ID for color mapping
                val element = elements.find { it.id == elementId }
                
                element?.let {
                    // Get color based on element ID
                    val colorIndex = it.id.hashCode().rem(colors.size).let { hash -> 
                        if (hash < 0) hash + colors.size else hash 
                    }
                    val color = colors[colorIndex]
                    
                    // Highlight active region
                    val isActive = it.id == activeId
                    val finalColor = if (isActive) color.copy(alpha = alphaSelected) else color.copy(alpha = alpha)
                    
                    drawRect(
                        color = finalColor,
                        topLeft = Offset(x.toFloat(), y.toFloat()),
                        size = Size(stepSize.toFloat(), stepSize.toFloat())
                    )
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

 