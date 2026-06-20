package com.relaxmind.app.features.patient

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.R
import com.relaxmind.app.Screen
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.getAvatarDrawableRes
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.BorderSoft
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.LexendTypography
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.SOSCoral
import com.relaxmind.app.ui.themes.SoftCream
import com.relaxmind.app.ui.themes.SoftLavender
import com.relaxmind.app.ui.themes.SoftMint
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import com.relaxmind.app.utils.WellnessScoreCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
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

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    // Wrap the screen inside Lexend typography theme
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = LexendTypography
    ) {
        Scaffold(
            containerColor = Color.White,
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
                    .padding(innerPadding)
            ) {
                // Background gradient blobs
                SoftGradientBackground(animateBlobs = true)

                if (isLoading && patient == null) {
                    LoadingIndicator(modifier = Modifier.align(Alignment.Center))
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 1. Dashboard Header
                        DashboardHeader(
                            patientName = patient?.name ?: "Carlos",
                            avatarUrl = patient?.avatarUrl ?: "",
                            onAvatarClick = onNavigateToEditProfile,
                            hasNotifications = true // Visual notification bell badge as mockup
                        )

                        // 2. Main Wellbeing Today Card
                        WellbeingTodayCard(
                            score = todayCheckIn?.score,
                            category = todayCheckIn?.category
                        )

                        DailyCheckInStatusCard(
                            completed = todayCheckIn != null,
                            score = todayCheckIn?.score,
                            category = todayCheckIn?.category,
                            onStartClick = onNavigateToCheckIn,
                            onProgressClick = { onNavigate(Screen.Progress.route) }
                        )

                        // 3. "Para ti hoy" Section
                        ParaTiHoySection(
                            goalCompleted = dailyGoal?.completed ?: false,
                            exerciseTitle = dailyGoalExercise?.title,
                            exerciseDuration = dailyGoalExercise?.durationMinutes,
                            onMeditateClick = onNavigateToMeditate,
                            appointmentTitle = nextAppointment?.title,
                            appointmentTime = nextAppointment?.time,
                            onReminderClick = { onNavigate(Screen.Schedule.route) }
                        )

                        // 4. "Accesos rápidos" Section
                        QuickAccessSection(
                            onMeditateClick = onNavigateToMeditate,
                            onScheduleClick = { onNavigate(Screen.Schedule.route) }
                        )

                        // 5. "Mi Diario" Card (Soft 3D style)
                        DiaryCard(
                            onDiaryClick = { onNavigate(Screen.Diary.route) }
                        )

                        // 6. "Hablar con Lumi" Card (Soft 3D style)
                        LumiCard(
                            onLumiClick = { onNavigate(Screen.LumiChat.createRoute(null)) }
                        )

                        // 7. "Mi Cuidador" Card (Soft 3D style)
                        CaregiverCard(
                            caregiverId = patient?.caregiverId,
                            caregiverName = caregiver?.let { "${it.name} ${it.lastName}" },
                            caregiverAvatar = caregiver?.avatarUrl ?: "",
                            caregiver = caregiver,
                            onLinkClick = onNavigateToLinkCaregiver
                        )

                        // 8. "Centros de Salud Cercanos" Quick Access
                        NearbyHealthCard(
                            onNearbyClick = { onNavigate(com.relaxmind.app.Screen.NearbyHealth.route) }
                        )

                        // Margin safe space for the floating bottom bar
                        Spacer(modifier = Modifier.height(100.dp))
                    }

                    // 7. SOS Floating Button
                    SOSFloatingButton(
                        onSOSHoldTriggered = onNavigateToSOS,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(bottom = 16.dp, end = 20.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 1. HEADER COMPOSABLE
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DashboardHeader(
    patientName: String,
    avatarUrl: String,
    onAvatarClick: () -> Unit,
    hasNotifications: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // User Avatar with Active green dot
            UserAvatar(
                avatarUrl = avatarUrl,
                onClick = onAvatarClick
            )

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "Hola, $patientName",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = "Tu espacio seguro aquí",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Notification Bell button
        SoftNotificationButton(
            hasNotifications = hasNotifications,
            onClick = { /* TODO: open notifications */ }
        )
    }
}

@Composable
private fun UserAvatar(
    avatarUrl: String,
    onClick: () -> Unit
) {
    val isCustomAvatar = avatarUrl.startsWith("relaxmind://avatar/")
    Box(
        modifier = Modifier
            .size(64.dp)
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
    ) {
        // Rounded avatar container
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(Color(0xFFD4F3E5)) // default soft green circle background
                .border(2.dp, Color.White, CircleShape),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (isCustomAvatar) {
                Image(
                    painter = painterResource(id = getAvatarDrawableRes(avatarUrl)),
                    contentDescription = "Avatar de usuario",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } else if (avatarUrl.isBlank()) {
                Image(
                    painter = painterResource(id = R.drawable.avatar),
                    contentDescription = "Avatar de usuario por defecto",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit
                )
            } else {
                AsyncImage(
                    model = avatarUrl,
                    contentDescription = "Avatar de usuario",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Small green active status indicator at bottom right
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(PatientGreenLight)
                .border(2.dp, Color.White, CircleShape)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun SoftNotificationButton(
    hasNotifications: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
            )
            .background(Color.White, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = RelaxIcons.Notifications,
            contentDescription = "Notificaciones",
            tint = TextPrimary,
            modifier = Modifier.size(24.dp)
        )
        if (hasNotifications) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF4338A8)) // CaregiverIndigo purple dot
                    .align(Alignment.TopEnd)
                    .offset(x = 1.dp, y = (-1).dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 2. WELLBEING TODAY CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun WellbeingTodayCard(
    score: Int?,
    category: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .shadow(
                elevation = 16.dp,
                shape = RoundedCornerShape(30.dp),
                ambientColor = PatientGreen.copy(alpha = 0.25f),
                spotColor = PatientGreen.copy(alpha = 0.25f)
            ),
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(30.dp))
        ) {
            // Programmatic Vector Canvas Background
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(30.dp))
            ) {
                // 1. Draw linear gradient background (minty/green soft palette)
                drawRect(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFE8F9F3), Color(0xFFCBEFDF)),
                        start = androidx.compose.ui.geometry.Offset(0f, 0f),
                        end = androidx.compose.ui.geometry.Offset(size.width, size.height)
                    )
                )

                // Helper to draw a flower
                fun drawFlower(centerX: Float, centerY: Float, flowerSize: Float, alpha: Float) {
                    val petalRadius = flowerSize / 3.2f
                    val centerRadius = flowerSize / 5.5f
                    val baseColor = Color.White.copy(alpha = alpha)
                    for (i in 0 until 5) {
                        val angle = i * 72f
                        val rad = Math.toRadians(angle.toDouble())
                        val distance = flowerSize / 4.2f
                        val px = centerX + distance * Math.cos(rad).toFloat()
                        val py = centerY + distance * Math.sin(rad).toFloat()
                        drawCircle(
                            color = baseColor,
                            radius = petalRadius,
                            center = androidx.compose.ui.geometry.Offset(px, py)
                        )
                    }
                    // Yellow/cream soft center
                    drawCircle(
                        color = Color(0xFFFFFBEA).copy(alpha = alpha * 1.4f.coerceAtMost(1f)),
                        radius = centerRadius,
                        center = androidx.compose.ui.geometry.Offset(centerX, centerY)
                    )
                }

                // Helper to draw a 4-pointed sparkle star
                fun drawSparkle(centerX: Float, centerY: Float, sparkleSize: Float, alpha: Float) {
                    val starPath = androidx.compose.ui.graphics.Path().apply {
                        moveTo(centerX, centerY - sparkleSize)
                        quadraticBezierTo(centerX, centerY, centerX + sparkleSize, centerY)
                        quadraticBezierTo(centerX, centerY, centerX, centerY + sparkleSize)
                        quadraticBezierTo(centerX, centerY, centerX - sparkleSize, centerY)
                        quadraticBezierTo(centerX, centerY, centerX, centerY - sparkleSize)
                        close()
                    }
                    drawPath(
                        path = starPath,
                        color = Color.White.copy(alpha = alpha)
                    )
                }

                // Top-right area flower
                drawFlower(
                    centerX = size.width * 0.85f,
                    centerY = size.height * 0.18f,
                    flowerSize = 32.dp.toPx(),
                    alpha = 0.55f
                )

                // Bottom-left area flower
                drawFlower(
                    centerX = size.width * 0.14f,
                    centerY = size.height * 0.82f,
                    flowerSize = 24.dp.toPx(),
                    alpha = 0.45f
                )

                // Sparkles (stars)
                drawSparkle(
                    centerX = size.width * 0.44f,
                    centerY = size.height * 0.22f,
                    sparkleSize = 10.dp.toPx(),
                    alpha = 0.65f
                )

                drawSparkle(
                    centerX = size.width * 0.76f,
                    centerY = size.height * 0.80f,
                    sparkleSize = 14.dp.toPx(),
                    alpha = 0.55f
                )

                drawSparkle(
                    centerX = size.width * 0.48f,
                    centerY = size.height * 0.74f,
                    sparkleSize = 8.dp.toPx(),
                    alpha = 0.45f
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Message
                Column(
                    modifier = Modifier.weight(1.1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Mi bienestar hoy",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(text = "✦", color = Color(0xFF68D391), fontSize = 16.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Prioriza tu paz mental hoy.",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        color = Color(0xFF5A5E6B)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right Column: Canvas drawn progress circle
                CircularWellbeingProgress(
                    score = score,
                    category = category,
                    modifier = Modifier.weight(0.9f)
                )
            }
        }
    }
}

@Composable
private fun DailyCheckInStatusCard(
    completed: Boolean,
    score: Int?,
    category: String?,
    onStartClick: () -> Unit,
    onProgressClick: () -> Unit
) {
    val action = if (completed) onProgressClick else onStartClick
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = PatientGreen.copy(alpha = 0.14f),
                spotColor = PatientGreen.copy(alpha = 0.16f)
            )
            .clickable(onClick = action),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (completed) PatientGreen else SoftMint),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (completed) RelaxIcons.Check else RelaxIcons.Meditation,
                    contentDescription = null,
                    tint = if (completed) Color.White else PatientGreen,
                    modifier = Modifier.size(23.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Check-in diario",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (completed) {
                        "Completado hoy${score?.let { " · $it/100" } ?: ""}${category?.let { " · $it" } ?: ""}"
                    } else {
                        "Registra cómo te sientes para actualizar tu bienestar."
                    },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = TextSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier
                    .shadow(2.dp, RoundedCornerShape(18.dp))
                    .background(if (completed) Color(0xFFE8F7F1) else PatientGreen, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (completed) {
                    Icon(
                        imageVector = RelaxIcons.Check,
                        contentDescription = null,
                        tint = PatientGreen,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = if (completed) "Listo" else "Iniciar",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (completed) PatientGreen else Color.White
                )
            }
        }
    }
}

@Composable
private fun CircularWellbeingProgress(
    score: Int?,
    category: String?,
    modifier: Modifier = Modifier
) {
    val displayScore = score ?: 74 // Default placeholder visual if null
    val targetProgress = displayScore / 100f
    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(1200, easing = FastOutSlowInEasing),
        label = "progress-ring-anim"
    )

    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.size(112.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val strokeWidth = 10.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val topLeft = androidx.compose.ui.geometry.Offset(
                    (size.width - diameter) / 2,
                    (size.height - diameter) / 2
                )
                val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)

                // Translucent track circle background
                drawArc(
                    color = Color.White.copy(alpha = 0.6f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    topLeft = topLeft,
                    size = arcSize
                )

                // Filled green arc
                drawArc(
                    color = PatientGreen,
                    startAngle = -90f,
                    sweepAngle = animatedProgress * 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                    topLeft = topLeft,
                    size = arcSize
                )
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = displayScore.toString(),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 26.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "/100",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (score != null) (category ?: "Bueno") else "Bueno",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = PatientGreen
                )
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 3. "PARA TI HOY" SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun ParaTiHoySection(
    goalCompleted: Boolean,
    exerciseTitle: String?,
    exerciseDuration: Int?,
    onMeditateClick: () -> Unit,
    appointmentTitle: String?,
    appointmentTime: String?,
    onReminderClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Para ti hoy",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = RelaxIcons.Meditation,
                contentDescription = null,
                tint = PatientGreen,
                modifier = Modifier.size(18.dp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Card 1: Today's Goal
            TodayGoalCard(
                title = exerciseTitle ?: "Respiración",
                duration = exerciseDuration ?: 8,
                completed = goalCompleted,
                onStartClick = onMeditateClick,
                modifier = Modifier.weight(1f)
            )

            // Card 2: Next Reminder
            NextReminderCard(
                title = appointmentTitle,
                time = appointmentTime,
                onCardClick = onReminderClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun TodayGoalCard(
    title: String,
    duration: Int,
    completed: Boolean,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SoftMint)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RelaxIcons.Meditation,
                        contentDescription = null,
                        tint = PatientGreen,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Meta de hoy",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = PatientGreen
                )
            }

            Column {
                Text(
                    text = title,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "4-7-8 · $duration min",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            // Action Pill
            Row(
                modifier = Modifier
                    .shadow(1.dp, RoundedCornerShape(50))
                    .background(
                        if (completed) Color.LightGray.copy(alpha = 0.5f) else PatientGreen,
                        RoundedCornerShape(50)
                    )
                    .clickable(enabled = !completed, onClick = onStartClick)
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
                Text(
                    text = if (completed) "Completada ✓" else "Comenzar",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = if (completed) TextSecondary else Color.White
                )
                if (!completed) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun NextReminderCard(
    title: String?,
    time: String?,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(160.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
            )
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = SoftCream)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.Start
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .background(Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RelaxIcons.Calendar,
                        contentDescription = null,
                        tint = SOSCoral,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Text(
                    text = "Próximo recordatorio",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = SOSCoral,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (title != null && time != null) {
                Column {
                    Text(
                        text = title,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = RelaxIcons.Calendar,
                            contentDescription = null,
                            tint = TextSecondary,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = time,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
            } else {
                Column {
                    Text(
                        text = "Agenda libre",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Todo al día hoy",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }

            // Just a placeholder row to align height structure
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 4. "ACCESOS RÁPIDOS" SECTION
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun QuickAccessSection(
    onMeditateClick: () -> Unit,
    onScheduleClick: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Accesos rápidos",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = "✦", color = Color(0xFF4338A8), fontSize = 16.sp)
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Meditar Quick Access
            QuickAccessCard(
                title = "Meditar",
                description = "Encuentra calma en minutos",
                icon = RelaxIcons.Meditation,
                backgroundColor = SoftMint,
                iconColor = PatientGreen,
                onClick = onMeditateClick,
                modifier = Modifier.weight(1f)
            )

            // Agenda Quick Access
            QuickAccessCard(
                title = "Agenda",
                description = "Tus citas y recordatorios en un lugar",
                icon = RelaxIcons.Calendar,
                backgroundColor = SoftLavender,
                iconColor = Color(0xFF4338A8),
                onClick = onScheduleClick,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun QuickAccessCard(
    title: String,
    description: String,
    icon: ImageVector,
    backgroundColor: Color,
    iconColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .height(140.dp)
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
            )
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .background(Color.White.copy(alpha = 0.65f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(14.dp))
            Text(
                text = title,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 5. "MI DIARIO" CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun DiaryCard(
    onDiaryClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
            )
            .clickable(onClick = onDiaryClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(SoftMint, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        tint = PatientGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Mi Diario",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Escribe tus emociones y pensamientos de hoy",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 6. CAREGIVER LINKING CARD
// ─────────────────────────────────────────────────────────────────────────────
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaregiverCard(
    caregiverId: String?,
    caregiverName: String?,
    caregiverAvatar: String,
    caregiver: com.relaxmind.app.data.model.Caregiver?,
    onLinkClick: () -> Unit
) {
    var showModal by remember { mutableStateOf(false) }

    // Modal de contacto del cuidador
    if (showModal && caregiver != null) {
        AlertDialog(
            onDismissRequest = { showModal = false },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = { showModal = false }) {
                    Text("Cerrar", fontFamily = LexendFontFamily, color = Color(0xFF4338A8))
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = Color.White,
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    AsyncImage(
                        model = caregiver.avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=${caregiver.name}+${caregiver.lastName}&background=4338A8&color=fff" },
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(76.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color(0xFF4338A8).copy(alpha = 0.25f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "${caregiver.name} ${caregiver.lastName}",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A1A2E)
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(Color(0xFFF1EDFF))
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = "Mi Cuidador",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = Color(0xFF4338A8)
                        )
                    }
                    Spacer(modifier = Modifier.height(20.dp))

                    // Info rows
                    val infoItems = buildList {
                        if (caregiver.email.isNotBlank()) add(Pair(Icons.Default.Email, caregiver.email))
                        if (caregiver.phone.isNotBlank()) add(Pair(Icons.Default.Phone, caregiver.phone))
                        if (caregiver.sex.isNotBlank()) add(Pair(Icons.Default.Person, "Género: ${caregiver.sex}"))
                        if (caregiver.birthDate.isNotBlank()) add(Pair(Icons.Default.DateRange, caregiver.birthDate))
                    }
                    infoItems.forEachIndexed { i, (icon, label) ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFFF8F7FF))
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(imageVector = icon, contentDescription = null, tint = Color(0xFF4338A8), modifier = Modifier.size(20.dp))
                            Text(text = label, fontFamily = LexendFontFamily, fontSize = 14.sp, color = Color(0xFF2C3E50))
                        }
                        if (i < infoItems.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        if (caregiverId == null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .background(Color(0xFFF1EDFF), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = RelaxIcons.Person,
                            contentDescription = null,
                            tint = Color(0xFF4338A8),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "Mi Cuidador",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "¿Tienes un cuidador vinculado?",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .shadow(1.dp, RoundedCornerShape(50))
                        .background(Color(0xFF4338A8), RoundedCornerShape(50))
                        .clickable(onClick = onLinkClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Vincular",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White
                    )
                }
            }
        } else {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { if (caregiver != null) showModal = true }
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    AsyncImage(
                        model = caregiverAvatar.ifBlank { "https://ui-avatars.com/api/?name=Caregiver&background=4338A8&color=fff" },
                        contentDescription = "Caregiver Avatar",
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, Color(0xFF4338A8).copy(alpha = 0.3f), CircleShape),
                        contentScale = ContentScale.Crop
                    )
                    Column {
                        Text(
                            text = "Mi Cuidador",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = caregiverName ?: "Cuidador",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = Color(0xFF4338A8)
                        )
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(PatientGreen.copy(alpha = 0.1f))
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
                                text = "Vinculado",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = PatientGreen
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = TextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 7. NEARBY HEALTH CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun NearbyHealthCard(
    onNearbyClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
            )
            .clickable(onClick = onNearbyClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .background(PatientGreenLight, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = PatientGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = "Centros de Salud Cercanos",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Encuentra ayuda profesional cerca de ti",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 8. SOS FLOATING BUTTON
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun SOSFloatingButton(
    onSOSHoldTriggered: () -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var isSosPressed by remember { mutableStateOf(false) }
    val progress by animateFloatAsState(
        targetValue = if (isSosPressed) 1f else 0f,
        animationSpec = tween(durationMillis = 2000, easing = LinearEasing),
        label = "SosProgress"
    )

    // Infinite pulse animations
    val infiniteTransition = rememberInfiniteTransition(label = "sos-infinite-pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sos-scale-anim"
    )
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sos-alpha-anim"
    )

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(pulseScale)
                .background(Color(0xFFFF5E5E).copy(alpha = pulseAlpha), CircleShape)
        )

        // Main Coral Button
        Box(
            modifier = Modifier
                .size(76.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFFC53030).copy(alpha = 0.4f),
                    spotColor = Color(0xFFC53030).copy(alpha = 0.4f)
                )
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFFFF5E5E), // Lighter red
                            Color(0xFFC53030)  // Darker red
                        )
                    ),
                    shape = CircleShape
                )
                .pointerInput(Unit) {
                    detectTapGestures(
                        onPress = {
                            isSosPressed = true
                            val holdJob = scope.launch {
                                delay(2000L) // Safe hold of 2 seconds
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                isSosPressed = false
                                onSOSHoldTriggered()
                            }
                            try {
                                awaitRelease()
                            } finally {
                                holdJob.cancel()
                                isSosPressed = false
                            }
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive, // Emergency alarm bell ringing icon
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "SOS",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = Color.White
                )
            }
        }

        if (progress > 0f) {
            CircularProgressIndicator(
                progress = { progress },
                modifier = Modifier.size(76.dp),
                color = Color.White,
                strokeWidth = 4.dp
            )
        }
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// 8. "LUMI AI ASSISTANT" CARD
// ─────────────────────────────────────────────────────────────────────────────
@Composable
private fun LumiCard(
    onLumiClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
            )
            .clickable(onClick = onLumiClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Image(
                    painter = painterResource(id = com.relaxmind.app.R.drawable.lumi),
                    contentDescription = "Lumi",
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                )
                Column {
                    Text(
                        text = "Hablar con Lumi",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Tu asistente IA de bienestar",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
