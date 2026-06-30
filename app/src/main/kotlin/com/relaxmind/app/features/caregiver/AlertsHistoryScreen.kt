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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.R
import com.relaxmind.app.Screen
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.AlertsHistorySkeleton
import com.relaxmind.app.ui.components.ErrorStateScreen
import com.relaxmind.app.ui.components.RelaxLoadingContent
import com.relaxmind.app.ui.components.ScreenHeader
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.rememberRelaxToastState
import com.relaxmind.app.ui.themes.AlertRed
import com.relaxmind.app.ui.themes.AlertRedSoft
import com.relaxmind.app.ui.themes.BorderSoft
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.CaregiverPurple
import com.relaxmind.app.ui.themes.LavenderPill
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.NeutralPurpleGray
import com.relaxmind.app.ui.themes.SoftLavender
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import com.relaxmind.app.ui.themes.WarningOrange
import com.relaxmind.app.ui.themes.WarningOrangeSoft
import java.util.Calendar
import java.util.Date

private enum class AlertFilter(val label: String) {
    ALL("Todos"),
    SOS("SOS"),
    LOW_CHECKIN("Check-in bajo"),
    MISSED_CHECKIN("Sin check-in"),
    UNLINK("Desvinculado")
}

private enum class AlertType {
    SOS,
    LOW_CHECKIN,
    NO_CHECKIN,
    UNLINK
}

private enum class AlertDateRange(val label: String) {
    LAST_10("Últimas 10"),
    TODAY("Hoy"),
    LAST_7_DAYS("7 días"),
    LAST_30_DAYS("30 días")
}

@Composable
fun AlertsHistoryScreen(
    viewModel: CaregiverViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    showBottomNav: Boolean = true
) {
    val alerts by viewModel.allAlerts.collectAsState()
    val patients by viewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val message by viewModel.message.collectAsState()
    val error by viewModel.error.collectAsState()

    val toastState = rememberRelaxToastState()
    var selectedFilter by remember { mutableStateOf(AlertFilter.ALL) }
    var selectedPatientId by remember { mutableStateOf<String?>(null) }
    var selectedDateRange by remember { mutableStateOf(AlertDateRange.LAST_10) }
    var alertToResolve by remember { mutableStateOf<CaregiverAlert?>(null) }
    var alertToView by remember { mutableStateOf<CaregiverAlert?>(null) }
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

    val selectedPatientName = patients
        .firstOrNull { it.patient.id == selectedPatientId }
        ?.let { "${it.patient.name} ${it.patient.lastName}".trim() }
        ?.ifBlank { "Paciente" }
        ?: "Todos"

    val filteredAlerts = remember(alerts, selectedFilter, selectedPatientId, selectedDateRange) {
        alerts
            .asSequence()
            .filter { selectedPatientId == null || it.patientId == selectedPatientId }
            .filter { selectedDateRange == AlertDateRange.LAST_10 || it.isWithinDateRange(selectedDateRange) }
            .filter { alert ->
                when (selectedFilter) {
                    AlertFilter.ALL -> true
                    AlertFilter.SOS -> alert.alertType() == AlertType.SOS
                    AlertFilter.LOW_CHECKIN -> alert.alertType() == AlertType.LOW_CHECKIN
                    AlertFilter.MISSED_CHECKIN -> alert.alertType() == AlertType.NO_CHECKIN
                    AlertFilter.UNLINK -> alert.alertType() == AlertType.UNLINK
                }
            }
            .take(10)
            .toList()
    }

    Scaffold(
        containerColor = Color.White,
        bottomBar = {
                if (showBottomNav) {
                    RelaxBottomNav(
                        selectedRoute = Screen.AlertsHistory.route,
                        onNavigate = onNavigate,
                        role = AppRole.CAREGIVER
                    )
                }
            }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
                .padding(innerPadding)
        ) {
            SoftCaregiverBackground()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 24.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    ScreenHeader(
                        title = "Alertas",
                        subtitle = "Historial y seguimiento de tus pacientes",
                        horizontalPadding = 0.dp
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
                        onAllPatientsClick = {
                            selectedPatientId = null
                            patientMenuExpanded = false
                        },
                        patients = patients,
                        onPatientClick = { patientId ->
                            selectedPatientId = patientId
                            patientMenuExpanded = false
                        }
                    )
                }
                item {
                    AlertDateRangeChips(
                        selectedRange = selectedDateRange,
                        onRangeChange = { selectedDateRange = it }
                    )
                }

                when {
                    isLoading && alerts.isEmpty() && error == null -> {
                        item {
                            AlertsHistorySkeleton()
                        }
                    }

                    error != null && alerts.isEmpty() -> {
                        item {
                            ErrorStateScreen(
                                message = error ?: "",
                                onRetry = { viewModel.observeCaregiverData() }
                            )
                        }
                    }

                    alerts.isEmpty() -> {
                        item { EmptyAlertsState() }
                    }

                    filteredAlerts.isEmpty() -> {
                        item { NoAlertsResultsState() }
                    }

                    else -> {
                        items(filteredAlerts, key = { "${it.id}-${it.createdAt?.time}" }) { alert ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn(tween(180)) + slideInVertically(initialOffsetY = { it / 5 }),
                                exit = fadeOut()
                            ) {
                                AlertItemCard(
                                    alert = alert,
                                    onClick = {
                                        if (!alert.resolved) alertToResolve = alert
                                        else alertToView = alert
                                    }
                                )
                            }
                        }
                        item { Spacer(modifier = Modifier.height(88.dp)) }
                    }
                }
            }

            RelaxToastHost(state = toastState)
        }
    }

    alertToResolve?.let { alert ->
        ResolveAlertDialog(
            onDismiss = { alertToResolve = null },
            onConfirm = {
                viewModel.markAlertResolved(alert.id)
                alertToResolve = null
            }
        )
    }

    alertToView?.let { alert ->
        AlertDetailBottomSheet(
            alert = alert,
            onDismiss = { alertToView = null }
        )
    }
}

@Composable
private fun SoftCaregiverBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(
                Brush.verticalGradient(
                    colors = listOf(SoftLavender.copy(alpha = 0.55f), Color.White)
                )
            )
    )
}

@Composable
private fun AlertsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 16.dp, start = 12.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.relaxmind.app.ui.components.RelaxBackButton(
            onClick = onBackClick,
            role = com.relaxmind.app.ui.components.AppRole.CAREGIVER,
            modifier = Modifier.padding(start = 8.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Text(
            text = stringResource(id = R.string.alerts_history_title),
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
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
        modifier = Modifier.fillMaxWidth()
    ) {
        items(AlertFilter.entries) { filter ->
            val selected = selectedFilter == filter
            val background by animateColorAsState(
                targetValue = if (selected) CaregiverPurple else Color.White,
                animationSpec = tween(180),
                label = "filter-bg-${filter.name}"
            )
            val contentColor by animateColorAsState(
                targetValue = if (selected) Color.White else TextPrimary,
                animationSpec = tween(180),
                label = "filter-text-${filter.name}"
            )

            Surface(
                modifier = Modifier
                    .height(46.dp)
                    .shadow(
                        elevation = if (selected) 8.dp else 0.dp,
                        shape = RoundedCornerShape(50),
                        ambientColor = CaregiverPurple.copy(alpha = 0.18f),
                        spotColor = CaregiverPurple.copy(alpha = 0.18f)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onFilterChange(filter) }
                    ),
                shape = RoundedCornerShape(50),
                color = background,
                border = if (selected) null else BorderStroke(1.dp, BorderSoft)
            ) {
                Box(
                    modifier = Modifier.padding(horizontal = 22.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = filter.label,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = contentColor
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
    onAllPatientsClick: () -> Unit,
    patients: List<CaregiverPatientSummary>,
    onPatientClick: (String) -> Unit
) {
    Box {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(58.dp)
                .shadow(
                    elevation = 12.dp,
                    shape = RoundedCornerShape(24.dp),
                    ambientColor = Color.Black.copy(alpha = 0.08f),
                    spotColor = Color.Black.copy(alpha = 0.08f)
                )
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = { onExpandedChange(true) }
                ),
            shape = RoundedCornerShape(24.dp),
            color = Color.White
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = null,
                    tint = CaregiverPurple,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = stringResource(id = R.string.alerts_patient_filter, selectedPatientName),
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Icon(
                    imageVector = Icons.Default.ExpandMore,
                    contentDescription = "Seleccionar paciente",
                    tint = TextPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { onExpandedChange(false) },
            modifier = Modifier.background(Color.White)
        ) {
            DropdownMenuItem(
                text = { DropdownLabel("Todos") },
                onClick = onAllPatientsClick
            )
            patients.forEach { summary ->
                val fullName = "${summary.patient.name} ${summary.patient.lastName}".trim()
                    .ifBlank { "Paciente" }
                DropdownMenuItem(
                    text = { DropdownLabel(fullName) },
                    onClick = { onPatientClick(summary.patient.id) }
                )
            }
        }
    }
}

@Composable
private fun AlertDateRangeChips(
    selectedRange: AlertDateRange,
    onRangeChange: (AlertDateRange) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(
            text = stringResource(id = R.string.alerts_date_range),
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = TextSecondary,
            modifier = Modifier.padding(start = 4.dp)
        )
        LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            items(AlertDateRange.entries) { range ->
                val selected = selectedRange == range
                Surface(
                    modifier = Modifier
                        .height(40.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = { onRangeChange(range) }
                        ),
                    shape = RoundedCornerShape(50),
                    color = if (selected) LavenderPill else Color.White,
                    border = BorderStroke(1.dp, if (selected) CaregiverIndigo else BorderSoft)
                ) {
                    Box(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = range.label,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = if (selected) CaregiverIndigo else TextPrimary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DropdownLabel(text: String) {
    Text(
        text = text,
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.Medium,
        color = TextPrimary
    )
}

@Composable
private fun AlertItemCard(
    alert: CaregiverAlert,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(targetValue = 1f, label = "alert-card-scale")

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        shape = RoundedCornerShape(26.dp),
        color = Color.White
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            AlertTypeIcon(type = alert.alertType())

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = alert.patientName.ifBlank { "Paciente" },
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Text(
                        text = "  ·  ${alert.displayTitle()}",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = alert.createdAtText.ifBlank { "Sin fecha" },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            AlertStatusBadge(resolved = alert.resolved)
        }
    }
}

@Composable
private fun AlertTypeIcon(type: AlertType) {
    val iconBackground = when (type) {
        AlertType.SOS -> AlertRed
        AlertType.LOW_CHECKIN -> WarningOrange
        AlertType.NO_CHECKIN -> NeutralPurpleGray
        AlertType.UNLINK -> NeutralPurpleGray
    }

    Box(
        modifier = Modifier
            .size(54.dp)
            .background(iconBackground, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        when (type) {
            AlertType.SOS -> Text(
                text = "SOS",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White
            )

            AlertType.LOW_CHECKIN -> Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = "Alerta de bienestar bajo",
                tint = Color.White,
                modifier = Modifier.size(28.dp)
            )

            AlertType.NO_CHECKIN -> Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = "Alerta sin check-in",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )

            AlertType.UNLINK -> Icon(
                imageVector = Icons.Default.PersonOff,
                contentDescription = "Acceso revocado",
                tint = Color.White,
                modifier = Modifier.size(26.dp)
            )
        }
    }
}

@Composable
private fun AlertStatusBadge(resolved: Boolean) {
    val borderColor = if (resolved) NeutralPurpleGray.copy(alpha = 0.6f) else AlertRed
    val textColor = if (resolved) NeutralPurpleGray else AlertRed
    val backgroundColor = if (resolved) Color.White else AlertRedSoft.copy(alpha = 0.55f)

    Surface(
        shape = RoundedCornerShape(9.dp),
        color = backgroundColor,
        border = BorderStroke(1.dp, borderColor)
    ) {
        Text(
            text = if (resolved) stringResource(id = R.string.alerts_resolved) else stringResource(id = R.string.alerts_pending),
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = textColor,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun EmptyAlertsState() {
    StateCard(
        title = "No hay alertas registradas",
        description = "Cuando ocurra una alerta SOS, check-in bajo o ausencia de check-in, aparecerá aquí."
    )
}

@Composable
private fun NoAlertsResultsState() {
    StateCard(
        title = "No hay alertas para este filtro",
        description = "Prueba cambiando el tipo de alerta o el paciente seleccionado."
    )
}

@Composable
private fun StateCard(title: String, description: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 34.dp)
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = CaregiverIndigo.copy(alpha = 0.08f),
                spotColor = CaregiverIndigo.copy(alpha = 0.08f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Column(
            modifier = Modifier.padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(58.dp)
                    .background(LavenderPill, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Notifications,
                    contentDescription = null,
                    tint = CaregiverIndigo,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = title,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun ResolveAlertDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White,
            shadowElevation = 18.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(58.dp)
                        .background(WarningOrangeSoft, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Notifications,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Marcar alerta como resuelta",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Confirma solo si ya atendiste esta alerta.",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    TextButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = CaregiverIndigo
                        )
                    }
                    Button(
                        onClick = onConfirm,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = CaregiverIndigo,
                            contentColor = Color.White
                        )
                    ) {
                        Text(
                            text = "Confirmar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

private fun CaregiverAlert.alertType(): AlertType = when {
    type.equals("UNLINK", ignoreCase = true) -> AlertType.UNLINK
    type.equals("sos", ignoreCase = true) -> AlertType.SOS
    type.contains("missed", ignoreCase = true) ||
        type.contains("sin_checkin", ignoreCase = true) ||
        type.contains("no_check", ignoreCase = true) -> AlertType.NO_CHECKIN
    type.contains("check", ignoreCase = true) ||
        type.contains("score", ignoreCase = true) ||
        type.contains("wellbeing", ignoreCase = true) -> AlertType.LOW_CHECKIN
    severity.equals("high", ignoreCase = true) -> AlertType.SOS
    else -> AlertType.NO_CHECKIN
}

private fun CaregiverAlert.displayTitle(): String {
    val raw = title.ifBlank { message }.ifBlank {
        when (alertType()) {
            AlertType.SOS -> "SOS"
            AlertType.LOW_CHECKIN -> "Bienestar bajo"
            AlertType.NO_CHECKIN -> "Sin check-in"
            AlertType.UNLINK -> "Acceso revocado"
        }
    }

    return when (alertType()) {
        AlertType.SOS -> "SOS"
        AlertType.LOW_CHECKIN -> raw
            .replace("Puntaje de bienestar bajo", "Bienestar bajo", ignoreCase = true)
            .replace("Check-in bajo", "Bienestar bajo", ignoreCase = true)
        AlertType.NO_CHECKIN -> "Sin check-in"
        AlertType.UNLINK -> "Acceso revocado"
    }
}

private fun CaregiverAlert.isWithinDateRange(range: AlertDateRange): Boolean {
    val createdDate = createdAt ?: return false
    if (range == AlertDateRange.LAST_10) return true

    val now = Calendar.getInstance()
    val created = Calendar.getInstance().apply { time = createdDate }

    return when (range) {
        AlertDateRange.TODAY ->
            now.get(Calendar.YEAR) == created.get(Calendar.YEAR) &&
                now.get(Calendar.DAY_OF_YEAR) == created.get(Calendar.DAY_OF_YEAR)
        AlertDateRange.LAST_7_DAYS -> createdDate.isAfterDaysAgo(7)
        AlertDateRange.LAST_30_DAYS -> createdDate.isAfterDaysAgo(30)
        AlertDateRange.LAST_10 -> true
    }
}

private fun Date.isAfterDaysAgo(days: Int): Boolean {
    val cutoff = Calendar.getInstance().apply {
        add(Calendar.DAY_OF_YEAR, -days)
    }.time
    return !before(cutoff)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AlertDetailBottomSheet(
    alert: CaregiverAlert,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val type = alert.alertType()
    val isSos = type == AlertType.SOS

    val headerBg = when (type) {
        AlertType.SOS -> AlertRedSoft
        AlertType.LOW_CHECKIN -> WarningOrangeSoft
        AlertType.NO_CHECKIN -> LavenderPill
        AlertType.UNLINK -> LavenderPill
    }
    val headerColor = when (type) {
        AlertType.SOS -> AlertRed
        AlertType.LOW_CHECKIN -> WarningOrange
        AlertType.NO_CHECKIN -> CaregiverIndigo
        AlertType.UNLINK -> CaregiverIndigo
    }
    val typeLabel = when (type) {
        AlertType.SOS -> "Alerta de emergencia (SOS)"
        AlertType.LOW_CHECKIN -> "Bienestar bajo detectado"
        AlertType.NO_CHECKIN -> "Check-in no realizado"
        AlertType.UNLINK -> "Acceso revocado"
    }
    val typeDesc = when (type) {
        AlertType.SOS -> "El paciente activó manualmente la alarma de emergencia."
        AlertType.LOW_CHECKIN -> "La puntuación del último check-in fue muy baja, indicando posible malestar."
        AlertType.NO_CHECKIN -> "El paciente no completó su check-in diario en el tiempo esperado."
        AlertType.UNLINK -> "El paciente revocaró el acceso del cuidador a su cuenta."
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = Color.White,
        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "Detalle de Alerta",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )

            // Type header
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = headerBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    AlertTypeIcon(type = type)
                    Column {
                        Text(
                            text = typeLabel,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = headerColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = typeDesc,
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Patient + Date
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF6F4FF)
            ) {
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Paciente:",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = alert.patientName.ifBlank { "Sin nombre" },
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Fecha:",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                        Text(
                            text = alert.createdAtText.ifBlank { "Sin fecha" },
                            fontFamily = LexendFontFamily,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                    }
                }
            }

            // Resolution status
            val resolvedBg = if (alert.resolved) Color(0xFFE8F8EF) else WarningOrangeSoft
            val resolvedColor = if (alert.resolved) Color(0xFF38C172) else WarningOrange
            val resolvedLabel = if (alert.resolved) "✓ Atendida" else "⏳ Pendiente de atención"
            val resolvedDesc = if (alert.resolved)
                "Esta alerta ya fue atendida por el cuidador."
            else
                "Esta alerta aún no ha sido marcada como atendida."

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = resolvedBg
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = resolvedLabel,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = resolvedColor
                        )
                        Text(
                            text = resolvedDesc,
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
