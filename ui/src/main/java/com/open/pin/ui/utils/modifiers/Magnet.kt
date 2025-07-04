package com.open.pin.ui.utils.modifiers

import android.annotation.SuppressLint
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.geometry.Offset
import com.open.pin.ui.utils.PinDimensions
import kotlin.math.sqrt
import kotlin.math.roundToInt
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable

/**
 * Applies a magnetic effect that makes the component move towards the pointer.
 * Uses shared interaction logic and coordinates with SnapCoordinator.
 */
@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun Modifier.magneticEffect(
    enabled: Boolean = true,
    maxOffset: Int = 6, // Reduced from 12dp to 6px for less aggressive movement
    sensitivity: Float = 1.0f, // Balanced sensitivity with smoothing
    elementId: String? = null,
    onHoverChanged: ((Boolean) -> Unit)? = null
) = composed {
    // Hysteresis margin to prevent rapid in/out boundary switching
    val boundaryMargin = 8f
    
    // Movement smoothing parameters
    val damping = 0.7f // Interpolation factor for smooth movement (0.0 = no movement, 1.0 = instant)
    val movementThreshold = 2f // Minimum mouse movement required to trigger position update
    
    // Use shared interaction element state
    val interactionState = rememberInteractionElementState(
        id = elementId ?: remember { "magnetic-${java.util.UUID.randomUUID()}" },
        enabled = enabled
    )
    
    var buttonOffset by remember { mutableStateOf(IntOffset(0, 0)) }
    var targetOffset by remember { mutableStateOf(Offset.Zero) } // Float precision target
    var lastMousePosition by remember { mutableStateOf(Offset.Zero) } // Track last position for threshold
    var isPointerInBounds by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    
    // Track globally active element for persistence
    val isGloballyActive = interactionState.isSnapped
    
    // Reset magnetic state when this element is no longer globally active
    LaunchedEffect(isGloballyActive) {
        if (!isGloballyActive && !isPointerInBounds) {
            targetOffset = Offset.Zero
            buttonOffset = IntOffset(0, 0)
            onHoverChanged?.invoke(false)
        }
    }

    if (!enabled) {
        return@composed this
    }

    this
        .interactionElement(interactionState) { isSnapped ->
            // Handle snap state changes if needed
        }
        .offset { buttonOffset }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    
                    when (event.type) {
                        PointerEventType.Enter -> {
                            isPointerInBounds = true
                            lastMousePosition = event.changes.first().position // Reset for threshold calculation
                            if (!isPressed) {
                                onHoverChanged?.invoke(true)
                            }
                        }
                        PointerEventType.Move -> {
                            val position = event.changes.first().position
                            
                            // Check if pointer is still within the bounds of this element
                            // Use hysteresis to prevent rapid boundary switching
                            val wasInBounds = isPointerInBounds
                            val margin = if (wasInBounds) boundaryMargin else -boundaryMargin
                            isPointerInBounds = position.x >= -margin && position.x <= size.width + margin &&
                                    position.y >= -margin && position.y <= size.height + margin
                            
                            // Skip magnetic positioning when pressed to eliminate jitter
                            if (isPointerInBounds && !isPressed) {
                                // Check if mouse has moved enough to warrant position update
                                val deltaX = position.x - lastMousePosition.x
                                val deltaY = position.y - lastMousePosition.y
                                val movementDistance = sqrt(deltaX * deltaX + deltaY * deltaY)
                                
                                if (movementDistance >= movementThreshold) {
                                    lastMousePosition = position
                                    
                                    // Calculate center of the component
                                    val centerX = size.width / 2f
                                    val centerY = size.height / 2f
                                    
                                    // Calculate target offset from center
                                    val newTargetX = (position.x - centerX) / centerX * maxOffset * sensitivity
                                    val newTargetY = (position.y - centerY) / centerY * maxOffset * sensitivity
                                    
                                    // Smooth interpolation toward target position
                                    val currentTarget = targetOffset
                                    targetOffset = Offset(
                                        x = currentTarget.x + (newTargetX - currentTarget.x) * damping,
                                        y = currentTarget.y + (newTargetY - currentTarget.y) * damping
                                    )
                                    
                                    // Only update buttonOffset if change is significant (reduces recomposition)
                                    val newButtonOffset = IntOffset(targetOffset.x.roundToInt(), targetOffset.y.roundToInt())
                                    if (newButtonOffset != buttonOffset) {
                                        buttonOffset = newButtonOffset
                                    }
                                }
                            } else {
                                // Only reset position if this element is no longer globally active
                                if (!isGloballyActive) {
                                    targetOffset = Offset.Zero
                                    buttonOffset = IntOffset(0, 0)
                                }
                                
                                // But only update hover state if not pressing and not globally active
                                if (!isPressed && wasInBounds && !isGloballyActive) {
                                    onHoverChanged?.invoke(false)
                                }
                            }
                        }
                        PointerEventType.Exit -> {
                            // Mark as no longer in bounds
                            isPointerInBounds = false
                            
                            // Only reset position if this element is no longer globally active
                            if (!isGloballyActive) {
                                targetOffset = Offset.Zero
                                buttonOffset = IntOffset(0, 0)
                            }
                            
                            // Only update hover state if not pressing and not globally active
                            if (!isPressed && !isGloballyActive) {
                                onHoverChanged?.invoke(false)
                            }
                        }
                        PointerEventType.Press -> {
                            isPressed = true
                        }
                        PointerEventType.Release -> {
                            val wasPressed = isPressed
                            isPressed = false
                            
                            // When released, update hover state based on current position
                            if (wasPressed) {
                                onHoverChanged?.invoke(isPointerInBounds)
                            }
                        }
                        else -> { /* ignore other events */ }
                    }
                }
            }
        }
}
