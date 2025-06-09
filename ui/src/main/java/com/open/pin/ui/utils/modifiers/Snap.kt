package com.open.pin.ui.utils.modifiers

import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * A singleton manager that tracks all snappable elements and handles cursor snapping
 * using Voronoi/Power diagram regions.
 */
object SnapManager {
    // Track all snappable elements currently on screen with their weights
    private val _elements = MutableStateFlow<List<SnappableElement>>(emptyList())
    val elements = _elements.asStateFlow()
    
    // Currently selected element ID if any
    private val _activeElementId = MutableStateFlow<String?>(null)
    val activeElementId = _activeElementId.asStateFlow()
    
    // The last known cursor position
    private val _cursorPosition = MutableStateFlow(Offset.Zero)
    val cursorPosition = _cursorPosition.asStateFlow()
    
    // Register a new snappable element
    fun registerElement(element: SnappableElement) {
        _elements.update { currentElements ->
            currentElements.filter { it.id != element.id } + element
        }
        // Recalculate active element when a new element is registered
        updateVoronoiOwner(_cursorPosition.value)
    }
    
    // Unregister an element (when it leaves composition)
    fun unregisterElement(id: String) {
        _elements.update { it.filter { element -> element.id != id } }
        if (_activeElementId.value == id) {
            // If the active element is removed, find a new one
            updateVoronoiOwner(_cursorPosition.value)
        }
    }
    
    // Process touch events from the activity
    fun processTouchEvent(event: MotionEvent) {
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN || 
            action == MotionEvent.ACTION_MOVE || 
            action == MotionEvent.ACTION_HOVER_MOVE) {
            
            val position = Offset(event.x, event.y)
            _cursorPosition.value = position
            updateVoronoiOwner(position)
        }
    }
    
    // Process generic motion events (for hover)
    fun processMotionEvent(event: MotionEvent) {
        if (event.actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
            val position = Offset(event.x, event.y)
            _cursorPosition.value = position
            updateVoronoiOwner(position)
        }
    }
    
    // Core Voronoi logic - determine which element should capture the cursor
    private fun updateVoronoiOwner(position: Offset) {
        val currentElements = _elements.value
        if (currentElements.isEmpty()) return
        
        // Find the element with minimum weighted distance (core of Voronoi/Power diagram)
        val closestElement = currentElements.minByOrNull { element ->
            // Apply power diagram weighting - divide squared distance by weight
            val distanceSquared = calculateDistanceSquared(position, element.center)
            distanceSquared / element.weight
        }
        
        // Update active element if changed
        if (closestElement?.id != _activeElementId.value) {
            _activeElementId.value = closestElement?.id
        }
    }
    
    private fun calculateDistanceSquared(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return dx * dx + dy * dy
    }
}

/**
 * Data class representing a UI element that can be snapped to
 */
data class SnappableElement(
    val id: String,
    val bounds: Rect,
    val center: Offset,
    val weight: Float = 1.0f  // Higher weight means larger Voronoi cell (used for Power diagram)
)

/**
 * Modifier that makes a composable element snappable by the cursor using Voronoi regions.
 * This creates a virtual "hit box" that extends beyond the visual bounds of the element
 * based on a Voronoi/Power diagram of all elements on screen.
 *
 * @param id Unique identifier for this snappable element
 * @param weight Weight factor that increases this element's "influence" in the Voronoi diagram
 *               Values > 1 give this element a larger region of influence
 *               Values < 1 shrink this element's region of influence
 * @param onSnap Callback that fires when this element's snap state changes
 */
@Composable
fun Modifier.snappable(
    id: String = remember { "snappable-${java.util.UUID.randomUUID()}" },
    weight: Float = 1.0f,
    onSnap: (Boolean) -> Unit = {}
) = composed {
    // Remember position state
    var elementSize by remember { mutableStateOf(IntSize.Zero) }
    var elementPosition by remember { mutableStateOf(Offset.Zero) }
    
    // Track whether this element is currently snapped to
    val isSnapped by remember {
        derivedStateOf {
            SnapManager.activeElementId.value == id
        }
    }
    
    // Effect to notify of snap state changes
    LaunchedEffect(isSnapped) {
        onSnap(isSnapped)
    }
    
    // Register/unregister with the manager
    DisposableEffect(id, elementSize, elementPosition, weight) {
        if (elementSize != IntSize.Zero) {
            val element = SnappableElement(
                id = id,
                bounds = Rect(
                    left = elementPosition.x,
                    top = elementPosition.y,
                    right = elementPosition.x + elementSize.width,
                    bottom = elementPosition.y + elementSize.height
                ),
                center = Offset(
                    x = elementPosition.x + elementSize.width / 2f,
                    y = elementPosition.y + elementSize.height / 2f
                ),
                weight = weight
            )

            SnapManager.registerElement(element)
            
            onDispose {
                SnapManager.unregisterElement(id)
            }
        } else {
            onDispose {}
        }
    }
    
    this
        .onGloballyPositioned { coordinates ->
            elementSize = coordinates.size
            elementPosition = coordinates.positionInRoot()
        }
} 