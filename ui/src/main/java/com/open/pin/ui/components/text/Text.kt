package com.open.pin.ui.components.text

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import com.open.pin.ui.theme.PinColors
import com.open.pin.ui.theme.PinTypography

@Composable
fun PinText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = PinColors.primary,
    style: TextStyle = PinTypography.bodyLarge,
    textAlign: TextAlign? = null,
    overflow: TextOverflow = TextOverflow.Clip,
    maxLines: Int = Int.MAX_VALUE
) {
    Text(
        text = text,
        modifier = modifier,
        color = color,
        style = style,
        textAlign = textAlign,
        overflow = overflow,
        maxLines = maxLines
    )
}