package com.relaxmind.app.features.caregiver

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
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.ui.unit.sp
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.RelaxToastState
import com.relaxmind.app.ui.components.rememberRelaxToastState
import androidx.compose.material3.Text
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.Screen
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxCard
import com.relaxmind.app.ui.components.RelaxInputField
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.SOSCoral
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.utils.WellnessScoreCalculator

@Composable
fun PatientsListScreen(
    viewModel: CaregiverViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    onPatientClick: (String) -> Unit,
    onScanQr: () -> Unit
) {
    val patients by viewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val toastState = rememberRelaxToastState()
    var query by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.observeCaregiverData()
    }

    LaunchedEffect(error) {
        if (!error.isNullOrBlank()) {
            toastState.showError(error.orEmpty())
            viewModel.consumeError()
        }
    }

    val filteredPatients = remember(patients, query) {
        val normalizedQuery = query.trim().lowercase()
        if (normalizedQuery.isBlank()) {
            patients
        } else {
            patients.filter { summary ->
                "${summary.patient.name} ${summary.patient.lastName}"
                    .trim()
                    .lowercase()
                    .contains(normalizedQuery)
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { RelaxTopBar(title = "Mis Pacientes") },
        bottomBar = {
            RelaxBottomNav(
                selectedRoute = Screen.PatientsList.route,
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
                    RelaxInputField(
                        value = query,
                        onValueChange = { query = it },
                        label = "Buscar paciente",
                        role = AppRole.CAREGIVER,
                        leadingIcon = Icons.Default.Search
                    )
                }

                if (patients.isEmpty()) {
                    item { EmptyPatientsState(onScanQr = onScanQr) }
                } else if (filteredPatients.isEmpty()) {
                    item {
                        Text(
                            text = "No encontramos pacientes con ese nombre.",
                            fontFamily = LexendFontFamily,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 28.dp),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    item {
                        Text(
                            text = "${filteredPatients.size} Pacientes vinculados",
                            fontFamily = LexendFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.5f),
                            modifier = Modifier.padding(bottom = 4.dp, start = 4.dp)
                        )
                    }
                    items(filteredPatients, key = { it.patient.id }) { summary ->
                        PatientListCard(
                            summary = summary,
                            onClick = { onPatientClick(summary.patient.id) }
                        )
                    }
                    item { Spacer(modifier = Modifier.height(82.dp)) }
                }
            }

            if (isLoading && patients.isEmpty()) {
                LoadingIndicator(modifier = Modifier.align(Alignment.Center))
            }

            RelaxToastHost(state = toastState)
        }
    }
}

@Composable
private fun PatientListCard(
    summary: CaregiverPatientSummary,
    onClick: () -> Unit
) {
    val patient = summary.patient
    val score = summary.latestScore
    
    val fullName = "${patient.name} ${patient.lastName}".trim().ifBlank { "Paciente" }

    RelaxCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        elevation = 0.dp // To rely on new soft shadow internally or we can add a custom subtle shadow
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(contentAlignment = Alignment.BottomEnd) {
                AsyncImage(
                    model = patient.avatarUrl.ifBlank {
                        "https://ui-avatars.com/api/?name=$fullName&background=4338A8&color=fff"
                    },
                    contentDescription = "Avatar de $fullName",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .border(2.dp, CaregiverIndigo.copy(alpha = 0.2f), CircleShape)
                )
                if (summary.hasPendingAlert) {
                    Box(
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.background)
                            .padding(2.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(SOSCoral)
                        )
                    }
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fullName,
                    fontFamily = LexendFontFamily,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onBackground,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (patient.condition.isNotBlank()) {
                    Text(
                        text = patient.condition,
                        fontFamily = LexendFontFamily,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
                Text(
                    text = "Últ. Check-in: ${summary.lastCheckInDate ?: "Sin registro"}",
                    fontFamily = LexendFontFamily,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.45f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.Center
            ) {
                WellbeingChip(score = score)
                Spacer(modifier = Modifier.height(12.dp))
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Ver detalles",
                    tint = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.3f),
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
private fun WellbeingChip(score: Int?) {
    val color = WellnessScoreCalculator.getScoreColor(score)
    val text = if (score == null) {
        "Sin puntaje"
    } else {
        "${WellnessScoreCalculator.getCategory(score)} $score/100"
    }

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Favorite,
                contentDescription = null,
                tint = if (score == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) else color,
                modifier = Modifier.size(12.dp)
            )
            Text(
                text = text,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = if (score == null) MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f) else color
            )
        }
    }
}

@Composable
private fun EmptyPatientsState(onScanQr: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 42.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        Box(
            modifier = Modifier
                .size(110.dp)
                .clip(CircleShape)
                .background(CaregiverIndigo.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(CaregiverIndigo.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = CaregiverIndigo,
                    modifier = Modifier.size(38.dp)
                )
            }
        }
        Text(
            text = "Aún no tienes pacientes",
            fontFamily = LexendFontFamily,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = "Escanea el código QR que genere un paciente desde su app para vincularlo a tu cuenta y comenzar a cuidarlo.",
            fontFamily = LexendFontFamily,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        RelaxButton(
            text = "Vincular un paciente",
            onClick = onScanQr,
            role = AppRole.CAREGIVER
        )
    }
}
