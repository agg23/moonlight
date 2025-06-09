package com.open.pin.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.Typography
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.open.pin.ui.utils.PinDimensions

// Colors from the official color scheme
val Laser503 = Color(0xFF00FFDF)
val Laser503Secondary = Color(0xFF009986)
val Laser503Accent = Color(0xFF004D43)
val Laser503Muted = Color(0xFF002622)
val Laser503Dim = Color(0xFF000302)

// Black turns laser off to give OLED like behavior
val LaserOff = Color(0xFF000000)

// Add new laser-specific error colors
val Laser503Error = Laser503.copy(alpha = 0.8f)  // Using primary laser color for error
val Laser503OnError = LaserOff  // Using laser off for contrast

// UI Kit Colors
val PinColors = ColorScheme(
    primary = Laser503,
    secondary = Laser503Secondary,
    tertiary = Laser503Accent,
    background = LaserOff,
    surface = LaserOff,
    onPrimary = LaserOff,
    onSecondary = LaserOff,
    onTertiary = LaserOff,
    onBackground = Laser503,
    onSurface = Laser503,
    error = Laser503Error,
    onError = Laser503OnError,
    primaryContainer = Laser503.copy(alpha = 0.1f),
    onPrimaryContainer = Laser503,
    secondaryContainer = Laser503Secondary.copy(alpha = 0.1f),
    onSecondaryContainer = Laser503Secondary,
    tertiaryContainer = Laser503Accent.copy(alpha = 0.1f),
    onTertiaryContainer = Laser503Accent,
    errorContainer = Laser503Error.copy(alpha = 0.1f),
    onErrorContainer = Laser503Error,
    surfaceVariant = Laser503Dim,
    onSurfaceVariant = Laser503Secondary,
    outline = Laser503Muted,
    outlineVariant = Laser503Secondary,
    scrim = LaserOff.copy(alpha = 0.6f),
    inverseSurface = Laser503,
    inverseOnSurface = LaserOff,
    inversePrimary = Laser503.copy(alpha = 0.8f),
    surfaceTint = Laser503
)

// Typography optimized for 2.5x2.5 inch projection at 800x720
val PinTypography = Typography(
    bodyLarge = TextStyle(
        fontFamily = PinFonts.Poppins,
        fontWeight = PinDimensions.fontWeightExtraBold,
        fontSize = PinDimensions.fontSizeLarge,
        lineHeight = 80.sp,
        letterSpacing = 0.5.sp
    ),
    titleLarge = TextStyle(
        fontFamily = PinFonts.Poppins,
        fontWeight = PinDimensions.fontWeightExtraBold,
        fontSize = PinDimensions.fontSizeExtraLarge,
        lineHeight = 88.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = PinFonts.Poppins,
        fontWeight = PinDimensions.fontWeightBold,
        fontSize = PinDimensions.fontSizeMedium,
        lineHeight = 72.sp,
        letterSpacing = 0.5.sp
    ),
    labelLarge = TextStyle(
        fontFamily = PinFonts.Poppins,
        fontWeight = PinDimensions.fontWeightBold,
        fontSize = PinDimensions.fontSizeSmall,
        lineHeight = 64.sp,
        letterSpacing = 0.5.sp
    )
)