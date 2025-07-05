package com.open.pin.ui.debug

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntSize
import com.open.pin.ui.utils.VoronoiCalculations
import com.open.pin.ui.utils.modifiers.rememberSnapCoordinator
import kotlinx.coroutines.delay

/**
 * A debug tool that visualizes the Voronoi/Power diagram regions of snappable elements.
 * Optimized version with intelligent caching and reduced calculation frequency.
 */
@Composable
fun VoronoiVisualizer(
    modifier: Modifier = Modifier,
    alpha: Float = 0.3f,
    alphaSelected: Float = 0.8f,
    refreshRateMs: Long = 100L // Limit updates to 10fps for performance
) {
    val coordinator = rememberSnapCoordinator()
    val elements by coordinator.elements.collectAsState()
    val activeId by coordinator.activeElementId.collectAsState()
    val cursorPosition by coordinator.cursorPosition.collectAsState()
    
    // Cached grid state
    var cachedGrid by remember { mutableStateOf<Map<Pair<Int, Int>, String>>(emptyMap()) }
    var cachedElements by remember { mutableStateOf<List<com.open.pin.ui.utils.modifiers.SnappableElement>>(emptyList()) }
    var cachedSize by remember { mutableStateOf(IntSize.Zero) }
    var lastUpdateTime by remember { mutableLongStateOf(0L) }
    
    // Colors - pre-calculated for better performance
    val colors = remember {
        listOf(
            Color.Red, Color.Green, Color.Blue, 
            Color.Yellow, Color.Cyan, Color.Magenta
        )
    }
    
    // Throttled grid recalculation
    LaunchedEffect(elements, refreshRateMs) {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastUpdateTime > refreshRateMs && elements != cachedElements) {
            delay(16) // Defer to next frame to avoid blocking
            cachedElements = elements
            lastUpdateTime = currentTime
        }
    }
    
    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val currentSize = IntSize(size.width.toInt(), size.height.toInt())
            
            // Only recalculate grid if elements or size changed significantly
            val shouldRecalculate = cachedElements != elements || 
                                  kotlin.math.abs(currentSize.width - cachedSize.width) > 50 ||
                                  kotlin.math.abs(currentSize.height - cachedSize.height) > 50
            
            if (shouldRecalculate && elements.isNotEmpty()) {
                // Increased step size for much better performance (reduced from 15 to 30)
                val stepSize = 30
                
                cachedGrid = VoronoiCalculations.calculateVoronoiGrid(
                    width = currentSize.width,
                    height = currentSize.height, 
                    stepSize = stepSize,
                    elements = elements
                )
                cachedSize = currentSize
            }
            
            // Render the cached grid
            cachedGrid.forEach { (coordinate, elementId) ->
                val (x, y) = coordinate
                
                // Pre-compute color index to avoid repeated hash calculations
                val colorIndex = elementId.hashCode().let { hash ->
                    val index = hash.rem(colors.size)
                    if (index < 0) index + colors.size else index
                }
                val color = colors[colorIndex]
                
                // Highlight active region
                val isActive = elementId == activeId
                val finalColor = if (isActive) color.copy(alpha = alphaSelected) else color.copy(alpha = alpha)
                
                drawRect(
                    color = finalColor,
                    topLeft = Offset(x.toFloat(), y.toFloat()),
                    size = Size(30f, 30f) // Fixed size for better performance
                )
            }
            
            // Draw cursor position with reduced frequency updates
            drawCircle(
                color = Color.White,
                radius = 12f, // Slightly smaller for less overdraw
                center = cursorPosition
            )
        }
    }
}

 