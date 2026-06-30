package com.relaxmind.app.features.patient

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.R
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.LexendTypography
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import kotlinx.coroutines.delay
import kotlin.math.sin

// ─────────────────────────────────────────────────────────────────────────────
// 1. DATA AND THEME DEFINITIONS
// ─────────────────────────────────────────────────────────────────────────────

private data class BreathingPhaseInfo(
    val stepText: String,
    val stepIndicator: Int,
    val title: String,
    val subtitle: String,
    val secondsRemaining: Int,
    val scale: Float,
    val currentPhaseText: String,
    val subText: String
)

data class MeditationThemeConfig(
    val bgGradient: List<Color>,
    val primaryColor: Color,
    val lightColor: Color,
    val accentColor: Color,
    val iconEmoji: String
)

private fun getThemeConfig(exerciseId: String): MeditationThemeConfig {
    return when (exerciseId) {
        "resp_478" -> MeditationThemeConfig(
            bgGradient = listOf(Color(0xFF0F6E56), Color(0xFF0A4D3C), Color(0xFF063C31)),
            primaryColor = Color(0xFF0F6E56),
            lightColor = Color(0xFFF4FBF7),
            accentColor = Color(0xFF68D391),
            iconEmoji = "🫁"
        )
        "resp_caja" -> MeditationThemeConfig(
            bgGradient = listOf(Color(0xFF1E3A8A), Color(0xFF172554), Color(0xFF0F172A)),
            primaryColor = Color(0xFF2563EB),
            lightColor = Color(0xFFEFF6FF),
            accentColor = Color(0xFF60A5FA),
            iconEmoji = "📦"
        )
        "body_scan" -> MeditationThemeConfig(
            bgGradient = listOf(Color(0xFF5B21B6), Color(0xFF4C1D95), Color(0xFF2E1065)),
            primaryColor = Color(0xFF7C3AED),
            lightColor = Color(0xFFF5F3FF),
            accentColor = Color(0xFFA78BFA),
            iconEmoji = "🧘"
        )
        "gratitud" -> MeditationThemeConfig(
            bgGradient = listOf(Color(0xFF115E59), Color(0xFF134E4A), Color(0xFF0F2D2A)),
            primaryColor = Color(0xFF0D9488),
            lightColor = Color(0xFFF0FDFA),
            accentColor = Color(0xFF2DD4BF),
            iconEmoji = "💖"
        )
        "resp_diafragmatica" -> MeditationThemeConfig(
            bgGradient = listOf(Color(0xFFB45309), Color(0xFF92400E), Color(0xFF78350F)),
            primaryColor = Color(0xFFD97706),
            lightColor = Color(0xFFFFFBEB),
            accentColor = Color(0xFFFBBF24),
            iconEmoji = "🌅"
        )
        else -> MeditationThemeConfig(
            bgGradient = listOf(Color(0xFF0F6E56), Color(0xFF0A4D3C), Color(0xFF063C31)),
            primaryColor = Color(0xFF0F6E56),
            lightColor = Color(0xFFF4FBF7),
            accentColor = Color(0xFF68D391),
            iconEmoji = "🌿"
        )
    }
}

private fun getExerciseImageResId(exerciseId: String): Int {
    return when (exerciseId) {
        "resp_478" -> R.drawable.meditacion
        "resp_caja" -> R.drawable.respiracion_caja
        "body_scan" -> R.drawable.escaneo_corporal
        "gratitud" -> R.drawable.meditacion_gratitud
        "resp_diafragmatica" -> R.drawable.respiracion_diafragmatica
        else -> R.drawable.meditacion
    }
}

private fun getBreathingPhaseInfo(exerciseId: String, type: String, elapsedSeconds: Int): BreathingPhaseInfo {
    return when (type) {
        "respiracion" -> {
            when (exerciseId) {
                "resp_caja" -> {
                    val cycleSecond = elapsedSeconds % 16
                    when {
                        cycleSecond < 4 -> {
                            val scale = 0.6f + (cycleSecond / 4f) * 0.4f
                            BreathingPhaseInfo(
                                stepText = "Paso 1 de 4", stepIndicator = 1,
                                title = "Inhala", subtitle = "por 4 segundos",
                                secondsRemaining = 4 - cycleSecond, scale = scale,
                                currentPhaseText = "Inhala", subText = "Luego mantén 4s"
                            )
                        }
                        cycleSecond < 8 -> {
                            BreathingPhaseInfo(
                                stepText = "Paso 2 de 4", stepIndicator = 2,
                                title = "Mantén", subtitle = "por 4 segundos",
                                secondsRemaining = 8 - cycleSecond, scale = 1.0f,
                                currentPhaseText = "Mantén", subText = "Luego exhala 4s"
                            )
                        }
                        cycleSecond < 12 -> {
                            val progress = (cycleSecond - 8) / 4f
                            val scale = 1.0f - progress * 0.4f
                            BreathingPhaseInfo(
                                stepText = "Paso 3 de 4", stepIndicator = 3,
                                title = "Exhala", subtitle = "por 4 segundos",
                                secondsRemaining = 12 - cycleSecond, scale = scale,
                                currentPhaseText = "Exhala", subText = "Luego mantén sin aire 4s"
                            )
                        }
                        else -> {
                            BreathingPhaseInfo(
                                stepText = "Paso 4 de 4", stepIndicator = 4,
                                title = "Mantén sin aire", subtitle = "por 4 segundos",
                                secondsRemaining = 16 - cycleSecond, scale = 0.6f,
                                currentPhaseText = "Vacío", subText = "Luego inhala de nuevo"
                            )
                        }
                    }
                }
                "resp_diafragmatica" -> {
                    val cycleSecond = elapsedSeconds % 10
                    when {
                        cycleSecond < 4 -> {
                            val scale = 0.6f + (cycleSecond / 4f) * 0.4f
                            BreathingPhaseInfo(
                                stepText = "Paso 1 de 2", stepIndicator = 1,
                                title = "Inhala inflando el abdomen", subtitle = "por 4 segundos",
                                secondsRemaining = 4 - cycleSecond, scale = scale,
                                currentPhaseText = "Inhala", subText = "Luego exhala 6s"
                            )
                        }
                        else -> {
                            val progress = (cycleSecond - 4) / 6f
                            val scale = 1.0f - progress * 0.4f
                            BreathingPhaseInfo(
                                stepText = "Paso 2 de 2", stepIndicator = 2,
                                title = "Exhala lentamente", subtitle = "por 6 segundos",
                                secondsRemaining = 10 - cycleSecond, scale = scale,
                                currentPhaseText = "Exhala", subText = "Luego inhala de nuevo"
                            )
                        }
                    }
                }
                else -> {
                    val cycleSecond = elapsedSeconds % 19
                    when {
                        cycleSecond < 4 -> {
                            val scale = 0.6f + (cycleSecond / 4f) * 0.4f
                            BreathingPhaseInfo(
                                stepText = "Paso 1 de 3", stepIndicator = 1,
                                title = "Inhala por la nariz", subtitle = "durante 4 segundos",
                                secondsRemaining = 4 - cycleSecond, scale = scale,
                                currentPhaseText = "Inhala", subText = "Luego mantén 7s · Exhala 8s"
                            )
                        }
                        cycleSecond < 11 -> {
                            BreathingPhaseInfo(
                                stepText = "Paso 2 de 3", stepIndicator = 2,
                                title = "Mantén el aire", subtitle = "durante 7 segundos",
                                secondsRemaining = 11 - cycleSecond, scale = 1.0f,
                                currentPhaseText = "Mantén", subText = "Luego exhala 8s"
                            )
                        }
                        else -> {
                            val progress = (cycleSecond - 11) / 8f
                            val scale = 1.0f - progress * 0.4f
                            BreathingPhaseInfo(
                                stepText = "Paso 3 de 3", stepIndicator = 3,
                                title = "Exhala por la boca", subtitle = "durante 8 segundos",
                                secondsRemaining = 19 - cycleSecond, scale = scale,
                                currentPhaseText = "Exhala", subText = "Luego inhala de nuevo"
                            )
                        }
                    }
                }
            }
        }
        "mindfulness" -> {
            val messages = listOf(
                "Enfócate en tu respiración",
                "Observa tus pensamientos sin juzgar",
                "Siente el peso de tu cuerpo",
                "Conéctate con el momento presente"
            )
            val index = (elapsedSeconds / 10) % messages.size
            val cycleSecond = elapsedSeconds % 10
            val scale = 0.9f + sin(elapsedSeconds * 0.6f) * 0.1f
            BreathingPhaseInfo(
                stepText = "Paso ${index + 1} de 4",
                stepIndicator = index + 1,
                title = messages[index],
                subtitle = "Mantente en el presente",
                secondsRemaining = 10 - cycleSecond,
                scale = scale,
                currentPhaseText = "Concéntrate",
                subText = "Sigue respirando a tu propio ritmo"
            )
        }
        else -> { // "relajacion"
            val messages = listOf(
                "Suelta la tensión de tus hombros",
                "Siente cómo se relaja tu cuerpo",
                "Disfruta de este momento de paz",
                "Respira profundamente y exhala suave"
            )
            val index = (elapsedSeconds / 10) % messages.size
            val cycleSecond = elapsedSeconds % 10
            val scale = 0.9f + sin(elapsedSeconds * 0.6f) * 0.1f
            BreathingPhaseInfo(
                stepText = "Paso ${index + 1} de 4",
                stepIndicator = index + 1,
                title = messages[index],
                subtitle = "Suelta y relaja",
                secondsRemaining = 10 - cycleSecond,
                scale = scale,
                currentPhaseText = "Relajación",
                subText = "Libera todo el estrés acumulado"
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. MODULAR COMPOSABLES (BACKGROUND, BUBBLE, PANEL, BUTTONS)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MeditationBreathingBackground(
    themeConfig: MeditationThemeConfig,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "particles")
    
    // Wave animation offsets
    val waveOffset1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 24f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave-1"
    )
    val waveOffset2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -18f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 8500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "wave-2"
    )
    
    // Floating particles offset and alpha
    val particleDrift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -60f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 7000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "particle-drift"
    )
    val particleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.25f,
        targetValue = 0.85f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "particle-alpha"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(themeConfig.bgGradient))
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            
            // Draw floating light sparks
            val sparkPositions = listOf(
                androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.48f),
                androidx.compose.ui.geometry.Offset(w * 0.82f, h * 0.35f),
                androidx.compose.ui.geometry.Offset(w * 0.28f, h * 0.20f),
                androidx.compose.ui.geometry.Offset(w * 0.74f, h * 0.58f),
                androidx.compose.ui.geometry.Offset(w * 0.40f, h * 0.70f),
                androidx.compose.ui.geometry.Offset(w * 0.90f, h * 0.74f)
            )
            
            sparkPositions.forEachIndexed { idx, point ->
                val driftSpeed = if (idx % 2 == 0) 1.25f else 0.75f
                val verticalY = (point.y + particleDrift * driftSpeed) % h
                val correctedY = if (verticalY < 0) verticalY + h else verticalY
                
                drawCircle(
                    color = themeConfig.accentColor.copy(alpha = particleAlpha * 0.42f),
                    radius = (2.2f + (idx % 3) * 1.2f).dp.toPx(),
                    center = androidx.compose.ui.geometry.Offset(point.x, correctedY)
                )
            }
            
            // Waves at the base
            val wave1 = Path().apply {
                moveTo(0f, h * 0.72f + waveOffset1.dp.toPx())
                quadraticBezierTo(w * 0.3f, h * 0.66f + waveOffset1.dp.toPx(), w * 0.62f, h * 0.74f + waveOffset1.dp.toPx())
                quadraticBezierTo(w * 0.86f, h * 0.78f + waveOffset1.dp.toPx(), w, h * 0.69f + waveOffset1.dp.toPx())
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = wave1, color = themeConfig.accentColor.copy(alpha = 0.05f))
            
            val wave2 = Path().apply {
                moveTo(0f, h * 0.77f + waveOffset2.dp.toPx())
                quadraticBezierTo(w * 0.38f, h * 0.81f + waveOffset2.dp.toPx(), w * 0.72f, h * 0.73f + waveOffset2.dp.toPx())
                quadraticBezierTo(w * 0.88f, h * 0.70f + waveOffset2.dp.toPx(), w, h * 0.76f + waveOffset2.dp.toPx())
                lineTo(w, h)
                lineTo(0f, h)
                close()
            }
            drawPath(path = wave2, color = themeConfig.accentColor.copy(alpha = 0.03f))
        }
    }
}

@Composable
fun MeditationTopBar(
    exerciseTitle: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.relaxmind.app.ui.components.RelaxBackButton(
            onClick = onBackClick,
            role = com.relaxmind.app.ui.components.AppRole.PATIENT
        )
        
        // Centered Title
        Text(
            text = exerciseTitle,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = Color.White,
            textAlign = TextAlign.Center,
            modifier = Modifier.weight(1f)
        )
        
        // Dummy space to keep symmetry
        Spacer(modifier = Modifier.width(46.dp))
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. ILLUSTRATIVE PHASES LAYOUT (CUBES, HOURGLASS, METAPHOR FLOWS)
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun PhaseLungsIcon(tint: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val leftPath = Path().apply {
            moveTo(w * 0.44f, h * 0.3f)
            cubicTo(w * 0.44f, h * 0.1f, w * 0.1f, h * 0.1f, w * 0.1f, h * 0.5f)
            cubicTo(w * 0.1f, h * 0.75f, w * 0.25f, h * 0.9f, w * 0.4f, h * 0.85f)
            close()
        }
        drawPath(path = leftPath, color = tint)
        val rightPath = Path().apply {
            moveTo(w * 0.56f, h * 0.3f)
            cubicTo(w * 0.56f, h * 0.1f, w * 0.9f, h * 0.1f, w * 0.9f, h * 0.5f)
            cubicTo(w * 0.9f, h * 0.75f, w * 0.75f, h * 0.9f, w * 0.6f, h * 0.85f)
            close()
        }
        drawPath(path = rightPath, color = tint)
    }
}

@Composable
fun PhaseHourglassIcon(tint: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.25f, h * 0.15f)
            lineTo(w * 0.75f, h * 0.15f)
            lineTo(w * 0.75f, h * 0.25f)
            lineTo(w * 0.54f, h * 0.5f)
            lineTo(w * 0.75f, h * 0.75f)
            lineTo(w * 0.75f, h * 0.85f)
            lineTo(w * 0.25f, h * 0.85f)
            lineTo(w * 0.25f, h * 0.75f)
            lineTo(w * 0.46f, h * 0.5f)
            lineTo(w * 0.25f, h * 0.25f)
            close()
        }
        drawPath(path = path, color = tint, style = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round))
        drawPath(
            path = Path().apply {
                moveTo(w * 0.3f, h * 0.25f)
                lineTo(w * 0.7f, h * 0.25f)
                lineTo(w * 0.5f, h * 0.48f)
                close()
            },
            color = tint.copy(alpha = 0.5f)
        )
        drawPath(
            path = Path().apply {
                moveTo(w * 0.5f, h * 0.52f)
                lineTo(w * 0.7f, h * 0.75f)
                lineTo(w * 0.3f, h * 0.75f)
                close()
            },
            color = tint.copy(alpha = 0.5f)
        )
    }
}

@Composable
fun PhaseWindIcon(tint: Color) {
    Canvas(modifier = Modifier.size(22.dp)) {
        val w = size.width
        val h = size.height
        val line1 = Path().apply {
            moveTo(w * 0.15f, h * 0.35f)
            lineTo(w * 0.65f, h * 0.35f)
            quadraticBezierTo(w * 0.8f, h * 0.35f, w * 0.8f, h * 0.45f)
            quadraticBezierTo(w * 0.8f, h * 0.55f, w * 0.65f, h * 0.55f)
        }
        drawPath(path = line1, color = tint, style = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round))
        
        val line2 = Path().apply {
            moveTo(w * 0.1f, h * 0.6f)
            lineTo(w * 0.55f, h * 0.6f)
        }
        drawPath(path = line2, color = tint, style = Stroke(width = 1.8f.dp.toPx(), cap = StrokeCap.Round))
    }
}

@Composable
fun PhaseCard(
    title: String,
    durationText: String,
    icon: @Composable (Color) -> Unit,
    isActive: Boolean,
    themeConfig: MeditationThemeConfig,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) Color.White else Color.Black.copy(alpha = 0.25f)
    val borderColor = if (isActive) themeConfig.accentColor else Color.White.copy(alpha = 0.15f)
    val textColor = if (isActive) themeConfig.primaryColor else Color.White.copy(alpha = 0.8f)
    val subTextColor = if (isActive) themeConfig.primaryColor.copy(alpha = 0.7f) else Color.White.copy(alpha = 0.6f)
    
    val scale by animateFloatAsState(
        targetValue = if (isActive) 1.05f else 0.95f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "phase-card-scale"
    )

    Card(
        modifier = modifier
            .scale(scale)
            .width(84.dp)
            .shadow(
                elevation = if (isActive) 8.dp else 0.dp,
                shape = RoundedCornerShape(18.dp),
                ambientColor = themeConfig.primaryColor.copy(alpha = 0.2f),
                spotColor = themeConfig.primaryColor.copy(alpha = 0.2f)
            )
            .border(1.5.dp, borderColor, RoundedCornerShape(18.dp)),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            icon(textColor)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = textColor,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = durationText,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                color = subTextColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun InhaleArrowsAnimation(
    themeConfig: MeditationThemeConfig,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "inhale-arrows")
    val animOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arrow-x"
    )
    val animAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "arrow-alpha"
    )

    Canvas(modifier = modifier.width(50.dp).height(40.dp)) {
        val w = size.width
        val h = size.height
        val cy = h / 2
        
        val arrowPositions = listOf(0.1f, 0.4f, 0.7f)
        arrowPositions.forEach { pos ->
            val x = (w * pos + animOffset.dp.toPx()) % w
            val alphaVal = if (x > w * 0.8f) (w - x) / (w * 0.2f) else 1f
            
            drawLine(
                color = themeConfig.accentColor.copy(alpha = animAlpha * alphaVal),
                start = androidx.compose.ui.geometry.Offset(x, cy - 6.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(x + 8.dp.toPx(), cy),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = themeConfig.accentColor.copy(alpha = animAlpha * alphaVal),
                start = androidx.compose.ui.geometry.Offset(x, cy + 6.dp.toPx()),
                end = androidx.compose.ui.geometry.Offset(x + 8.dp.toPx(), cy),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
            drawLine(
                color = themeConfig.accentColor.copy(alpha = animAlpha * alphaVal),
                start = androidx.compose.ui.geometry.Offset(x - 6.dp.toPx(), cy),
                end = androidx.compose.ui.geometry.Offset(x + 8.dp.toPx(), cy),
                strokeWidth = 2.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@Composable
fun ExhaleWindAnimation(
    themeConfig: MeditationThemeConfig,
    modifier: Modifier = Modifier
) {
    val transition = rememberInfiniteTransition(label = "exhale-wind")
    val animOffset by transition.animateFloat(
        initialValue = 0f,
        targetValue = 30f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wind-x"
    )
    val animAlpha by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wind-alpha"
    )

    Canvas(modifier = modifier.width(50.dp).height(40.dp)) {
        val w = size.width
        val h = size.height
        val cy = h / 2
        
        val xPoints = listOf(0.1f, 0.4f, 0.7f)
        xPoints.forEach { pos ->
            val x = (w * pos + animOffset.dp.toPx()) % w
            val alphaVal = if (x > w * 0.8f) (w - x) / (w * 0.2f) else 1f
            
            val path = Path().apply {
                moveTo(x, cy - 3.dp.toPx())
                quadraticBezierTo(x + 6.dp.toPx(), cy - 8.dp.toPx(), x + 12.dp.toPx(), cy - 3.dp.toPx())
                quadraticBezierTo(x + 18.dp.toPx(), cy + 2.dp.toPx(), x + 24.dp.toPx(), cy - 3.dp.toPx())
            }
            drawPath(
                path = path,
                color = themeConfig.accentColor.copy(alpha = animAlpha * alphaVal),
                style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. PROGRESS BAR & ACTION BUTTONS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun BreathingProgressBar(
    progress: Float,
    themeConfig: MeditationThemeConfig,
    modifier: Modifier = Modifier
) {
    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(28.dp)
    ) {
        val w = size.width
        val h = size.height
        val trackHeight = 8.dp.toPx()
        val cy = h / 2
        val radius = 10.dp.toPx()
        
        drawRoundRect(
            color = themeConfig.primaryColor.copy(alpha = 0.15f),
            topLeft = androidx.compose.ui.geometry.Offset(0f, cy - trackHeight / 2),
            size = androidx.compose.ui.geometry.Size(w, trackHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
        )
        
        val progressWidth = w * progress.coerceIn(0f, 1f)
        if (progressWidth > 0f) {
            drawRoundRect(
                color = themeConfig.primaryColor,
                topLeft = androidx.compose.ui.geometry.Offset(0f, cy - trackHeight / 2),
                size = androidx.compose.ui.geometry.Size(progressWidth, trackHeight),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(trackHeight / 2, trackHeight / 2)
            )
        }
        
        val thumbX = progressWidth.coerceIn(radius, w - radius)
        drawCircle(
            color = themeConfig.accentColor.copy(alpha = 0.40f),
            radius = radius + 4.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(thumbX, cy)
        )
        drawCircle(
            color = Color.White,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(thumbX, cy)
        )
        drawCircle(
            color = themeConfig.primaryColor,
            radius = radius - 3.dp.toPx(),
            center = androidx.compose.ui.geometry.Offset(thumbX, cy)
        )
    }
}

@Composable
fun PlayPauseIcon(
    isPaused: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    if (isPaused) {
        Canvas(modifier = modifier) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
                moveTo(0f, 0f)
                lineTo(w, h / 2)
                lineTo(0f, h)
                close()
            }
            drawPath(path = path, color = tint)
        }
    } else {
        Row(
            modifier = modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(tint, RoundedCornerShape(1.5.dp))
            )
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(tint, RoundedCornerShape(1.5.dp))
            )
        }
    }
}

@Composable
fun CheckMarkIcon(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val path = Path().apply {
            moveTo(w * 0.15f, h * 0.55f)
            lineTo(w * 0.42f, h * 0.82f)
            lineTo(w * 0.85f, h * 0.20f)
        }
        drawPath(
            path = path,
            color = tint,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round, join = StrokeJoin.Round)
        )
    }
}

@Composable
fun MeditationActionButtons(
    isPaused: Boolean,
    themeConfig: MeditationThemeConfig,
    onPauseClick: () -> Unit,
    onCompleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .weight(1.0f)
                .height(56.dp)
                .shadow(
                    elevation = 0.dp,
                    shape = RoundedCornerShape(100)
                )
                .clip(RoundedCornerShape(100))
                .background(themeConfig.primaryColor.copy(alpha = 0.15f))
                .clickable(onClick = onPauseClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                PlayPauseIcon(
                    isPaused = isPaused,
                    tint = themeConfig.primaryColor,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = if (isPaused) "Reanudar" else "Pausar",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = themeConfig.primaryColor
                )
            }
        }
        
        Box(
            modifier = Modifier
                .weight(1.0f)
                .height(56.dp)
                .shadow(
                    elevation = 16.dp,
                    shape = RoundedCornerShape(100),
                    ambientColor = themeConfig.primaryColor,
                    spotColor = themeConfig.primaryColor
                )
                .clip(RoundedCornerShape(100))
                .background(themeConfig.primaryColor)
                .clickable(onClick = onCompleteClick),
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                CheckMarkIcon(
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Completar",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = Color.White
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. MAIN DETAIL SCREEN COMPOSABLE
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun MeditationDetailScreen(
    exerciseId: String,
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val exercise by viewModel.selectedExercise.collectAsState()
    val successCompletion by viewModel.meditationCompleteSuccess.collectAsState(initial = null)

    var elapsedSeconds by remember { mutableStateOf(viewModel.getExerciseProgress(exerciseId)) }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(exerciseId) {
        viewModel.loadMeditationExercise(exerciseId)
        viewModel.resetMeditationCompleteSuccess()
    }

    var showExitWarning by remember { mutableStateOf(false) }
    val onBackRequest = {
        if (elapsedSeconds > 0 && successCompletion == null && elapsedSeconds < (exercise?.durationMinutes ?: 1) * 60) {
            isPaused = true
            showExitWarning = true
        } else {
            onNavigateBack()
        }
    }
    
    BackHandler {
        onBackRequest()
    }

    val totalDurationMinutes = exercise?.durationMinutes ?: 5
    val totalSeconds = if (totalDurationMinutes == 1) 90 else totalDurationMinutes * 60

    // Core Timer Loop
    LaunchedEffect(isPaused, exercise) {
        if (exercise != null) {
            while (elapsedSeconds < totalSeconds) {
                delay(1000L)
                if (!isPaused) {
                    elapsedSeconds++
                }
            }
        }
    }

    val remainingSeconds = (totalSeconds - elapsedSeconds).coerceAtLeast(0)
    val themeConfig = getThemeConfig(exerciseId)
    val phaseInfo = getBreathingPhaseInfo(exercise?.id ?: "", exercise?.type ?: "respiracion", elapsedSeconds)

    // Apply Lexend fonts globally to detail screen
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = LexendTypography
    ) {
        if (successCompletion != null) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = {
                    viewModel.resetMeditationCompleteSuccess()
                    onNavigateBack()
                }
            ) {
                val adviceTexts = listOf(
                    "Recuerda que tomarte unos minutos al día para respirar reduce tus niveles de cortisol.",
                    "La constancia es clave. Cada pequeño paso cuenta para tu bienestar mental.",
                    "Respirar profundo oxigena tu cerebro y mejora tu enfoque.",
                    "Estos breves instantes de calma te preparan para afrontar mejor el resto del día."
                )
                val randomAdvice = remember { adviceTexts.random() }

                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .shadow(32.dp, RoundedCornerShape(32.dp), ambientColor = themeConfig.primaryColor)
                        .background(Color.White, RoundedCornerShape(32.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        val completedIconRes = when (exerciseId) {
                            "resp_caja" -> com.relaxmind.app.R.drawable.respiracion_caja_icono
                            "body_scan" -> com.relaxmind.app.R.drawable.escaneo_corporal_icono
                            "gratitud" -> com.relaxmind.app.R.drawable.meditacion_gratitud_icono
                            "resp_diafragmatica" -> com.relaxmind.app.R.drawable.respiracion_diafragmatica_icono
                            else -> com.relaxmind.app.R.drawable.meditacion_icono
                        }
                        
                        Image(
                            painter = painterResource(id = completedIconRes),
                            contentDescription = "Icono completado",
                            modifier = Modifier.size(80.dp)
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "¡Ejercicio completado!",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = themeConfig.primaryColor,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = randomAdvice,
                            fontFamily = LexendFontFamily,
                            fontSize = 15.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .shadow(16.dp, RoundedCornerShape(100), spotColor = themeConfig.primaryColor)
                                .clip(RoundedCornerShape(100))
                                .background(themeConfig.primaryColor)
                                .clickable {
                                    viewModel.resetMeditationCompleteSuccess()
                                    onNavigateBack()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ir al inicio",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
        
        if (showExitWarning) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showExitWarning = false }
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.95f)
                        .shadow(24.dp, RoundedCornerShape(32.dp))
                        .background(Color.White, RoundedCornerShape(32.dp))
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = "¿Deseas salir?",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 22.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = "Si sales ahora no completarás el ejercicio, pero tu progreso se guardará y podrás reanudarlo cuando vuelvas.",
                            fontFamily = LexendFontFamily,
                            fontSize = 15.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(32.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Button(
                                onClick = { showExitWarning = false },
                                modifier = Modifier.weight(1f).height(56.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PatientGreenLight.copy(alpha = 0.15f)),
                                shape = RoundedCornerShape(100),
                                elevation = ButtonDefaults.buttonElevation(0.dp),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Cancelar", color = PatientGreen, fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }
                            Button(
                                onClick = { 
                                    viewModel.saveExerciseProgress(exerciseId, elapsedSeconds)
                                    showExitWarning = false
                                    onNavigateBack() 
                                },
                                modifier = Modifier.weight(1f).height(56.dp).shadow(12.dp, RoundedCornerShape(100), spotColor = PatientGreen),
                                colors = ButtonDefaults.buttonColors(containerColor = PatientGreen),
                                shape = RoundedCornerShape(100),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Text("Salir", color = Color.White, fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, fontSize = 14.sp, maxLines = 1)
                            }
                        }
                    }
                }
            }
        }

        Scaffold { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                if (exercise == null || isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Brush.verticalGradient(themeConfig.bgGradient)),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                } else {
                    val currentExercise = exercise!!
                    
                    // Dynamic particle wave background
                    MeditationBreathingBackground(
                        themeConfig = themeConfig
                    )

                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 1. Top navigation bar
                        MeditationTopBar(
                            exerciseTitle = currentExercise.title,
                            onBackClick = onBackRequest
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Use stepIndicator directly for the active phase
                        val activePhase = phaseInfo.stepIndicator
                        
                        val isRespiracion = currentExercise.type == "respiracion"
                        
                        // Dynamic card info
                        val hasHold = currentExercise.id != "resp_diafragmatica"
                        val holdDur = if (currentExercise.id == "resp_caja") "4 seg" else "7 seg"
                        val exhaleDur = when (currentExercise.id) {
                            "resp_caja" -> "4 seg"
                            "resp_diafragmatica" -> "6 seg"
                            else -> "8 seg"
                        }
                        
                        // Active states
                        val isInhaleActive = activePhase == 1
                        val isHoldActive = activePhase == 2 || (currentExercise.id == "resp_caja" && activePhase == 4)
                        val isExhaleActive = activePhase == 3 || (currentExercise.id == "resp_diafragmatica" && activePhase == 2)

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(horizontal = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (isRespiracion) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceAround
                                ) {
                                    // Left Column: Inhale Card
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        PhaseCard(
                                            title = "Inhala",
                                            durationText = "4 seg",
                                            icon = { PhaseLungsIcon(it) },
                                            isActive = isInhaleActive,
                                            themeConfig = themeConfig
                                        )
                                        
                                        Spacer(modifier = Modifier.height(12.dp))
                                        
                                        AnimatedVisibility(
                                            visible = isInhaleActive,
                                            enter = fadeIn(animationSpec = tween(300))
                                        ) {
                                            InhaleArrowsAnimation(themeConfig = themeConfig)
                                        }
                                    }

                                    // Center Box: Character with pulsating ring
                                    Box(
                                        modifier = Modifier.size(200.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val animatedScale by animateFloatAsState(
                                            targetValue = phaseInfo.scale,
                                            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                                            label = "breath-circle-scale"
                                        )
                                        
                                        // Pulsating halo ring behind character
                                        Box(
                                            modifier = Modifier
                                                .size(190.dp * animatedScale)
                                                .clip(CircleShape)
                                                .background(themeConfig.accentColor.copy(alpha = 0.08f))
                                                .border(2.dp, themeConfig.accentColor.copy(alpha = 0.20f), CircleShape)
                                        )
                                        
                                        Image(
                                            painter = painterResource(id = getExerciseImageResId(currentExercise.id)),
                                            contentDescription = "Ilustración de Ejercicio",
                                            modifier = Modifier
                                                .size(185.dp * animatedScale),
                                            contentScale = ContentScale.Fit
                                        )
                                    }

                                    // Right Column: Hold & Exhale Cards
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(10.dp)
                                    ) {
                                        if (hasHold) {
                                            PhaseCard(
                                                title = if (currentExercise.id == "resp_caja" && activePhase == 4) "Vacío" else "Mantén",
                                                durationText = holdDur,
                                                icon = { PhaseHourglassIcon(it) },
                                                isActive = isHoldActive,
                                                themeConfig = themeConfig
                                            )
                                        }
                                        
                                        PhaseCard(
                                            title = "Exhala",
                                            durationText = exhaleDur,
                                            icon = { PhaseWindIcon(it) },
                                            isActive = isExhaleActive,
                                            themeConfig = themeConfig
                                        )
                                        
                                        AnimatedVisibility(
                                            visible = isExhaleActive,
                                            enter = fadeIn(animationSpec = tween(300))
                                        ) {
                                            ExhaleWindAnimation(themeConfig = themeConfig)
                                        }
                                    }
                                }
                            } else {
                                // For general relaxation/mindfulness (simple visual style)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Box(
                                        modifier = Modifier.size(220.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val animatedScale by animateFloatAsState(
                                            targetValue = phaseInfo.scale,
                                            animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                                            label = "breath-circle-scale-simple"
                                        )
                                        
                                        Box(
                                            modifier = Modifier
                                                .size(210.dp * animatedScale)
                                                .clip(CircleShape)
                                                .background(themeConfig.accentColor.copy(alpha = 0.08f))
                                                .border(2.dp, themeConfig.accentColor.copy(alpha = 0.20f), CircleShape)
                                        )
                                        
                                        Image(
                                            painter = painterResource(id = getExerciseImageResId(currentExercise.id)),
                                            contentDescription = "Ilustración de Ejercicio",
                                            modifier = Modifier
                                                .size(200.dp * animatedScale),
                                            contentScale = ContentScale.Fit
                                        )
                                    }
                                    
                                    Spacer(modifier = Modifier.height(20.dp))
                                    
                                    // Glassmorphic prompt card
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth(0.85f)
                                            .border(1.5.dp, Color.White.copy(alpha = 0.25f), RoundedCornerShape(20.dp)),
                                        shape = RoundedCornerShape(20.dp),
                                        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.12f))
                                    ) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Text(
                                                text = phaseInfo.title,
                                                fontFamily = LexendFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 18.sp,
                                                color = Color.White,
                                                textAlign = TextAlign.Center
                                            )
                                            if (phaseInfo.subtitle.isNotEmpty()) {
                                                Spacer(modifier = Modifier.height(4.dp))
                                                Text(
                                                    text = phaseInfo.subtitle,
                                                    fontFamily = LexendFontFamily,
                                                    fontWeight = FontWeight.Medium,
                                                    fontSize = 13.sp,
                                                    color = themeConfig.accentColor,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // 3. Neumorphic bottom sheet panel
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.TopCenter
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .shadow(
                                        elevation = 16.dp,
                                        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp),
                                        ambientColor = themeConfig.primaryColor.copy(alpha = 0.15f),
                                        spotColor = themeConfig.primaryColor.copy(alpha = 0.15f)
                                    )
                                    .background(
                                        color = Color.White,
                                        shape = RoundedCornerShape(topStart = 38.dp, topEnd = 38.dp)
                                    )
                                    .padding(horizontal = 24.dp, vertical = 24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Spacer(modifier = Modifier.height(14.dp))
                                
                                // Large Current Phase count (e.g. Inhala... 4)
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "${phaseInfo.currentPhaseText}...",
                                        fontFamily = LexendFontFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 32.sp,
                                        color = themeConfig.primaryColor,
                                        textAlign = TextAlign.Center
                                    )
                                    Text(
                                        text = "${phaseInfo.secondsRemaining}",
                                        fontFamily = LexendFontFamily,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 46.sp,
                                        color = themeConfig.primaryColor,
                                        textAlign = TextAlign.Center
                                    )
                                }

                                Spacer(modifier = Modifier.height(16.dp))

                                // Themed glowing progress bar
                                val progressFraction = elapsedSeconds.toFloat() / totalSeconds.toFloat()
                                BreathingProgressBar(
                                    progress = progressFraction,
                                    themeConfig = themeConfig
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                // Time remaining label
                                val minutesLeft = remainingSeconds / 60
                                val secondsLeft = remainingSeconds % 60
                                Text(
                                    text = String.format("%d:%02d restantes", minutesLeft, secondsLeft),
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 18.sp,
                                    color = TextSecondary,
                                    textAlign = TextAlign.Center
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                // Action buttons (Pausar / Completar)
                                MeditationActionButtons(
                                    isPaused = isPaused,
                                    themeConfig = themeConfig,
                                    onPauseClick = { isPaused = !isPaused },
                                    onCompleteClick = {
                                        viewModel.completeMeditation(currentExercise.id)
                                    }
                                )
                            }

                            // Protruding circular float themed icon
                            Box(
                                modifier = Modifier
                                    .offset(y = (-32).dp)
                                    .size(64.dp)
                                    .shadow(
                                        elevation = 6.dp,
                                        shape = CircleShape,
                                        ambientColor = themeConfig.primaryColor.copy(alpha = 0.2f),
                                        spotColor = themeConfig.primaryColor.copy(alpha = 0.2f)
                                    )
                                    .background(themeConfig.lightColor, CircleShape)
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                // Draw specific phase-related protruding badge
                                when (currentExercise.id) {
                                    "resp_478" -> PhaseLungsIcon(tint = themeConfig.primaryColor)
                                    "resp_caja" -> BoxBreathingIcon(modifier = Modifier.size(46.dp))
                                    "body_scan" -> BodyScanIcon(modifier = Modifier.size(46.dp))
                                    "gratitud" -> GratitudeIcon(modifier = Modifier.size(46.dp))
                                    "resp_diafragmatica" -> DiaphragmaticIcon(modifier = Modifier.size(46.dp))
                                    else -> PhaseLungsIcon(tint = themeConfig.primaryColor)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
