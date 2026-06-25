package com.relaxmind.app.features.common

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.rememberRelaxToastState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.BorderSoft
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.SOSCoral
import com.relaxmind.app.ui.themes.SoftMint
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import kotlin.math.roundToInt

private enum class StepType {
    EMOTIONAL,
    SLEEP,
    ENERGY,
    STRESS,
    HABITS,
    BINARY,
    NOTES
}

@Composable
fun CheckInScreen(
    isInitialTest: Boolean,
    viewModel: CheckInViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val emotionalState by viewModel.emotionalState.collectAsState()
    val sleep by viewModel.sleep.collectAsState()
    val energy by viewModel.energy.collectAsState()
    val stress by viewModel.stress.collectAsState()
    val frequencyAnswers by viewModel.frequencyAnswers.collectAsState()
    val binaryAnswers by viewModel.binaryAnswers.collectAsState()
    val notes by viewModel.notes.collectAsState()

    val toastState = rememberRelaxToastState()

    // Resolve steps list dynamically
    val steps = remember(isInitialTest) {
        if (isInitialTest) {
            listOf(
                StepType.EMOTIONAL,
                StepType.ENERGY,
                StepType.STRESS,
                StepType.HABITS,
                StepType.BINARY,
                StepType.NOTES
            )
        } else {
            listOf(
                StepType.EMOTIONAL,
                StepType.SLEEP,
                StepType.ENERGY,
                StepType.STRESS
            )
        }
    }

    var currentStepIndex by remember { mutableStateOf(0) }
    val currentStep = steps[currentStepIndex]
    val totalSteps = steps.size
    val progress = (currentStepIndex + 1).toFloat() / totalSteps.toFloat()

    // Handle VM errors
    LaunchedEffect(uiState) {
        if (uiState is CheckInUiState.Error) {
            toastState.showError((uiState as CheckInUiState.Error).message)
        }
    }

    // Reset ViewModel state when screen is opened
    LaunchedEffect(Unit) {
        viewModel.clearState()
    }

    // If final success, show animated result screen
    if (uiState is CheckInUiState.Success) {
        val successState = uiState as CheckInUiState.Success
        AnimatedCheckInResultView(
            score = successState.score,
            category = successState.category,
            onDismiss = onFinished
        )
        return
    }

    Scaffold(
        topBar = {
            RelaxTopBar(
                title = "RelaxMind",
                onBackClick = onNavigateBack,
                actions = {
                    if (isInitialTest && currentStepIndex < totalSteps - 1) {
                        TextButton(onClick = { viewModel.submitCheckIn(isInitialTest = true, isSkipped = true) }) {
                            Text(
                                text = "Omitir test",
                                style = MaterialTheme.typography.labelLarge,
                                color = PatientGreen
                            )
                        }
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SoftGradientBackground(animateBlobs = true)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(8.dp))
                if (isInitialTest) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .background(
                                color = PatientGreen.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Paso 2 de 2",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PatientGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                }
                // Progress indicator
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp)),
                    color = PatientGreen,
                    trackColor = PatientGreen.copy(alpha = 0.12f)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = if (isInitialTest) {
                        "Pregunta ${currentStepIndex + 1} de $totalSteps"
                    } else {
                        "Pregunta ${currentStepIndex + 1} de $totalSteps"
                    },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = TextSecondary,
                    modifier = Modifier.align(Alignment.Start)
                )

                // Animated step content transitions
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    AnimatedContent(
                        targetState = currentStep,
                        transitionSpec = {
                            if (steps.indexOf(targetState) > steps.indexOf(initialState)) {
                                (fadeIn() + androidx.compose.animation.slideInHorizontally { width -> width })
                                    .togetherWith(fadeOut() + androidx.compose.animation.slideOutHorizontally { width -> -width })
                            } else {
                                (fadeIn() + androidx.compose.animation.slideInHorizontally { width -> -width })
                                    .togetherWith(fadeOut() + androidx.compose.animation.slideOutHorizontally { width -> width })
                            }
                        },
                        label = "checkin-step-transition"
                    ) { step ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState()),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Spacer(modifier = Modifier.height(20.dp))
                            when (step) {
                                StepType.EMOTIONAL -> {
                                    if (isInitialTest) {
                                        EmotionalStepView(
                                            selected = emotionalState,
                                            onSelect = { viewModel.selectEmotionalState(it) }
                                        )
                                    } else {
                                        DailyEmotionalStepView(
                                            selected = emotionalState,
                                            onSelect = { viewModel.selectEmotionalState(it) }
                                        )
                                    }
                                }
                                StepType.SLEEP -> SleepStepView(
                                    selected = sleep,
                                    onSelect = { viewModel.selectSleep(it) }
                                )
                                StepType.ENERGY -> EnergyStepView(
                                    value = energy,
                                    onValueChange = { viewModel.setEnergy(it) }
                                )
                                StepType.STRESS -> StressStepView(
                                    value = stress,
                                    onValueChange = { viewModel.setStress(it) }
                                )
                                StepType.HABITS -> HabitsStepView(
                                    answers = frequencyAnswers,
                                    onAnswerSelected = { index, value ->
                                        viewModel.setFrequencyAnswer(index, value)
                                    }
                                )
                                StepType.BINARY -> BinaryStepView(
                                    answers = binaryAnswers,
                                    onAnswerSelected = { index, value ->
                                        viewModel.setBinaryAnswer(index, value)
                                    }
                                )
                                StepType.NOTES -> NotesStepView(
                                    notes = notes,
                                    onNotesChange = { viewModel.setNotes(it) }
                                )
                            }
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }
                }

                // Bottom actions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back button
                    if (currentStepIndex > 0) {
                        TextButton(onClick = { currentStepIndex-- }) {
                            Text(
                                text = "Anterior",
                                style = MaterialTheme.typography.labelLarge,
                                color = PatientGreen
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.width(80.dp)) // Placeholder
                    }

                    // Next / Finish button
                    val isNextEnabled = when (currentStep) {
                        StepType.EMOTIONAL -> emotionalState != null
                        StepType.SLEEP -> sleep != null
                        StepType.BINARY -> binaryAnswers.all { it == 0 || it == 1 }
                        else -> true
                    }

                    val isLastStep = currentStepIndex == totalSteps - 1
                    RelaxButton(
                        text = if (isLastStep) "Finalizar" else "Siguiente",
                        onClick = {
                            if (isLastStep) {
                                viewModel.submitCheckIn(isInitialTest)
                            } else {
                                currentStepIndex++
                            }
                        },
                        enabled = isNextEnabled,
                        variant = ButtonVariant.PRIMARY,
                        role = AppRole.PATIENT
                    )
                }
            }

            if (uiState is CheckInUiState.Loading) {
                FullScreenLoadingOverlay()
            }

            RelaxToastHost(state = toastState)
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 1 — Emotional State
// ---------------------------------------------------------------------------
@Composable
private fun CheckInStepTitle(
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 25.sp,
            lineHeight = 31.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            lineHeight = 20.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun EmotionalStepView(
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        MoodCardOption(1, "Muy mal", "Necesito apoyo hoy"),
        MoodCardOption(2, "Mal", "Ha sido un dia pesado"),
        MoodCardOption(3, "Bien", "Estoy estable"),
        MoodCardOption(4, "Muy bien", "Me siento con calma"),
        MoodCardOption(5, "Excelente", "Me siento pleno")
    )
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        CheckInStepTitle(
            title = "¿Cómo te has sentido últimamente?",
            subtitle = "Elige la opción que más se parezca a tu estado emocional."
        )
        Spacer(modifier = Modifier.height(20.dp))
        options.forEach { option ->
            MoodSelectionCard(
                option = option,
                isSelected = selected == option.value,
                onClick = { onSelect(option.value) }
            )
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 2 — Sleep Quality
// ---------------------------------------------------------------------------
@Composable
private fun SleepStepView(
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        MoodCardOption(1, "Pésimo", "Casi no pude descansar"),
        MoodCardOption(2, "Mal", "Dormí poco o interrumpido"),
        MoodCardOption(3, "Regular", "Descansé lo justo"),
        MoodCardOption(4, "Bien", "Dormí de forma reparadora"),
        MoodCardOption(5, "Excelente", "Desperté con energía")
    )
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        CheckInStepTitle(
            title = "¿Cómo dormiste anoche?",
            subtitle = "Tu descanso también ayuda a entender tu bienestar de hoy."
        )
        Spacer(modifier = Modifier.height(20.dp))
        options.forEach { option ->
            MoodSelectionCard(
                option = option,
                isSelected = selected == option.value,
                onClick = { onSelect(option.value) }
            )
        }
    }
}

@Composable
private fun DailyEmotionalStepView(
    selected: Int?,
    onSelect: (Int) -> Unit
) {
    val options = listOf(
        MoodCardOption(1, "Muy mal", "Necesito apoyo hoy"),
        MoodCardOption(2, "Mal", "Ha sido un dia pesado"),
        MoodCardOption(3, "Bien", "Estoy estable"),
        MoodCardOption(4, "Muy bien", "Me siento con calma"),
        MoodCardOption(5, "Excelente", "Me siento pleno")
    )
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        CheckInStepTitle(
            title = "Como te sientes hoy?",
            subtitle = "Una respuesta rapida nos ayuda a cuidar tu bienestar diario."
        )
        Spacer(modifier = Modifier.height(20.dp))
        options.forEach { option ->
            MoodSelectionCard(
                option = option,
                isSelected = selected == option.value,
                onClick = { onSelect(option.value) }
            )
        }
    }
}

private data class MoodCardOption(val value: Int, val text: String, val helper: String)

@Composable
private fun MoodSelectionCard(
    option: MoodCardOption,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .shadow(
                elevation = if (isSelected) 10.dp else 4.dp,
                shape = RoundedCornerShape(22.dp),
                ambientColor = PatientGreen.copy(alpha = if (isSelected) 0.20f else 0.08f),
                spotColor = PatientGreen.copy(alpha = if (isSelected) 0.16f else 0.06f)
            )
            .clickable(onClick = onClick)
            .border(
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) PatientGreen else BorderSoft
                ),
                shape = RoundedCornerShape(22.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) SoftMint else Color.White
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) PatientGreen else PatientGreen.copy(alpha = 0.10f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = option.value.toString(),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isSelected) Color.White else PatientGreen
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = option.text,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = if (isSelected) PatientGreen else TextPrimary
                    )
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = option.helper,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            if (isSelected) {
                Icon(
                    imageVector = RelaxIcons.Check,
                    contentDescription = "Seleccionado",
                    tint = PatientGreen,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 3 — Energy level
// ---------------------------------------------------------------------------
@Composable
private fun EnergyStepView(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var lastHapticValue by remember { mutableStateOf(value) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        CheckInStepTitle(
            title = "¿Cuánta energía sientes hoy?",
            subtitle = "Desliza para indicar tu nivel del 1 al 10."
        )
        Spacer(modifier = Modifier.height(28.dp))

        // Dynamic Battery Indicator
        EnergyBatteryIndicator(level = value)

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = value.toString(),
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 72.sp,
            color = PatientGreen
        )
        Spacer(modifier = Modifier.height(16.dp))

        val gradientColors = listOf(
            SOSCoral,
            Color(0xFFED8936),
            Color(0xFFECC94B),
            PatientGreenLight,
            PatientGreen
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(colors = gradientColors))
            )
            Slider(
                value = value.toFloat(),
                onValueChange = {
                    val roundedValue = it.roundToInt()
                    if (roundedValue != lastHapticValue) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        lastHapticValue = roundedValue
                    }
                    onValueChange(roundedValue)
                },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = PatientGreen,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1\nMuy poca", fontFamily = LexendFontFamily, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
            Text("10\nMucha", fontFamily = LexendFontFamily, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun EnergyBatteryIndicator(level: Int) {
    // Battery visual representation
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(100.dp)
            .height(200.dp)
            .shadow(10.dp, RoundedCornerShape(26.dp), ambientColor = PatientGreen.copy(alpha = 0.12f), spotColor = PatientGreen.copy(alpha = 0.10f))
            .background(Color.White, RoundedCornerShape(26.dp))
            .border(1.dp, BorderSoft, RoundedCornerShape(26.dp))
            .padding(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Bottom
        ) {
            val filledPercentage = level / 10f
            val batteryColor = when {
                level <= 3 -> SOSCoral
                level <= 6 -> Color(0xFFECC94B)
                else -> PatientGreen
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(filledPercentage)
                    .clip(RoundedCornerShape(6.dp))
                    .background(batteryColor)
            )
            if (filledPercentage < 1f) {
                Spacer(modifier = Modifier.weight(1f - filledPercentage))
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 4 — Stress Level
// ---------------------------------------------------------------------------
@Composable
private fun StressStepView(
    value: Int,
    onValueChange: (Int) -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var lastHapticValue by remember { mutableStateOf(value) }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
        CheckInStepTitle(
            title = "¿Cuánto estrés sientes?",
            subtitle = "Marca la intensidad de tu estrés actual."
        )
        Spacer(modifier = Modifier.height(28.dp))

        // Stress visual representation
        StressIndicatorView(level = value)

        Spacer(modifier = Modifier.height(28.dp))
        val stressColor = when {
            value <= 3 -> PatientGreen
            value <= 6 -> Color(0xFFECC94B)
            else -> SOSCoral
        }
        Text(
            text = value.toString(),
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 72.sp,
            color = stressColor
        )
        Spacer(modifier = Modifier.height(16.dp))

        val gradientColors = listOf(
            PatientGreen,
            PatientGreenLight,
            Color(0xFFECC94B),
            Color(0xFFED8936),
            SOSCoral
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(Brush.horizontalGradient(colors = gradientColors))
            )
            Slider(
                value = value.toFloat(),
                onValueChange = {
                    val roundedValue = it.roundToInt()
                    if (roundedValue != lastHapticValue) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        lastHapticValue = roundedValue
                    }
                    onValueChange(roundedValue)
                },
                valueRange = 1f..10f,
                steps = 8,
                colors = SliderDefaults.colors(
                    thumbColor = stressColor,
                    activeTrackColor = Color.Transparent,
                    inactiveTrackColor = Color.Transparent,
                    activeTickColor = Color.Transparent,
                    inactiveTickColor = Color.Transparent
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("1\nSin estrés", fontFamily = LexendFontFamily, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
            Text("10\nMucho", fontFamily = LexendFontFamily, fontSize = 12.sp, color = TextSecondary, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun StressIndicatorView(level: Int) {
    // A simple visual Gauge for Stress
    Box(
        modifier = Modifier.size(160.dp),
        contentAlignment = Alignment.Center
    ) {
        val stressColor = when {
            level <= 3 -> PatientGreen
            level <= 6 -> Color(0xFFECC94B)
            else -> SOSCoral
        }
        Box(
            modifier = Modifier
                .size(150.dp)
                .clip(CircleShape)
                .background(stressColor.copy(alpha = 0.15f))
        )
        val label = when {
            level <= 3 -> "Calma"
            level <= 6 -> "Tensión"
            level <= 8 -> "Alerta"
            else -> "Pausa"
        }
        Text(
            text = label,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = stressColor
        )
    }
}

// ---------------------------------------------------------------------------
// STEP 5 — Habits frequency
// ---------------------------------------------------------------------------
@Composable
private fun HabitsStepView(
    answers: List<Int>,
    onAnswerSelected: (Int, Int) -> Unit
) {
    val questions = listOf(
        "¿Realizas actividad física?",
        "¿Mantienes contacto social con amigos o familia?",
        "¿Dedicas tiempo a actividades que disfrutas?",
        "¿Sigues una rutina diaria?"
    )
    val options = listOf("Nunca", "Casi nunca", "A veces", "Casi siempre", "Siempre")

    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "¿Con qué frecuencia...?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(18.dp))

        questions.forEachIndexed { qIdx, question ->
            Text(
                text = question,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(vertical = 8.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                options.forEachIndexed { oIdx, label ->
                    val isSelected = answers[qIdx] == oIdx + 1
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) PatientGreen else Color.White)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) PatientGreen else Color(0xFFCBD5E0),
                                shape = RoundedCornerShape(20.dp)
                            )
                            .clickable { onAnswerSelected(qIdx, oIdx + 1) }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 6 — Binary Yes/No Cards with Swipe
// ---------------------------------------------------------------------------
@Composable
private fun BinaryStepView(
    answers: List<Int>,
    onAnswerSelected: (Int, Int) -> Unit
) {
    val questions = listOf(
        "¿Has podido concentrarte en tus actividades habituales?",
        "¿Has podido disfrutar momentos del día?"
    )

    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "¿En el último tiempo...?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Desliza la tarjeta a la derecha para SÍ o a la izquierda para NO",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
        )
        Spacer(modifier = Modifier.height(20.dp))

        questions.forEachIndexed { index, question ->
            val answer = answers.getOrNull(index)
            SwipeQuestionCard(
                questionText = question,
                selectedValue = answer,
                onAnswered = { valVal -> onAnswerSelected(index, valVal) }
            )
        }
    }
}

@Composable
private fun SwipeQuestionCard(
    questionText: String,
    selectedValue: Int?,
    onAnswered: (Int) -> Unit
) {
    val currentOnAnswered by rememberUpdatedState(onAnswered)
    var offsetX by remember { mutableStateOf(0f) }
    val animatedOffset by animateFloatAsState(targetValue = offsetX, label = "swipe-offset")
    val density = LocalDensity.current
    val thresholdPx = with(density) { 80.dp.toPx() }

    val cardBgColor = when {
        offsetX > 10f -> Color(0xFFE8F5F0).copy(alpha = (offsetX / thresholdPx).coerceIn(0.15f, 1f))
        offsetX < -10f -> Color(0xFFFEECEB).copy(alpha = (-offsetX / thresholdPx).coerceIn(0.15f, 1f))
        selectedValue == 1 -> Color(0xFFE8F5F0)
        selectedValue == 0 -> Color(0xFFFEECEB)
        else -> Color.White
    }

    val cardBorderColor = when {
        offsetX > 10f -> PatientGreen.copy(alpha = (offsetX / thresholdPx).coerceIn(0.15f, 1f))
        offsetX < -10f -> SOSCoral.copy(alpha = (-offsetX / thresholdPx).coerceIn(0.15f, 1f))
        selectedValue == 1 -> PatientGreen
        selectedValue == 0 -> SOSCoral
        else -> Color(0xFFE2E8F0)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .height(130.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX > thresholdPx) {
                            currentOnAnswered(1)
                        } else if (offsetX < -thresholdPx) {
                            currentOnAnswered(0)
                        }
                        offsetX = 0f
                    },
                    onDragCancel = {
                        offsetX = 0f
                    },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        offsetX = (offsetX + dragAmount).coerceIn(-thresholdPx * 1.5f, thresholdPx * 1.5f)
                    }
                )
            }
            .offset { IntOffset(animatedOffset.roundToInt(), 0) }
            .border(
                border = BorderStroke(2.dp, cardBorderColor),
                shape = RoundedCornerShape(16.dp)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(cardBgColor)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = questionText,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 8.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            val indicatorText = when {
                offsetX >= thresholdPx -> "SÍ"
                offsetX <= -thresholdPx -> "NO"
                offsetX > 15f -> "Desliza para SÍ →"
                offsetX < -15f -> "← Desliza para NO"
                selectedValue == 1 -> "SÍ"
                selectedValue == 0 -> "NO"
                else -> "Desliza izquierda o derecha"
            }

            val indicatorColor = when {
                offsetX >= thresholdPx -> PatientGreen
                offsetX <= -thresholdPx -> SOSCoral
                selectedValue == 1 -> PatientGreen
                selectedValue == 0 -> SOSCoral
                else -> Color.Gray
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val showCheck = selectedValue == 1 || offsetX >= thresholdPx
                if (showCheck) {
                    Icon(
                        imageVector = RelaxIcons.Check,
                        contentDescription = null,
                        tint = PatientGreen,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                }
                Text(
                    text = indicatorText,
                    style = MaterialTheme.typography.labelLarge,
                    color = indicatorColor,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// STEP 7 — Notes
// ---------------------------------------------------------------------------
@Composable
private fun NotesStepView(
    notes: String,
    onNotesChange: (String) -> Unit
) {
    Column(horizontalAlignment = Alignment.Start, modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "¿Algo más que quieras compartir?",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(18.dp))
        OutlinedTextField(
            value = notes,
            onValueChange = onNotesChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            placeholder = { Text("Escribe libremente (opcional)") },
            maxLines = 10,
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PatientGreen,
                unfocusedBorderColor = Color(0xFFCBD5E0)
            )
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "${notes.length} / 500",
            style = MaterialTheme.typography.bodyMedium,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

// ---------------------------------------------------------------------------
// SUCCESS VIEW — Animated Results Screen
// ---------------------------------------------------------------------------
@Composable
private fun CheckInResultView(
    score: Int,
    category: String,
    onDismiss: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "¡Check-in Completado!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PatientGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tu nivel de bienestar de hoy",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(48.dp))

            // Score circular gauge
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(6.dp, PatientGreen.copy(alpha = 0.12f), CircleShape)
                    .border(12.dp, PatientGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = score.toString(),
                        style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                        fontWeight = FontWeight.ExtraBold,
                        color = PatientGreen
                    )
                    Text(
                        text = "/ 100",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray
                    )
                }
            }

            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = "Estado: $category",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PatientGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "¡Buen trabajo tomándote un momento para ti hoy!",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(64.dp))
            RelaxButton(
                text = "Ver mi dashboard",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.PRIMARY,
                role = AppRole.PATIENT
            )
        }
    }
}

@Composable
private fun AnimatedCheckInResultView(
    score: Int,
    category: String,
    onDismiss: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var showCategory by remember { mutableStateOf(false) }
    val animatedScore by animateIntAsState(
        targetValue = score,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "checkin-score-counter"
    )
    val categoryAlpha by animateFloatAsState(
        targetValue = if (showCategory) 1f else 0f,
        animationSpec = androidx.compose.animation.core.tween(durationMillis = 420),
        label = "checkin-category-alpha"
    )
    val composition by rememberLottieComposition(
        LottieCompositionSpec.JsonString(if (score >= 50) CheckInCelebrationLottieJson else CheckInCalmLottieJson)
    )
    val lottieProgress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        restartOnPlay = true
    )

    LaunchedEffect(score) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        kotlinx.coroutines.delay(520)
        showCategory = true
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            LottieAnimation(
                composition = composition,
                progress = { lottieProgress },
                modifier = Modifier.size(116.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "¡Check-in completado!",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.ExtraBold,
                color = PatientGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Tu nivel de bienestar de hoy",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(34.dp))

            Box(
                modifier = Modifier
                    .size(200.dp)
                    .border(6.dp, PatientGreen.copy(alpha = 0.12f), CircleShape)
                    .border(12.dp, PatientGreen, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (category == "Sin puntaje") {
                        Text(
                            text = "-",
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = PatientGreen
                        )
                    } else {
                        Text(
                            text = animatedScore.toString(),
                            style = MaterialTheme.typography.displayLarge.copy(fontSize = 80.sp),
                            fontWeight = FontWeight.ExtraBold,
                            color = PatientGreen
                        )
                        Text(
                            text = "/ 100",
                            style = MaterialTheme.typography.bodyLarge,
                            color = Color.Gray
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(34.dp))
            Text(
                text = "Estado: $category",
                modifier = Modifier.alpha(categoryAlpha),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = PatientGreen
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "¡Buen trabajo tomándote un momento para ti hoy!",
                modifier = Modifier.alpha(categoryAlpha),
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(64.dp))
            RelaxButton(
                text = "Ver mi dashboard",
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                variant = ButtonVariant.PRIMARY,
                role = AppRole.PATIENT
            )
        }
    }
}

private const val CheckInCelebrationLottieJson = """
{
  "v":"5.7.4","fr":30,"ip":0,"op":60,"w":220,"h":220,"nm":"check-celebration","ddd":0,"assets":[],
  "layers":[
    {"ddd":0,"ind":1,"ty":4,"nm":"circle","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":0,"k":[110,110,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":1,"k":[{"t":0,"s":[30,30,100]},{"t":28,"s":[112,112,100]},{"t":60,"s":[100,100,100]}]}},"shapes":[{"ty":"gr","it":[{"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[118,118]}},{"ty":"fl","c":{"a":0,"k":[0.407,0.827,0.568,1]},"o":{"a":0,"k":100}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}],"ip":0,"op":60,"st":0,"bm":0},
    {"ddd":0,"ind":2,"ty":4,"nm":"check","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":0,"k":[110,114,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":1,"k":[{"t":10,"s":[20,20,100]},{"t":34,"s":[110,110,100]}]}},"shapes":[{"ty":"gr","it":[{"ty":"sh","ks":{"a":0,"k":{"i":[[0,0],[0,0],[0,0]],"o":[[0,0],[0,0],[0,0]],"v":[[-34,-2],[-10,24],[38,-30]],"c":false}}},{"ty":"st","c":{"a":0,"k":[1,1,1,1]},"o":{"a":0,"k":100},"w":{"a":0,"k":12},"lc":2,"lj":2},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}],"ip":0,"op":60,"st":0,"bm":0}
  ]
}
"""

private const val CheckInCalmLottieJson = """
{
  "v":"5.7.4","fr":30,"ip":0,"op":60,"w":220,"h":220,"nm":"calm-check","ddd":0,"assets":[],
  "layers":[
    {"ddd":0,"ind":1,"ty":4,"nm":"soft-circle","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":0,"k":[110,110,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":1,"k":[{"t":0,"s":[76,76,100]},{"t":30,"s":[104,104,100]},{"t":60,"s":[96,96,100]}]}},"shapes":[{"ty":"gr","it":[{"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[124,124]}},{"ty":"fl","c":{"a":0,"k":[0.949,0.824,0.263,1]},"o":{"a":0,"k":100}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}],"ip":0,"op":60,"st":0,"bm":0},
    {"ddd":0,"ind":2,"ty":4,"nm":"breath-check","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":0,"k":0},"p":{"a":0,"k":[110,112,0]},"a":{"a":0,"k":[0,0,0]},"s":{"a":1,"k":[{"t":10,"s":[55,55,100]},{"t":42,"s":[100,100,100]}]}},"shapes":[{"ty":"gr","it":[{"ty":"sh","ks":{"a":0,"k":{"i":[[0,0],[0,0],[0,0]],"o":[[0,0],[0,0],[0,0]],"v":[[-34,-2],[-10,24],[38,-30]],"c":false}}},{"ty":"st","c":{"a":0,"k":[1,1,1,1]},"o":{"a":0,"k":100},"w":{"a":0,"k":12},"lc":2,"lj":2},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}],"ip":0,"op":60,"st":0,"bm":0}
  ]
}
"""
