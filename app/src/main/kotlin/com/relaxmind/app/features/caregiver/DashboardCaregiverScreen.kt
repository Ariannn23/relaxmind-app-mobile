package com.relaxmind.app.features.caregiver

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxCard
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.ScoreGray
import com.relaxmind.app.ui.themes.ScoreGreenDark
import com.relaxmind.app.ui.themes.ScoreOrange
import com.relaxmind.app.ui.themes.ScoreRed

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
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.loadDashboard()
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error.orEmpty())
            viewModel.consumeError()
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            RelaxBottomNav(
                selectedRoute = Screen.CaregiverDashboard.route,
                onNavigate = onNavigate,
                role = AppRole.CAREGIVER
            )
        },
        floatingActionButton = {
            if (patients.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = onScanQr,
                    containerColor = CaregiverIndigo,
                    contentColor = androidx.compose.ui.graphics.Color.White,
                    icon = { Icon(Icons.Default.Add, contentDescription = null) },
                    text = { Text("Vincularme con un paciente", style = MaterialTheme.typography.labelLarge) }
                )
            } else {
                FloatingActionButton(
                    onClick = onScanQr,
                    containerColor = CaregiverIndigo,
                    contentColor = androidx.compose.ui.graphics.Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Vincular paciente")
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalArrangement = Arrangement.spacedBy(22.dp)
            ) {
                item {
                    Header(
                        name = caregiver?.name.orEmpty(),
                        avatarUrl = caregiver?.avatarUrl.orEmpty(),
                        patientCount = patients.size
                    )
                }

                item {
                    ActiveAlertsSection(
                        alerts = alerts,
                        onAlertsClick = onAlertsClick,
                        onAlertClick = { alertId -> onNavigate(Screen.SOSAlert.createRoute(alertId)) }
                    )
                }

                item {
                    PatientsSection(
                        patients = patients,
                        onViewAll = { onNavigate(Screen.PatientsList.route) },
                        onPatientClick = onPatientClick
                    )
                }

                item { Spacer(modifier = Modifier.height(92.dp)) }
            }

            if (isLoading && caregiver == null) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }
}

@Composable
private fun Header(
    name: String,
    avatarUrl: String,
    patientCount: Int
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "Hola, ${name.ifBlank { "cuidador" }}",
                style = MaterialTheme.typography.headlineMedium.copy(fontSize = 24.sp),
                color = CaregiverIndigo,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Tienes $patientCount pacientes",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
            )
        }
        AsyncImage(
            model = avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=RM&background=4338A8&color=fff" },
            contentDescription = "Avatar cuidador",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(62.dp)
                .clip(CircleShape)
                .border(3.dp, CaregiverIndigo.copy(alpha = 0.28f), CircleShape)
        )
    }
}

@Composable
private fun ActiveAlertsSection(
    alerts: List<CaregiverAlert>,
    onAlertsClick: () -> Unit,
    onAlertClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(
            title = "Alertas activas",
            actionText = if (alerts.isNotEmpty()) "Ver todas las alertas" else null,
            onAction = onAlertsClick
        )
        if (alerts.isEmpty()) {
            RelaxCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Todo tranquilo hoy",
                    style = MaterialTheme.typography.titleMedium,
                    color = PatientGreen,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "No hay alertas pendientes de revisión.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
        } else {
            alerts.take(3).forEach { alert ->
                AlertCard(alert = alert, onClick = { onAlertClick(alert.id) })
            }
        }
    }
}

@Composable
private fun AlertCard(
    alert: CaregiverAlert,
    onClick: () -> Unit
) {
    val icon = when (alert.type.lowercase()) {
        "schedule", "appointment", "agenda" -> Icons.Default.CalendarMonth
        "score", "checkin", "progress" -> Icons.Default.MonitorHeart
        else -> Icons.Default.Warning
    }
    val color = when (alert.severity.lowercase()) {
        "high", "sos" -> ScoreRed
        "medium" -> ScoreOrange
        else -> CaregiverIndigo
    }

    RelaxCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = color)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.patientName.ifBlank { "Paciente" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = alert.title.ifBlank { alert.message.ifBlank { "Alerta pendiente" } },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (alert.createdAtText.isNotBlank()) {
                    Text(
                        text = alert.createdAtText,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.46f)
                    )
                }
            }
            RelaxButton(
                text = "Ver",
                onClick = onClick,
                role = AppRole.CAREGIVER
            )
        }
    }
}

@Composable
private fun PatientsSection(
    patients: List<CaregiverPatientSummary>,
    onViewAll: () -> Unit,
    onPatientClick: (String) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SectionTitle(
            title = "Mis pacientes (${patients.size})",
            actionText = if (patients.isNotEmpty()) "Ver todos" else null,
            onAction = onViewAll
        )
        if (patients.isEmpty()) {
            RelaxCard(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "Aún no tienes pacientes vinculados",
                    style = MaterialTheme.typography.titleMedium,
                    color = CaregiverIndigo,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Usa el botón inferior para escanear el código de un paciente.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
        } else {
            LazyRow(horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                items(patients, key = { it.patient.id }) { summary ->
                    PatientChip(summary = summary, onClick = { onPatientClick(summary.patient.id) })
                }
            }
        }
    }
}

@Composable
private fun PatientChip(
    summary: CaregiverPatientSummary,
    onClick: () -> Unit
) {
    val patient = summary.patient
    val borderColor = scoreColor(summary.latestScore)
    Column(
        modifier = Modifier
            .width(88.dp)
            .clip(RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        AsyncImage(
            model = patient.avatarUrl.ifBlank {
                "https://ui-avatars.com/api/?name=${patient.name.ifBlank { "Paciente" }}&background=0F6E56&color=fff"
            },
            contentDescription = "Avatar paciente",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(60.dp)
                .clip(CircleShape)
                .border(3.dp, borderColor, CircleShape)
        )
        Text(
            text = patient.name.ifBlank { "Paciente" },
            style = MaterialTheme.typography.labelSmall,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun SectionTitle(
    title: String,
    actionText: String?,
    onAction: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = CaregiverIndigo,
            fontWeight = FontWeight.Bold
        )
        if (actionText != null) {
            TextButton(onClick = onAction) {
                Text(
                    text = actionText,
                    color = CaregiverIndigo,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

private fun scoreColor(score: Int?) = when {
    score == null -> ScoreGray
    score < 40 -> ScoreRed
    score < 70 -> ScoreOrange
    else -> ScoreGreenDark
}
