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
 * Optimized snap coordinator with event throttling and intelligent caching.
 * Manages Voronoi-based cursor snapping with performance improvements.
 */
class SnapCoordinator {
    // Track all snappable elements currently on screen with their weights
    private val _elements = MutableStateFlow<List<SnappableElement>>(emptyList())
    val elements = _elements.asStateFlow()
    
    // Currently selected element ID if any
    private val _activeElementId = MutableStateFlow<String?>(null)
    val activeElementId = _activeElementId.asStateFlow()
    
    // Element activation callbacks
    private val _activationCallbacks = mutableMapOf<String, () -> Unit>()
    
    // The last known cursor position
    private val _cursorPosition = MutableStateFlow(Offset.Zero)
    val cursorPosition = _cursorPosition.asStateFlow()
    
    // Throttling state
    private var lastUpdateTime = 0L
    private var lastPosition = Offset.Zero
    private val updateThresholdMs = 16L // ~60fps max update rate
    private val movementThreshold = 5f // Minimum movement to trigger update
    
    // Register a new snappable element
    fun registerElement(element: SnappableElement, onActivate: (() -> Unit)? = null) {
        _elements.update { currentElements ->
            currentElements.filter { it.id != element.id } + element
        }
        onActivate?.let { _activationCallbacks[element.id] = it }
        // Recalculate active element when a new element is registered
        updateVoronoiOwner(_cursorPosition.value)
    }
    
    // Unregister an element (when it leaves composition)
    fun unregisterElement(id: String) {
        _elements.update { it.filter { element -> element.id != id } }
        _activationCallbacks.remove(id)
        if (_activeElementId.value == id) {
            // If the active element is removed, find a new one
            updateVoronoiOwner(_cursorPosition.value)
        }
    }
    
    // Process touch events with throttling for better performance
    fun processTouchEvent(event: MotionEvent) {
        val action = event.actionMasked
        if (action == MotionEvent.ACTION_DOWN || 
            action == MotionEvent.ACTION_MOVE || 
            action == MotionEvent.ACTION_HOVER_MOVE) {
            
            val position = Offset(event.x, event.y)
            processPositionUpdate(position, forceUpdate = action == MotionEvent.ACTION_DOWN)
        }
        
        if (action == MotionEvent.ACTION_DOWN) {
            // Laser display only sends down events
            _activeElementId.value?.let { activeId ->
                _activationCallbacks[activeId]?.invoke()
            }
        }
    }
    
    // Process generic motion events (for hover) with throttling
    fun processMotionEvent(event: MotionEvent) {
        if (event.actionMasked == MotionEvent.ACTION_HOVER_MOVE) {
            val position = Offset(event.x, event.y)
            processPositionUpdate(position, forceUpdate = false)
        }
    }
    
    // Throttled position update to reduce computational overhead
    private fun processPositionUpdate(position: Offset, forceUpdate: Boolean = false) {
        val currentTime = System.currentTimeMillis()
        val timeDelta = currentTime - lastUpdateTime
        val positionDelta = kotlin.math.sqrt(
            (position.x - lastPosition.x) * (position.x - lastPosition.x) +
            (position.y - lastPosition.y) * (position.y - lastPosition.y)
        )
        
        // Update if enough time has passed AND cursor moved significantly, or if forced
        if (forceUpdate || (timeDelta >= updateThresholdMs && positionDelta >= movementThreshold)) {
            _cursorPosition.value = position
            updateVoronoiOwner(position)
            lastUpdateTime = currentTime
            lastPosition = position
        } else {
            // Always update cursor position for visual feedback, but skip expensive calculations
            _cursorPosition.value = position
        }
    }
    
    // Optimized Voronoi logic with early exit conditions
    private fun updateVoronoiOwner(position: Offset) {
        val currentElements = _elements.value
        
        // Early exit if no elements
        if (currentElements.isEmpty()) {
            if (_activeElementId.value != null) {
                _activeElementId.value = null
            }
            return
        }
        
        // Use optimized calculation
        val closestElement = VoronoiCalculations.findClosestElement(position, currentElements)
        
        // Only update if changed to reduce state flow emissions
        val newActiveId = closestElement?.id
        if (newActiveId != _activeElementId.value) {
            _activeElementId.value = newActiveId
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