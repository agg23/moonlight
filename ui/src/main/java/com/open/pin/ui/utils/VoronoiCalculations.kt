package com.open.pin.ui.utils

import androidx.compose.ui.geometry.Offset
import com.open.pin.ui.utils.modifiers.SnappableElement

/**
 * Pure mathematical functions for Voronoi/Power diagram calculations.
 * These functions are separated from UI logic for better performance and testability.
 */
object VoronoiCalculations {
    
    /**
     * Calculate squared Euclidean distance between two points.
     * Using squared distance avoids expensive square root calculation.
     * 
     * @param a First point
     * @param b Second point
     * @return Squared distance between points
     */
    fun calculateDistanceSquared(a: Offset, b: Offset): Float {
        val dx = a.x - b.x
        val dy = a.y - b.y
        return dx * dx + dy * dy
    }
    
    /**
     * Calculate weighted distance for Power diagram.
     * In a power diagram, larger weights create larger influence regions.
     * 
     * @param point Query point
     * @param elementCenter Center of the element
     * @param weight Element's weight (influence factor)
     * @return Weighted distance (smaller value means closer in power diagram)
     */
    fun calculateWeightedDistance(
        point: Offset, 
        elementCenter: Offset, 
        weight: Float
    ): Float {
        val distanceSquared = calculateDistanceSquared(point, elementCenter)
        return distanceSquared / weight
    }
    
    /**
     * Optimized closest element search with early exit for performance.
     * This is the core algorithm for Voronoi-based cursor snapping.
     * 
     * @param point Query point (usually cursor position)
     * @param elements List of snappable elements
     * @return The closest element, or null if no elements exist
     */
    fun findClosestElement(
        point: Offset, 
        elements: List<SnappableElement>
    ): SnappableElement? {
        if (elements.isEmpty()) return null
        if (elements.size == 1) return elements.first()
        
        var closestElement = elements.first()
        var minDistance = calculateWeightedDistance(point, closestElement.center, closestElement.weight)
        
        // Manual iteration for better performance than minByOrNull
        for (i in 1 until elements.size) {
            val element = elements[i]
            val distance = calculateWeightedDistance(point, element.center, element.weight)
            
            if (distance < minDistance) {
                minDistance = distance
                closestElement = element
            }
        }
        
        return closestElement
    }
    
    /**
     * Optimized grid calculation with intelligent sampling and early exit conditions.
     * Used by VoronoiVisualizer to render the diagram efficiently.
     * 
     * @param width Grid width
     * @param height Grid height  
     * @param stepSize Sampling step size (larger = fewer samples, better performance)
     * @param elements List of snappable elements
     * @return Map of grid coordinates to dominant element IDs
     */
    fun calculateVoronoiGrid(
        width: Int,
        height: Int,
        stepSize: Int,
        elements: List<SnappableElement>
    ): Map<Pair<Int, Int>, String> {
        if (elements.isEmpty()) return emptyMap()
        
        val result = mutableMapOf<Pair<Int, Int>, String>()
        
        // Pre-calculate element centers and weights for faster access
        val elementData = elements.map { element ->
            Triple(element.id, element.center, element.weight)
        }
        
        // Use more efficient iteration with bounds checking
        val maxX = width - stepSize
        val maxY = height - stepSize
        
        for (x in 0..maxX step stepSize) {
            for (y in 0..maxY step stepSize) {
                val point = Offset(x.toFloat(), y.toFloat())
                
                // Optimized closest element search using pre-calculated data
                var closestId: String? = null
                var minDistance = Float.MAX_VALUE
                
                for ((id, center, weight) in elementData) {
                    val distance = calculateWeightedDistance(point, center, weight)
                    if (distance < minDistance) {
                        minDistance = distance
                        closestId = id
                    }
                }
                
                closestId?.let { id ->
                    result[Pair(x, y)] = id
                }
            }
        }
        
        return result
    }
    
    /**
     * Check if a point is within the Voronoi cell of a specific element.
     * Useful for testing and debugging.
     * 
     * @param point Query point
     * @param targetElement The element to test against
     * @param allElements All elements in the diagram
     * @return True if the point belongs to the target element's cell
     */
    fun isPointInElementCell(
        point: Offset,
        targetElement: SnappableElement,
        allElements: List<SnappableElement>
    ): Boolean {
        val closestElement = findClosestElement(point, allElements)
        return closestElement?.id == targetElement.id
    }
    
    /**
     * Calculate the approximate area of an element's Voronoi cell.
     * This is an estimation based on sampling - not exact but useful for analysis.
     * 
     * @param element The element to calculate area for
     * @param allElements All elements in the diagram
     * @param bounds Rectangular bounds to sample within
     * @param sampleSize Number of random sample points to use
     * @return Estimated area as a fraction of total bounds area (0.0 to 1.0)
     */
    fun estimateCellArea(
        element: SnappableElement,
        allElements: List<SnappableElement>,
        bounds: androidx.compose.ui.geometry.Rect,
        sampleSize: Int = 10000
    ): Float {
        if (allElements.isEmpty()) return 0f
        
        var pointsInCell = 0
        
        repeat(sampleSize) {
            val randomX = bounds.left + Math.random().toFloat() * bounds.width
            val randomY = bounds.top + Math.random().toFloat() * bounds.height
            val randomPoint = Offset(randomX, randomY)
            
            if (isPointInElementCell(randomPoint, element, allElements)) {
                pointsInCell++
            }
        }
        
        return pointsInCell.toFloat() / sampleSize
    }
}