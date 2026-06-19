package com.relaxmind.app.features.patient

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.SOSCoral
import kotlinx.coroutines.delay
import java.time.LocalTime
import kotlin.math.sin

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

private fun getBreathingPhaseInfo(type: String, elapsedSeconds: Int): BreathingPhaseInfo {
    return when (type) {
        "respiracion" -> {
            val cycleSecond = elapsedSeconds % 19
            when {
                cycleSecond < 4 -> {
                    val scale = 0.6f + (cycleSecond / 4f) * 0.4f
                    BreathingPhaseInfo(
                        stepText = "Paso 1 de 3",
                        stepIndicator = 1,
                        title = "Inhala por la nariz",
                        subtitle = "durante 4 segundos",
                        secondsRemaining = 4 - cycleSecond,
                        scale = scale,
                        currentPhaseText = "Inhala... ${4 - cycleSecond}s",
                        subText = "Luego mantén 7s · Exhala 8s"
                    )
                }
                cycleSecond < 11 -> {
                    BreathingPhaseInfo(
                        stepText = "Paso 2 de 3",
                        stepIndicator = 2,
                        title = "Mantén el aire",
                        subtitle = "durante 7 segundos",
                        secondsRemaining = 11 - cycleSecond,
                        scale = 1.0f,
                        currentPhaseText = "Aguanta... ${11 - cycleSecond}s",
                        subText = "Luego exhala 8s"
                    )
                }
                else -> {
                    val progress = (cycleSecond - 11) / 8f
                    val scale = 1.0f - progress * 0.4f
                    BreathingPhaseInfo(
                        stepText = "Paso 3 de 3",
                        stepIndicator = 3,
                        title = "Exhala por la boca",
                        subtitle = "durante 8 segundos",
                        secondsRemaining = 19 - cycleSecond,
                        scale = scale,
                        currentPhaseText = "Exhala... ${19 - cycleSecond}s",
                        subText = "Luego inhala de nuevo"
                    )
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
                currentPhaseText = "Mente tranquila",
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
                currentPhaseText = "Cuerpo relajado",
                subText = "Libera todo el estrés acumulado"
            )
        }
    }
}

@Composable
fun MeditationDetailScreen(
    exerciseId: String,
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val exercise by viewModel.selectedExercise.collectAsState()
    val successCompletion by viewModel.meditationCompleteSuccess.collectAsState(initial = null)

    var elapsedSeconds by remember { mutableStateOf(0) }
    var isPaused by remember { mutableStateOf(false) }

    LaunchedEffect(exerciseId) {
        viewModel.loadMeditationExercise(exerciseId)
        viewModel.resetMeditationCompleteSuccess()
    }

    val totalDurationMinutes = exercise?.durationMinutes ?: 5
    val totalSeconds = totalDurationMinutes * 60

    // Timer Loop
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

    val isFinished = elapsedSeconds >= totalSeconds
    val remainingSeconds = totalSeconds - elapsedSeconds

    val phaseInfo = getBreathingPhaseInfo(exercise?.type ?: "respiracion", elapsedSeconds)

    // Celebration Modal
    if (successCompletion != null) {
        AlertDialog(
            onDismissRequest = {
                viewModel.resetMeditationCompleteSuccess()
                onNavigateBack()
            },
            title = {
                Text(
                    text = "🎉 ¡Ejercicio completado!",
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            text = {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(PatientGreenLight),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("🧘", fontSize = 40.sp)
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Has completado con éxito este ejercicio de meditación.",
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "+${successCompletion?.second} Calm XP",
                        color = PatientGreen,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            },
            confirmButton = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    RelaxButton(
                        text = "Volver",
                        onClick = {
                            viewModel.resetMeditationCompleteSuccess()
                            onNavigateBack()
                        },
                        variant = ButtonVariant.PRIMARY,
                        role = AppRole.PATIENT
                    )
                }
            },
            shape = RoundedCornerShape(24.dp),
            containerColor = Color.White
        )
    }

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF04261D), // Dark pine green
                            Color(0xFF0F4D3C), // Forest green
                            Color(0xFF1E6C58)  // Calm lake teal
                        )
                    )
                )
                .padding(paddingValues)
        ) {
            if (exercise == null || isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                val currentExercise = exercise!!

                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Custom Header TopBar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onNavigateBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Text(
                            text = currentExercise.title,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        IconButton(onClick = { /* Info action */ }) {
                            Icon(
                                imageVector = Icons.Filled.Info,
                                contentDescription = "Info",
                                tint = Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Step indicator
                    Text(
                        text = phaseInfo.stepText,
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Dot indicators
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val totalStepsCount = if (currentExercise.type == "respiracion") 3 else 4
                        for (i in 1..totalStepsCount) {
                            val isSelected = phaseInfo.stepIndicator == i
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected) Color(0xFF68D391) else Color.White.copy(alpha = 0.3f)
                                    )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(28.dp))

                    // Phase Instruction Text (Animated)
                    AnimatedContent(
                        targetState = phaseInfo.title,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(300)) togetherWith fadeOut(animationSpec = tween(300))
                        },
                        label = "meditation-phase-instruction"
                    ) { titleText ->
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = titleText,
                                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 22.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.padding(horizontal = 24.dp)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = phaseInfo.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color(0xFF68D391), // Light green
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(40.dp))

                    // Visual breathing indicator (synchronized scaling circle)
                    val animatedScale by animateFloatAsState(targetValue = phaseInfo.scale, label = "breath-scale")
                    Box(
                        modifier = Modifier.size(280.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        // Outer background aura ring
                        Box(
                            modifier = Modifier
                                .size(240.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.15f), CircleShape)
                        )

                        // Pulsating Inner breathing circle
                        Box(
                            modifier = Modifier
                                .size(180.dp * animatedScale)
                                .clip(CircleShape)
                                .background(Color(0xFF68D391).copy(alpha = 0.12f))
                                .border(2.dp, Color(0xFF68D391).copy(alpha = 0.5f), CircleShape)
                        )

                        // Center countdown display
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = phaseInfo.secondsRemaining.toString(),
                                style = MaterialTheme.typography.headlineLarge.copy(fontSize = 52.sp),
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                text = "SEGUNDOS",
                                style = MaterialTheme.typography.labelSmall,
                                color = Color.White.copy(alpha = 0.6f),
                                letterSpacing = 1.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // White bottom panel
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                            .background(Color.White)
                            .padding(24.dp)
                    ) {
                        // Current Phase status row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(Color(0xFFE8F5F0)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("🫁", fontSize = 18.sp)
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = phaseInfo.currentPhaseText,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = phaseInfo.subText,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color.Gray
                                    )
                                }
                            }
                            
                            // Category Icon
                            Text(
                                text = when (currentExercise.type) {
                                    "respiracion" -> "🫁"
                                    "mindfulness" -> "🧘"
                                    else -> "🌿"
                                },
                                fontSize = 24.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        // Linear progress showing exercise time
                        val progressFraction = elapsedSeconds.toFloat() / totalSeconds.toFloat()
                        LinearProgressIndicator(
                            progress = { progressFraction.coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PatientGreen,
                            trackColor = Color(0xFFEDF2F7)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        // Time remaining
                        val minutesLeft = remainingSeconds / 60
                        val secondsLeft = remainingSeconds % 60
                        Text(
                            text = String.format("%d:%02d restantes", minutesLeft, secondsLeft),
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            modifier = Modifier.fillMaxWidth(),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        // Buttons row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!isFinished) {
                                // Pausar/Reanudar
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .border(1.dp, Color.Gray, RoundedCornerShape(24.dp))
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable { isPaused = !isPaused }
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.Center
                                    ) {
                                        Text(
                                            text = if (isPaused) "Reanudar" else "Pausar",
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.Bold,
                                            color = Color.DarkGray
                                        )
                                    }
                                }

                                // Terminar antes (small outline button)
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(48.dp)
                                        .border(1.dp, PatientGreen, RoundedCornerShape(24.dp))
                                        .clip(RoundedCornerShape(24.dp))
                                        .clickable {
                                            // Instantly jump to finish
                                            elapsedSeconds = totalSeconds
                                        }
                                        .background(Color.White),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Terminar antes",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = PatientGreen
                                    )
                                }
                            } else {
                                // Completar (full width primary button)
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .clip(RoundedCornerShape(24.dp))
                                        .background(PatientGreen)
                                        .clickable {
                                            viewModel.completeMeditation(currentExercise.id)
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Completar",
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = Color.White
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Advice Card
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF7FAFC))
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFE2E8F0)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("🌿", fontSize = 16.sp)
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Consejo",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Respira profundo y mantén un ritmo calmado.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color.Gray
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
