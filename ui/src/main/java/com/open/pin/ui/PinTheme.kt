package com.open.pin.ui

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import com.open.pin.ui.theme.PinColors
import com.open.pin.ui.theme.PinTypography

// Create a CompositionLocal to provide PinColors through the composition tree
private val LocalPinColors = staticCompositionLocalOf<ColorScheme> {
    error("No PinColors provided")
}

/**
 * PinTheme applies the custom Pin UI styling to the content
 */
@Composable
fun PinTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalPinColors provides PinColors
    ) {
        MaterialTheme(
            colorScheme = PinColors,
            typography = PinTypography,
            content = content
        )
    }
}

/**
 * Provides access to the Pin UI colors in composables
 */
object PinTheme {
    val colors: ColorScheme
        @Composable
        @ReadOnlyComposable
        get() = LocalPinColors.current
}