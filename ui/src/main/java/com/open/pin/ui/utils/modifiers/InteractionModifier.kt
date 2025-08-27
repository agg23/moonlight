package com.open.pin.ui.utils.modifiers

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.unit.IntSize

/**
 * Shared interaction state for elements that support both magnetic and snap behaviors.
 * Centralizes common logic between magnetic and snap modifiers.
 */
@Composable
fun rememberInteractionElementState(
    id: String = remember { "element-${java.util.UUID.randomUUID()}" },
    weight: Float = 1.0f,
    enabled: Boolean = true,
    onActivate: (() -> Unit)? = null
): InteractionElementState {
    val coordinator = rememberSnapCoordinator()
    return remember(id, weight, enabled, coordinator, onActivate) {
        InteractionElementState(
            id = id,
            weight = weight,
            enabled = enabled,
            coordinator = coordinator,
            onActivate = onActivate
        )
    }
}

/**
 * State holder for interactive elements that centralizes shared logic
 */
class InteractionElementState(
    val id: String,
    val weight: Float,
    val enabled: Boolean,
    val coordinator: SnapCoordinator,
    private val onActivate: (() -> Unit)? = null
) {
    // Element position and size tracking
    var elementSize by mutableStateOf(IntSize.Zero)
        private set
    var elementPosition by mutableStateOf(Offset.Zero)
        private set
    
    // Computed properties
    val center: Offset
        get() = Offset(
            x = elementPosition.x + elementSize.width / 2f,
            y = elementPosition.y + elementSize.height / 2f
        )
    
    val bounds: Rect
        get() = Rect(
            left = elementPosition.x,
            top = elementPosition.y,
            right = elementPosition.x + elementSize.width,
            bottom = elementPosition.y + elementSize.height
        )
    
    // Snap state tracking
    @Composable
    fun isSnappedState(): State<Boolean> {
        val activeElementId by coordinator.activeElementId.collectAsState()
        return derivedStateOf { enabled && activeElementId == id }
    }
    
    /**
     * Update element position and size (called by onGloballyPositioned)
     */
    fun updatePosition(size: IntSize, position: Offset) {
        elementSize = size
        elementPosition = position
        
        // Register/update with coordinator if size is valid
        if (enabled && size != IntSize.Zero) {
            val element = SnappableElement(
                id = id,
                bounds = bounds,
                center = center,
                weight = weight
            )
            coordinator.registerElement(element, onActivate)
        }
    }
    
    /**
     * Unregister from coordinator
     */
    fun unregister() {
        coordinator.unregisterElement(id)
    }
}

/**
 * Base modifier that handles common interaction logic for elements that support
 * both magnetic and snap behaviors. This eliminates code duplication between modifiers.
 */
@Composable
fun Modifier.interactionElement(
    state: InteractionElementState,
    onSnapChanged: ((Boolean) -> Unit)? = null
) = composed {
    // Effect to notify of snap state changes
    val isSnappedState by state.isSnappedState()
    LaunchedEffect(isSnappedState) {
        onSnapChanged?.invoke(isSnappedState)
    }
    
    // Register/unregister with the coordinator
    DisposableEffect(state) {
        onDispose {
            state.unregister()
        }
    }
    
    this.onGloballyPositioned { coordinates ->
        state.updatePosition(
            size = coordinates.size,
            position = coordinates.positionInRoot()
        )
    }
}

/**
 * Configuration for magnetic behavior
 */
data class MagneticConfig(
    val maxOffset: Int,
    val sensitivity: Float = 1.5f,
    val enabled: Boolean = true
)

/**
 * Configuration for snap behavior
 */
data class SnapConfig(
    val weight: Float = 1.0f,
    val enabled: Boolean = true
)