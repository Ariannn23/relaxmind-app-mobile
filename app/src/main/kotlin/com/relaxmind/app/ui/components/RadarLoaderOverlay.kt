package com.relaxmind.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun FullScreenRadarLoaderOverlay(
    color: Color = Color(0xFF4CAF50), // Default green
    backgroundColor: Color = Color.Black.copy(alpha = 0.5f)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        val infiniteTransition = rememberInfiniteTransition(label = "radar_transition")

        // Circle 1
        val scale1 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radar_scale_1"
        )
        val alpha1 by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radar_alpha_1"
        )

        // Circle 2 (Delayed)
        val scale2 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, delayMillis = 666, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radar_scale_2"
        )
        val alpha2 by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, delayMillis = 666, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radar_alpha_2"
        )

        // Circle 3 (Delayed)
        val scale3 by infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = 3f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, delayMillis = 1333, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radar_scale_3"
        )
        val alpha3 by infiniteTransition.animateFloat(
            initialValue = 1f,
            targetValue = 0f,
            animationSpec = infiniteRepeatable(
                animation = tween(2000, delayMillis = 1333, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "radar_alpha_3"
        )

        // Draw circles
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(scale1)
                .background(color = color.copy(alpha = alpha1), shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(scale2)
                .background(color = color.copy(alpha = alpha2), shape = CircleShape)
        )
        Box(
            modifier = Modifier
                .size(60.dp)
                .scale(scale3)
                .background(color = color.copy(alpha = alpha3), shape = CircleShape)
        )
        
        // Center static circle
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(color = color, shape = CircleShape)
        )
    }
}
