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
    val buttonHeightPrimary = 135.dp
    val buttonHeightSecondary = 160.dp
    val buttonHeightBorderless = 140.dp
    val buttonHeightListItem = 160.dp

    // Button corner radius
    val buttonCornerRadius = 40.dp

    // border thickness
    val borderThickness = 6.dp
    val borderThicknessSecondary = 2.dp

    // Corner radius options
    val buttonCornerRadiusDefault = 16.dp
    val buttonCornerRadiusRound = 24.dp

    // horizontal and vertical padding
    val paddingHorizontalLarge = 48.dp
    val paddingHorizontalMedium = 32.dp
    val paddingHorizontalSmall = 16.dp
    val paddingHorizontalExtraSmall = 8.dp

    val paddingVerticalLarge = 40.dp
    val paddingVerticalMedium = 24.dp
    val paddingVerticalSmall = 12.dp
    val paddingVerticalExtraSmall = 6.dp

    // Update button content padding to use new horizontal/vertical values
    val buttonPaddingHorizontal = paddingHorizontalLarge
    val buttonPaddingVertical = paddingVerticalMedium

    // Spacing between elements
    val spacingBetweenElements = 24.dp

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