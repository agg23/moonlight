package com.open.pin.ui_test

import android.os.Bundle
import android.view.MotionEvent
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.core.view.WindowCompat
import androidx.compose.foundation.layout.Box
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.open.pin.ui.PinTheme
import com.open.pin.ui.components.button.ButtonStyle
import com.open.pin.ui.components.button.ButtonEffect
import com.open.pin.ui.debug.VoronoiVisualizer
import com.open.pin.ui.utils.modifiers.ProvideSnapCoordinator
import com.open.pin.ui.utils.modifiers.SnapCoordinator
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.open.pin.ui.theme.PinColors
import com.open.pin.ui.components.button.PinButton
import com.open.pin.ui.components.views.ListView

class MainActivity : ComponentActivity() {
    private lateinit var snapCoordinator: SnapCoordinator
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the activity full screen by hiding the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Create snap coordinator for touch event handling
        snapCoordinator = SnapCoordinator()

        // Set content first
        setContent {
            PinTheme {
                ProvideSnapCoordinator(coordinator = snapCoordinator) {
                    FullScreenContent()
                }
            }
        }

        // Then configure window appearance after the view is created
        window.decorView.post {
            hideSystemUI()
        }
    }

    // Update event dispatchers to use our SnapCoordinator
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let { snapCoordinator.processTouchEvent(it) }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
        event?.let { snapCoordinator.processMotionEvent(it) }
        return super.dispatchGenericMotionEvent(event)
    }

    private fun hideSystemUI() {
        window.setDecorFitsSystemWindows(false)
        window.insetsController?.let {
            it.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
            it.systemBarsBehavior = WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemUI()
        }
    }
}

@Composable
fun FullScreenContent() {
    // State to control visualizer visibility
    var showVisualizer by remember { mutableStateOf(false) }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(color = PinTheme.colors.background)
    ) {
        // Main UI content
        PinUiDemo()
        
        // Debug dot - small circle in top right corner
        Box(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .size(24.dp)  // Small size
                .background(
                    color = if (showVisualizer) PinColors.primary else PinColors.tertiary,
                    shape = CircleShape
                )
                .clickable { showVisualizer = !showVisualizer }
                .zIndex(10f)
        )
        
        // Conditionally show the Voronoi visualizer overlay
        if (showVisualizer) {
            VoronoiVisualizer(
                modifier = Modifier.zIndex(5f),
                alpha = 0.4f
            )
        }
    }
}

@Composable
fun PinUiDemo() {
    // Simple settings demo with just the buttons we want to test
    MenuDemo()
}

@Composable
private fun MenuDemo() {
    val buttonEffect = remember {
        ButtonEffect(
            enableMagnetic = true,
            enableSnap = true,
            snapWeight = 1.2f
        )
    }
    
    ListView(
        horizontalAlignment = Alignment.CenterHorizontally,
        leftIndent = 0.dp,      // No ListView indents
        rightIndent = 0.dp,
        itemSpacing = 16.dp,
        showScrollButtons = true,
        autoHideButtons = true
    ) {
        Column(
            horizontalAlignment = Alignment.Start,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ViewHeading(title = "Demo", centered = false)  // Edge-to-edge heading
            
            Column(
                modifier = Modifier.padding(
                    start = 12.dp,    // Manual indent for buttons only
                    end = 36.dp       // Space for scroll button
                ),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                PinButton(
                    text = "Item1",
                    onClick = { /* Handle click */ },
                    style = ButtonStyle.List,
                    effect = buttonEffect
                )
                
                PinButton(
                    text = "Item2",
                    onClick = { /* Handle click */ },
                    style = ButtonStyle.List,
                    effect = buttonEffect
                )
                
                PinButton(
                    text = "Item3",
                    onClick = { /* Handle click */ },
                    style = ButtonStyle.List,
                    effect = buttonEffect
                )

                PinButton(
                    text = "Item4",
                    onClick = { /* Handle click */ },
                    style = ButtonStyle.List,
                    effect = buttonEffect
                )

                PinButton(
                    text = "Item5",
                    onClick = { /* Handle click */ },
                    style = ButtonStyle.List,
                    effect = buttonEffect
                )

                PinButton(
                    text = "Item6",
                    onClick = { /* Handle click */ },
                    style = ButtonStyle.List,
                    effect = buttonEffect
                )

                PinButton(
                    text = "Item7",
                    onClick = { /* Handle click */ },
                    style = ButtonStyle.List,
                    effect = buttonEffect
                )

                PinButton(
                    text = "Item8",
                    onClick = { /* Handle click */ },
                    style = ButtonStyle.List,
                    effect = buttonEffect
                )

                PinButton(
                    text = "Item9",
                    onClick = { /* Handle click */ },
                    style = ButtonStyle.List,
                    effect = buttonEffect
                )
            }
        }
    }
}

@Composable
private fun ViewHeading(modifier: Modifier = Modifier, title: String, centered: Boolean = false) {
    Box(
        modifier = modifier,
        contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart
    ) {
        Text(
            text = title,
            color = PinColors.primary,
            fontSize = 88.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PinUiDemoPreview() {
    PinTheme {
        FullScreenContent()
    }
}