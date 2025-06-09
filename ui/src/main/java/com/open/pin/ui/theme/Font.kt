package com.open.pin.ui.theme

import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.open.pin.ui.R

object PinFonts {
    val Poppins = FontFamily(
        androidx.compose.ui.text.font.Font(R.font.poppins_regular, FontWeight.Normal),
        androidx.compose.ui.text.font.Font(R.font.poppins_medium, FontWeight.Medium),
        androidx.compose.ui.text.font.Font(R.font.poppins_bold, FontWeight.Bold),
        androidx.compose.ui.text.font.Font(R.font.poppins_extrabold, FontWeight.ExtraBold)
    )
}