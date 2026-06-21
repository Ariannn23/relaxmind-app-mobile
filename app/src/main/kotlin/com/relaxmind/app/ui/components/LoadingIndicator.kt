package com.relaxmind.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.R
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

private val LoadingBackground = Color(0xFFFFFFFF)
private val LoadingSoftMint = Color(0xFFF4FBF7)
private val LoadingTrack = Color(0xFFDDF3E9)

@Composable
fun RelaxLoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "Cargando...",
    subtitle: String? = null,
    showProgressBar: Boolean = true,
    compact: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(LoadingBackground, LoadingSoftMint, LoadingBackground)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        RelaxLoadingContent(
            message = message,
            subtitle = subtitle,
            showProgressBar = showProgressBar,
            compact = compact,
            modifier = Modifier.offset(y = if (compact) 0.dp else (-28).dp)
        )
    }
}

@Composable
fun RelaxLoadingOverlay(
    visible: Boolean,
    modifier: Modifier = Modifier,
    message: String = "Cargando...",
    subtitle: String? = null,
    showProgressBar: Boolean = true,
    compact: Boolean = false
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(220)),
        exit = fadeOut(animationSpec = tween(180))
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(LoadingBackground),
            contentAlignment = Alignment.Center
        ) {
            RelaxLoadingContent(
                message = message,
                subtitle = subtitle,
                showProgressBar = showProgressBar,
                compact = compact
            )
        }
    }
}

@Composable
fun RelaxLoadingContent(
    modifier: Modifier = Modifier,
    message: String = "Cargando...",
    subtitle: String? = null,
    showProgressBar: Boolean = true,
    compact: Boolean = false
) {
    val characterSize = if (compact) 70.dp else 100.dp
    val progressWidth = if (compact) 188.dp else 260.dp

    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RelaxLoadingCharacter(size = characterSize)

        Spacer(modifier = Modifier.height(if (compact) 16.dp else 24.dp))

        Text(
            text = message,
            fontFamily = LexendFontFamily,
            fontSize = if (compact) 18.sp else 26.sp,
            color = PatientGreen,
            style = MaterialTheme.typography.titleLarge
        )

        if (!subtitle.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = subtitle,
                fontFamily = LexendFontFamily,
                fontSize = if (compact) 12.sp else 14.sp,
                color = TextSecondary,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        if (showProgressBar) {
            Spacer(modifier = Modifier.height(if (compact) 18.dp else 26.dp))
            RelaxProgressBar(width = progressWidth)
        }
    }
}

@Composable
fun RelaxLoadingCharacter(
    modifier: Modifier = Modifier,
    size: Dp = 184.dp
) {
    val transition = rememberInfiniteTransition(label = "relax-loading-character")
    val verticalOffset by transition.animateFloat(
        initialValue = -4f,
        targetValue = 4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading-character-offset"
    )
    val characterScale by transition.animateFloat(
        initialValue = 0.985f,
        targetValue = 1.02f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2100, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading-character-scale"
    )

    Box(
        modifier = modifier.size(size + 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(size * 0.92f)
                .shadow(
                    elevation = 18.dp,
                    shape = CircleShape,
                    ambientColor = PatientGreenLight.copy(alpha = 0.18f),
                    spotColor = PatientGreenLight.copy(alpha = 0.18f)
                )
                .clip(CircleShape)
                .background(LoadingTrack.copy(alpha = 0.36f))
        )

        Image(
            painter = painterResource(id = R.drawable.loaded3),
            contentDescription = "Cargando RelaxMind",
            modifier = Modifier
                .size(size)
                .offset(y = verticalOffset.dp)
                .scale(characterScale),
            contentScale = ContentScale.Fit
        )
    }
}

@Composable
fun RelaxProgressBar(
    modifier: Modifier = Modifier,
    width: Dp = 260.dp
) {
    val transition = rememberInfiniteTransition(label = "relax-loading-progress")
    
    // Progress width animation
    val progress by transition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "loading-progress"
    )

    // Shimmer/highlight shift animation for 3D gloss effect
    val shimmerOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress-shimmer"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        // Main container with 3D inset styling
        Box(
            modifier = Modifier
                .width(width)
                .height(14.dp)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(7.dp),
                    clip = false,
                    ambientColor = Color(0xFF1B4332).copy(alpha = 0.3f),
                    spotColor = Color(0xFF1B4332).copy(alpha = 0.3f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(Color(0xFFE5F6EE), Color(0xFFC7EBD8))
                    ),
                    shape = RoundedCornerShape(7.dp)
                )
                .border(
                    width = 1.dp,
                    color = Color(0xFFCBE7DB),
                    shape = RoundedCornerShape(7.dp)
                )
        ) {
            // Active progress bar
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(progress)
                    .clip(RoundedCornerShape(7.dp))
                    .background(
                        // Vertical glossy gradient for 3D cylinder illusion
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color(0xFFE2F9EB), // Bright highlight top
                                PatientGreenLight, // Midtone
                                Color(0xFF0F6E56)  // Shadow bottom
                            )
                        )
                    )
            ) {
                // Moving glossy shimmer overlay
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.linearGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.0f),
                                    Color.White.copy(alpha = 0.55f),
                                    Color.White.copy(alpha = 0.0f)
                                ),
                                start = androidx.compose.ui.geometry.Offset(shimmerOffset - 200f, 0f),
                                end = androidx.compose.ui.geometry.Offset(shimmerOffset, 100f)
                            )
                        )
                )
            }
        }
    }
}

@Composable
fun RelaxAnimatedProgressBar(
    modifier: Modifier = Modifier,
    width: Dp = 260.dp
) {
    RelaxProgressBar(modifier = modifier, width = width)
}

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LoadingBackground),
        contentAlignment = Alignment.Center
    ) {
        RelaxLoadingContent(message = stringResource(id = R.string.loading_default))
    }
}

@Composable
fun FullScreenLoadingScreen(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LoadingBackground,
    indicatorColor: Color = PatientGreen,
    textColor: Color = TextPrimary
) {
    RelaxLoadingScreen(
        modifier = modifier.background(backgroundColor),
        message = text.ifBlank { stringResource(id = R.string.loading_default) }
    )
}

@Composable
fun FullScreenLoadingOverlay(
    modifier: Modifier = Modifier,
    overlayColor: Color = LoadingBackground
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(overlayColor),
        contentAlignment = Alignment.Center
    ) {
        RelaxLoadingContent(message = stringResource(id = R.string.loading_default))
    }
}

@Preview(name = "Relax Loading Screen", showBackground = true)
@Composable
private fun RelaxLoadingScreenPreview() {
    RelaxMindTheme(darkTheme = false) {
        RelaxLoadingScreen()
    }
}

@Preview(name = "Relax Loading Compact", showBackground = true)
@Composable
private fun RelaxLoadingCompactPreview() {
    RelaxMindTheme(darkTheme = false) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            RelaxLoadingContent(compact = true)
        }
    }
}

