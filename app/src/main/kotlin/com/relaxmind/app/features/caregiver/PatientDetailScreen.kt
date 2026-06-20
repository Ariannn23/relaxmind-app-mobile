package com.relaxmind.app.features.caregiver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.RelaxToastState
import com.relaxmind.app.ui.components.rememberRelaxToastState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.Image
import com.relaxmind.app.ui.components.getAvatarDrawableRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.data.model.CheckIn
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxCard
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.components.WellnessCalendarGrid
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.SOSCoral
import com.relaxmind.app.utils.WellnessScoreCalculator
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun PatientDetailScreen(
    patientId: String,
    viewModel: CaregiverViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onSosAlertClick: (String) -> Unit
) {
    val patient by viewModel.selectedPatient.collectAsState()
    val checkIns by viewModel.selectedPatientCheckIns.collectAsState()
    val streak by viewModel.selectedPatientStreak.collectAsState()
    val alerts by viewModel.selectedPatientAlerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val toastState = rememberRelaxToastState()
    var selectedTab by remember { mutableIntStateOf(0) }

    LaunchedEffect(patientId) {
        viewModel.loadPatientDetail(patientId)
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            toastState.showError(error.orEmpty())
            viewModel.consumeError()
        }
    }

    val fullName = patient?.let { "${it.name} ${it.lastName}".trim() }.orEmpty().ifBlank { "Paciente" }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RelaxTopBar(
                title = fullName,
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(
                        onClick = {
                            val phone = patient?.phone.orEmpty()
                            if (phone.isNotBlank()) {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                            }
                        },
                        enabled = !patient?.phone.isNullOrBlank()
                    ) {
                        Icon(
                            imageVector = Icons.Default.Phone,
                            contentDescription = "Llamar paciente",
                            tint = CaregiverIndigo
                        )
                    }
                }
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
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    PatientHeader(
                        avatarUrl = patient?.avatarUrl.orEmpty(),
                        fullName = fullName,
                        condition = patient?.condition.orEmpty(),
                        score = checkIns.firstOrNull()?.score
                    )
                }

                item {
                    TabRow(selectedTabIndex = selectedTab, containerColor = MaterialTheme.colorScheme.surface) {
                        listOf("Progreso", "Historial", "Alertas SOS").forEachIndexed { index, label ->
                            Tab(
                                selected = selectedTab == index,
                                onClick = { selectedTab = index },
                                text = { Text(label, style = MaterialTheme.typography.labelMedium) },
                                selectedContentColor = CaregiverIndigo,
                                unselectedContentColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                            )
                        }
                    }
                }

                when (selectedTab) {
                    0 -> item {
                        PatientProgressTab(
                            checkIns = checkIns,
                            streakDays = streak?.currentStreak ?: 0
                        )
                    }
                    1 -> {
                        if (checkIns.isEmpty()) {
                            item { EmptyText("Este paciente aun no tiene check-ins registrados.") }
                        } else {
                            items(checkIns, key = { it.id.ifBlank { it.date } }) { checkIn ->
                                CheckInHistoryCard(checkIn = checkIn)
                            }
                        }
                    }
                    2 -> {
                        val sosAlerts = alerts.filter { it.type.equals("sos", ignoreCase = true) }
                        if (sosAlerts.isEmpty()) {
                            item { EmptyText("No hay alertas SOS para este paciente.") }
                        } else {
                            items(sosAlerts, key = { it.id }) { alert ->
                                PatientSosAlertCard(
                                    alert = alert,
                                    onOpenLocation = { onSosAlertClick(alert.id) }
                                )
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(24.dp)) }
            }

            if (isLoading && patient == null) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }

            RelaxToastHost(state = toastState)
        }
    }
}

@Composable
private fun PatientHeader(
    avatarUrl: String,
    fullName: String,
    condition: String,
    score: Int?
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        val isCustomAvatar = avatarUrl.startsWith("relaxmind://avatar/")
        if (isCustomAvatar) {
            Image(
                painter = painterResource(id = getAvatarDrawableRes(avatarUrl)),
                contentDescription = "Avatar de $fullName",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(4.dp, WellnessScoreCalculator.getScoreColor(score), CircleShape)
                    .background(Color(0xFFF3F4F6))
            )
        } else {
            AsyncImage(
                model = avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=$fullName&background=4338A8&color=fff" },
                contentDescription = "Avatar de $fullName",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .border(4.dp, WellnessScoreCalculator.getScoreColor(score), CircleShape)
            )
        }
        Text(
            text = fullName,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        if (condition.isNotBlank()) {
            Text(
                text = condition,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
            )
        }
    }
}

@Composable
private fun PatientProgressTab(
    checkIns: List<CheckIn>,
    streakDays: Int
) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    val monthlyCheckIns = remember(checkIns, selectedMonth) {
        checkIns.mapNotNull { checkIn ->
            runCatching { LocalDate.parse(checkIn.date) }.getOrNull()?.let { date ->
                if (date.year == selectedMonth.year && date.monthValue == selectedMonth.monthValue) {
                    date.dayOfMonth to checkIn.score
                } else {
                    null
                }
            }
        }.toMap()
    }

    RelaxCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { selectedMonth = selectedMonth.minusMonths(1) }) {
                Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior")
            }
            Text(
                text = monthTitle(selectedMonth),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = CaregiverIndigo
            )
            IconButton(onClick = { selectedMonth = selectedMonth.plusMonths(1) }) {
                Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente")
            }
        }
        WellnessCalendarGrid(
            year = selectedMonth.year,
            month = selectedMonth.monthValue,
            checkIns = monthlyCheckIns,
            modifier = Modifier.padding(top = 10.dp)
        )
        Text(
            text = "Racha actual: $streakDays dias",
            style = MaterialTheme.typography.titleSmall,
            color = CaregiverIndigo,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 10.dp)
        )
    }
}

@Composable
private fun CheckInHistoryCard(checkIn: CheckIn) {
    val color = WellnessScoreCalculator.getScoreColor(checkIn.score)
    RelaxCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = checkIn.date.ifBlank { "Sin fecha" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = checkIn.category.ifBlank { WellnessScoreCalculator.getCategory(checkIn.score) },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f)
                )
            }
            ScoreChip(score = checkIn.score, color = color)
        }
    }
}

@Composable
private fun PatientSosAlertCard(
    alert: CaregiverAlert,
    onOpenLocation: () -> Unit
) {
    RelaxCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(SOSCoral.copy(alpha = 0.14f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = SOSCoral)
            }
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = alert.createdAtText.ifBlank { "Alerta SOS" },
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = if (alert.resolved) "Resuelta" else "Pendiente",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (alert.resolved) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f) else SOSCoral
                )
            }
            RelaxButton(
                text = "Ver ubicacion",
                onClick = onOpenLocation,
                role = AppRole.CAREGIVER
            )
        }
    }
}

@Composable
private fun ScoreChip(score: Int, color: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.18f))
            .padding(horizontal = 12.dp, vertical = 7.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "$score/100",
            style = MaterialTheme.typography.labelMedium,
            color = color
        )
    }
}

@Composable
private fun EmptyText(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 24.dp),
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
    )
}

private fun monthTitle(month: YearMonth): String {
    val monthName = month.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
    return monthName.replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() } +
        " ${month.year}"
}
