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
     * Find the closest element to a point using power diagram weighting.
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
        
        return elements.minByOrNull { element ->
            calculateWeightedDistance(point, element.center, element.weight)
        }
    }
    
    /**
     * Get the dominant element at each point in a grid for visualization.
     * Used by VoronoiVisualizer to render the diagram.
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
        val result = mutableMapOf<Pair<Int, Int>, String>()
        
        for (x in 0 until width step stepSize) {
            for (y in 0 until height step stepSize) {
                val point = Offset(x.toFloat(), y.toFloat())
                val closestElement = findClosestElement(point, elements)
                
                closestElement?.let { element ->
                    result[Pair(x, y)] = element.id
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