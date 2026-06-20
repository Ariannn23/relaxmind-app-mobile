package com.relaxmind.app.features.caregiver

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.relaxmind.app.data.model.CheckIn
import com.relaxmind.app.data.model.Patient
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.rememberRelaxToastState
import com.relaxmind.app.ui.themes.BorderSoft
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.CaregiverPurple
import com.relaxmind.app.ui.themes.LavenderPill
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.SoftLavender
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private enum class PatientDetailTab(val label: String) {
    PROGRESS("Progreso"),
    HISTORY("Historial")
}

private enum class PatientScoreStatus(
    val label: String,
    val range: String,
    val color: Color,
    val softColor: Color
) {
    GOOD("Bueno", "75 - 100", Color(0xFF38C172), Color(0xFFE8F8EF)),
    MODERATE("Moderado", "50 - 74", Color(0xFFFACC15), Color(0xFFFFF8D8)),
    LOW("Bajo", "25 - 49", Color(0xFFFF7A1A), Color(0xFFFFEBD8)),
    CRITICAL("Muy bajo", "0 - 24", Color(0xFFFF4D5A), Color(0xFFFFE8EA)),
    NO_DATA("Sin datos", "", Color(0xFFE5E7EB), Color(0xFFF3F4F6))
}

@Composable
fun PatientDetailScreen(
    patientId: String,
    viewModel: CaregiverViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onSosAlertClick: (String) -> Unit,
    onNavigate: (String) -> Unit
) {
    val patient by viewModel.selectedPatient.collectAsState()
    val checkIns by viewModel.selectedPatientCheckIns.collectAsState()
    val streak by viewModel.selectedPatientStreak.collectAsState()
    val alerts by viewModel.selectedPatientAlerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val toastState = rememberRelaxToastState()
    var selectedTab by remember { mutableStateOf(PatientDetailTab.PROGRESS) }

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
        containerColor = Color.White,
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
                .background(Color.White)
                .padding(innerPadding)
        ) {
            PatientDetailBackground()

            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .systemBarsPadding()
                    .imePadding()
                    .padding(horizontal = 18.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    PatientDetailHeader(
                        title = fullName,
                        onBackClick = onNavigateBack,
                        onCallClick = {
                            val phone = patient?.phone.orEmpty()
                            if (phone.isNotBlank()) {
                                context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
                            } else {
                                toastState.showError("Número no disponible. Este paciente no tiene un teléfono registrado.")
                            }
                        }
                    )
                }

                if (patient != null) {
                    item {
                        PatientProfileSummary(
                            patient = patient!!,
                            fullName = fullName,
                            latestScore = checkIns.firstOrNull()?.score
                        )
                    }
                    item {
                        PatientDetailTabs(
                            selectedTab = selectedTab,
                            onTabSelected = { selectedTab = it }
                        )
                    }
                    item {
                        AnimatedContent(
                            targetState = selectedTab,
                            label = "patient-detail-tab"
                        ) { tab ->
                            when (tab) {
                                PatientDetailTab.PROGRESS -> PatientProgressTab(
                                    checkIns = checkIns,
                                    streakDays = streak?.currentStreak ?: 0
                                )
                                PatientDetailTab.HISTORY -> PatientHistoryTab(
                                    checkIns = checkIns,
                                    alerts = alerts,
                                    onSosAlertClick = onSosAlertClick
                                )
                            }
                        }
                    }
                    item {
                        CaregiverReadOnlyNotice(
                            compact = selectedTab == PatientDetailTab.HISTORY
                        )
                    }
                    item { Spacer(modifier = Modifier.height(92.dp)) }
                }
            }

            if (isLoading && patient == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = CaregiverIndigo)
                }
            }

            if (!isLoading && patient == null) {
                PatientDetailErrorState(
                    onRetry = { viewModel.loadPatientDetail(patientId) },
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(horizontal = 24.dp)
                )
            }

            RelaxToastHost(state = toastState)
        }
    }
}

@Composable
private fun PatientDetailBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp)
            .background(
                Brush.verticalGradient(
                    listOf(SoftLavender.copy(alpha = 0.38f), Color.White)
                )
            )
    )
}

@Composable
private fun PatientDetailHeader(
    title: String,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        HeaderIconButton(
            contentDescription = "Volver",
            onClick = onBackClick
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = null,
                tint = CaregiverPurple,
                modifier = Modifier.size(30.dp)
            )
        }
        Text(
            text = title,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        HeaderIconButton(
            contentDescription = "Llamar paciente",
            onClick = onCallClick
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = null,
                tint = CaregiverPurple,
                modifier = Modifier.size(30.dp)
            )
        }
    }
}

@Composable
private fun HeaderIconButton(
    contentDescription: String,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = contentDescription,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        content()
    }
}

@Composable
private fun PatientProfileSummary(
    patient: Patient,
    fullName: String,
    latestScore: Int?
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(22.dp)
    ) {
        val avatarName = URLEncoder.encode(fullName, StandardCharsets.UTF_8.toString())
        AsyncImage(
            model = patient.avatarUrl.ifBlank {
                "https://ui-avatars.com/api/?name=$avatarName&background=4338A8&color=fff"
            },
            contentDescription = "Avatar de $fullName",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(116.dp)
                .clip(CircleShape)
                .border(4.dp, scoreStatus(latestScore).color.copy(alpha = 0.82f), CircleShape)
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = fullName,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                color = TextPrimary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = patient.condition.ifBlank { "Sin condición registrada" },
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 16.sp,
                color = TextSecondary,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PatientDetailTabs(
    selectedTab: PatientDetailTab,
    onTabSelected: (PatientDetailTab) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth()) {
            PatientDetailTab.entries.forEach { tab ->
                val selected = selectedTab == tab
                val textColor by animateColorAsState(
                    targetValue = if (selected) CaregiverPurple else Color(0xFF77748D),
                    animationSpec = tween(180),
                    label = "tab-color-${tab.name}"
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .height(58.dp)
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClickLabel = "Tab ${tab.label}",
                            onClick = { onTabSelected(tab) }
                        ),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = tab.label,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 20.sp,
                        color = textColor
                    )
                    Spacer(modifier = Modifier.height(13.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(if (selected) 3.dp else 1.dp)
                            .background(if (selected) CaregiverPurple else BorderSoft)
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientProgressTab(
    checkIns: List<CheckIn>,
    streakDays: Int
) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth) }

    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        PatientMonthlyCalendarCard(
            month = selectedMonth,
            checkIns = checkIns,
            selectedDay = selectedDay,
            onSelectedDayChange = { selectedDay = it },
            onPreviousMonth = { selectedMonth = selectedMonth.minusMonths(1) },
            onNextMonth = { selectedMonth = selectedMonth.plusMonths(1) }
        )
        PatientStreakCard(streakDays = streakDays)
    }
}

@Composable
private fun PatientMonthlyCalendarCard(
    month: YearMonth,
    checkIns: List<CheckIn>,
    selectedDay: Int,
    onSelectedDayChange: (Int) -> Unit,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit
) {
    val monthlyScores = remember(checkIns, month) {
        checkIns.mapNotNull { checkIn ->
            checkIn.localDateOrNull()?.takeIf { it.year == month.year && it.month == month.month }
                ?.let { it.dayOfMonth to checkIn.score }
        }.toMap()
    }

    SoftCard(contentPadding = 16.dp) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            MonthNavButton(label = previousMonthName(month), isPrevious = true, onClick = onPreviousMonth)
            Text(
                text = monthTitle(month),
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = TextPrimary
            )
            MonthNavButton(label = nextMonthName(month), isPrevious = false, onClick = onNextMonth)
        }

        Spacer(modifier = Modifier.height(18.dp))

        val dayLabels: List<String> = listOf("Lun", "Mar", "Mié", "Jue", "Vie", "Sáb", "Dom")
        Row(modifier = Modifier.fillMaxWidth()) {
            dayLabels.forEach { label: String ->
                Text(
                    text = label,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        val firstDayOffset = month.atDay(1).dayOfWeek.value - 1
        val totalSlots = firstDayOffset + month.lengthOfMonth()
        val rows = (totalSlots + 6) / 7

        Column(verticalArrangement = Arrangement.spacedBy(9.dp)) {
            repeat(rows) { row: Int ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    repeat(7) { column: Int ->
                        val slot = row * 7 + column
                        val day = slot - firstDayOffset + 1
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..month.lengthOfMonth()) {
                                PatientScoreDayCircle(
                                    day = day,
                                    score = monthlyScores[day],
                                    selected = day == selectedDay,
                                    onClick = { onSelectedDayChange(day) }
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        PatientScoreLegend()
    }
}

@Composable
private fun MonthNavButton(
    label: String,
    isPrevious: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .height(40.dp)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (isPrevious) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Mes anterior",
                tint = CaregiverPurple,
                modifier = Modifier.size(19.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(
            text = label,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = CaregiverPurple
        )
        if (!isPrevious) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Mes siguiente",
                tint = CaregiverPurple,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun PatientScoreDayCircle(
    day: Int,
    score: Int?,
    selected: Boolean,
    onClick: () -> Unit
) {
    val status = scoreStatus(score)
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        animationSpec = tween(180),
        label = "day-scale-$day"
    )

    Box(
        modifier = Modifier
            .size(38.dp)
            .scale(scale)
            .clip(CircleShape)
            .background(if (score == null) status.color.copy(alpha = 0.65f) else status.color)
            .border(
                width = if (selected) 2.dp else 0.dp,
                color = if (selected) CaregiverPurple else Color.Transparent,
                shape = CircleShape
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "Día $day, ${status.label}",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = day.toString(),
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
            color = if (score == null) Color(0xFF8B90A0) else Color.White
        )
    }
}

@Composable
private fun PatientScoreLegend() {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            listOf(
                PatientScoreStatus.GOOD,
                PatientScoreStatus.MODERATE,
                PatientScoreStatus.LOW,
                PatientScoreStatus.CRITICAL
            ).forEach { status ->
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(9.dp).background(status.color, CircleShape))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = status.label.removePrefix("Muy "),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp,
                            color = TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))
                    Text(
                        text = status.range,
                        fontFamily = LexendFontFamily,
                        fontSize = 10.sp,
                        color = TextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun PatientStreakCard(streakDays: Int) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        color = Color(0xFFF6F4FF)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 18.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Box(
                modifier = Modifier.size(48.dp).background(Color(0xFFFFE8EA), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = null,
                    tint = Color(0xFFFF7A1A),
                    modifier = Modifier.size(28.dp)
                )
            }
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Racha actual: ",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "$streakDays días",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = CaregiverPurple
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Sigue así, cada día cuenta.",
                    fontFamily = LexendFontFamily,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun PatientHistoryTab(
    checkIns: List<CheckIn>,
    alerts: List<CaregiverAlert>,
    onSosAlertClick: (String) -> Unit
) {
    if (checkIns.isEmpty()) {
        EmptyPatientDetailState(
            title = "Aún no hay historial de check-ins",
            description = "Los registros aparecerán cuando el paciente complete su check-in."
        )
        return
    }

    SoftCard(contentPadding = 0.dp) {
        checkIns.take(10).forEachIndexed { index, checkIn ->
            PatientHistoryItem(
                checkIn = checkIn,
                generatedAlert = alerts.any { alert -> alert.patientId == checkIn.patientId && alert.createdAtText.contains(checkIn.date) },
                onClick = {
                    alerts.firstOrNull { it.type.equals("sos", ignoreCase = true) && it.patientId == checkIn.patientId }
                        ?.let { onSosAlertClick(it.id) }
                }
            )
            if (index < checkIns.take(10).lastIndex) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 22.dp)
                        .height(1.dp)
                        .background(BorderSoft)
                )
            }
        }
    }
}

@Composable
private fun PatientHistoryItem(
    checkIn: CheckIn,
    generatedAlert: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Box(
            modifier = Modifier.size(36.dp).background(LavenderPill, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CalendarMonth,
                contentDescription = null,
                tint = CaregiverPurple,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${checkIn.relativeDateLabel()} · ${checkIn.timeLabel()}",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextPrimary
            )
            AnimatedVisibility(visible = generatedAlert || checkIn.score < 25, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    modifier = Modifier.padding(top = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = PatientScoreStatus.CRITICAL.color,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Alerta enviada",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = PatientScoreStatus.CRITICAL.color
                    )
                }
            }
        }
        PatientScoreChip(score = checkIn.score)
        Row(
            modifier = Modifier.clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "Ver detalle",
                onClick = onClick
            ),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Ver detalle",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = CaregiverPurple
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = CaregiverPurple,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun PatientScoreChip(score: Int) {
    val status = scoreStatus(score)
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = status.softColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(modifier = Modifier.size(8.dp).background(status.color, CircleShape))
            Text(
                text = "$score/100 ${status.label}",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = status.color
            )
        }
    }
}

@Composable
private fun CaregiverReadOnlyNotice(compact: Boolean) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFF6F4FF)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = if (compact) 16.dp else 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = CaregiverIndigo,
                modifier = Modifier.size(22.dp)
            )
            Column {
                if (compact) {
                    Text(
                        text = "Vista de cuidador",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                }
                Text(
                    text = if (compact) {
                        "Puedes ver el historial completo, pero no puedes modificarlo."
                    } else {
                        "Vista de cuidador: solo puedes ver la información, no puedes editarla."
                    },
                    fontFamily = LexendFontFamily,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun EmptyPatientDetailState(title: String, description: String) {
    SoftCard {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = title,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
                fontFamily = LexendFontFamily,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun PatientDetailErrorState(onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 12.dp
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "No pudimos cargar la información del paciente",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Intenta nuevamente.",
                fontFamily = LexendFontFamily,
                fontSize = 14.sp,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Reintentar",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                color = CaregiverPurple,
                modifier = Modifier.clickable(onClick = onRetry)
            )
        }
    }
}

@Composable
private fun SoftCard(
    contentPadding: androidx.compose.ui.unit.Dp = 22.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color.Black.copy(alpha = 0.06f),
                spotColor = Color.Black.copy(alpha = 0.06f)
            ),
        shape = RoundedCornerShape(28.dp),
        color = Color.White
    ) {
        Column(modifier = Modifier.padding(contentPadding), content = content)
    }
}

private fun scoreStatus(score: Int?): PatientScoreStatus = when {
    score == null -> PatientScoreStatus.NO_DATA
    score >= 75 -> PatientScoreStatus.GOOD
    score >= 50 -> PatientScoreStatus.MODERATE
    score >= 25 -> PatientScoreStatus.LOW
    else -> PatientScoreStatus.CRITICAL
}

private fun monthTitle(month: YearMonth): String {
    val monthName = month.month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }
    return "$monthName ${month.year}"
}

private fun previousMonthName(month: YearMonth): String =
    month.minusMonths(1).month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

private fun nextMonthName(month: YearMonth): String =
    month.plusMonths(1).month.getDisplayName(TextStyle.FULL, Locale("es", "ES"))
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale("es", "ES")) else it.toString() }

private fun CheckIn.localDateOrNull(): LocalDate? =
    runCatching { LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE) }.getOrNull()

private fun CheckIn.relativeDateLabel(): String {
    val checkInDate = localDateOrNull() ?: return date.ifBlank { "Sin fecha" }
    val diff = java.time.temporal.ChronoUnit.DAYS.between(checkInDate, LocalDate.now()).toInt()
    return when (diff) {
        0 -> "Hoy"
        1 -> "Ayer"
        in 2..6 -> "Hace $diff días"
        else -> checkInDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    }
}

private fun CheckIn.timeLabel(): String {
    val created = createdAt ?: return "--:--"
    return SimpleDateFormat("HH:mm", Locale("es", "ES")).format(created)
}
