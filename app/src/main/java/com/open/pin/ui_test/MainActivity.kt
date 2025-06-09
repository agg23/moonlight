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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
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
import com.open.pin.ui.utils.modifiers.SnapManager
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.Text
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.open.pin.ui.theme.PinColors
import com.open.pin.ui.components.button.PinButton
import com.open.pin.ui.components.button.PinCircularButton
import com.open.pin.ui.components.views.ListView
import com.open.pin.ui.components.views.ListViewOrientation
import com.open.pin.ui.components.views.PolyItemPlacement
import com.open.pin.ui.components.views.PolyView
import com.open.pin.ui.components.views.PolyViewParams
import com.open.pin.ui.components.views.RadialView
import com.open.pin.ui.components.views.RadialViewParams

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Make the activity full screen by hiding the system bars
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Set content first
        setContent {
            PinTheme {
                FullScreenContent()
            }
        }

        // Then configure window appearance after the view is created
        window.decorView.post {
            hideSystemUI()
        }
    }

    // Update event dispatchers to use our SnapManager
    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        event?.let { SnapManager.processTouchEvent(it) }
        return super.dispatchTouchEvent(event)
    }

    override fun dispatchGenericMotionEvent(event: MotionEvent?): Boolean {
        event?.let { SnapManager.processMotionEvent(it) }
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
                .padding(16.dp)
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
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(color = PinTheme.colors.background)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Common effect for all buttons
        val buttonEffect = remember {
            ButtonEffect(
                enableMagnetic = true,
                enableSnap = true,
                snapWeight = 1.2f
            )
        }
        
        ViewHeading("settings")
        
        // Vertical ListView Demo
        ListView(
            horizontalAlignment = Alignment.CenterHorizontally,
            spacing = 12.dp,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            PinButton(
                text = "List Item 1",
                onClick = { /* Handle click */ },
                style = ButtonStyle.List,
                effect = buttonEffect
            )
            
            PinButton(
                text = "List Item 2",
                onClick = { /* Handle click */ },
                style = ButtonStyle.List,
                effect = buttonEffect
            )
            
            PinButton(
                text = "List Item 3",
                onClick = { /* Handle click */ },
                style = ButtonStyle.List,
                effect = buttonEffect
            )
        }
        
        Divider(Modifier.padding(vertical = 8.dp))
        
        ViewHeading("Horizontal")
        
        // Horizontal ListView Demo
        ListView(
            orientation = ListViewOrientation.HORIZONTAL,
            spacing = 12.dp,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PinCircularButton(
                onClick = { /* Handle click */ },
                icon = Icons.Default.Home,
                size = 60.dp,
                effect = buttonEffect
            )
            
            PinCircularButton(
                onClick = { /* Handle click */ },
                icon = Icons.Default.Search,
                size = 60.dp,
                effect = buttonEffect
            )
            
            PinCircularButton(
                onClick = { /* Handle click */ },
                icon = Icons.Default.Settings,
                size = 60.dp,
                effect = buttonEffect
            )
        }
        
        Divider()
        
        ViewHeading("RadialView")
        
        // RadialView Demo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            RadialView(
                params = RadialViewParams(
                    radius = 120.dp,
                    startAngle = 0f,
                    sweepAngle = 360f
                ),
                modifier = Modifier,
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Add,
                        size = 70.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Edit,
                        size = 70.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Delete,
                        size = 70.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Check,
                        size = 70.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Close,
                        size = 70.dp,
                        effect = buttonEffect
                    )
                }
            )
        }
        
        Divider(Modifier.padding(vertical = 8.dp))
        
        ViewHeading("Triangle")
        
        // PolyView Demo - Triangle
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp),
            contentAlignment = Alignment.Center
        ) {
            PolyView(
                params = PolyViewParams(
                    sides = 3,
                    radius = 120.dp,
                    rotation = 0f
                ),
                modifier = Modifier,
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Close,
                        size = 70.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Close,
                        size = 70.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Close,
                        size = 70.dp,
                        effect = buttonEffect
                    )
                }
            )
        }
        
        Divider(Modifier.padding(vertical = 8.dp))
        
        ViewHeading("capture")
        
        // PolyView Demo - Pentagon with edge placement
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            PolyView(
                params = PolyViewParams(
                    sides = 5,
                    radius = 150.dp,
                    rotation = 0f,
                    itemPlacement = PolyItemPlacement.EDGES
                ),
                modifier = Modifier,
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.AutoMirrored.Filled.ArrowForward,
                        size = 120.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.AutoMirrored.Filled.ArrowBack,
                        size = 120.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.AutoMirrored.Filled.ExitToApp,
                        size = 120.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Settings,
                        size = 120.dp,
                        effect = buttonEffect
                    )
                },
                {
                    PinCircularButton(
                        onClick = { /* Handle click */ },
                        icon = Icons.Default.Search,
                        size = 120.dp,
                        effect = buttonEffect
                    )
                }
            )
        }
        
        Spacer(modifier = Modifier.height(50.dp))
    }
}

@Composable
private fun ViewHeading(title: String, centered: Boolean = false, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
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