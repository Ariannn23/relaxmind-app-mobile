package com.relaxmind.app.features.caregiver

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.Screen
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.FullScreenLoadingScreen
import com.relaxmind.app.ui.components.NotificationPermissionDialog
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.getAvatarDrawableRes
import com.relaxmind.app.ui.components.hasNotificationPermission
import com.relaxmind.app.ui.components.openNotificationSettings
import com.relaxmind.app.ui.components.rememberNotificationPermissionLauncher
import com.relaxmind.app.ui.components.rememberNotificationPermissionStatus
import com.relaxmind.app.ui.components.requestNotificationPermission
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.relaxmind.app.ui.components.rememberRelaxToastState
import com.relaxmind.app.ui.components.CaregiverDashboardSkeleton
import com.relaxmind.app.ui.components.ErrorStateScreen
import com.relaxmind.app.ui.themes.*

//  Caregiver-specific palette 
private val CaregiverPurple = Color(0xFF4F2DE8)
private val CaregiverIndigoDark = Color(0xFF4338A8)
private val CaregiverLavender = Color(0xFFF1EDFF)
private val AlertRed = Color(0xFFE8582A)
private val AlertRedSoft = Color(0xFFFFF1F1)
private val AlertOrange = Color(0xFFF59E0B)
private val AlertOrangeSoft = Color(0xFFFFFBEB)
private val ScoreGreen = Color(0xFF0F6E56)
private val ScoreGreenSoft = Color(0xFFECFDF5)
private val ScoreOrangeSoft = Color(0xFFFFF7E6)
private val ScoreRedSoft = Color(0xFFFFF0EE)

// 
// MAIN SCREEN
// 

data class CaregiverDashboardColors(
    val background: Brush,
    val surface: Color,
    val card: Color,
    val primary: Brush,
    val primaryLight: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textMuted: Color,
    val border: Color,
    val isDark: Boolean
)

@Composable
fun caregiverDashboardColors(isDark: Boolean): CaregiverDashboardColors {
    return if (isDark) {
        CaregiverDashboardColors(
            background = Brush.verticalGradient(
                colors = listOf(Color(0xFF080A1A), Color(0xFF0D1024), Color(0xFF050711))
            ),
            surface = Color(0xFF101329),
            card = Color(0xFF151936),
            primary = Brush.verticalGradient(listOf(Color(0xFF7C3AED), Color(0xFF4F2DE8))),
            primaryLight = Color(0xFF8B5CF6),
            textPrimary = Color(0xFFF7F4FF),
            textSecondary = Color(0xFFC7BEDF),
            textMuted = Color(0xFF8F87A8),
            border = Color(0xFF322B55),
            isDark = true
        )
    } else {
        CaregiverDashboardColors(
            background = Brush.verticalGradient(
                colors = listOf(CaregiverLavender, Color(0xFFF8F8FD))
            ),
            surface = Color(0xFFFFFFFF),
            card = Color(0xFFFFFFFF),
            primary = Brush.verticalGradient(listOf(CaregiverPurple, CaregiverIndigoDark)),
            primaryLight = CaregiverPurple,
            textPrimary = TextPrimary,
            textSecondary = TextSecondary,
            textMuted = Color(0xFF8FA89B),
            border = Color(0xFFE6E8EF),
            isDark = false
        )
    }
}

@Composable
fun DashboardCaregiverScreen(
    viewModel: CaregiverViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    onScanQr: () -> Unit,
    onPatientClick: (String) -> Unit,
    onAlertsClick: () -> Unit,
    showBottomNav: Boolean = true
) {
    val caregiver by viewModel.caregiver.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val alerts by viewModel.activeAlerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val toastState = rememberRelaxToastState()
    val context = LocalContext.current
    val notificationsPermissionGranted = rememberNotificationPermissionStatus()
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var notificationPromptShown by remember { mutableStateOf(false) }
    val notificationPermissionLauncher = rememberNotificationPermissionLauncher { granted ->
        viewModel.updateNotificationsEnabled(granted)
        showNotificationPermissionDialog = !granted
    }
    
    val isDarkCaregiverDashboard = true
    val colors = caregiverDashboardColors(isDark = isDarkCaregiverDashboard)

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            toastState.showError(error.orEmpty())
            viewModel.consumeError()
        }
    }

    LaunchedEffect(caregiver?.id, caregiver?.notificationsEnabled, notificationsPermissionGranted) {
        val currentCaregiver = caregiver ?: return@LaunchedEffect
        if (!notificationPromptShown && (!currentCaregiver.notificationsEnabled || !notificationsPermissionGranted)) {
            notificationPromptShown = true
            showNotificationPermissionDialog = true
        }
    }

    if (isLoading && caregiver == null && error == null) {
        FullScreenLoadingScreen(text = "Cargando...", isCaregiver = true)
        return
    } else if (error != null && caregiver == null) {
        Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
            ErrorStateScreen(
                message = error ?: "",
                onRetry = { viewModel.loadDashboard() }
            )
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(colors.background)) {
        // Soft gradient background is handled by colors.background Brush above
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
        )

        var showLimitDialog by remember { mutableStateOf(false) }

        if (showLimitDialog) {
            AlertDialog(
                onDismissRequest = { showLimitDialog = false },
                title = { Text("Lmite alcanzado", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold, color = colors.textPrimary) },
                text = { Text("Has alcanzado el lmite mximo de 5 pacientes vinculados.", fontFamily = LexendFontFamily, color = colors.textSecondary) },
                confirmButton = { TextButton(onClick = { showLimitDialog = false }) { Text("Entendido", fontFamily = LexendFontFamily, color = colors.primaryLight, fontWeight = FontWeight.Bold) } },
                containerColor = colors.surface
            )
        }

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                if (showBottomNav) {
                    RelaxBottomNav(
                        selectedRoute = Screen.CaregiverDashboard.route,
                        onNavigate = onNavigate,
                        role = AppRole.CAREGIVER,
                        darkMode = colors.isDark
                    )
                }
            },
            floatingActionButton = {
                CaregiverFAB(colors = colors, onScanQr = {
                    if (patients.size >= 5) {
                        showLimitDialog = true
                    } else {
                        onScanQr()
                    }
                })
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp)
                        .padding(top = 20.dp, bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // 1. Header
                    DashboardHeader(
                        colors = colors,
                        name = caregiver?.name.orEmpty(),
                        avatarUrl = caregiver?.avatarUrl.orEmpty()
                    )

                    // 2. Active Alerts Card
                    ActiveAlertsCard(
                        colors = colors,
                        alerts = alerts,
                        onAlertsClick = onAlertsClick,
                        onAlertClick = { alert ->
                            if (alert.type.equals("sos", ignoreCase = true) || alert.severity.equals("high", ignoreCase = true)) {
                                onNavigate(Screen.SOSAlert.createRoute(alert.id))
                            } else {
                                viewModel.markAlertResolved(alert.id)
                                onNavigate(Screen.PatientDetail.createRoute(alert.patientId))
                            }
                        }
                    )

                    // 3. Patients Card
                    PatientsCard(
                        colors = colors,
                        patients = patients,
                        onViewAll = { onNavigate(Screen.PatientsList.route) },
                        onPatientClick = onPatientClick
                    )

                    Spacer(modifier = Modifier.height(80.dp))
                }

                // Toast overlay
                RelaxToastHost(state = toastState)
            }
        }
    }

    if (showNotificationPermissionDialog) {
        NotificationPermissionDialog(
            role = AppRole.CAREGIVER,
            title = "Activa tus notificaciones",
            message = "Necesitas notificaciones para recibir alertas SOS y avisos de bienestar de tus pacientes en tiempo real.",
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

//  FAB 
@Composable
private fun CaregiverFAB(colors: CaregiverDashboardColors, onScanQr: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = if (colors.isDark) Color(0xFF7C3AED).copy(alpha = 0.35f) else CaregiverPurple.copy(alpha = 0.4f),
                spotColor = if (colors.isDark) Color(0xFF7C3AED).copy(alpha = 0.35f) else CaregiverPurple.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(colors.primary)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onScanQr
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Default.Add,
            contentDescription = "Vincular paciente",
            tint = Color.White,
            modifier = Modifier.size(30.dp)
        )
    }
}

//  Header 
@Composable
private fun DashboardHeader(colors: CaregiverDashboardColors, name: String, avatarUrl: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola, ${name.ifBlank { "Mara" }} ",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = colors.textPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Aqu tienes un resumen del bienestar\nde tus pacientes.",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = colors.textSecondary,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Caregiver avatar
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape, ambientColor = if (colors.isDark) Color(0xFF8B5CF6).copy(alpha = 0.25f) else CaregiverIndigoDark.copy(alpha = 0.15f))
                .clip(CircleShape)
                .border(3.dp, if (colors.isDark) Color(0xFF8B5CF6) else CaregiverLavender, CircleShape)
        ) {
            val isCustomAvatar = avatarUrl.startsWith("relaxmind://avatar/")
            if (isCustomAvatar) {
                Image(
                    painter = painterResource(id = getAvatarDrawableRes(avatarUrl)),
                    contentDescription = "Avatar cuidadora",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize().background(Color(0xFFF3F4F6))
                )
            } else {
                AsyncImage(
                    model = avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=M&background=4338A8&color=fff&size=144" },
                    contentDescription = "Avatar cuidadora",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

//  Active Alerts Card 
@Composable
private fun ActiveAlertsCard(
    colors: CaregiverDashboardColors,
    alerts: List<CaregiverAlert>,
    onAlertsClick: () -> Unit,
    onAlertClick: (CaregiverAlert) -> Unit
) {
    val hasAlerts = alerts.isNotEmpty()
    val cardBg = if (colors.isDark) {
        if (hasAlerts) Color(0xFF3B1D24) else Color(0xFF10251F)
    } else {
        if (hasAlerts) AlertRedSoft else ScoreGreenSoft
    }
    val cardBorder = if (colors.isDark) {
        if (hasAlerts) Color(0xFF7C2E39) else Color(0xFF255C45)
    } else {
        if (hasAlerts) Color(0xFFFFD4D4) else Color(0xFFC6F6D5)
    }
    val iconColor = if (colors.isDark) {
        if (hasAlerts) Color(0xFFFF6B5F) else Color(0xFF7BE0A5)
    } else {
        if (hasAlerts) AlertRed else ScoreGreen
    }
    val iconBg = if (colors.isDark) {
        if (hasAlerts) Color(0xFF5A252C) else Color(0xFF163D2E)
    } else {
        if (hasAlerts) Color.White else Color.White
    }

    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, cardBorder, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        color = cardBg,
        shadowElevation = 8.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(iconBg),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "ALERTAS ACTIVAS",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = iconColor,
                    letterSpacing = 0.8.sp
                )
                Spacer(modifier = Modifier.weight(1f))
                if (alerts.isNotEmpty()) {
                    Text(
                        text = "Ver todas",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = CaregiverIndigoDark,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onAlertsClick
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (alerts.isEmpty()) {
                // Empty state
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ScoreGreen,
                        modifier = Modifier.size(24.dp)
                    )
                    Column {
                        Text(
                            text = "Todo tranquilo hoy",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "No hay alertas pendientes de revisin.",
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )
                    }
                }
            } else {
                // Alert rows
                alerts.take(3).forEachIndexed { index, alert ->
                    if (index > 0) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 12.dp),
                            thickness = 1.dp,
                            color = BorderSoft
                        )
                    }
                    AlertRow(colors = colors, alert = alert, onClick = { onAlertClick(alert) })
                }
            }
        }
    }
}

@Composable
private fun AlertRow(colors: CaregiverDashboardColors, alert: CaregiverAlert, onClick: () -> Unit) {
    val isSOS = alert.severity.lowercase() in listOf("high", "sos")
    val isUnlink = alert.type.equals("UNLINK", ignoreCase = true)
    
    val iconBg = when {
        isSOS -> AlertRed
        isUnlink -> Color(0xFF6B7280) // Gray
        else -> AlertOrange
    }
    
    val iconContent: @Composable () -> Unit = when {
        isSOS -> {
            {
                Text(
                    text = "SOS",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 9.sp,
                    color = Color.White
                )
            }
        }
        isUnlink -> {
            {
                Icon(
                    imageVector = Icons.Default.PersonOff,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
        else -> {
            {
                Icon(
                    imageVector = Icons.Default.BarChart,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Severity icon
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(CircleShape)
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            iconContent()
        }

        // Info
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = alert.patientName.ifBlank { "Paciente" },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = colors.textPrimary
                )
                if (alert.createdAtText.isNotBlank()) {
                    Text(
                        text = "  ${alert.createdAtText}",
                        fontFamily = LexendFontFamily,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }
            }
            Text(
                text = alert.title.ifBlank { alert.message.ifBlank { "Alerta pendiente" } },
                fontFamily = LexendFontFamily,
                fontSize = 12.sp,
                color = colors.textSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Ver button
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = if (colors.isDark) Color(0xFF8B5CF6).copy(alpha = 0.25f) else CaregiverIndigoDark.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(12.dp))
                .background(colors.primary)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                    onClick = onClick
                )
                .padding(horizontal = 18.dp, vertical = 9.dp)
        ) {
            Text(
                text = "Ver",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = Color.White
            )
        }
    }
}

//  Patients Card 
@Composable
private fun PatientsCard(
    colors: CaregiverDashboardColors,
    patients: List<CaregiverPatientSummary>,
    onViewAll: () -> Unit,
    onPatientClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth().border(1.dp, colors.border, RoundedCornerShape(30.dp)),
        shape = RoundedCornerShape(30.dp),
        color = colors.card,
        shadowElevation = 10.dp,
        tonalElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // Section header
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(if (colors.isDark) Color(0xFF2B2448) else CaregiverLavender),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = if (colors.isDark) Color(0xFFA78BFA) else CaregiverIndigoDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "MIS PACIENTES",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = if (colors.isDark) Color(0xFFA78BFA) else CaregiverIndigoDark,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Count row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Mis pacientes (${patients.size})",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = colors.textPrimary
                )
                if (patients.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onViewAll
                        )
                    ) {
                        Text(
                            text = "Ver todos",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = if (colors.isDark) Color(0xFFA78BFA) else CaregiverIndigoDark
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = if (colors.isDark) Color(0xFFA78BFA) else CaregiverIndigoDark,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (patients.isEmpty()) {
                // Empty state
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (colors.isDark) Color(0xFF2B2448) else CaregiverLavender)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = if (colors.isDark) Color(0xFFA78BFA) else CaregiverIndigoDark,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin pacientes vinculados",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = if (colors.isDark) Color(0xFFA78BFA) else CaregiverIndigoDark,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Usa el botn + para escanear el cdigo de un paciente",
                            fontFamily = LexendFontFamily,
                            fontSize = 12.sp,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            } else {
                // Patients row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    patients.take(3).forEach { summary ->
                        PatientCard(
                            colors = colors,
                            summary = summary,
                            onClick = { onPatientClick(summary.patient.id) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                    // Fill empty slots if less than 3
                    repeat((3 - patients.size.coerceAtMost(3)).coerceAtLeast(0)) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun PatientCard(
    colors: CaregiverDashboardColors,
    summary: CaregiverPatientSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val patient = summary.patient
    val score = summary.latestScore
    val palette = com.relaxmind.app.ui.themes.getWellnessPalette(score)
    val statusLabel = com.relaxmind.app.ui.themes.getWellnessStatusLabel(score)

    val borderColor = if (score == null) {
        if (colors.isDark) Color(0xFF636174) else Color(0xFFCBD5E0)
    } else palette.primary

    val chipBg = if (score == null) {
        if (colors.isDark) Color(0xFF24263A) else Color(0xFFF0F0F0)
    } else palette.softBackground

    val chipText = if (score == null) {
        "Sin datos"
    } else {
        "$statusLabel $score/100"
    }

    val chipColor = if (score == null) {
        if (colors.isDark) Color(0xFFB7B5C8) else colors.textSecondary
    } else palette.primary

    val chipBorderColor = if (score == null) {
        if (colors.isDark) Color(0xFF4A4D63) else Color.Transparent
    } else palette.ringTrack

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onClick
            )
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Avatar with colored border + status dot
        Box(modifier = Modifier.size(72.dp)) {
            val isCustomAvatar = patient.avatarUrl.startsWith("relaxmind://avatar/")
            if (isCustomAvatar) {
                Image(
                    painter = painterResource(id = getAvatarDrawableRes(patient.avatarUrl)),
                    contentDescription = "Avatar ${patient.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .border(3.dp, borderColor, CircleShape)
                        .padding(3.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF3F4F6))
                )
            } else {
                AsyncImage(
                    model = patient.avatarUrl.ifBlank {
                        "https://ui-avatars.com/api/?name=${patient.name.take(2)}&background=4338A8&color=fff&size=144"
                    },
                    contentDescription = "Avatar ${patient.name}",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(70.dp)
                        .border(3.dp, borderColor, CircleShape)
                        .padding(3.dp)
                        .clip(CircleShape)
                )
            }
            // Status indicator dot
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .align(Alignment.BottomEnd)
                    .offset((-2).dp, (-2).dp)
                    .border(2.dp, Color.White, CircleShape)
                    .clip(CircleShape)
                    .background(borderColor)
            )
        }

        // Name
        Text(
            text = patient.name.ifBlank { "Paciente" },
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = colors.textPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Condition
        Text(
            text = patient.condition.ifBlank { "" },
            fontFamily = LexendFontFamily,
            fontSize = 10.sp,
            color = colors.textSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            lineHeight = 13.sp
        )

        // Score chip
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50.dp))
                .background(chipBg)
                .border(1.dp, chipBorderColor, RoundedCornerShape(50.dp))
                .padding(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Text(
                text = chipText,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 10.sp,
                color = chipColor,
                maxLines = 1
            )
        }
    }
}

