package com.relaxmind.app.features.caregiver

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.relaxmind.app.R
import com.relaxmind.app.ui.components.getAvatarDrawableRes
import com.relaxmind.app.Screen
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxLoadingContent
import com.relaxmind.app.ui.components.ScreenHeader
import com.relaxmind.app.ui.components.ScrollToTopEvents
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.PatientListSkeleton
import com.relaxmind.app.ui.components.rememberRelaxToastState
import com.relaxmind.app.ui.themes.*

enum class WellbeingStatus { Good, Moderate, Low, Critical, NoData }

fun getWellbeingStatus(score: Int?): WellbeingStatus {
    return when {
        score == null -> WellbeingStatus.NoData
        score <= 20 -> WellbeingStatus.Critical
        score <= 40 -> WellbeingStatus.Low
        score <= 60 -> WellbeingStatus.Moderate
        else -> WellbeingStatus.Good
    }
}

@Composable
fun PatientsListScreen(
    viewModel: CaregiverViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    onPatientClick: (String) -> Unit,
    onScanQr: () -> Unit,
    showBottomNav: Boolean = true
) {
    val patients by viewModel.patients.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPatientsLoading by viewModel.isPatientsLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val toastState = rememberRelaxToastState()
    var query by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current
    val listState = rememberLazyListState()

    LaunchedEffect(Unit) {
        viewModel.observeCaregiverData()
    }

    LaunchedEffect(Unit) {
        ScrollToTopEvents.requests.collect { route ->
            if (route == Screen.PatientsList.route) {
                listState.animateScrollToItem(0)
            }
        }
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
                val fullName = "${summary.patient.name} ${summary.patient.lastName}".trim().lowercase()
                val condition = summary.patient.condition.lowercase()
                fullName.contains(normalizedQuery) || condition.contains(normalizedQuery)
            }
        }
    }

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
                if (showBottomNav) {
                    RelaxBottomNav(
                        selectedRoute = Screen.PatientsList.route,
                        onNavigate = onNavigate,
                        role = AppRole.CAREGIVER
                    )
                }
            }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(SoftLavender, Color(0xFFF8F8FD))
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp)
            ) {
                ScreenHeader(
                    title = "Pacientes",
                    subtitle = "Personas vinculadas a tu cuidado",
                    horizontalPadding = 0.dp
                )
                
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item { Spacer(modifier = Modifier.height(8.dp)) }
                    
                    item {
                        PatientSearchBar(
                            query = query,
                            onQueryChange = { query = it },
                            onSearch = { focusManager.clearFocus() }
                        )
                    }

                    item { Spacer(modifier = Modifier.height(16.dp)) }

                    if ((isLoading || isPatientsLoading) && patients.isEmpty() && error == null) {
                        item {
                            PatientListSkeleton(
                                modifier = Modifier.padding(horizontal = 0.dp)
                            )
                        }
                    } else if (error != null && patients.isEmpty()) {
                        item {
                            com.relaxmind.app.ui.components.ErrorStateScreen(
                                message = error ?: "",
                                onRetry = { viewModel.observeCaregiverData() }
                            )
                        }
                    } else if (patients.isEmpty()) {
                        item { EmptyPatientsState(onScanQr = onScanQr) }
                    } else if (filteredPatients.isEmpty()) {
                        item { NoPatientResultsState() }
                    } else {
                        items(filteredPatients, key = { it.patient.id }) { summary ->
                            PatientCard(
                                summary = summary,
                                onClick = { onPatientClick(summary.patient.id) },
                                onAlertClick = { onNavigate(Screen.AlertsHistory.route) }
                            )
                        }
                        item { Spacer(modifier = Modifier.height(82.dp)) }
                    }
                }
            }

            RelaxToastHost(state = toastState)
        }
    }
}

@Composable
fun PatientsHeader(onBackClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .systemBarsPadding()
            .padding(top = 20.dp, bottom = 16.dp, start = 12.dp, end = 24.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.padding(start = 12.dp)) {
            com.relaxmind.app.ui.components.RelaxBackButton(
                onClick = onBackClick,
                role = com.relaxmind.app.ui.components.AppRole.CAREGIVER
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = stringResource(id = R.string.patients_title),
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 22.sp,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.size(48.dp)) // To balance the layout
    }
}

@Composable
fun PatientSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    onSearch: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(4.dp, RoundedCornerShape(32.dp), spotColor = SoftLavender)
            .background(SoftLavender, RoundedCornerShape(32.dp))
            .padding(horizontal = 20.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Icono buscar",
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = stringResource(id = R.string.patients_search_hint),
                        fontFamily = LexendFontFamily,
                        color = TextSecondary.copy(alpha = 0.7f),
                        fontSize = 16.sp
                    )
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    textStyle = TextStyle(
                        fontFamily = LexendFontFamily,
                        fontSize = 16.sp,
                        color = TextPrimary
                    ),
                    singleLine = true,
                    cursorBrush = SolidColor(CaregiverIndigo),
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                    keyboardActions = KeyboardActions(onSearch = { onSearch() })
                )
            }
        }
    }
}

@Composable
fun PatientCard(
    summary: CaregiverPatientSummary,
    onClick: () -> Unit,
    onAlertClick: () -> Unit
) {
    val patient = summary.patient
    val score = summary.latestScore
    val status = getWellbeingStatus(score)
    val fullName = "${patient.name} ${patient.lastName}".trim().ifBlank { "Paciente" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = CaregiverIndigo.copy(alpha = 0.08f), ambientColor = CaregiverIndigo.copy(alpha = 0.04f))
            .background(Color.White, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PatientAvatarWithStatus(
                avatarUrl = patient.avatarUrl,
                name = fullName,
                status = status
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = fullName,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold, 
                        fontSize = 18.sp,
                        color = TextPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = summary.lastCheckInDate ?: "Sin registro",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = patient.condition.ifBlank { "Sin condición registrada" },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    WellbeingScoreChip(score = score, status = status)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (summary.hasPendingAlert) {
                            IconButton(
                                onClick = onAlertClick,
                                modifier = Modifier.size(32.dp)
                            ) {
                                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Notifications,
                                        contentDescription = "Alerta activa",
                                        tint = AlertRed,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    // Red dot indicator
                                    Box(
                                        modifier = Modifier
                                            .size(10.dp)
                                            .   align(Alignment.TopEnd)
                                            .offset(x = (-4).dp, y = 4.dp)
                                            .clip(CircleShape)
                                            .background(Color.White)
                                            .padding(2.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier.fillMaxSize().background(AlertRed, CircleShape)
                                        )
                                    }
                                }
                            }
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Ver detalle",
                            tint = CaregiverIndigo,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun PatientAvatarWithStatus(
    avatarUrl: String,
    name: String,
    status: WellbeingStatus
) {
    val borderColor = when (status) {
        WellbeingStatus.Good -> GoodGreen
        WellbeingStatus.Moderate -> ModerateYellow
        WellbeingStatus.Low -> LowOrange
        WellbeingStatus.Critical -> CriticalRed
        WellbeingStatus.NoData -> BorderSoft
    }

    Box(
        modifier = Modifier
            .size(88.dp)
            .clip(CircleShape)
            .border(3.dp, borderColor, CircleShape)
            .background(SoftLavender)
    ) {
        if (avatarUrl.startsWith("relaxmind://avatar/")) {
            Image(
                painter = painterResource(id = getAvatarDrawableRes(avatarUrl)),
                contentDescription = "Avatar de $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        } else {
            AsyncImage(
                model = avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=$name&background=F1EDFF&color=4338A8&size=200" },
                contentDescription = "Avatar de $name",
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize().clip(CircleShape)
            )
        }
    }
}

@Composable
fun WellbeingScoreChip(score: Int?, status: WellbeingStatus) {
    val (bgColor, textColor) = when (status) {
        WellbeingStatus.Good -> GoodGreenSoft to GoodGreen
        WellbeingStatus.Moderate -> ModerateYellowSoft to ModerateYellow
        WellbeingStatus.Low -> LowOrangeSoft to LowOrange
        WellbeingStatus.Critical -> CriticalRedSoft to CriticalRed
        WellbeingStatus.NoData -> BorderSoft to TextSecondary
    }

    val text = if (score != null) {
        val category = when (status) {
            WellbeingStatus.Good -> "Bueno"
            WellbeingStatus.Moderate -> "Moderado"
            WellbeingStatus.Low -> "Bajo"
            WellbeingStatus.Critical -> "Crítico"
            WellbeingStatus.NoData -> ""
        }
        "$category $score/100"
    } else {
        "Sin datos"
    }

    Row(
        modifier = Modifier
            .background(bgColor, RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(textColor, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp,
            color = textColor
        )
    }
}

@Composable
fun EmptyPatientsState(onScanQr: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 40.dp)
            .shadow(16.dp, RoundedCornerShape(28.dp), spotColor = CaregiverIndigo.copy(alpha = 0.08f), ambientColor = CaregiverIndigo.copy(alpha = 0.04f))
            .background(Color.White, RoundedCornerShape(28.dp))
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .background(SoftLavender, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = CaregiverIndigo,
                    modifier = Modifier.size(36.dp)
                )
            }
            Text(
                text = "Aún no tienes pacientes vinculados",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Pide al paciente que genere un código QR o un código de 6 dígitos para vincularse contigo.",
                fontFamily = LexendFontFamily,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
            RelaxButton(
                text = stringResource(id = R.string.patients_link_now),
                onClick = onScanQr,
                role = AppRole.CAREGIVER
            )
        }
    }
}

@Composable
fun NoPatientResultsState() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 60.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "No encontramos pacientes con ese nombre",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 16.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Intenta buscar por nombre o condición.",
            fontFamily = LexendFontFamily,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun PatientsLoadingSkeleton() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))
        RelaxLoadingContent(
            message = stringResource(id = R.string.patients_loading),
            compact = true
        )
    }
}
