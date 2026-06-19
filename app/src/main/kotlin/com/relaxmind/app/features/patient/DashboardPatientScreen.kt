package com.relaxmind.app.features.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.style.TextAlign
import com.relaxmind.app.ui.themes.PatientGreen
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxCard
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.utils.WellnessScoreCalculator
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.LinearEasing
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun DashboardPatientScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateToCheckIn: () -> Unit,
    onNavigateToMeditate: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToLinkCaregiver: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val patient by viewModel.patient.collectAsState()
    val todayCheckIn by viewModel.todayCheckIn.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val dailyGoalExercise by viewModel.dailyGoalExercise.collectAsState()
    val nextAppointment by viewModel.nextAppointment.collectAsState()
    val caregiver by viewModel.caregiver.collectAsState()

    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            RelaxBottomNav(
                selectedRoute = "patient/dashboard",
                onNavigate = onNavigate,
                role = AppRole.PATIENT
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (isLoading && patient == null) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            } else {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // Header Block
                    HeaderBlock(
                        patientName = patient?.name ?: "",
                        avatarUrl = patient?.avatarUrl ?: "",
                        onAvatarClick = onNavigateToEditProfile
                    )

                    // Score Card Block
                    ScoreCardBlock(
                        score = todayCheckIn?.score,
                        category = todayCheckIn?.category,
                        onCheckInClick = onNavigateToCheckIn
                    )

                    // Daily Goal Block
                    DailyGoalBlock(
                        goalCompleted = dailyGoal?.completed ?: false,
                        exerciseTitle = dailyGoalExercise?.title,
                        exerciseDuration = dailyGoalExercise?.durationMinutes,
                        onMeditateClick = onNavigateToMeditate,
                        onCheckboxChange = { completed ->
                            viewModel.toggleDailyGoalCompletion(completed)
                        }
                    )

                    // Diary Card Shortcut
                    DiaryBlock(
                        onDiaryClick = { onNavigate(com.relaxmind.app.Screen.Diary.route) }
                    )

                    // Next Appointment Block
                    NextAppointmentBlock(
                        appointmentTitle = nextAppointment?.title,
                        appointmentTime = nextAppointment?.time
                    )

                    // Caregiver Linking Block
                    CaregiverBlock(
                        caregiverId = patient?.caregiverId,
                        caregiverName = caregiver?.let { "${it.name} ${it.lastName}" },
                        caregiverAvatar = caregiver?.avatarUrl ?: "",
                        onLinkClick = onNavigateToLinkCaregiver
                    )

                    Spacer(modifier = Modifier.height(100.dp)) // Extra space for FAB safety
                }

                // Floating SOS button placed inside the root Box container
                var isSosPressed by remember { mutableStateOf(false) }
                val progress by animateFloatAsState(
                    targetValue = if (isSosPressed) 1f else 0f,
                    animationSpec = tween(durationMillis = 2000, easing = LinearEasing),
                    label = "SosProgress"
                )

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(24.dp)
                        .size(64.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .shadow(elevation = 8.dp, shape = CircleShape)
                            .background(Color(0xFFE8582A), CircleShape)
                            .pointerInput(Unit) {
                                detectTapGestures(
                                    onPress = {
                                        isSosPressed = true
                                        val job = scope.launch {
                                            delay(2000L) // 2-second hold
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            isSosPressed = false
                                            onNavigateToSOS()
                                        }
                                        try {
                                            awaitRelease()
                                        } finally {
                                            job.cancel()
                                            isSosPressed = false
                                        }
                                    }
                                )
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "SOS",
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        )
                    }

                    if (progress > 0f) {
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxSize(),
                            color = Color.White,
                            strokeWidth = 4.dp
                        )
                    }
                }
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Header Block Composable
// ---------------------------------------------------------------------------
@Composable
private fun HeaderBlock(
    patientName: String,
    avatarUrl: String,
    onAvatarClick: () -> Unit
) {
    val todayFormatted = remember {
        val date = LocalDate.now()
        val dayOfWeek = date.format(DateTimeFormatter.ofPattern("EEEE", Locale("es", "ES")))
        val dayOfMonth = date.dayOfMonth
        val month = date.format(DateTimeFormatter.ofPattern("MMMM", Locale("es", "ES")))
        "Hoy es $dayOfWeek $dayOfMonth de $month"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Buenos días, $patientName 👋",
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 20.sp),
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = todayFormatted,
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }

        PatientAvatar(
            avatarUrl = avatarUrl,
            onClick = onAvatarClick
        )
    }
}

@Composable
private fun PatientAvatar(
    avatarUrl: String,
    onClick: () -> Unit
) {
    val isCustomAvatar = avatarUrl.startsWith("relaxmind://avatar/")
    val modifier = Modifier
        .size(48.dp)
        .clip(CircleShape)
        .border(2.dp, PatientGreen.copy(alpha = 0.3f), CircleShape)
        .clickable(onClick = onClick)

    if (isCustomAvatar) {
        val colors = getAvatarColors(avatarUrl)
        Box(
            modifier = modifier.background(Brush.linearGradient(colors))
        )
    } else {
        AsyncImage(
            model = avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=P&background=0F6E56&color=fff" },
            contentDescription = "Perfil",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

private fun getAvatarColors(url: String): List<Color> {
    return when (url) {
        "relaxmind://avatar/01" -> listOf(Color(0xFFA7F3D0), Color(0xFF0F6E56))
        "relaxmind://avatar/02" -> listOf(Color(0xFFFFD6A5), Color(0xFFED8936))
        "relaxmind://avatar/03" -> listOf(Color(0xFFD8B4FE), Color(0xFF7C3AED))
        "relaxmind://avatar/04" -> listOf(Color(0xFFA5F3FC), Color(0xFF0891B2))
        "relaxmind://avatar/05" -> listOf(Color(0xFFFBCFE8), Color(0xFFDB2777))
        "relaxmind://avatar/06" -> listOf(Color(0xFFBFDBFE), Color(0xFF2563EB))
        "relaxmind://avatar/07" -> listOf(Color(0xFFFEF3C7), Color(0xFFEAB308))
        "relaxmind://avatar/08" -> listOf(Color(0xFFFECACA), Color(0xFFEF4444))
        "relaxmind://avatar/09" -> listOf(Color(0xFFCCFBF1), Color(0xFF14B8A6))
        "relaxmind://avatar/10" -> listOf(Color(0xFFFED7AA), Color(0xFFEA580C))
        "relaxmind://avatar/11" -> listOf(Color(0xFFE9D5FF), Color(0xFFA855F7))
        "relaxmind://avatar/12" -> listOf(Color(0xFFFDE68A), Color(0xFFB45309))
        else -> listOf(Color(0xFFCBD5E0), Color(0xFF718096))
    }
}

// ---------------------------------------------------------------------------
// Score Card Block Composable
// ---------------------------------------------------------------------------
@Composable
private fun ScoreCardBlock(
    score: Int?,
    category: String?,
    onCheckInClick: () -> Unit
) {
    val cardColor = WellnessScoreCalculator.getScoreColor(score)
    val isLightBackground = cardColor == Color(0xFFECC94B) || cardColor == Color(0xFFCBD5E0) || cardColor == Color(0xFF68D391)
    val contentTextColor = if (isLightBackground) Color(0xFF2D3748) else Color.White

    RelaxCard(
        modifier = Modifier.fillMaxWidth(),
        containerColor = cardColor
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (score != null) {
                // Heart Icon
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(Color.White.copy(alpha = 0.2f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RelaxIcons.Meditation,
                        contentDescription = null,
                        tint = contentTextColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Mi bienestar hoy",
                    style = MaterialTheme.typography.bodyMedium,
                    color = contentTextColor.copy(alpha = 0.8f)
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "$score / 100",
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.Bold,
                    color = contentTextColor
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = category ?: "",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = contentTextColor
                )
            } else {
                Text(
                    text = "Aún no has registrado tu check-in de hoy",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    color = contentTextColor,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(16.dp))
                RelaxButton(
                    text = "Hacer check-in",
                    onClick = onCheckInClick,
                    variant = ButtonVariant.PRIMARY,
                    role = AppRole.PATIENT,
                    modifier = Modifier.width(200.dp)
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Daily Goal Block Composable
// ---------------------------------------------------------------------------
@Composable
private fun DailyGoalBlock(
    goalCompleted: Boolean,
    exerciseTitle: String?,
    exerciseDuration: Int?,
    onMeditateClick: () -> Unit,
    onCheckboxChange: (Boolean) -> Unit
) {
    RelaxCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "🎯 Meta de Hoy",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold,
            color = PatientGreen
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (exerciseTitle != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val durationText = if (exerciseDuration != null) " • $exerciseDuration min" else ""
                Text(
                    text = "$exerciseTitle$durationText",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RelaxButton(
                        text = "Ir a meditar",
                        onClick = onMeditateClick,
                        variant = ButtonVariant.OUTLINE,
                        role = AppRole.PATIENT,
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    Checkbox(
                        checked = goalCompleted,
                        onCheckedChange = { onCheckboxChange(it) },
                        enabled = !goalCompleted,
                        colors = CheckboxDefaults.colors(
                            checkedColor = PatientGreen,
                            uncheckedColor = Color.Gray
                        )
                    )
                }
            }
        } else {
            Text(
                text = "Se está generando tu meta...",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Next Appointment Block Composable
// ---------------------------------------------------------------------------
@Composable
private fun NextAppointmentBlock(
    appointmentTitle: String?,
    appointmentTime: String?
) {
    RelaxCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "📅 Próximo Recordatorio",
            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
            fontWeight = FontWeight.SemiBold,
            color = PatientGreen
        )
        Spacer(modifier = Modifier.height(12.dp))

        if (appointmentTitle != null && appointmentTime != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PatientGreen.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RelaxIcons.Calendar,
                        contentDescription = null,
                        tint = PatientGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = "$appointmentTitle — $appointmentTime",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
            }
        } else {
            Text(
                text = "No tienes recordatorios pendientes",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Caregiver Linking Block Composable
// ---------------------------------------------------------------------------
@Composable
private fun CaregiverBlock(
    caregiverId: String?,
    caregiverName: String?,
    caregiverAvatar: String,
    onLinkClick: () -> Unit
) {
    RelaxCard(modifier = Modifier.fillMaxWidth()) {
        if (caregiverId == null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(PatientGreen.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = RelaxIcons.Person,
                            contentDescription = null,
                            tint = PatientGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = "Mi Cuidador",
                            style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
                            fontWeight = FontWeight.SemiBold,
                            color = PatientGreen
                        )
                        Text(
                            text = "¿Tienes un cuidador?",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                RelaxButton(
                    text = "Vincular",
                    onClick = onLinkClick,
                    variant = ButtonVariant.PRIMARY,
                    role = AppRole.PATIENT
                )
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    AsyncImage(
                        model = caregiverAvatar.ifBlank { "https://ui-avatars.com/api/?name=Caregiver&background=4338A8&color=fff" },
                        contentDescription = "Caregiver",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFF4338A8).copy(alpha = 0.3f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = RelaxIcons.Person,
                                contentDescription = null,
                                tint = PatientGreen,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Mi Cuidador",
                                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 14.sp),
                                fontWeight = FontWeight.SemiBold,
                                color = PatientGreen
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${caregiverName ?: "Cuidador"} • Vinculado ✓",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
                
                // Vinculado Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(PatientGreen.copy(alpha = 0.12f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .background(PatientGreen, CircleShape)
                        )
                        Text(
                            text = "Vinculado ✓",
                            style = MaterialTheme.typography.labelMedium,
                            color = PatientGreen,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryBlock(
    onDiaryClick: () -> Unit
) {
    RelaxCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(PatientGreen.copy(alpha = 0.08f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = PatientGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Mi Diario",
                        style = MaterialTheme.typography.headlineSmall.copy(fontSize = 16.sp),
                        fontWeight = FontWeight.Bold,
                        color = PatientGreen
                    )
                    Text(
                        text = "Escribe tus emociones y pensamientos de hoy",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            RelaxButton(
                text = "Ver diario",
                onClick = onDiaryClick,
                variant = ButtonVariant.PRIMARY,
                role = AppRole.PATIENT
            )
        }
    }
}
