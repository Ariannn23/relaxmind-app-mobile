package com.relaxmind.app.ui.components.auth

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.themes.BackgroundWhite
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.SoftCream
import com.relaxmind.app.ui.themes.SoftLavender
import com.relaxmind.app.ui.themes.SoftMint
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.CaregiverBlue

@Composable
fun SoftGradientBackground(
    modifier: Modifier = Modifier,
    animateBlobs: Boolean = true,
    role: AppRole = AppRole.PATIENT
) {
    val infiniteTransition = rememberInfiniteTransition(label = "login-blobs")
    val blobOffset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = if (animateBlobs) 12f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "blob-drift"
    )

    // Colors
    val color1 = if (role == AppRole.PATIENT) SoftMint.copy(alpha = 0.95f) else CaregiverBlue.copy(alpha = 0.6f)
    val color1Light = if (role == AppRole.PATIENT) PatientGreenLight.copy(alpha = 0.18f) else CaregiverIndigo.copy(alpha = 0.12f)

    val color2 = if (role == AppRole.PATIENT) SoftCream.copy(alpha = 0.92f) else SoftLavender.copy(alpha = 0.8f)
    val color2Light = if (role == AppRole.PATIENT) Color(0xFFFFE8C8).copy(alpha = 0.15f) else Color.Transparent

    val color3 = if (role == AppRole.PATIENT) SoftLavender.copy(alpha = 0.75f) else CaregiverIndigo.copy(alpha = 0.4f)
    val bottomTint = if (role == AppRole.PATIENT) SoftMint.copy(alpha = 0.08f) else CaregiverIndigo.copy(alpha = 0.05f)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundWhite)
    ) {
        Box(
            modifier = Modifier
                .size(320.dp)
                .offset(x = (-80).dp, y = (420 + blobOffset).dp)
                .blur(90.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(color1, color1Light, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(280.dp)
                .offset(x = 220.dp, y = (-40 - blobOffset * 0.5f).dp)
                .blur(80.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(color2, color2Light, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .size(260.dp)
                .offset(x = 180.dp, y = 520.dp)
                .blur(85.dp)
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(color3, Color.Transparent)
                    ),
                    shape = CircleShape
                )
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            BackgroundWhite.copy(alpha = 0.4f),
                            BackgroundWhite,
                            bottomTint
                        )
                    )
                )
        )
    }
}
