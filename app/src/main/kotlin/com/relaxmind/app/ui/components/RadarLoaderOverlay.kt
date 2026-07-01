package com.relaxmind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.relaxmind.app.ui.themes.PatientGreen

@Composable
fun FullScreenRadarLoaderOverlay(
    color: Color = PatientGreen,
    backgroundColor: Color = Color.Black.copy(alpha = 0.5f)
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        RadarAnimation(
            color = color,
            modifier = Modifier.size(180.dp)
        )
    }
}
