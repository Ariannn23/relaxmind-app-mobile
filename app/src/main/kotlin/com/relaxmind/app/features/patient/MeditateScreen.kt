package com.relaxmind.app.features.patient

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.R
import com.relaxmind.app.Screen
import com.relaxmind.app.data.model.MeditationExercise
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.ScreenHeader
import com.relaxmind.app.ui.components.ScrollToTopEvents
import com.relaxmind.app.ui.components.MeditateSkeleton
import com.relaxmind.app.ui.components.ErrorStateScreen
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.LexendTypography
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.SoftMint
import com.relaxmind.app.ui.themes.SoftCream
import com.relaxmind.app.ui.themes.SoftLavender
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

// -----------------------------------------------------------------------------
// 1. DYNAMIC 3D CLAY ICONS IN COMPOSE CANVAS
// -----------------------------------------------------------------------------

@Composable
fun LungsIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw left lung lobe
        val leftPath = Path().apply {
            moveTo(w * 0.44f, h * 0.32f)
            cubicTo(w * 0.44f, h * 0.12f, w * 0.12f, h * 0.12f, w * 0.10f, h * 0.42f)
            cubicTo(w * 0.08f, h * 0.68f, w * 0.18f, h * 0.92f, w * 0.40f, h * 0.88f)
            cubicTo(w * 0.46f, h * 0.86f, w * 0.46f, h * 0.52f, w * 0.44f, h * 0.32f)
        }
        drawPath(
            path = leftPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF68D391), Color(0xFF0F6E56))
            )
        )
        
        // Draw right lung lobe
        val rightPath = Path().apply {
            moveTo(w * 0.56f, h * 0.32f)
            cubicTo(w * 0.56f, h * 0.12f, w * 0.88f, h * 0.12f, w * 0.90f, h * 0.42f)
            cubicTo(w * 0.92f, h * 0.68f, w * 0.82f, h * 0.92f, w * 0.60f, h * 0.88f)
            cubicTo(w * 0.54f, h * 0.86f, w * 0.54f, h * 0.52f, w * 0.56f, h * 0.32f)
        }
        drawPath(
            path = rightPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF68D391), Color(0xFF0F6E56))
            )
        )
        
        // Trachea/Stem
        drawRoundRect(
            color = Color.White.copy(alpha = 0.55f),
            topLeft = androidx.compose.ui.geometry.Offset(w * 0.48f, h * 0.16f),
            size = androidx.compose.ui.geometry.Size(w * 0.04f, h * 0.42f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2.dp.toPx(), 2.dp.toPx())
        )
        
        // Trachea branches
        drawLine(
            color = Color.White.copy(alpha = 0.55f),
            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.48f),
            end = androidx.compose.ui.geometry.Offset(w * 0.38f, h * 0.58f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.55f),
            start = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.48f),
            end = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.58f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        
        // Glossy bubble 3D highlights
        drawCircle(
            color = Color.White.copy(alpha = 0.16f),
            radius = w * 0.08f,
            center = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.36f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.16f),
            radius = w * 0.08f,
            center = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.36f)
        )
    }
}

@Composable
fun BoxBreathingIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Top Face (Rhombus) - Lightest Blue
        val topPath = Path().apply {
            moveTo(w * 0.5f, h * 0.22f)
            lineTo(w * 0.82f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.54f)
            lineTo(w * 0.18f, h * 0.38f)
            close()
        }
        drawPath(
            path = topPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFEBF8FF), Color(0xFF90CDF4))
            )
        )
        
        // Left Face - Medium Blue
        val leftPath = Path().apply {
            moveTo(w * 0.18f, h * 0.38f)
            lineTo(w * 0.5f, h * 0.54f)
            lineTo(w * 0.5f, h * 0.84f)
            lineTo(w * 0.18f, h * 0.68f)
            close()
        }
        drawPath(
            path = leftPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF63B3ED), Color(0xFF3182CE))
            )
        )
        
        // Right Face - Darkest Blue
        val rightPath = Path().apply {
            moveTo(w * 0.5f, h * 0.54f)
            lineTo(w * 0.82f, h * 0.38f)
            lineTo(w * 0.82f, h * 0.68f)
            lineTo(w * 0.5f, h * 0.84f)
            close()
        }
        drawPath(
            path = rightPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF4299E1), Color(0xFF2B6CB0))
            )
        )
        
        // Highlights to enforce clay 3D effect
        drawPath(
            path = topPath,
            color = Color.White.copy(alpha = 0.5f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawPath(
            path = leftPath,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(width = 1.5.dp.toPx())
        )
        drawPath(
            path = rightPath,
            color = Color.White.copy(alpha = 0.3f),
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
fun BodyScanIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Background aura glow
        drawCircle(
            color = Color(0xFFE9D5FF).copy(alpha = 0.45f),
            radius = w * 0.36f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.56f)
        )
        
        // Crossed legs at base
        val legsPath = Path().apply {
            moveTo(w * 0.18f, h * 0.74f)
            quadraticBezierTo(w * 0.5f, h * 0.88f, w * 0.82f, h * 0.74f)
            quadraticBezierTo(w * 0.86f, h * 0.68f, w * 0.78f, h * 0.64f)
            quadraticBezierTo(w * 0.5f, h * 0.78f, w * 0.22f, h * 0.64f)
            quadraticBezierTo(w * 0.14f, h * 0.68f, w * 0.18f, h * 0.74f)
            close()
        }
        drawPath(
            path = legsPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFD8B4FE), Color(0xFF805AD5))
            )
        )
        
        // Torso
        val torsoPath = Path().apply {
            moveTo(w * 0.5f, h * 0.38f)
            lineTo(w * 0.41f, h * 0.48f)
            lineTo(w * 0.37f, h * 0.66f)
            lineTo(w * 0.63f, h * 0.66f)
            lineTo(w * 0.59f, h * 0.48f)
            close()
        }
        drawPath(
            path = torsoPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFC084FC), Color(0xFF7C3AED))
            )
        )
        
        // Head
        drawCircle(
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFE9D5FF), Color(0xFF9F7AEA))
            ),
            radius = w * 0.12f,
            center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.25f)
        )
        
        // Arm vectors resting on knees
        val leftArm = Path().apply {
            moveTo(w * 0.42f, h * 0.45f)
            quadraticBezierTo(w * 0.26f, h * 0.54f, w * 0.28f, h * 0.68f)
        }
        drawPath(
            path = leftArm,
            color = Color(0xFF9F7AEA),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        val rightArm = Path().apply {
            moveTo(w * 0.58f, h * 0.45f)
            quadraticBezierTo(w * 0.74f, h * 0.54f, w * 0.72f, h * 0.68f)
        }
        drawPath(
            path = rightArm,
            color = Color(0xFF9F7AEA),
            style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Energy sparks
        drawCircle(color = Color.White, radius = w * 0.03f, center = androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.68f))
        drawCircle(color = Color.White, radius = w * 0.03f, center = androidx.compose.ui.geometry.Offset(w * 0.72f, h * 0.68f))
    }
}

@Composable
fun GratitudeIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Draw the heart first (behind/above the hand)
        val heartPath = Path().apply {
            moveTo(w * 0.5f, h * 0.26f)
            cubicTo(w * 0.38f, h * 0.12f, w * 0.2f, h * 0.22f, w * 0.24f, h * 0.42f)
            cubicTo(w * 0.26f, h * 0.55f, w * 0.4f, h * 0.66f, w * 0.5f, h * 0.76f)
            cubicTo(w * 0.6f, h * 0.66f, w * 0.74f, h * 0.55f, w * 0.76f, h * 0.42f)
            cubicTo(w * 0.8f, h * 0.22f, w * 0.62f, h * 0.12f, w * 0.5f, h * 0.26f)
            close()
        }
        drawPath(
            path = heartPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF2DD4BF), Color(0xFF0D9488))
            )
        )
        
        // Inner accent path for 3D feeling
        drawPath(
            path = heartPath,
            color = Color.White.copy(alpha = 0.14f),
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Hand shape supporting heart
        val handPath = Path().apply {
            moveTo(w * 0.16f, h * 0.66f)
            quadraticBezierTo(w * 0.34f, h * 0.85f, w * 0.7f, h * 0.80f)
            quadraticBezierTo(w * 0.82f, h * 0.76f, w * 0.82f, h * 0.63f)
            quadraticBezierTo(w * 0.76f, h * 0.60f, w * 0.70f, h * 0.64f)
            quadraticBezierTo(w * 0.48f, h * 0.70f, w * 0.28f, h * 0.65f)
            close()
        }
        drawPath(
            path = handPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF99F6E4), Color(0xFF14B8A6))
            )
        )
        
        // Inner hand highlight
        drawCircle(color = Color.White, radius = w * 0.05f, center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.36f))
    }
}

@Composable
fun DiaphragmaticIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Leaf shape
        val leafPath = Path().apply {
            moveTo(w * 0.2f, h * 0.8f)
            cubicTo(w * 0.24f, h * 0.4f, w * 0.5f, h * 0.15f, w * 0.84f, h * 0.16f)
            cubicTo(w * 0.76f, h * 0.6f, w * 0.5f, h * 0.84f, w * 0.24f, h * 0.80f)
            close()
        }
        
        drawPath(
            path = leafPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFBBF24), Color(0xFFB45309))
            )
        )
        
        // Leaf center vein
        drawLine(
            color = Color.White.copy(alpha = 0.5f),
            start = androidx.compose.ui.geometry.Offset(w * 0.2f, h * 0.8f),
            end = androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.26f),
            strokeWidth = 2.dp.toPx(),
            cap = StrokeCap.Round
        )
        
        // Small vein lines
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = androidx.compose.ui.geometry.Offset(w * 0.4f, h * 0.6f),
            end = androidx.compose.ui.geometry.Offset(w * 0.31f, h * 0.51f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = androidx.compose.ui.geometry.Offset(w * 0.52f, h * 0.48f),
            end = androidx.compose.ui.geometry.Offset(w * 0.43f, h * 0.39f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = Color.White.copy(alpha = 0.4f),
            start = androidx.compose.ui.geometry.Offset(w * 0.64f, h * 0.36f),
            end = androidx.compose.ui.geometry.Offset(w * 0.55f, h * 0.27f),
            strokeWidth = 1.5.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

// -----------------------------------------------------------------------------
// 2. BANNER DECORATIVE ICONS (STAR & WIND)
// -----------------------------------------------------------------------------

@Composable
fun ClayStarIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        val cy = h / 2
        val outerRadius = w * 0.44f
        val innerRadius = w * 0.19f
        
        val path = Path()
        val points = 5
        var angle = -Math.PI / 2
        val angleStep = Math.PI / points
        
        for (i in 0 until 2 * points) {
            val r = if (i % 2 == 0) outerRadius else innerRadius
            val x = cx + r * Math.cos(angle).toFloat()
            val y = cy + r * Math.sin(angle).toFloat()
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            angle += angleStep
        }
        path.close()
        
        // Draw main star with gold gradient
        drawPath(
            path = path,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFFF9E6), Color(0xFFF6AD55), Color(0xFFDD6B20))
            )
        )
        
        // 3D Shaded halves
        val shadePath = Path().apply {
            moveTo(cx, cy)
            var a = -Math.PI / 2
            for (i in 0 until points) {
                val xOuter = cx + outerRadius * Math.cos(a).toFloat()
                val yOuter = cy + outerRadius * Math.sin(a).toFloat()
                val aInner = a + angleStep
                val xInner = cx + innerRadius * Math.cos(aInner).toFloat()
                val yInner = cy + innerRadius * Math.sin(aInner).toFloat()
                
                lineTo(xOuter, yOuter)
                lineTo(xInner, yInner)
                lineTo(cx, cy)
                
                a += 2 * angleStep
            }
            close()
        }
        drawPath(
            path = shadePath,
            color = Color(0xFFC05621).copy(alpha = 0.25f)
        )
        
        // Glossy bubble accent
        drawCircle(
            color = Color.White.copy(alpha = 0.45f),
            radius = w * 0.1f,
            center = androidx.compose.ui.geometry.Offset(cx - w * 0.08f, cy - h * 0.08f)
        )
    }
}

@Composable
fun WindLeafIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        
        // Wind path 1
        val line1 = Path().apply {
            moveTo(w * 0.1f, h * 0.40f)
            lineTo(w * 0.55f, h * 0.40f)
            quadraticBezierTo(w * 0.72f, h * 0.40f, w * 0.72f, h * 0.50f)
            quadraticBezierTo(w * 0.72f, h * 0.60f, w * 0.55f, h * 0.60f)
            quadraticBezierTo(w * 0.42f, h * 0.60f, w * 0.42f, h * 0.52f)
        }
        drawPath(
            path = line1,
            color = PatientGreen.copy(alpha = 0.65f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Wind path 2 (middle helper)
        val line2 = Path().apply {
            moveTo(w * 0.2f, h * 0.54f)
            lineTo(w * 0.48f, h * 0.54f)
        }
        drawPath(
            path = line2,
            color = PatientGreen.copy(alpha = 0.5f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Wind path 3 (bottom loop)
        val line3 = Path().apply {
            moveTo(w * 0.05f, h * 0.68f)
            lineTo(w * 0.6f, h * 0.68f)
            quadraticBezierTo(w * 0.78f, h * 0.68f, w * 0.78f, h * 0.78f)
            quadraticBezierTo(w * 0.78f, h * 0.88f, w * 0.6f, h * 0.88f)
            quadraticBezierTo(w * 0.48f, h * 0.88f, w * 0.48f, h * 0.80f)
        }
        drawPath(
            path = line3,
            color = PatientGreen.copy(alpha = 0.65f),
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )
        
        // Tiny floating leaf in the wind
        val leafPath = Path().apply {
            moveTo(w * 0.62f, h * 0.28f)
            cubicTo(w * 0.64f, h * 0.16f, w * 0.78f, h * 0.10f, w * 0.88f, h * 0.16f)
            cubicTo(w * 0.84f, h * 0.32f, w * 0.74f, h * 0.38f, w * 0.62f, h * 0.28f)
        }
        drawPath(
            path = leafPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFF68D391), Color(0xFF0F6E56))
            )
        )
        
        // Center line for floating leaf
        drawLine(
            color = Color.White.copy(alpha = 0.6f),
            start = androidx.compose.ui.geometry.Offset(w * 0.62f, h * 0.28f),
            end = androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.19f),
            strokeWidth = 1.dp.toPx()
        )
    }
}

// -----------------------------------------------------------------------------
// 3. AUXILIARY CARD DRAWABLES (CLOCK & CATEGORY ICONS)
// -----------------------------------------------------------------------------

@Composable
fun ClockIcon(
    modifier: Modifier = Modifier,
    tint: Color = TextSecondary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val radius = w * 0.45f
        val cx = w / 2
        val cy = h / 2
        
        // Outer circle
        drawCircle(
            color = tint,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(cx, cy),
            style = Stroke(width = 1.8.dp.toPx())
        )
        
        // Center node
        drawCircle(
            color = tint,
            radius = 1.5.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )
        
        // Hours / Minutes hands
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(cx, cy),
            end = androidx.compose.ui.geometry.Offset(cx, cy - radius * 0.58f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = androidx.compose.ui.geometry.Offset(cx, cy),
            end = androidx.compose.ui.geometry.Offset(cx + radius * 0.42f, cy + radius * 0.1f),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun CategoryIcon(
    type: String,
    modifier: Modifier = Modifier,
    tint: Color = TextSecondary
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2
        
        when (type.lowercase()) {
            "respiración", "respiracion" -> {
                // Outline leaf for breathing
                val leafPath = Path().apply {
                    moveTo(w * 0.25f, h * 0.75f)
                    cubicTo(w * 0.28f, h * 0.4f, w * 0.5f, h * 0.25f, w * 0.75f, h * 0.25f)
                    cubicTo(w * 0.72f, h * 0.6f, w * 0.5f, h * 0.75f, w * 0.25f, h * 0.75f)
                }
                drawPath(
                    path = leafPath,
                    color = tint,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )
                drawLine(
                    color = tint,
                    start = androidx.compose.ui.geometry.Offset(w * 0.25f, h * 0.75f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.66f, h * 0.34f),
                    strokeWidth = 1.8.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
            "mindfulness" -> {
                // Lotus petal shapes for mindfulness
                val lotus = Path().apply {
                    moveTo(cx, h * 0.75f)
                    quadraticBezierTo(w * 0.2f, h * 0.6f, w * 0.24f, h * 0.34f)
                    quadraticBezierTo(w * 0.5f, h * 0.5f, cx, h * 0.75f)
                    quadraticBezierTo(w * 0.76f, h * 0.5f, w * 0.76f, h * 0.34f)
                    quadraticBezierTo(w * 0.8f, h * 0.6f, cx, h * 0.75f)
                    
                    // Central petal
                    moveTo(cx, h * 0.75f)
                    quadraticBezierTo(w * 0.38f, h * 0.32f, cx, h * 0.18f)
                    quadraticBezierTo(w * 0.62f, h * 0.32f, cx, h * 0.75f)
                }
                drawPath(
                    path = lotus,
                    color = tint,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
            else -> {
                // Relajación - wavy ocean/wind lines
                val wave = Path().apply {
                    moveTo(w * 0.2f, h * 0.42f)
                    quadraticBezierTo(w * 0.35f, h * 0.28f, w * 0.5f, h * 0.42f)
                    quadraticBezierTo(w * 0.65f, h * 0.56f, w * 0.8f, h * 0.42f)
                    
                    moveTo(w * 0.2f, h * 0.64f)
                    quadraticBezierTo(w * 0.35f, h * 0.50f, w * 0.5f, h * 0.64f)
                    quadraticBezierTo(w * 0.65f, h * 0.78f, w * 0.8f, h * 0.64f)
                }
                drawPath(
                    path = wave,
                    color = tint,
                    style = Stroke(width = 1.8.dp.toPx(), cap = StrokeCap.Round)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 4. ROUTER FOR MEDITATION ICON TYPE
// -----------------------------------------------------------------------------

@Composable
fun MeditationExerciseIcon(
    exerciseId: String,
    modifier: Modifier = Modifier
) {
    when (exerciseId) {
        "resp_478" -> LungsIcon(modifier)
        "resp_caja" -> BoxBreathingIcon(modifier)
        "body_scan" -> BodyScanIcon(modifier)
        "gratitud" -> GratitudeIcon(modifier)
        "resp_diafragmatica" -> DiaphragmaticIcon(modifier)
        else -> LungsIcon(modifier) // Fallback default
    }
}

// Helper to resolve specific background details and category overrides
private fun getExerciseDisplayConfig(exercise: MeditationExercise, isDark: Boolean = false): Triple<Color, String, Int> {
    return when (exercise.id) {
        "resp_478" -> Triple(if (isDark) Color(0xFF132A24) else Color(0xFFE6F7F0), "Respiración", exercise.durationMinutes)
        "resp_caja" -> Triple(if (isDark) Color(0xFF132333) else Color(0xFFEBF8FF), "Respiración", exercise.durationMinutes)
        "body_scan" -> Triple(if (isDark) Color(0xFF1F1B38) else Color(0xFFF1EDFF), "Mindfulness", exercise.durationMinutes)
        "gratitud" -> Triple(if (isDark) Color(0xFF11302A) else Color(0xFFE6FFFA), "Mindfulness", exercise.durationMinutes)
        "resp_diafragmatica" -> Triple(if (isDark) Color(0xFF451A03) else Color(0xFFFFFBEB), "Relajación", exercise.durationMinutes)
        else -> {
            val bg = when (exercise.type) {
                "respiracion" -> if (isDark) Color(0xFF132A24) else Color(0xFFE6F7F0)
                "mindfulness" -> if (isDark) Color(0xFF1F1B38) else Color(0xFFF1EDFF)
                else -> if (isDark) Color(0xFF133026) else Color(0xFFE6F9F2)
            }
            val cat = when (exercise.type) {
                "respiracion" -> "Respiración"
                "mindfulness" -> "Mindfulness"
                else -> "Relajación"
            }
            Triple(bg, cat, exercise.durationMinutes)
        }
    }
}

// -----------------------------------------------------------------------------
// 5. REDESIGNED COMPOSABLES & MAIN LAYOUT
// -----------------------------------------------------------------------------

@Composable
fun MeditateHeader(
    modifier: Modifier = Modifier
) {
    ScreenHeader(
        title = "Meditar",
        subtitle = "Respira hondo, relájate y encuentra tu centro",
        modifier = modifier,
        horizontalPadding = 0.dp
    )
}

@Composable
fun GoalBannerCard(
    goalTitle: String,
    isCompleted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressedState && !isCompleted) 0.98f else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "banner-scale"
    )

    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val cardColor = if (isDark) Color(0xFF1E2F2C) else SoftMint
    val titleColor = if (isDark) Color(0xFF68D391).copy(alpha = 0.8f) else PatientGreen.copy(alpha = 0.8f)
    val textColor = if (isDark) com.relaxmind.app.ui.themes.TextDarkPrimary else TextPrimary

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = PatientGreen.copy(alpha = 0.08f),
                spotColor = PatientGreen.copy(alpha = 0.08f)
            )
            .border(
                width = 1.2.dp,
                color = Color(0xFF68D391).copy(alpha = 0.35f),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isCompleted,
                onClick = onClick
            ),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Star 3D illustration left
            ClayStarIcon(
                modifier = Modifier
                    .size(44.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = stringResource(id = R.string.meditate_today_goal),
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = titleColor
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = goalTitle,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor
                )
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // Wind decoration right or CheckCircle if completed
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completado",
                    tint = PatientGreen,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                WindLeafIcon(
                    modifier = Modifier.size(44.dp)
                )
            }
        }
    }
}

@Composable
fun MeditationExerciseCard(
    exercise: MeditationExercise,
    isMetaDeHoy: Boolean,
    isCompleted: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val (bgColor, categoryLabel, displayDuration) = getExerciseDisplayConfig(exercise, isDark)
    
    val interactionSource = remember { MutableInteractionSource() }
    val isPressedState by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressedState) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 100),
        label = "card-scale"
    )
    val cardColor = if (isDark) com.relaxmind.app.ui.themes.SurfaceDark else Color.White
    val borderColor = if (isDark) com.relaxmind.app.ui.themes.BorderDark else Color(0xFFE2F3EB)
    val shadowColor = if (isDark) Color(0xFF68D391).copy(alpha = 0.05f) else Color(0xFF8FA89B).copy(alpha = 0.15f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 14.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .border(
                width = 1.2.dp,
                color = borderColor,
                shape = RoundedCornerShape(24.dp)
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                enabled = !isCompleted,
                onClick = onClick
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left illustrated icon box
            Box(
                modifier = Modifier
                    .size(62.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                MeditationExerciseIcon(
                    exerciseId = exercise.id,
                    modifier = Modifier.size(48.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Middle descriptions
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = exercise.title,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isDark) com.relaxmind.app.ui.themes.TextDarkPrimary else TextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    
                    if (isMetaDeHoy) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isDark) Color(0xFF132A23) else Color(0xFFE6F7F0))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = stringResource(id = R.string.meditate_goal_badge),
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 9.sp,
                                color = if (isDark) Color(0xFF68D391) else PatientGreen
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    CategoryIcon(
                        type = categoryLabel,
                        modifier = Modifier.size(14.dp),
                        tint = if (isDark) com.relaxmind.app.ui.themes.TextDarkSecondary else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = categoryLabel,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isDark) com.relaxmind.app.ui.themes.TextDarkSecondary else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    ClockIcon(
                        modifier = Modifier.size(13.dp),
                        tint = if (isDark) com.relaxmind.app.ui.themes.TextDarkSecondary else TextSecondary
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$displayDuration min",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = if (isDark) com.relaxmind.app.ui.themes.TextDarkSecondary else TextSecondary
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(10.dp))
            
            // Right Arrow
            if (isCompleted) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Completado",
                    tint = PatientGreen,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Comenzar ejercicio",
                    tint = PatientGreenLight,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 6. MAIN MEDITATE SCREEN COMPOSABLE
// -----------------------------------------------------------------------------

@Composable
fun MeditateScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    showBottomNav: Boolean = true
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val exercises by viewModel.meditationExercises.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val dailyGoalExercise by viewModel.dailyGoalExercise.collectAsState()

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadDashboardData()
                viewModel.loadMeditationExercises()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // App theme state
    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val bgColor = if (isDark) com.relaxmind.app.ui.themes.BackgroundDark else Color.White
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        ScrollToTopEvents.requests.collect { route ->
            if (route == Screen.Meditate.route) {
                listState.animateScrollToItem(0)
            }
        }
    }

    // Apply Lexend Theme globally inside the screen
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = LexendTypography
    ) {
        Scaffold(
            containerColor = bgColor,
            bottomBar = {
                if (showBottomNav) {
                    RelaxBottomNav(
                        selectedRoute = "patient/meditate",
                        onNavigate = onNavigate,
                        role = AppRole.PATIENT,
                        darkMode = isDark
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Soft background gradient spots
                if (!isDark) {
                    SoftGradientBackground(animateBlobs = true)
                }

                if (isLoading && exercises.isEmpty() && error == null) {
                    MeditateSkeleton(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(top = 24.dp, bottom = 120.dp)
                    )
                } else if (error != null && exercises.isEmpty()) {
                    ErrorStateScreen(
                        message = error ?: "",
                        onRetry = { 
                            viewModel.loadDashboardData()
                            viewModel.loadMeditationExercises()
                        }
                    )
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        item {
                            MeditateHeader()
                        }
                        
                        item { Spacer(modifier = Modifier.height(4.dp)) }

                        // Daily Goal Banner Card
                        val showDailyGoal = dailyGoalExercise != null
                        if (showDailyGoal) {
                            item {
                                dailyGoalExercise?.let { exercise ->
                                    val isGoalCompleted = dailyGoal?.completed == true
                                    GoalBannerCard(
                                        goalTitle = exercise.title,
                                        isCompleted = isGoalCompleted,
                                        onClick = {
                                            if (!isGoalCompleted) {
                                                onNavigate(Screen.MeditationDetail.createRoute(exercise.id))
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        // Static spacing if banner not visible
                        if (!showDailyGoal) {
                            item { Spacer(modifier = Modifier.height(4.dp)) }
                        }

                        // Vertical Exercises Catalog List
                        items(exercises) { exercise ->
                            val isMetaDeHoy = dailyGoalExercise?.id == exercise.id
                            
                            // Visual slide animation on load
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(animationSpec = tween(600, easing = FastOutSlowInEasing)) +
                                        slideInVertically(
                                            initialOffsetY = { 80 },
                                            animationSpec = tween(600, easing = FastOutSlowInEasing)
                                        )
                            ) {
                                MeditationExerciseCard(
                                    exercise = exercise,
                                    isMetaDeHoy = isMetaDeHoy,
                                    isCompleted = isMetaDeHoy && dailyGoal?.completed == true,
                                    onClick = {
                                        onNavigate(Screen.MeditationDetail.createRoute(exercise.id))
                                    }
                                )
                            }
                        }

                        // Margin bottom safe space to float above bottom capsule navbar
                        item { Spacer(modifier = Modifier.height(110.dp)) }
                    }
                }
            }
        }
    }
}

