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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.R
import com.relaxmind.app.ui.themes.CaregiverPurple
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.ui.themes.TextPrimary

private val LoadingBackground = Color(0xFFFFFFFF)

@Composable
fun RelaxLoadingScreen(
    modifier: Modifier = Modifier,
    message: String = "Cargando...",
    subtitle: String? = null,
    showProgressBar: Boolean = true,
    compact: Boolean = false,
    isCaregiver: Boolean = false
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
            compact = compact,
            isCaregiver = isCaregiver
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
    compact: Boolean = false,
    isCaregiver: Boolean = false
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
                compact = compact,
                isCaregiver = isCaregiver
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
    compact: Boolean = false,
    isCaregiver: Boolean = false
) {
    val color = if (isCaregiver) CaregiverPurple else PatientGreen

    Column(
        modifier = modifier.padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        RadarAnimation(
            color = color,
            modifier = Modifier.size(if (compact) 112.dp else 136.dp)
        )

        Spacer(modifier = Modifier.height(if (compact) 18.dp else 26.dp))

        Text(
            text = message,
            fontFamily = LexendFontFamily,
            fontSize = if (compact) 16.sp else 20.sp,
            fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold,
            color = TextPrimary
        )
    }
}

@Composable
fun LoadingIndicator(
    modifier: Modifier = Modifier,
    isCaregiver: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LoadingBackground),
        contentAlignment = Alignment.Center
    ) {
        RelaxLoadingContent(
            message = stringResource(id = R.string.loading_default),
            isCaregiver = isCaregiver
        )
    }
}

@Composable
fun FullScreenLoadingScreen(
    text: String,
    modifier: Modifier = Modifier,
    backgroundColor: Color = LoadingBackground,
    indicatorColor: Color = PatientGreen,
    textColor: Color = TextPrimary,
    isCaregiver: Boolean = false
) {
    RelaxLoadingScreen(
        modifier = modifier.background(backgroundColor),
        message = stringResource(id = R.string.loading_default),
        isCaregiver = isCaregiver
    )
}

@Composable
fun FullScreenLoadingOverlay(
    modifier: Modifier = Modifier,
    overlayColor: Color = LoadingBackground,
    isCaregiver: Boolean = false
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(overlayColor),
        contentAlignment = Alignment.Center
    ) {
        RelaxLoadingContent(
            message = stringResource(id = R.string.loading_default),
            isCaregiver = isCaregiver
        )
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

@Composable
fun RadarLoadingOverlay(
    visible: Boolean,
    isCaregiver: Boolean,
    modifier: Modifier = Modifier,
    text: String = "Cargando..."
) {
    if (!visible) return

    val color = if (isCaregiver) com.relaxmind.app.ui.themes.CaregiverPurple else PatientGreen

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .pointerInput(Unit) { detectTapGestures { } },
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            RadarAnimation(color = color)
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = text,
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                fontFamily = LexendFontFamily
            )
        }
    }
}

@Composable
fun RadarAnimation(
    color: Color,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "radar")

    val scale1 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale1"
    )
    val alpha1 by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha1"
    )

    val scale2 by transition.animateFloat(
        initialValue = 1f,
        targetValue = 3.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "scale2"
    )
    val alpha2 by transition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = androidx.compose.animation.core.LinearEasing, delayMillis = 1000),
            repeatMode = RepeatMode.Restart
        ),
        label = "alpha2"
    )

    val innerScale by transition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "innerScale"
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .scale(scale1)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha1))
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .scale(scale2)
                .clip(CircleShape)
                .background(color.copy(alpha = alpha2))
        )
        Box(
            modifier = Modifier
                .size(32.dp)
                .scale(innerScale)
                .clip(CircleShape)
                .background(color)
        )
    }
}

