package com.open.pin.ui.utils

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

/**
 * Central definitions for all dimensions used in the Pin UI kit
 * Optimized for projection on a 2.5x2.5 inch surface at 800x720 resolution
 */
object PinDimensions {
    // Button heights
    val buttonHeightPrimary = 150.dp

    // Button corner radius
    val buttonCornerRadius = 40.dp

    // border thickness
    val borderThickness = 6.dp

    // horizontal and vertical padding
    val paddingHorizontalMedium = 32.dp
    val paddingHorizontalSmall = 16.dp

    val paddingVerticalMedium = 24.dp
    val paddingVerticalSmall = 12.dp

    // Icon sizes
    val iconSizeLarge = 72.dp
    val iconSizeMedium = 48.dp
    val iconSizeSmall = 32.dp

    // Font sizes
    val fontSizeExtraLarge = 72.sp
    val fontSizeLarge = 64.sp
    val fontSizeMedium = 56.sp
    val fontSizeSmall = 48.sp

    // Font weights - using FontWeight from Compose
    val fontWeightExtraBold = FontWeight.ExtraBold
    val fontWeightBold = FontWeight.Bold
    val fontWeightSemiBold = FontWeight.SemiBold
    val fontWeightNormal = FontWeight.Normal
}