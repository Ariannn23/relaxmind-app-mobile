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
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.getAvatarDrawableRes
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.relaxmind.app.ui.components.rememberRelaxToastState
import com.relaxmind.app.ui.themes.*

// ── Caregiver-specific palette ────────────────────────────────────────────────
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

// ─────────────────────────────────────────────────────────────────────────────
// MAIN SCREEN
// ─────────────────────────────────────────────────────────────────────────────
@Composable
fun DashboardCaregiverScreen(
    viewModel: CaregiverViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    onScanQr: () -> Unit,
    onPatientClick: (String) -> Unit,
    onAlertsClick: () -> Unit
) {
    val caregiver by viewModel.caregiver.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val alerts by viewModel.activeAlerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val toastState = rememberRelaxToastState()

    LaunchedEffect(Unit) { viewModel.loadDashboard() }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            toastState.showError(error.orEmpty())
            viewModel.consumeError()
        }
    }

    if (isLoading && caregiver == null) {
        FullScreenLoadingScreen(text = "Cargando dashboard...")
        return
    }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F8FD))) {
        // Soft gradient background at top
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(CaregiverLavender, Color(0xFFF8F8FD))
                    )
                )
        )

        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                RelaxBottomNav(
                    selectedRoute = Screen.CaregiverDashboard.route,
                    onNavigate = onNavigate,
                    role = AppRole.CAREGIVER
                )
            },
            floatingActionButton = {
                CaregiverFAB(onScanQr = onScanQr)
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
                        name = caregiver?.name.orEmpty(),
                        avatarUrl = caregiver?.avatarUrl.orEmpty()
                    )

                    // 2. Active Alerts Card
                    ActiveAlertsCard(
                        alerts = alerts,
                        onAlertsClick = onAlertsClick,
                        onAlertClick = { alertId ->
                            onNavigate(Screen.SOSAlert.createRoute(alertId))
                        }
                    )

                    // 3. Patients Card
                    PatientsCard(
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
}

// ── FAB ───────────────────────────────────────────────────────────────────────
@Composable
private fun CaregiverFAB(onScanQr: () -> Unit) {
    Box(
        modifier = Modifier
            .size(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = CaregiverPurple.copy(alpha = 0.4f),
                spotColor = CaregiverPurple.copy(alpha = 0.5f)
            )
            .clip(CircleShape)
            .background(Brush.linearGradient(listOf(CaregiverPurple, CaregiverIndigoDark)))
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

// ── Header ────────────────────────────────────────────────────────────────────
@Composable
private fun DashboardHeader(name: String, avatarUrl: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Hola, ${name.ifBlank { "María" }} 👋",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Aquí tienes un resumen del bienestar\nde tus pacientes.",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondary,
                lineHeight = 20.sp
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Caregiver avatar
        Box(
            modifier = Modifier
                .size(72.dp)
                .shadow(8.dp, CircleShape, ambientColor = CaregiverIndigoDark.copy(alpha = 0.15f))
                .clip(CircleShape)
                .border(3.dp, CaregiverLavender, CircleShape)
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

// ── Active Alerts Card ────────────────────────────────────────────────────────
@Composable
private fun ActiveAlertsCard(
    alerts: List<CaregiverAlert>,
    onAlertsClick: () -> Unit,
    onAlertClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = if (alerts.isEmpty()) Color(0xFFF6FFF8) else AlertRedSoft,
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
                        .background(if (alerts.isEmpty()) ScoreGreenSoft else Color(0xFFFFE4E4)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = if (alerts.isEmpty()) ScoreGreen else AlertRed,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "ALERTAS ACTIVAS",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = if (alerts.isEmpty()) ScoreGreen else AlertRed,
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
                            color = ScoreGreen
                        )
                        Text(
                            text = "No hay alertas pendientes de revisión.",
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
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
                    AlertRow(alert = alert, onClick = { onAlertClick(alert.id) })
                }
            }
        }
    }
}

@Composable
private fun AlertRow(alert: CaregiverAlert, onClick: () -> Unit) {
    val isSOS = alert.severity.lowercase() in listOf("high", "sos")
    val iconBg = if (isSOS) AlertRed else AlertOrange
    val iconContent: @Composable () -> Unit = if (isSOS) {
        {
            Text(
                text = "SOS",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 9.sp,
                color = Color.White
            )
        }
    } else {
        {
            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(20.dp)
            )
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
                    color = TextPrimary
                )
                if (alert.createdAtText.isNotBlank()) {
                    Text(
                        text = " · ${alert.createdAtText}",
                        fontFamily = LexendFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            Text(
                text = alert.title.ifBlank { alert.message.ifBlank { "Alerta pendiente" } },
                fontFamily = LexendFontFamily,
                fontSize = 12.sp,
                color = TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        // Ver button
        Box(
            modifier = Modifier
                .shadow(4.dp, RoundedCornerShape(12.dp), ambientColor = CaregiverIndigoDark.copy(alpha = 0.2f))
                .clip(RoundedCornerShape(12.dp))
                .background(Brush.linearGradient(listOf(CaregiverPurple, CaregiverIndigoDark)))
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

// ── Patients Card ─────────────────────────────────────────────────────────────
@Composable
private fun PatientsCard(
    patients: List<CaregiverPatientSummary>,
    onViewAll: () -> Unit,
    onPatientClick: (String) -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(30.dp),
        color = Color.White,
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
                        .background(CaregiverLavender),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Group,
                        contentDescription = null,
                        tint = CaregiverIndigoDark,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Text(
                    text = "MIS PACIENTES",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = CaregiverIndigoDark,
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
                    color = TextPrimary
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
                            color = CaregiverIndigoDark
                        )
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = null,
                            tint = CaregiverIndigoDark,
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
                        .background(CaregiverLavender)
                        .padding(20.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.PersonAdd,
                            contentDescription = null,
                            tint = CaregiverIndigoDark,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Sin pacientes vinculados",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = CaregiverIndigoDark,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Usa el botón + para escanear el código de un paciente",
                            fontFamily = LexendFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary,
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
    summary: CaregiverPatientSummary,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val patient = summary.patient
    val score = summary.latestScore
    val borderColor = when {
        score == null -> Color(0xFFCBD5E0)
        score < 40 -> AlertRed
        score < 70 -> AlertOrange
        else -> ScoreGreen
    }
    val chipBg = when {
        score == null -> Color(0xFFF0F0F0)
        score < 40 -> ScoreRedSoft
        score < 70 -> ScoreOrangeSoft
        else -> ScoreGreenSoft
    }
    val chipText = when {
        score == null -> "Sin datos"
        score < 40 -> "Bajo $score/100"
        score < 70 -> "Moderado $score/100"
        else -> "Bueno $score/100"
    }
    val chipColor = when {
        score == null -> TextSecondary
        score < 40 -> AlertRed
        score < 70 -> AlertOrange
        else -> ScoreGreen
    }

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
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )

        // Condition
        Text(
            text = patient.condition.ifBlank { "—" },
            fontFamily = LexendFontFamily,
            fontSize = 10.sp,
            color = TextSecondary,
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
