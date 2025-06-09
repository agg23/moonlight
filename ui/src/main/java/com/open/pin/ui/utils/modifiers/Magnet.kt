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

/**
 * Applies a magnetic effect that makes the component move towards the pointer
 */
@SuppressLint("ReturnFromAwaitPointerEventScope")
fun Modifier.magneticEffect(
    enabled: Boolean = true,
    maxOffset: Int = PinDimensions.paddingVerticalSmall.value.toInt(),
    sensitivity: Float = 1.5f,
    onHoverChanged: ((Boolean) -> Unit)? = null
) = composed {
    var buttonOffset by remember { mutableStateOf(IntOffset(0, 0)) }
    var isPointerInBounds by remember { mutableStateOf(false) }
    var isPressed by remember { mutableStateOf(false) }

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
                            val wasInBounds = isPointerInBounds
                            isPointerInBounds = position.x >= 0 && position.x <= size.width &&
                                    position.y >= 0 && position.y <= size.height
                            
                            if (isPointerInBounds) {
                                // Calculate center of the component
                                val centerX = size.width / 2
                                val centerY = size.height / 2
                                
                                // Calculate offset from center with sensitivity adjustment
                                val offsetX = ((position.x - centerX) / centerX * maxOffset * sensitivity).roundToInt()
                                val offsetY = ((position.y - centerY) / centerY * maxOffset * sensitivity).roundToInt()
                                
                                buttonOffset = IntOffset(offsetX, offsetY)
                            } else {
                                // Reset position if moved outside bounds - do this always for magnet effect
                                buttonOffset = IntOffset(0, 0)
                                
                                // But only update hover state if not pressing
                                if (!isPressed && wasInBounds) {
                                    onHoverChanged?.invoke(false)
                                }
                            }
                        }
                        PointerEventType.Exit -> {
                            // Reset position when pointer leaves
                            isPointerInBounds = false
                            buttonOffset = IntOffset(0, 0)
                            
                            // Only update hover state if not pressing
                            if (!isPressed) {
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
