package com.open.pin.ui.debug

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.open.pin.ui.PinTheme
import com.open.pin.ui.utils.modifiers.ProvideSnapCoordinator
import com.open.pin.ui.utils.modifiers.SnapCoordinator

@Preview(widthDp = 800, heightDp = 720)
annotation class AiPinPreview

@Composable
fun PinPreviewView(content: @Composable () -> Unit) {
    val snapCoordinator = remember { mutableStateOf(SnapCoordinator()) }

    PinTheme {
        ProvideSnapCoordinator(coordinator = snapCoordinator.value) {
            content()
        }
    }
}