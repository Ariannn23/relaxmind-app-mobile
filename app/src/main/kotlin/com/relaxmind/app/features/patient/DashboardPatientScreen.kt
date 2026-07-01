package com.relaxmind.app.features.patient

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.animation.core.*
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Notifications
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.SentimentDissatisfied
import androidx.compose.material3.DropdownMenu
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material3.Divider
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.R
import com.relaxmind.app.Screen
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.getAvatarDrawableRes
import com.relaxmind.app.ui.components.DashboardSkeleton
import com.relaxmind.app.ui.components.ErrorStateScreen
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.NotificationPermissionDialog
import com.relaxmind.app.ui.components.RelaxCard
import com.relaxmind.app.ui.components.ScrollToTopEvents
import com.relaxmind.app.ui.components.hasNotificationPermission
import com.relaxmind.app.ui.components.openNotificationSettings
import com.relaxmind.app.ui.components.rememberNotificationPermissionLauncher
import com.relaxmind.app.ui.components.rememberNotificationPermissionStatus
import com.relaxmind.app.ui.components.requestNotificationPermission
import com.relaxmind.app.utils.SoundPlayerManager
import com.relaxmind.app.data.model.UserAchievement
import com.relaxmind.app.features.patient.AchievementUnlockedScreen
import com.relaxmind.app.ui.themes.Outfit
import com.relaxmind.app.ui.themes.Urbanist
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
import com.relaxmind.app.ui.themes.ThemeState
import com.relaxmind.app.utils.WellnessScoreCalculator
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class PatientDashboardColors(
    val isDark: Boolean,
    val background: Color,
    val backgroundBrush: Brush,
    val card: Color,
    val cardSoft: Color,
    val primary: Color,
    val primaryStrong: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val notificationButton: Color,
    val wellbeingBrush: Brush,
    val checkInCard: Color,
    val checkInBorder: Color,
    val checkInAccent: Color
)

private val LocalPatientDashboardColors = staticCompositionLocalOf {
    patientDashboardColors(isDark = false)
}

private fun patientDashboardColors(isDark: Boolean): PatientDashboardColors {
    return if (isDark) {
        PatientDashboardColors(
            isDark = true,
            background = com.relaxmind.app.ui.themes.BackgroundDark,
            backgroundBrush = Brush.verticalGradient(
                listOf(com.relaxmind.app.ui.themes.BackgroundDark, Color(0xFF0F1116), Color(0xFF0B0D11))
            ),
            card = com.relaxmind.app.ui.themes.SurfaceDark,
            cardSoft = Color(0xFF2B2F3D),
            primary = com.relaxmind.app.ui.themes.PatientGreenLight,
            primaryStrong = com.relaxmind.app.ui.themes.PatientGreen,
            textPrimary = com.relaxmind.app.ui.themes.TextDarkPrimary,
            textSecondary = com.relaxmind.app.ui.themes.TextDarkSecondary,
            textMuted = Color(0xFF6B7280),
            border = com.relaxmind.app.ui.themes.BorderDark,
            notificationButton = Color.White.copy(alpha = 0.05f),
            wellbeingBrush = Brush.linearGradient(
                listOf(Color(0xFF1D202B), Color(0xFF1A1C25), com.relaxmind.app.ui.themes.SurfaceDark)
            ),
            checkInCard = Color(0xFF332026),
            checkInBorder = Color(0xFF4A2B33),
            checkInAccent = Color(0xFFFF7A8A)
        )
    } else {
        PatientDashboardColors(
            isDark = false,
            background = Color.White,
            backgroundBrush = Brush.verticalGradient(listOf(Color.White, Color.White)),
            card = Color.White,
            cardSoft = SoftMint,
            primary = PatientGreen,
            primaryStrong = PatientGreen,
            textPrimary = TextPrimary,
            textSecondary = TextSecondary,
            textMuted = TextSecondary,
            border = BorderSoft,
            notificationButton = Color.White,
            wellbeingBrush = Brush.linearGradient(listOf(Color(0xFFEBF8FF), Color(0xFFE6F9F2))),
            checkInCard = Color(0xFFFFF1F2),
            checkInBorder = Color.Transparent,
            checkInAccent = Color(0xFFF43F5E)
        )
    }
}

private fun darkQuickAccessColor(key: String): Color = when (key) {
    "sounds" -> Color(0xFF123D3A)
    "library" -> Color(0xFF242340)
    "diary" -> Color(0xFF352B21)
    "lumi" -> Color(0xFF123247)
    "caregiver" -> Color(0xFF2D2448)
    "health" -> Color(0xFF12342F)
    else -> Color(0xFF102C29)
}

private fun lightQuickAccessShadow(isDark: Boolean): Color =
    if (isDark) Color(0xFF68D391).copy(alpha = 0.08f) else Color(0xFF8A88A6).copy(alpha = 0.2f)

@Composable
fun DashboardPatientScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateToCheckIn: () -> Unit,
    onNavigateToInitialTest: () -> Unit = {},
    onNavigateToMeditate: () -> Unit,
    onNavigateToEditProfile: () -> Unit,
    onNavigateToLinkCaregiver: () -> Unit,
    onNavigateToSOS: () -> Unit,
    onNavigate: (String) -> Unit,
    showBottomNav: Boolean = true
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val patient by viewModel.patient.collectAsState()
    val todayCheckIn by viewModel.todayCheckIn.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val dailyGoalExercise by viewModel.dailyGoalExercise.collectAsState()
    val nextAppointment by viewModel.nextAppointment.collectAsState()
    val caregiver by viewModel.caregiver.collectAsState()
    val isDarkDashboard by ThemeState.darkMode.collectAsState()
    val dashboardColors = remember(isDarkDashboard) { patientDashboardColors(isDarkDashboard) }
    val initialTestBannerState by viewModel.initialTestBannerState.collectAsState()
    val isDismissed by viewModel.isInitialTestNotificationDismissed.collectAsState()
    val context = LocalContext.current
    val notificationsPermissionGranted = rememberNotificationPermissionStatus()
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var notificationPromptShown by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberNotificationPermissionLauncher { granted ->
        viewModel.updateNotificationsEnabled(granted)
        showNotificationPermissionDialog = !granted
    }
    val scrollState = rememberScrollState()

    LaunchedEffect(patient?.id, patient?.notificationsEnabled, notificationsPermissionGranted) {
        val currentPatient = patient ?: return@LaunchedEffect
        if (!notificationPromptShown && (!currentPatient.notificationsEnabled || !notificationsPermissionGranted)) {
            notificationPromptShown = true
            showNotificationPermissionDialog = true
        }
    }

    val lifecycleOwner = androidx.compose.ui.platform.LocalLifecycleOwner.current
    androidx.compose.runtime.DisposableEffect(lifecycleOwner) {
        val observer = androidx.lifecycle.LifecycleEventObserver { _, event ->
            if (event == androidx.lifecycle.Lifecycle.Event.ON_RESUME) {
                viewModel.loadDashboardData()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Refresh the 24h countdown every minute while the dashboard is open
    LaunchedEffect(Unit) {
        while (true) {
            kotlinx.coroutines.delay(60_000L)
            viewModel.refreshInitialTestBannerState()
        }
    }

    LaunchedEffect(Unit) {
        ScrollToTopEvents.requests.collect { route ->
            if (route == Screen.PatientDashboard.route) {
                scrollState.animateScrollTo(0)
            }
        }
    }

    // Wrap the screen inside Lexend typography theme
    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = LexendTypography
    ) {
        CompositionLocalProvider(LocalPatientDashboardColors provides dashboardColors) {
        Scaffold(
            containerColor = dashboardColors.background,
            bottomBar = {
                if (showBottomNav) {
                    RelaxBottomNav(
                        selectedRoute = "patient/dashboard",
                        onNavigate = { route -> 
                            if (route == "caregiver/patients") {
                                onNavigateToEditProfile()
                            } else {
                                onNavigate(route)
                            }
                        },
                        role = AppRole.PATIENT,
                        darkMode = isDarkDashboard
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (dashboardColors.isDark) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(dashboardColors.backgroundBrush)
                    )
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = dashboardColors.primary.copy(alpha = 0.10f),
                            radius = size.minDimension * 0.45f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.10f, size.height * 0.18f)
                        )
                        drawCircle(
                            color = Color(0xFF123247).copy(alpha = 0.18f),
                            radius = size.minDimension * 0.38f,
                            center = androidx.compose.ui.geometry.Offset(size.width * 0.92f, size.height * 0.70f)
                        )
                    }
                } else {
                    SoftGradientBackground(animateBlobs = true)
                }

                if (isLoading && patient == null && error == null) {
                    DashboardSkeleton(modifier = Modifier.align(Alignment.Center))
                } else if (error != null && patient == null) {
                    ErrorStateScreen(
                        message = error ?: "",
                        onRetry = { viewModel.loadDashboardData() }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(scrollState)
                            .padding(horizontal = 24.dp, vertical = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 1. Header (User Info & Notifications)
                        DashboardHeader(
                            patientName = patient?.name ?: "Carlos",
                            avatarUrl = patient?.avatarUrl ?: "",
                            onAvatarClick = { onNavigate(Screen.PatientSettings.route) },
                            initialTestBannerState = initialTestBannerState,
                            isDismissed = isDismissed,
                            hasPendingUnlink = patient?.pendingCaregiverUnlinkAlert == true,
                            caregiverName = "${patient?.caregiverName.orEmpty()} ${patient?.caregiverLastName.orEmpty()}".trim(),
                            onCompleteInitialTestClick = onNavigateToInitialTest,
                            onDismissNotification = { viewModel.dismissInitialTestNotification() },
                            onUnlinkNotificationClick = { viewModel.clearUnlinkAlert() }
                        )
                        
                        // 2. Main Wellbeing Today Card
                        WellbeingTodayCard(
                            score = todayCheckIn?.score,
                            category = todayCheckIn?.category,
                            isTestSkipped = initialTestBannerState != InitialTestBannerState.None
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

                        QuickAccessSection(
                            onSoundsClick = { onNavigate(Screen.RelaxSounds.route) },
                            onLibraryClick = { onNavigate(Screen.Library.createRoute("patient")) }
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
                            caregiverName = caregiver
                                ?.let { "${it.name} ${it.lastName}".trim() }
                                ?.takeIf { it.isNotBlank() },
                            caregiverAvatar = caregiver?.avatarUrl ?: "",
                            caregiver = caregiver,
                            isCaregiverLoading = isLoading && patient?.caregiverId != null && caregiver == null,
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
                        onSOSHoldTriggered = {
                            if (patient?.notificationsEnabled == true && hasNotificationPermission(context)) {
                                onNavigateToSOS()
                            } else {
                                showNotificationPermissionDialog = true
                            }
                        },
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            // 100.dp to avoid the bottom bar, 16.dp for normal margin
                            .padding(bottom = 116.dp, end = 20.dp)
                    )
                }

            }
        }
        }
    }

    if (showNotificationPermissionDialog) {
        NotificationPermissionDialog(
            role = AppRole.PATIENT,
            title = "Activa tus notificaciones",
            message = "Para recibir recordatorios de check-in, avisos de agenda y poder enviar SOS con seguridad, RelaxMind necesita que las notificaciones estén activadas.",
            primaryText = "Activar ahora",
            secondaryText = "Ir a ajustes",
            onPrimaryClick = {
                if (hasNotificationPermission(context)) {
                    viewModel.updateNotificationsEnabled(true)
                    showNotificationPermissionDialog = false
                } else {
                    requestNotificationPermission(
                        launcher = notificationPermissionLauncher,
                        onAlreadyGranted = {
                            viewModel.updateNotificationsEnabled(true)
                            showNotificationPermissionDialog = false
                        }
                    )
                }
            },
            onDismiss = {
                openNotificationSettings(context)
                showNotificationPermissionDialog = false
            }
        )
    }
}

// -----------------------------------------------------------------------------
// 1. HEADER COMPOSABLE
// -----------------------------------------------------------------------------
@Composable
private fun DashboardHeader(
    patientName: String,
    avatarUrl: String,
    onAvatarClick: () -> Unit,
    initialTestBannerState: InitialTestBannerState = InitialTestBannerState.None,
    isDismissed: Boolean = false,
    hasPendingUnlink: Boolean = false,
    caregiverName: String = "",
    onCompleteInitialTestClick: () -> Unit = {},
    onDismissNotification: () -> Unit = {},
    onUnlinkNotificationClick: () -> Unit = {}
) {
    val colors = LocalPatientDashboardColors.current
    val hasInitialTestNotification = !isDismissed && initialTestBannerState is InitialTestBannerState.SkippedWithin24h
    val hasNotifications = hasInitialTestNotification || hasPendingUnlink
    var notificationsExpanded by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    var showUnlinkReceivedDialog by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }

    if (showUnlinkReceivedDialog) {
        com.relaxmind.app.ui.components.UnlinkNotificationDialog(
            type = com.relaxmind.app.ui.components.UnlinkDialogType.RECEIVED,
            otherPartyName = if (caregiverName.isNotBlank()) caregiverName else "Tu cuidador",
            primaryColor = com.relaxmind.app.ui.themes.PatientGreen,
            onDismissRequest = {
                showUnlinkReceivedDialog = false
                onUnlinkNotificationClick()
            }
        )
    }

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
                    text = stringResource(id = R.string.dashboard_greeting, patientName),
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = stringResource(id = R.string.dashboard_safe_space),
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = colors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Notification Bell button
        Box {
            SoftNotificationButton(
                hasNotifications = hasNotifications,
                onClick = { notificationsExpanded = !notificationsExpanded }
            )
            
            if (notificationsExpanded) {
                Popup(
                    alignment = Alignment.TopEnd,
                    offset = IntOffset(12, 140),
                    onDismissRequest = { notificationsExpanded = false },
                    properties = PopupProperties(focusable = true)
                ) {
                    var animateIn by remember { mutableStateOf(false) }
                    LaunchedEffect(Unit) {
                        animateIn = true
                    }
                    Column {
                        AnimatedVisibility(
                            visible = animateIn,
                            enter = fadeIn(animationSpec = tween(220)) +
                                    scaleIn(
                                        initialScale = 0.35f,
                                        transformOrigin = TransformOrigin(1f, 0f),
                                        animationSpec = spring(
                                            dampingRatio = Spring.DampingRatioLowBouncy,
                                            stiffness = Spring.StiffnessMediumLow
                                        )
                                    ) +
                                    expandVertically(
                                        expandFrom = Alignment.Top,
                                        animationSpec = tween(250, easing = FastOutSlowInEasing)
                                    ),
                            exit = fadeOut(animationSpec = tween(150)) +
                                    scaleOut(
                                        targetScale = 0.6f,
                                        transformOrigin = TransformOrigin(1f, 0f),
                                        animationSpec = tween(150)
                                    )
                        ) {
                            ElevatedCard(
                                modifier = Modifier
                                    .width(340.dp)
                                    .shadow(
                                        elevation = 16.dp, 
                                        shape = RoundedCornerShape(24.dp),
                                        ambientColor = if (colors.isDark) colors.primary.copy(alpha = 0.4f) else Color(0xFF8A88A6).copy(alpha = 0.5f),
                                        spotColor = if (colors.isDark) colors.primary.copy(alpha = 0.4f) else Color(0xFF8A88A6).copy(alpha = 0.5f)
                                    )
                                    .border(1.dp, if (colors.isDark) colors.primary.copy(alpha=0.3f) else Color(0xFFE2E8F0), RoundedCornerShape(24.dp)),
                                shape = RoundedCornerShape(24.dp),
                                colors = androidx.compose.material3.CardDefaults.elevatedCardColors(
                                    containerColor = if (colors.isDark) Color(0xFF111827) else Color(0xFFF8FAFC)
                                )
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    if (hasNotifications) {
                                        Text(
                                            text = "Notificaciones",
                                            fontFamily = LexendFontFamily,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp,
                                            color = colors.textPrimary,
                                            modifier = Modifier.padding(bottom = 10.dp)
                                        )
                                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                            if (hasPendingUnlink) {
                                                DashboardNotificationItem(
                                                    title = "Vínculo Terminado",
                                                    subtitle = "Toca para ver los detalles.",
                                                    icon = androidx.compose.material.icons.Icons.Default.SentimentDissatisfied,
                                                    iconColorLight = Color(0xFFB91C1C),
                                                    iconColorDark = Color(0xFFFCA5A5),
                                                    bgColorLight = Color(0xFFFFF0F0),
                                                    bgColorDark = Color(0xFF3F1919),
                                                    fillColorLight = Color(0xFFFFD6D6),
                                                    fillColorDark = Color(0xFF5C2323),
                                                    onDismiss = {
                                                        notificationsExpanded = false
                                                        showUnlinkReceivedDialog = true
                                                    },
                                                    onClick = {
                                                        notificationsExpanded = false
                                                        showUnlinkReceivedDialog = true
                                                    }
                                                )
                                            }

                                            if (hasInitialTestNotification && initialTestBannerState is InitialTestBannerState.SkippedWithin24h) {
                                                val state = initialTestBannerState as InitialTestBannerState.SkippedWithin24h
                                                val timeText = if (state.hoursLeft > 0) "${state.hoursLeft}h ${state.minutesLeft}min" else "${state.minutesLeft} min"
                                                DashboardNotificationItem(
                                                    title = "Test inicial pendiente",
                                                    subtitle = "Te quedan $timeText para completarlo",
                                                    icon = Icons.Filled.DateRange,
                                                    iconColorLight = Color(0xFFB91C1C), // Red 700
                                                    iconColorDark = Color(0xFFFCA5A5), // Red 300
                                                    bgColorLight = Color(0xFFFFF0F0), // Very soft red
                                                    bgColorDark = Color(0xFF3F1919),
                                                    fillColorLight = Color(0xFFFFD6D6), // Darker red fill
                                                    fillColorDark = Color(0xFF5C2323),
                                                    onDismiss = {
                                                        notificationsExpanded = false
                                                        onDismissNotification()
                                                    },
                                                    onClick = {
                                                        notificationsExpanded = false
                                                        onCompleteInitialTestClick()
                                                    }
                                                )
                                            }
                                        }
                                    } else {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            modifier = Modifier.fillMaxWidth().padding(16.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.NotificationsOff,
                                                contentDescription = null,
                                                tint = colors.primary,
                                                modifier = Modifier.size(42.dp)
                                            )
                                            Spacer(modifier = Modifier.height(16.dp))
                                            Text(
                                                text = "No tienes notificaciones",
                                                fontFamily = LexendFontFamily,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 16.sp,
                                                color = colors.textPrimary,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Aquí aparecerán tus recordatorios y mensajes.",
                                                fontFamily = LexendFontFamily,
                                                fontWeight = FontWeight.Normal,
                                                fontSize = 13.sp,
                                                color = colors.textSecondary,
                                                textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardNotificationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconColorLight: Color,
    iconColorDark: Color,
    bgColorLight: Color,
    bgColorDark: Color,
    fillColorLight: Color,
    fillColorDark: Color,
    onDismiss: () -> Unit,
    onClick: () -> Unit
) {
    val colors = LocalPatientDashboardColors.current
    var isPressed by remember { mutableStateOf(false) }
    var showDelete by remember { mutableStateOf(false) }
    val pressProgress by animateFloatAsState(
        targetValue = if (isPressed || showDelete) 1f else 0f,
        animationSpec = tween(if (isPressed) 800 else 300, easing = LinearEasing),
        label = "fill"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed && !showDelete) 0.96f else 1f,
        animationSpec = tween(150, easing = LinearEasing),
        label = "scale"
    )

    LaunchedEffect(pressProgress) {
        if (pressProgress >= 1f && isPressed) {
            showDelete = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .clip(RoundedCornerShape(20.dp))
            .background(if (colors.isDark) bgColorDark else bgColorLight)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        if (!showDelete) {
                            isPressed = true
                            tryAwaitRelease()
                            isPressed = false
                        }
                    },
                    onTap = {
                        if (!showDelete) onClick()
                    }
                )
            }
    ) {
        // Container for fill to ensure it takes max height
        Box(modifier = Modifier.matchParentSize()) {
            // Smooth Fill Background from left to right
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(pressProgress)
                    .background(if (colors.isDark) fillColorDark else fillColorLight)
            )
        }
        
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (colors.isDark) fillColorDark.copy(alpha = 0.5f) else Color.White.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (colors.isDark) iconColorDark else iconColorLight,
                    modifier = Modifier.size(24.dp)
                )
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (colors.isDark) iconColorDark else iconColorLight
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (showDelete) "Eliminar notificación" else if (pressProgress > 0f) "Mantén presionado para ocultar" else subtitle,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = if (colors.isDark) iconColorDark.copy(alpha = 0.8f) else iconColorLight.copy(alpha = 0.8f)
                )
            }
            if (showDelete) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0xFF991B1B) else Color(0xFFEF4444))
                        .clickable { onDismiss() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Eliminar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) fillColorDark else Color.White.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Ir",
                        tint = if (colors.isDark) iconColorDark else iconColorLight,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun UserAvatar(
    avatarUrl: String,
    onClick: () -> Unit
) {
    val colors = LocalPatientDashboardColors.current
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
                .background(if (colors.isDark) Color(0xFF143731) else Color(0xFFD4F3E5))
                .border(2.dp, if (colors.isDark) colors.primary else Color.White, CircleShape),
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
                .background(colors.primary)
                .border(2.dp, if (colors.isDark) colors.background else Color.White, CircleShape)
                .align(Alignment.BottomEnd)
        )
    }
}

@Composable
private fun SoftNotificationButton(
    hasNotifications: Boolean,
    onClick: () -> Unit
) {
    val colors = LocalPatientDashboardColors.current
    Box(
        modifier = Modifier
            .size(46.dp)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(16.dp),
                ambientColor = if (colors.isDark) colors.primary.copy(alpha = 0.10f) else Color(0xFF8A88A6).copy(alpha = 0.2f),
                spotColor = if (colors.isDark) colors.primary.copy(alpha = 0.10f) else Color(0xFF8A88A6).copy(alpha = 0.2f)
            )
            .background(colors.notificationButton, RoundedCornerShape(16.dp))
            .border(1.dp, if (colors.isDark) colors.border else Color.Transparent, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Rounded.Notifications,
            contentDescription = "Notificaciones",
            tint = colors.primary,
            modifier = Modifier.size(24.dp)
        )
        if (hasNotifications) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (colors.isDark) Color(0xFF68D391) else PatientGreen)
                    .align(Alignment.TopEnd)
                    .offset(x = 1.dp, y = (-1).dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 2. WELLBEING TODAY CARD
// -----------------------------------------------------------------------------
@Composable
private fun WellbeingTodayCard(
    score: Int?,
    category: String?,
    isTestSkipped: Boolean = false
) {
    val colors = LocalPatientDashboardColors.current
    val palette = com.relaxmind.app.ui.themes.getWellnessPalette(score)
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
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
                drawRect(
                    brush = colors.wellbeingBrush
                )

                // Helper to draw a flower
                fun drawFlower(centerX: Float, centerY: Float, flowerSize: Float, alpha: Float) {
                    val petalRadius = flowerSize / 3.2f
                    val centerRadius = flowerSize / 5.5f
                    val baseColor = Color.White.copy(alpha = if (colors.isDark) alpha * 0.16f else alpha)
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
                    // Colored soft center
                    drawCircle(
                        color = (if (score != null) palette.primary else (if (colors.isDark) colors.primary else Color(0xFFFFFBEA))).copy(alpha = if (colors.isDark) alpha * 0.18f else alpha * 1.4f.coerceAtMost(1f)),
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
                        color = Color.White.copy(alpha = if (colors.isDark) alpha * 0.15f else alpha)
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
                            text = stringResource(id = R.string.dashboard_wellbeing_today),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, tint = palette.primary, modifier = Modifier.size(16.dp))
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = stringResource(id = R.string.dashboard_wellbeing_hint),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        lineHeight = 17.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Right Column: Canvas drawn progress circle
                CircularWellbeingProgress(
                    score = score,
                    category = category,
                    isNoData = isTestSkipped && score == null,
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
    isTestSkipped: Boolean = false,
    onStartClick: () -> Unit,
    onProgressClick: () -> Unit
) {
    val colors = LocalPatientDashboardColors.current
    val action = if (completed) onProgressClick else onStartClick
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, colors.checkInBorder, RoundedCornerShape(26.dp))
            .clickable(onClick = action),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = colors.checkInCard)
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
                    .background(if (completed) colors.checkInAccent.copy(alpha = if (colors.isDark) 0.70f else 1f) else colors.checkInAccent.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (completed) RelaxIcons.Check else RelaxIcons.Meditation,
                    contentDescription = null,
                    tint = if (completed) Color.White else colors.checkInAccent,
                    modifier = Modifier.size(23.dp)
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isTestSkipped && !completed) "Test inicial pendiente" else stringResource(id = R.string.dashboard_daily_checkin),
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = colors.textPrimary
                )
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = if (completed) {
                        "Completado hoy${score?.let { " · $it/100" } ?: ""}${category?.let { " · $it" } ?: ""}"
                    } else if (isTestSkipped) {
                        "Completa tu test inicial para personalizar tu experiencia."
                    } else {
                        "Registra cómo te sientes para actualizar tu bienestar."
                    },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    lineHeight = 16.sp,
                    color = colors.textSecondary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                modifier = Modifier
                    .background(if (completed) colors.checkInAccent.copy(alpha = 0.16f) else colors.checkInAccent, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                if (completed) {
                    Icon(
                        imageVector = RelaxIcons.Check,
                        contentDescription = null,
                        tint = colors.checkInAccent,
                        modifier = Modifier.size(16.dp)
                    )
                }
                Text(
                    text = if (completed) stringResource(id = R.string.dashboard_checkin_ready) 
                           else if (isTestSkipped) "Iniciar test inicial"
                           else stringResource(id = R.string.dashboard_checkin_start),
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (completed) colors.checkInAccent else Color.White
                )
            }
        }
    }
}

@Composable
private fun CircularWellbeingProgress(
    score: Int?,
    category: String?,
    isNoData: Boolean = false,
    modifier: Modifier = Modifier
) {
    val colors = LocalPatientDashboardColors.current
    val palette = com.relaxmind.app.ui.themes.getWellnessPalette(score)
    // When test was skipped and no score exists yet, show 0 progress arc (empty)
    val displayScore = if (isNoData) null else (score ?: 74)
    val targetProgress = if (isNoData) 0f else ((displayScore ?: 74) / 100f)
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
                    color = if (isNoData) Color(0xFFDDDDDD).copy(alpha = 0.5f)
                            else palette.ringTrack.copy(alpha = if (colors.isDark) 0.3f else 1f),
                    startAngle = 0f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth),
                    topLeft = topLeft,
                    size = arcSize
                )

                // Filled colored arc (skip drawing if no data)
                if (!isNoData) {
                    drawArc(
                        color = palette.primary,
                        startAngle = -90f,
                        sweepAngle = animatedProgress * 360f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = androidx.compose.ui.graphics.StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (isNoData) {
                    Text(
                        text = "—",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 28.sp,
                        color = colors.textSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Sin datos",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                } else {
                    Row(verticalAlignment = Alignment.Bottom) {
                        Text(
                            text = (displayScore ?: 74).toString(),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 26.sp,
                            color = if (score != null) palette.primary else colors.textPrimary
                        )
                        Text(
                            text = "/100",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (score != null) com.relaxmind.app.ui.themes.getWellnessStatusLabel(score) else "Bueno",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = if (score != null) palette.primary else colors.primary
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// INITIAL TEST DEADLINE BANNER
// -----------------------------------------------------------------------------
@Composable
private fun InitialTestDeadlineBanner(
    state: InitialTestBannerState.SkippedWithin24h,
    onCompleteClick: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "banner-pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "banner-alpha"
    )

    val timeText = when {
        state.hoursLeft > 0 -> "${state.hoursLeft}h ${state.minutesLeft}min"
        else -> "${state.minutesLeft} minutos"
    }

    val colors = LocalPatientDashboardColors.current

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = colors.primary.copy(alpha = 0.1f),
                spotColor = colors.primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = if (colors.isDark) Color(0xFF1E293B) else Color(0xFFF1F5F9))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Clock icon circle
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0xFF334155) else Color(0xFFE2E8F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Filled.DateRange,
                        contentDescription = null,
                        tint = colors.textPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "⏰ Tienes $timeText para tu test inicial",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp,
                        color = colors.textPrimary,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "Después solo podrás hacer check-ins.",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 11.sp,
                        color = colors.textSecondary
                    )
                }

                // CTA button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (colors.isDark) colors.primary else Color(0xFFE0F2FE))
                        .clickable(onClick = onCompleteClick)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Completar",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        color = if (colors.isDark) Color.White else colors.primary
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 3. "PARA TI HOY" SECTION
// -----------------------------------------------------------------------------
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
    val colors = LocalPatientDashboardColors.current
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.dashboard_for_you_today),
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = colors.primary,
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
private fun AnimatedRecommendationBackground(
    modifier: Modifier = Modifier,
    colors: List<Color>,
    accentColor: Color
) {
    val transition = rememberInfiniteTransition(label = "recommendation-bg")
    val drift by transition.animateFloat(
        initialValue = -18f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(2600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recommendation-bg-drift"
    )
    Box(
        modifier = modifier.background(
            brush = Brush.linearGradient(colors = colors)
        )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            drawCircle(
                color = Color.White.copy(alpha = 0.22f),
                radius = size.minDimension * 0.34f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.92f + drift.dp.toPx(), size.height * 0.08f)
            )
            drawCircle(
                color = accentColor.copy(alpha = 0.13f),
                radius = size.minDimension * 0.24f,
                center = androidx.compose.ui.geometry.Offset(size.width * 0.18f - drift.dp.toPx(), size.height * 0.98f)
            )
            drawLine(
                color = Color.White.copy(alpha = 0.32f),
                start = androidx.compose.ui.geometry.Offset(size.width * 0.66f, size.height * 0.18f + drift.dp.toPx() * 0.25f),
                end = androidx.compose.ui.geometry.Offset(size.width * 0.95f, size.height * 0.58f + drift.dp.toPx() * 0.25f),
                strokeWidth = 2.dp.toPx(),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

@Composable
private fun DashboardAssetIcon(
    drawableRes: Int,
    contentDescription: String?,
    backgroundColor: Color,
    modifier: Modifier = Modifier,
    imageSize: androidx.compose.ui.unit.Dp = 34.dp
) {
    Box(
        modifier = modifier
            .size(48.dp)
            .background(backgroundColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = drawableRes),
            contentDescription = contentDescription,
            modifier = Modifier.size(imageSize),
            contentScale = ContentScale.Fit
        )
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
    val colors = LocalPatientDashboardColors.current
    Card(
        modifier = modifier
            .height(160.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = colors.primary.copy(alpha = if (colors.isDark) 0.12f else 0.22f),
                spotColor = colors.primary.copy(alpha = if (colors.isDark) 0.12f else 0.22f)
            )
            .clickable(enabled = !completed, onClick = onStartClick),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedRecommendationBackground(
                modifier = Modifier.fillMaxSize(),
                colors = if (colors.isDark) {
                    listOf(Color(0xFF102C29), Color(0xFF12342F), Color(0xFF143731))
                } else {
                    listOf(Color(0xFFE8F8EF), Color(0xFFC7F3DA), Color(0xFFAEE9CF))
                },
                accentColor = colors.primary
            )
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
                            .background(if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.9f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = RelaxIcons.Meditation,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = stringResource(id = R.string.dashboard_today_goal),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = colors.primary
                    )
                }

                Column {
                    Text(
                        text = title,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = colors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$duration min",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                Row(
                    modifier = Modifier
                        .shadow(2.dp, RoundedCornerShape(50))
                        .background(
                            if (completed) colors.cardSoft.copy(alpha = 0.65f) else colors.primaryStrong,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text(
                        text = if (completed) stringResource(id = R.string.dashboard_completed) else stringResource(id = R.string.dashboard_start),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = if (completed) colors.textSecondary else Color.White
                    )
                    if (!completed) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Completado",
                            tint = colors.textSecondary,
                            modifier = Modifier.size(14.dp)
                        )
                    }
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
    val colors = LocalPatientDashboardColors.current
    Card(
        modifier = modifier
            .height(160.dp)
            .clickable(onClick = onCardClick),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            AnimatedRecommendationBackground(
                modifier = Modifier.fillMaxSize(),
                colors = if (colors.isDark) {
                    listOf(Color(0xFF102C29), Color(0xFF123D3A), Color(0xFF123247))
                } else {
                    listOf(Color(0xFFF0FBF5), Color(0xFFD7F6E6), Color(0xFFBCECD7))
                },
                accentColor = colors.primary
            )
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
                            .background(if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.9f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = RelaxIcons.Calendar,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "Agenda",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = colors.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                if (title != null && time != null) {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text(
                            text = title,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = time,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                } else {
                    Column {
                        Text(
                            text = stringResource(id = R.string.dashboard_free_schedule),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Todo al día hoy",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .width(118.dp)
                        .shadow(2.dp, RoundedCornerShape(50))
                        .background(colors.primaryStrong, RoundedCornerShape(50))
                        .clickable(onClick = onCardClick)
                        .padding(horizontal = 16.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = stringResource(id = R.string.dashboard_review),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = Color.White
                    )
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
// 4. "ACCESOS RAPIDOS" SECTION
// -----------------------------------------------------------------------------
@Composable
private fun QuickAccessSection(
    onSoundsClick: () -> Unit,
    onLibraryClick: () -> Unit
) {
    val activeSounds by SoundPlayerManager.playingSoundIds.collectAsState()
    val isAnySoundPlaying = activeSounds.isNotEmpty()
    val colors = LocalPatientDashboardColors.current

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
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = RelaxIcons.QuickSpark,
                contentDescription = null,
                tint = if (colors.isDark) Color(0xFFB779FF) else Color(0xFF4338A8),
                modifier = Modifier.size(17.dp)
            )
        }

        RelaxCard(
            onClick = onSoundsClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (colors.isDark) darkQuickAccessColor("sounds") else Color(0xFFECFDFB)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(contentAlignment = Alignment.BottomEnd) {
                        DashboardAssetIcon(
                            drawableRes = R.drawable.sonidos,
                            contentDescription = "Sonidos relajantes",
                            backgroundColor = if (colors.isDark) Color(0xFF2B8074) else Color(0xFFB5EBE6),
                            imageSize = 34.dp
                        )
                        if (isAnySoundPlaying) {
                            Box(
                                modifier = Modifier
                                    .size(20.dp)
                                    .background(Color(0xFF68C0B4), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                EqualizerAnimation(
                                    modifier = Modifier.size(width = 12.dp, height = 14.dp),
                                    color = Color.White
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.dashboard_relax_sounds),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Música de fondo para calmarte",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ir a sonidos",
                    tint = colors.textSecondary
                )
            }
        }

        RelaxCard(
            onClick = onLibraryClick,
            modifier = Modifier.fillMaxWidth(),
            containerColor = if (colors.isDark) darkQuickAccessColor("library") else Color(0xFFF3F0FF)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    DashboardAssetIcon(
                        drawableRes = R.drawable.biblioteca,
                        contentDescription = "Biblioteca de apoyo",
                        backgroundColor = if (colors.isDark) Color(0xFF4A3D78) else Color(0xFFDDD6FE),
                        imageSize = 34.dp
                    )
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Text(
                            text = stringResource(id = R.string.dashboard_support_library),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Artículos para entender y manejar tu bienestar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = "Ir a biblioteca",
                    tint = colors.textSecondary
                )
            }
        }
    }
}
@Composable
fun EqualizerAnimation(
    modifier: Modifier = Modifier,
    color: Color = PatientGreen
) {
    val transition = rememberInfiniteTransition(label = "equalizer")
    val h1 by transition.animateFloat(
        initialValue = 4f,
        targetValue = 18f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar1"
    )
    val h2 by transition.animateFloat(
        initialValue = 16f,
        targetValue = 6f,
        animationSpec = infiniteRepeatable(
            animation = tween(350, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar2"
    )
    val h3 by transition.animateFloat(
        initialValue = 8f,
        targetValue = 20f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bar3"
    )

    Canvas(modifier = modifier.size(width = 16.dp, height = 20.dp)) {
        val barWidth = 3.dp.toPx()
        val spacing = 2.dp.toPx()
        val totalHeight = size.height

        // Bar 1
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(0f, totalHeight - h1.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(barWidth, h1.dp.toPx())
        )
        // Bar 2
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset(barWidth + spacing, totalHeight - h2.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(barWidth, h2.dp.toPx())
        )
        // Bar 3
        drawRect(
            color = color,
            topLeft = androidx.compose.ui.geometry.Offset((barWidth + spacing) * 2f, totalHeight - h3.dp.toPx()),
            size = androidx.compose.ui.geometry.Size(barWidth, h3.dp.toPx())
        )
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
    val colors = LocalPatientDashboardColors.current
    Card(
        modifier = modifier
            .height(140.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (colors.isDark) colors.card else backgroundColor)
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
                    .background(if (colors.isDark) Color.White.copy(alpha = 0.08f) else Color.White.copy(alpha = 0.65f), CircleShape),
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
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 11.sp,
                lineHeight = 13.sp,
                color = colors.textSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 5. "MI DIARIO" CARD
// -----------------------------------------------------------------------------
@Composable
private fun DiaryCard(
    onDiaryClick: () -> Unit
) {
    val colors = LocalPatientDashboardColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onDiaryClick),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (colors.isDark) darkQuickAccessColor("diary") else Color(0xFFFFF3E7))
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
                DashboardAssetIcon(
                    drawableRes = R.drawable.diario,
                    contentDescription = "Mi Diario",
                    backgroundColor = if (colors.isDark) Color(0xFF7A4A20) else Color(0xFFFED7AA),
                    imageSize = 34.dp
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.dashboard_diary),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.dashboard_diary_desc),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 6. CAREGIVER LINKING CARD
// -----------------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CaregiverCard(
    caregiverId: String?,
    caregiverName: String?,
    caregiverAvatar: String,
    caregiver: com.relaxmind.app.data.model.Caregiver?,
    isCaregiverLoading: Boolean,
    onLinkClick: () -> Unit
) {
    var showModal by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val colors = LocalPatientDashboardColors.current
    val displayName = caregiverName ?: when {
        caregiverId == null -> "Cuidador"
        isCaregiverLoading -> "Cargando datos del cuidador..."
        else -> "Datos del cuidador no disponibles"
    }

    // Modal de contacto del cuidador
    if (showModal && caregiver != null) {
        val caregiverFullName = "${caregiver.name} ${caregiver.lastName}".trim().ifBlank { "Cuidador" }
        val caregiverCanCall = caregiver.phone.isNotBlank()
        val caregiverDialogPurple = Color(0xFF4338A8)
        val caregiverDialogLavender = Color(0xFFF7F4FF)
        val caregiverDialogPill = Color(0xFFEDE9FE)
        val caregiverDialogMuted = Color(0xFF8B86A8)
        AlertDialog(
            onDismissRequest = { showModal = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (caregiverCanCall) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:${caregiver.phone}")
                            }
                            context.startActivity(intent)
                        } else {
                            Toast.makeText(context, "El cuidador no tiene un número registrado", Toast.LENGTH_SHORT).show()
                        }
                    },
                    enabled = caregiverCanCall,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = caregiverDialogPurple,
                        disabledContainerColor = caregiverDialogPill,
                        disabledContentColor = caregiverDialogMuted
                    ),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Phone,
                        contentDescription = null,
                        tint = if (caregiverCanCall) Color.White else caregiverDialogMuted,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.dashboard_call),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = if (caregiverCanCall) Color.White else caregiverDialogMuted
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showModal = false }) {
                    Text("Cerrar", fontFamily = LexendFontFamily, color = caregiverDialogPurple)
                }
            },
            shape = RoundedCornerShape(28.dp),
            containerColor = caregiverDialogLavender,
            title = null,
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(caregiverDialogLavender)
                        .padding(top = 4.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Avatar
                    if (caregiver.avatarUrl.startsWith("relaxmind://avatar/")) {
                        Image(
                            painter = painterResource(id = getAvatarDrawableRes(caregiver.avatarUrl)),
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(2.dp, caregiverDialogPurple.copy(alpha = 0.25f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = caregiver.avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=${Uri.encode(caregiverFullName)}&background=4338A8&color=fff" },
                            contentDescription = "Avatar",
                            modifier = Modifier
                                .size(76.dp)
                                .clip(CircleShape)
                                .border(2.dp, caregiverDialogPurple.copy(alpha = 0.25f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = caregiverFullName,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color(0xFF1A1A2E)
                    )
                    Box(
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .clip(RoundedCornerShape(50))
                            .background(caregiverDialogPill)
                            .padding(horizontal = 12.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = stringResource(id = R.string.dashboard_caregiver),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = caregiverDialogPurple
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
                    if (infoItems.isEmpty()) {
                        Text(
                            text = "Aún no hay información de contacto registrada.",
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )
                    } else {
                        infoItems.forEachIndexed { i, (icon, label) ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(14.dp))
                                    .background(Color.White)
                                    .border(1.dp, Color(0xFFE5E0F7), RoundedCornerShape(14.dp))
                                    .padding(horizontal = 16.dp, vertical = 12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Icon(imageVector = icon, contentDescription = null, tint = caregiverDialogPurple, modifier = Modifier.size(20.dp))
                                Text(text = label, fontFamily = LexendFontFamily, fontSize = 14.sp, color = Color(0xFF2C3E50))
                            }
                            if (i < infoItems.lastIndex) Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        )
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (colors.isDark) darkQuickAccessColor("caregiver") else Color(0xFFF3E8FF))
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
                    DashboardAssetIcon(
                        drawableRes = R.drawable.cuidador,
                        contentDescription = "Mi Cuidador",
                        backgroundColor = if (colors.isDark) Color(0xFF574089) else Color(0xFFE9D5FF),
                        imageSize = 34.dp
                    )
                    Column {
                        Text(
                            text = stringResource(id = R.string.dashboard_caregiver),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "¿Tienes un cuidador vinculado?",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = colors.textSecondary
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .shadow(1.dp, RoundedCornerShape(50))
                        .background(if (colors.isDark) Color(0xFF7C5CFF) else Color(0xFF4338A8), RoundedCornerShape(50))
                        .clickable(onClick = onLinkClick)
                        .padding(horizontal = 14.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(id = R.string.dashboard_caregiver_link),
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
                    .clickable {
                        if (caregiver != null) {
                            showModal = true
                        } else {
                            val message = if (isCaregiverLoading) {
                                "Cargando datos del cuidador"
                            } else {
                                "No se pudieron cargar los datos del cuidador"
                            }
                            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                        }
                    }
                    .padding(18.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    if (caregiverAvatar.startsWith("relaxmind://avatar/")) {
                        Image(
                            painter = painterResource(id = getAvatarDrawableRes(caregiverAvatar)),
                            contentDescription = "Caregiver Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, if (colors.isDark) Color(0xFFB779FF).copy(alpha = 0.45f) else Color(0xFF4338A8).copy(alpha = 0.3f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        AsyncImage(
                            model = caregiverAvatar.ifBlank { "https://ui-avatars.com/api/?name=${Uri.encode(displayName)}&background=4338A8&color=fff" },
                            contentDescription = "Caregiver Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(1.5.dp, if (colors.isDark) Color(0xFFB779FF).copy(alpha = 0.45f) else Color(0xFF4338A8).copy(alpha = 0.3f), CircleShape),
                            contentScale = ContentScale.Crop
                        )
                    }
                    Column {
                        Text(
                            text = stringResource(id = R.string.dashboard_caregiver),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colors.textPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = displayName,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (colors.isDark) Color(0xFFC4B5FD) else Color(0xFF4338A8)
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
                            .background((if (colors.isDark) Color(0xFF7C5CFF) else Color(0xFF4338A8)).copy(alpha = 0.16f))
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .background(if (colors.isDark) Color(0xFFC4B5FD) else Color(0xFF4338A8), CircleShape)
                            )
                            Text(
                                text = stringResource(id = R.string.dashboard_caregiver_linked),
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = if (colors.isDark) Color(0xFFC4B5FD) else Color(0xFF4338A8)
                            )
                        }
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = colors.textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 7. NEARBY HEALTH CARD
// -----------------------------------------------------------------------------
@Composable
private fun NearbyHealthCard(
    onNearbyClick: () -> Unit
) {
    val colors = LocalPatientDashboardColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onNearbyClick),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (colors.isDark) darkQuickAccessColor("health") else Color(0xFFF6FCE5))
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
                DashboardAssetIcon(
                    drawableRes = R.drawable.centros_cercanos,
                    contentDescription = "Centros de Salud Cercanos",
                    backgroundColor = if (colors.isDark) Color(0xFF347A45) else Color(0xFFE3F4B3),
                    imageSize = 34.dp
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.dashboard_health_centers),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.dashboard_health_centers_desc),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
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
    val secondPulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.4f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sos-second-scale-anim"
    )
    val secondPulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.32f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, delayMillis = 500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sos-second-alpha-anim"
    )

    Box(
        modifier = modifier.size(76.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer pulsing ring
        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(pulseScale)
                .background(Color(0xFFFF5E5E).copy(alpha = pulseAlpha), CircleShape)
        )

        Box(
            modifier = Modifier
                .size(76.dp)
                .scale(secondPulseScale)
                .background(Color(0xFFFF5E5E).copy(alpha = secondPulseAlpha), CircleShape)
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
                modifier = Modifier.requiredSize(92.dp), // requiredSize ensures it draws outside without resizing the parent Box
                color = Color(0xFFFF5722), // Deep Orange / more red than amber
                trackColor = Color(0xFFC53030).copy(alpha = 0.3f), // Soft dark red background track
                strokeWidth = 6.dp,
                strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
            )
        }
    }
}

// -----------------------------------------------------------------------------
// 8. "LUMI AI ASSISTANT" CARD
// -----------------------------------------------------------------------------
@Composable
private fun LumiCard(
    onLumiClick: () -> Unit
) {
    val colors = LocalPatientDashboardColors.current
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onLumiClick),
        shape = RoundedCornerShape(26.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = if (colors.isDark) darkQuickAccessColor("lumi") else Color(0xFFE0F2FE))
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
                DashboardAssetIcon(
                    drawableRes = R.drawable.lumi,
                    contentDescription = "Lumi",
                    backgroundColor = if (colors.isDark) Color(0xFF2E7290) else Color(0xFFBAE6FD),
                    imageSize = 34.dp
                )
                Column {
                    Text(
                        text = stringResource(id = R.string.dashboard_lumi),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = colors.textPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = stringResource(id = R.string.dashboard_lumi_desc),
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = colors.textSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = colors.textSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
