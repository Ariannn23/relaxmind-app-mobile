package com.relaxmind.app.features.caregiver

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.Screen
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxCard
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.ScoreOrange
import com.relaxmind.app.ui.themes.ScoreRed

private enum class AlertFilter(val label: String) {
    ALL("Todos"),
    SOS("SOS"),
    LOW_CHECKIN("Check-in bajo"),
    MISSED_CHECKIN("Sin check-in")
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
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedFilter by remember { mutableStateOf(AlertFilter.ALL) }
    var selectedPatientId by remember { mutableStateOf<String?>(null) }
    var alertToResolve by remember { mutableStateOf<CaregiverAlert?>(null) }
    var patientMenuExpanded by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.observeCaregiverData()
    }

    LaunchedEffect(message) {
        if (!message.isNullOrBlank()) {
            snackbarHostState.showSnackbar(message.orEmpty())
            viewModel.consumeMessage()
        }
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            snackbarHostState.showSnackbar(error.orEmpty())
            viewModel.consumeError()
        }
    }

    val filteredAlerts = remember(alerts, selectedFilter, selectedPatientId) {
        alerts
            .asSequence()
            .filter { alert -> selectedPatientId == null || alert.patientId == selectedPatientId }
            .filter { alert ->
                when (selectedFilter) {
                    AlertFilter.ALL -> true
                    AlertFilter.SOS -> alert.type.equals("sos", ignoreCase = true)
                    AlertFilter.LOW_CHECKIN -> alert.type.contains("check", ignoreCase = true) ||
                        alert.type.contains("score", ignoreCase = true)
                    AlertFilter.MISSED_CHECKIN -> alert.type.contains("missed", ignoreCase = true) ||
                        alert.type.contains("sin_checkin", ignoreCase = true)
                }
            }
            .take(10)
            .toList()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { RelaxTopBar(title = "Historial de Alertas") },
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
                .background(MaterialTheme.colorScheme.background)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    FilterRow(
                        selectedFilter = selectedFilter,
                        onFilterChange = { selectedFilter = it }
                    )
                }

                if (patients.size > 1) {
                    item {
                        Box {
                            AssistChip(
                                onClick = { patientMenuExpanded = true },
                                label = {
                                    Text(
                                        text = patients
                                            .firstOrNull { it.patient.id == selectedPatientId }
                                            ?.patient
                                            ?.name
                                            ?.ifBlank { "Paciente" }
                                            ?: "Todos los pacientes"
                                    )
                                },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ExpandMore,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                },
                                colors = AssistChipDefaults.assistChipColors(
                                    labelColor = CaregiverIndigo,
                                    trailingIconContentColor = CaregiverIndigo
                                )
                            )
                            DropdownMenu(
                                expanded = patientMenuExpanded,
                                onDismissRequest = { patientMenuExpanded = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text("Todos los pacientes") },
                                    onClick = {
                                        selectedPatientId = null
                                        patientMenuExpanded = false
                                    }
                                )
                                patients.forEach { summary ->
                                    val fullName = "${summary.patient.name} ${summary.patient.lastName}".trim()
                                        .ifBlank { "Paciente" }
                                    DropdownMenuItem(
                                        text = { Text(fullName) },
                                        onClick = {
                                            selectedPatientId = summary.patient.id
                                            patientMenuExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }

                if (filteredAlerts.isEmpty()) {
                    item {
                        Text(
                            text = "No hay alertas para este filtro.",
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
                        )
                    }
                } else {
                    items(filteredAlerts, key = { it.id }) { alert ->
                        AlertHistoryCard(
                            alert = alert,
                            onResolveClick = { alertToResolve = alert }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(82.dp)) }
                }
            }

            if (isLoading && alerts.isEmpty()) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }
        }
    }

    alertToResolve?.let { alert ->
        AlertDialog(
            onDismissRequest = { alertToResolve = null },
            title = { Text("Marcar como resuelta") },
            text = { Text("Esta accion cambiara el estado de la alerta a resuelta.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.markAlertResolved(alert.id)
                        alertToResolve = null
                    }
                ) {
                    Text("Confirmar", color = CaregiverIndigo)
                }
            },
            dismissButton = {
                TextButton(onClick = { alertToResolve = null }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
private fun FilterRow(
    selectedFilter: AlertFilter,
    onFilterChange: (AlertFilter) -> Unit
) {
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(AlertFilter.entries) { filter ->
            FilterChip(
                selected = selectedFilter == filter,
                onClick = { onFilterChange(filter) },
                label = { Text(filter.label) },
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = CaregiverIndigo.copy(alpha = 0.14f),
                    selectedLabelColor = CaregiverIndigo
                )
            )
        }
    }
}

@Composable
private fun AlertHistoryCard(
    alert: CaregiverAlert,
    onResolveClick: () -> Unit
) {
    val color = alertColor(alert)

    RelaxCard(modifier = Modifier.fillMaxWidth(), elevation = 3.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(color.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(alertIcon(alert), contentDescription = null, tint = color)
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
                    text = alert.createdAtText.ifBlank { "Sin fecha" },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.52f)
                )
                Text(
                    text = alert.message.ifBlank { alert.title.ifBlank { "Alerta registrada" } },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.68f),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                if (!alert.resolved) {
                    TextButton(onClick = onResolveClick) {
                        Text("Marcar como resuelta", color = CaregiverIndigo)
                    }
                }
            }
            StatusChip(resolved = alert.resolved)
        }
    }
}

@Composable
private fun StatusChip(resolved: Boolean) {
    val color = if (resolved) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.42f) else ScoreRed
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.14f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (resolved) "Resuelta" else "Pendiente",
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}

private fun alertIcon(alert: CaregiverAlert) = when {
    alert.type.equals("sos", ignoreCase = true) -> Icons.Default.Warning
    alert.type.contains("check", ignoreCase = true) || alert.type.contains("score", ignoreCase = true) -> Icons.Default.MonitorHeart
    else -> Icons.Default.CalendarMonth
}

@Composable
private fun alertColor(alert: CaregiverAlert): Color = when {
    alert.type.equals("sos", ignoreCase = true) || alert.severity.equals("high", ignoreCase = true) -> ScoreRed
    alert.type.contains("check", ignoreCase = true) || alert.type.contains("score", ignoreCase = true) -> ScoreOrange
    else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
}
