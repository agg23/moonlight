package com.open.pin.ui.utils.modifiers

import android.view.MotionEvent
import androidx.compose.runtime.*
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.unit.IntSize
import com.open.pin.ui.utils.VoronoiCalculations
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * Clean, testable snap coordinator that manages Voronoi-based cursor snapping.
 * Replaces the singleton SnapManager with a composition-local approach.
 */
class SnapCoordinator {
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
        val closestElement = VoronoiCalculations.findClosestElement(position, currentElements)
        
        // Update active element if changed
        if (closestElement?.id != _activeElementId.value) {
            _activeElementId.value = closestElement?.id
        }
    }
}

/**
 * CompositionLocal for providing SnapCoordinator throughout the composition tree
 */
val LocalSnapCoordinator = compositionLocalOf<SnapCoordinator?> { null }

/**
 * Provider composable that creates and provides a SnapCoordinator
 */
@Composable
fun ProvideSnapCoordinator(
    coordinator: SnapCoordinator? = null,
    content: @Composable () -> Unit
) {
    val actualCoordinator = coordinator ?: remember { SnapCoordinator() }
    CompositionLocalProvider(LocalSnapCoordinator provides actualCoordinator) {
        content()
    }
}

/**
 * Convenience function to get the current SnapCoordinator
 * Creates a new coordinator if none is provided via CompositionLocal
 */
@Composable
fun rememberSnapCoordinator(): SnapCoordinator {
    val coordinator = LocalSnapCoordinator.current
    
    // If no coordinator is provided, create a new one
    return coordinator ?: remember { SnapCoordinator() }
}