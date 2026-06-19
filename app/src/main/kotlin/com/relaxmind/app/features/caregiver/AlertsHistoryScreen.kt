package com.relaxmind.app.features.caregiver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.Screen
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.rememberRelaxToastState
import com.relaxmind.app.ui.themes.BorderSoft
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.SoftLavender
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

private val CaregiverPurple = Color(0xFF4F2DE8)
private val LavenderPill = Color(0xFFEDE9FE)
private val AlertRed = Color(0xFFE8582A)
private val AlertRedSoft = Color(0xFFFFEEEE)
private val WarningOrange = Color(0xFFF59E0B)
private val WarningOrangeSoft = Color(0xFFFFF4E5)
private val NeutralPurpleGray = Color(0xFF7A7895)
private val NeutralSoft = Color(0xFFF4F3FA)
private val BackgroundWhite = Color(0xFFFFFFFF)
private val SoftScreenBg = Color(0xFFF8F8FD)

private enum class AlertFilter(val label: String) {
    ALL("Todos"),
    SOS("SOS"),
    LOW_CHECKIN("Check-in bajo"),
    MISSED_CHECKIN("Sin check-in")
}

private enum class AlertVisualType {
    SOS,
    LOW_CHECKIN,
    NO_CHECKIN
}

@Composable
fun AlertsHistoryScreen(
    viewModel: CaregiverViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val alerts by viewModel.allAlerts.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val error by viewModel.error.collectAsState()
    val toastState = rememberRelaxToastState()

    var selectedFilter by remember { mutableStateOf(AlertFilter.ALL) }
    var selectedPatientId by remember { mutableStateOf<String?>(null) }
    var alertToResolve by remember { mutableStateOf<CaregiverAlert?>(null) }
    var patientMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.observeCaregiverData()
    }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            toastState.showSuccess(message.orEmpty())
            viewModel.consumeMessage()
        }
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            toastState.showError(error.orEmpty())
            viewModel.consumeError()
        }
    }

    val hasAnyAlert = alerts.isNotEmpty()
    val selectedPatientName = patients
        .firstOrNull { it.patient.id == selectedPatientId }
        ?.let { "${it.patient.name} ${it.patient.lastName}".trim().ifBlank { "Paciente" } }
        ?: "Todos"

    val filteredAlerts = remember(alerts, selectedFilter, selectedPatientId) {
        alerts
            .asSequence()
            .filter { alert -> selectedPatientId == null || alert.patientId == selectedPatientId }
            .filter { alert ->
                when (selectedFilter) {
                    AlertFilter.ALL -> true
                    AlertFilter.SOS -> alert.visualType() == AlertVisualType.SOS
                    AlertFilter.LOW_CHECKIN -> alert.visualType() == AlertVisualType.LOW_CHECKIN
                    AlertFilter.MISSED_CHECKIN -> alert.visualType() == AlertVisualType.NO_CHECKIN
                }
            }
            .take(10)
            .toList()
    }

    SoftCaregiverBackground {
        Scaffold(
            containerColor = Color.Transparent,
            bottomBar = {
                RelaxBottomNav(
                    selectedRoute = Screen.AlertsHistory.route,
                    onNavigate = onNavigate,
                    role = AppRole.CAREGIVER
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .imePadding()
                        .navigationBarsPadding(),
                    contentPadding = PaddingValues(start = 22.dp, end = 22.dp, top = 18.dp, bottom = 26.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    item {
                        AlertsHistoryHeader(
                            onBackClick = { onNavigate(Screen.CaregiverDashboard.route) }
                        )
                    }

                    item {
                        AlertFilterChips(
                            selectedFilter = selectedFilter,
                            onFilterChange = { selectedFilter = it }
                        )
                    }

                    item {
                        PatientFilterDropdown(
                            selectedPatientName = selectedPatientName,
                            expanded = patientMenuExpanded,
                            onExpandedChange = { patientMenuExpanded = it },
                            patients = patients,
                            onPatientSelected = { patientId ->
                                selectedPatientId = patientId
                                patientMenuExpanded = false
                            }
                        )
                    }

                    item {
                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(220)) + slideInVertically(initialOffsetY = { it / 7 }),
                            exit = fadeOut(tween(160))
                        ) {
                            AlertHistoryList(
                                alerts = filteredAlerts,
                                hasAnyAlert = hasAnyAlert,
                                onResolveClick = { alertToResolve = it }
                            )
                        }
                    }

                    item { Spacer(modifier = Modifier.height(28.dp)) }
                }

                if (isLoading && alerts.isEmpty()) {
                    FullScreenLoadingOverlay(overlayColor = Color.White.copy(alpha = 0.58f))
                }

                RelaxToastHost(state = toastState)
            }
        }
    }

    alertToResolve?.let { alert ->
        AlertDialog(
            onDismissRequest = { alertToResolve = null },
            containerColor = BackgroundWhite,
            shape = RoundedCornerShape(28.dp),
            title = {
                Text(
                    text = "Marcar como resuelta",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "La alerta quedara registrada como resuelta.",
                    fontFamily = LexendFontFamily,
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.markAlertResolved(alert.id)
                        alertToResolve = null
                    }
                ) {
                    Text(
                        text = "Confirmar",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        color = CaregiverPurple
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { alertToResolve = null }) {
                    Text(
                        text = "Cancelar",
                        fontFamily = LexendFontFamily,
                        color = TextSecondary
                    )
                }
            }
        )
    }
}

@Composable
private fun SoftCaregiverBackground(content: @Composable () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SoftScreenBg)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SoftLavender, SoftScreenBg)
                    )
                )
        )
        content()
    }
}

@Composable
private fun AlertsHistoryHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IconButton(
            onClick = onBackClick,
            modifier = Modifier
                .size(48.dp)
                .semantics { contentDescription = "Volver al dashboard" }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = TextPrimary,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            text = "Historial de Alertas",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 28.sp,
            color = TextPrimary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun AlertFilterChips(
    selectedFilter: AlertFilter,
    onFilterChange: (AlertFilter) -> Unit
) {
    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        contentPadding = PaddingValues(vertical = 2.dp)
    ) {
        items(AlertFilter.entries) { filter ->
            val selected = selectedFilter == filter
            val backgroundColor by animateColorAsState(
                targetValue = if (selected) CaregiverPurple else BackgroundWhite,
                animationSpec = tween(180),
                label = "filter-bg-${filter.name}"
            )
            val textColor by animateColorAsState(
                targetValue = if (selected) Color.White else TextPrimary,
                animationSpec = tween(180),
                label = "filter-text-${filter.name}"
            )
            Surface(
                modifier = Modifier
                    .height(52.dp)
                    .shadow(
                        elevation = if (selected) 10.dp else 3.dp,
                        shape = RoundedCornerShape(999.dp),
                        ambientColor = CaregiverPurple.copy(alpha = if (selected) 0.24f else 0.06f),
                        spotColor = CaregiverPurple.copy(alpha = if (selected) 0.24f else 0.06f)
                    )
                    .semantics { contentDescription = "Filtro ${filter.label}" },
                shape = RoundedCornerShape(999.dp),
                color = backgroundColor,
                border = if (selected) null else BorderStroke(1.dp, BorderSoft),
                onClick = { onFilterChange(filter) }
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = textColor
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientFilterDropdown(
    selectedPatientName: String,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    patients: List<CaregiverPatientSummary>,
    onPatientSelected: (String?) -> Unit
) {
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .shadow(
                    elevation = 10.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = NeutralPurpleGray.copy(alpha = 0.10f),
                    spotColor = NeutralPurpleGray.copy(alpha = 0.10f)
                )
                .semantics { contentDescription = "Selector de paciente" },
            shape = RoundedCornerShape(24.dp),
            color = BackgroundWhite,
            onClick = { onExpandedChange(true) }
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Groups,
                    contentDescription = null,
                    tint = CaregiverPurple,
                    modifier = Modifier.size(26.dp)
                )
                Text(
                    text = "Paciente: $selectedPatientName",
                    modifier = Modifier.weight(1f),
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextPrimary,
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier
                .background(BackgroundWhite)
                .semantics { contentDescription = "Opciones de pacientes" }
        ) {
            DropdownMenuItem(
                text = {
                    Text(
                        text = "Todos",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                onClick = { onPatientSelected(null) }
            )
            patients.forEach { summary ->
                val fullName = "${summary.patient.name} ${summary.patient.lastName}".trim().ifBlank { "Paciente" }
                DropdownMenuItem(
                    text = {
                        Text(
                            text = fullName,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    },
                    onClick = { onPatientSelected(summary.patient.id) }
                )
            }
        }
    }
}

@Composable
private fun AlertHistoryList(
    alerts: List<CaregiverAlert>,
    hasAnyAlert: Boolean,
    onResolveClick: (CaregiverAlert) -> Unit
) {
    when {
        !hasAnyAlert -> EmptyAlertsState(
            title = "No hay alertas registradas",
            subtitle = "Cuando ocurra una alerta SOS, check-in bajo o ausencia de check-in, aparecera aqui."
        )
        alerts.isEmpty() -> EmptyAlertsState(
            title = "No hay alertas para este filtro",
            subtitle = "Prueba cambiando el tipo de alerta o el paciente seleccionado."
        )
        else -> Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(28.dp),
                    ambientColor = NeutralPurpleGray.copy(alpha = 0.11f),
                    spotColor = NeutralPurpleGray.copy(alpha = 0.11f)
                ),
            shape = RoundedCornerShape(28.dp),
            color = BackgroundWhite
        ) {
            Column(modifier = Modifier.padding(vertical = 8.dp)) {
                alerts.forEachIndexed { index, alert ->
                    AlertHistoryItem(
                        alert = alert,
                        onResolveClick = { onResolveClick(alert) }
                    )
                    if (index < alerts.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(start = 92.dp, end = 28.dp),
                            thickness = 1.dp,
                            color = BorderSoft.copy(alpha = 0.72f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlertHistoryItem(
    alert: CaregiverAlert,
    onResolveClick: () -> Unit
) {
    val pressedScale by animateFloatAsState(targetValue = 1f, label = "alert-item-scale")
    val visualType = alert.visualType()
    val title = alertTitle(alert, visualType)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .scale(pressedScale)
            .clickable(
                role = Role.Button,
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = {}
            )
            .padding(horizontal = 24.dp, vertical = 18.dp),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        AlertTypeIcon(type = visualType)

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = alert.patientName.ifBlank { "Paciente" },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "  ·  ",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    color = TextSecondary
                )
                Text(
                    text = title,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = alert.createdAtText.ifBlank { "Sin fecha" },
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondary
            )

            if (!alert.resolved) {
                Text(
                    text = "Marcar como resuelta",
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = onResolveClick
                        ),
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = CaregiverPurple
                )
            }
        }

        AlertStatusBadge(resolved = alert.resolved)
    }
}

@Composable
private fun AlertTypeIcon(type: AlertVisualType) {
    val (bg, icon, label) = when (type) {
        AlertVisualType.SOS -> Triple(AlertRed, null, "SOS")
        AlertVisualType.LOW_CHECKIN -> Triple(WarningOrange, Icons.Default.BarChart, null)
        AlertVisualType.NO_CHECKIN -> Triple(NeutralPurpleGray, Icons.Default.CalendarMonth, null)
    }

    Box(
        modifier = Modifier
            .size(58.dp)
            .shadow(
                elevation = 8.dp,
                shape = CircleShape,
                ambientColor = bg.copy(alpha = 0.24f),
                spotColor = bg.copy(alpha = 0.24f)
            )
            .clip(CircleShape)
            .background(bg)
            .semantics {
                contentDescription = when (type) {
                    AlertVisualType.SOS -> "Alerta SOS"
                    AlertVisualType.LOW_CHECKIN -> "Alerta de bienestar bajo"
                    AlertVisualType.NO_CHECKIN -> "Alerta sin check-in"
                }
            },
        contentAlignment = Alignment.Center
    ) {
        if (label != null) {
            Text(
                text = label,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 14.sp,
                color = Color.White
            )
        } else if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun AlertStatusBadge(resolved: Boolean) {
    val borderColor = if (resolved) NeutralPurpleGray.copy(alpha = 0.58f) else AlertRed
    val background = if (resolved) Color.White else AlertRedSoft
    val label = if (resolved) "RESUELTA" else "PENDIENTE"

    Box(
        modifier = Modifier
            .border(1.4.dp, borderColor, RoundedCornerShape(10.dp))
            .background(background, RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = borderColor
        )
    }
}

@Composable
private fun EmptyAlertsState(
    title: String,
    subtitle: String
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = NeutralPurpleGray.copy(alpha = 0.10f),
                spotColor = NeutralPurpleGray.copy(alpha = 0.10f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = BackgroundWhite
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 24.dp, vertical = 34.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(LavenderPill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = CaregiverIndigo,
                    modifier = Modifier.size(32.dp)
                )
            }
            Text(
                text = title,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
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
}

private fun CaregiverAlert.visualType(): AlertVisualType {
    val normalizedType = type.lowercase()
    val normalizedText = "$title $message $severity".lowercase()
    return when {
        normalizedType == "sos" || normalizedText.contains("sos") -> AlertVisualType.SOS
        normalizedType.contains("missed") ||
            normalizedType.contains("sin_checkin") ||
            normalizedType.contains("no_check") ||
            normalizedText.contains("sin check") -> AlertVisualType.NO_CHECKIN
        else -> AlertVisualType.LOW_CHECKIN
    }
}

private fun alertTitle(alert: CaregiverAlert, type: AlertVisualType): String {
    return when (type) {
        AlertVisualType.SOS -> "SOS"
        AlertVisualType.NO_CHECKIN -> "Sin check-in"
        AlertVisualType.LOW_CHECKIN -> alert.message
            .ifBlank { alert.title }
            .ifBlank { "Bienestar bajo" }
    }
}
