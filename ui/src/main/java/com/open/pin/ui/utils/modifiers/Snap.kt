package com.open.pin.ui.utils.modifiers

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect

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
 * @param onActivate Callback that fires when this element is activated
 */
@Composable
fun Modifier.snappable(
    id: String = remember { "snappable-${java.util.UUID.randomUUID()}" },
    weight: Float = 1.0f,
    onSnap: (Boolean) -> Unit = {},
    onActivate: (() -> Unit)? = null
) = composed {
    // Use shared interaction element state
    val interactionState = rememberInteractionElementState(
        id = id,
        weight = weight,
        enabled = true,
        onActivate = onActivate
    )
    
    this.interactionElement(
        state = interactionState,
        onSnapChanged = onSnap
    )
} 