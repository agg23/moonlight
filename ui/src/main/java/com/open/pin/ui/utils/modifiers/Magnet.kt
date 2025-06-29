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
import com.open.pin.ui.utils.PinDimensions
import kotlin.math.roundToInt
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.LaunchedEffect

/**
 * Applies a magnetic effect that makes the component move towards the pointer.
 * Coordinates with SnapManager to maintain selection state until a new element is selected.
 */
@SuppressLint("ReturnFromAwaitPointerEventScope")
fun Modifier.magneticEffect(
    enabled: Boolean = true,
    maxOffset: Int = PinDimensions.paddingVerticalSmall.value.toInt(),
    sensitivity: Float = 1.5f,
    elementId: String? = null,
    onHoverChanged: ((Boolean) -> Unit)? = null
) = composed {
    var buttonOffset by remember { mutableStateOf(IntOffset(0, 0)) }
    var isPointerInBounds by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }
    
    // Track globally active element for persistence
    val activeElementId by SnapManager.activeElementId.collectAsState()
    val isGloballyActive = elementId != null && activeElementId == elementId
    
    // Reset magnetic state when this element is no longer globally active
    LaunchedEffect(isGloballyActive) {
        if (!isGloballyActive && !isPointerInBounds) {
            buttonOffset = IntOffset(0, 0)
            onHoverChanged?.invoke(false)
        }
    }

    if (!enabled) {
        return@composed this
    }

    this
        .offset { buttonOffset }
        .pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    val event = awaitPointerEvent()
                    
                    when (event.type) {
                        PointerEventType.Enter -> {
                            isPointerInBounds = true
                            if (!isPressed) {
                                onHoverChanged?.invoke(true)
                            }
                        }
                        PointerEventType.Move -> {
                            val position = event.changes.first().position
                            
                            // Check if pointer is still within the bounds of this element
                            // Account for magnetic displacement to prevent feedback loop
                            val wasInBounds = isPointerInBounds
                            isPointerInBounds = position.x >= -buttonOffset.x && position.x <= size.width - buttonOffset.x &&
                                    position.y >= -buttonOffset.y && position.y <= size.height - buttonOffset.y
                            
                            if (isPointerInBounds) {
                                // Calculate center of the component
                                val centerX = size.width / 2
                                val centerY = size.height / 2
                                
                                // Calculate offset from center with sensitivity adjustment
                                val offsetX = ((position.x - centerX) / centerX * maxOffset * sensitivity).roundToInt()
                                val offsetY = ((position.y - centerY) / centerY * maxOffset * sensitivity).roundToInt()
                                
                                buttonOffset = IntOffset(offsetX, offsetY)
                            } else {
                                // Only reset position if this element is no longer globally active
                                if (!isGloballyActive) {
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
