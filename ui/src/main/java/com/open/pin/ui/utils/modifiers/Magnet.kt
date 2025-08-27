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
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.geometry.Offset
import com.open.pin.ui.utils.PinDimensions
import kotlin.math.sqrt
import kotlin.math.roundToInt
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Composable

/**
 * Optimized magnetic effect with reduced computational overhead.
 * Uses cached calculations and intelligent movement thresholds.
 */
@SuppressLint("ReturnFromAwaitPointerEventScope")
@Composable
fun Modifier.magneticEffect(
    enabled: Boolean = true,
    maxOffset: Int = 18,
    sensitivity: Float = 1.5f,
    elementId: String? = null,
    onHoverChanged: ((Boolean) -> Unit)? = null
) = composed {
    // Hysteresis margin to prevent rapid in/out boundary switching
    val boundaryMargin = 8f

    // Optimized movement parameters
    val damping = 0.7f // Interpolation factor for smooth movement
    val movementThreshold = 8f // Increased threshold to reduce update frequency
    val updateThrottleMs = 16L // Limit updates to ~60fps

    var buttonOffset by remember { mutableStateOf(IntOffset(0, 0)) }
    var targetOffset by remember { mutableStateOf(Offset.Zero) } // Float precision target
    var lastMousePosition by remember { mutableStateOf(Offset.Zero) } // Track last position for threshold
    var isPointerInBounds by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    var lastUpdateTime by remember { mutableStateOf(0L) }

    // Pre-calculate expensive values
    val maxOffsetFloat by remember(maxOffset, sensitivity) {
        mutableStateOf(maxOffset * sensitivity)
    }

    fun updateMagneticPosition(position: Offset, elementSize: IntSize) {
        val currentTime = System.currentTimeMillis()

        // Throttle updates for better performance
        if (currentTime - lastUpdateTime >= updateThrottleMs) {
            // Check if mouse has moved enough to warrant position update
            val deltaX = position.x - lastMousePosition.x
            val deltaY = position.y - lastMousePosition.y
            val movementDistanceSquared = deltaX * deltaX + deltaY * deltaY

            // Use squared distance to avoid expensive sqrt calculation
            if (movementDistanceSquared >= movementThreshold * movementThreshold) {
                lastMousePosition = position
                lastUpdateTime = currentTime

                // Pre-calculate center values
                val centerX = elementSize.width * 0.5f
                val centerY = elementSize.height * 0.5f

                // Optimized offset calculation
                val normalizedX = (position.x - centerX) / centerX
                val normalizedY = (position.y - centerY) / centerY

                val newTargetX = normalizedX * maxOffsetFloat
                val newTargetY = normalizedY * maxOffsetFloat

                // Smooth interpolation toward target position
                val currentTarget = targetOffset
                targetOffset = Offset(
                    x = currentTarget.x + (newTargetX - currentTarget.x) * damping,
                    y = currentTarget.y + (newTargetY - currentTarget.y) * damping
                )

                // Only update buttonOffset if change is significant
                val newButtonOffset =
                    IntOffset(targetOffset.x.roundToInt(), targetOffset.y.roundToInt())
                if (newButtonOffset != buttonOffset) {
                    buttonOffset = newButtonOffset
                }
            }
        }
    }

    // Use shared interaction element state (without callback initially)
    val interactionState = rememberInteractionElementState(
        id = elementId ?: remember { "magnetic-${java.util.UUID.randomUUID()}" },
        enabled = enabled
    )

    // Track globally active element for persistence
    val isGloballyActive by interactionState.isSnappedState()

    val globalCursorPosition by interactionState.coordinator.cursorPosition.collectAsState()

    LaunchedEffect(globalCursorPosition, isGloballyActive) {
        if (isGloballyActive && !isPointerInBounds && !isPressed) {
            // Convert global position to local coordinates
            val elementPosition = interactionState.elementPosition
            val localPosition = Offset(
                x = globalCursorPosition.x - elementPosition.x,
                y = globalCursorPosition.y - elementPosition.y
            )
            updateMagneticPosition(localPosition, interactionState.elementSize)
        }
    }

    LaunchedEffect(isGloballyActive) {
        if (isGloballyActive) {
            onHoverChanged?.invoke(true)
        } else if (!isGloballyActive && !isPointerInBounds) {
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
                            lastMousePosition =
                                event.changes.first().position // Reset for threshold calculation
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
                            isPointerInBounds =
                                position.x >= -margin && position.x <= size.width + margin &&
                                        position.y >= -margin && position.y <= size.height + margin

                            // Apply magnetic positioning when pointer is in bounds OR when globally active
                            if (isPointerInBounds && !isPressed) {
                                updateMagneticPosition(position, IntSize(size.width, size.height))
                            } else {
                                // Only reset position if this element is no longer globally active
                                if (!isGloballyActive && (targetOffset != Offset.Zero || buttonOffset != IntOffset.Zero)) {
                                    targetOffset = Offset.Zero
                                    buttonOffset = IntOffset.Zero
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
