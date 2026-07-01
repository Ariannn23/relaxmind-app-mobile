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
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.ui.window.Dialog
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import com.relaxmind.app.ui.components.getAvatarDrawableRes
import com.relaxmind.app.Screen
import com.relaxmind.app.data.model.CaregiverAlert
import com.relaxmind.app.data.model.CheckIn
import com.relaxmind.app.data.model.Patient
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.ProgressChart
import com.relaxmind.app.ui.components.ProgressEmptyState
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.ShimmerEffect
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
    onNavigate: (String) -> Unit,
    showBottomNav: Boolean = true
) {
    val patient by viewModel.selectedPatient.collectAsState()
    val checkIns by viewModel.selectedPatientCheckIns.collectAsState()
    val alerts by viewModel.selectedPatientAlerts.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val isPatientDetailLoading by viewModel.isPatientDetailLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val context = LocalContext.current
    val toastState = rememberRelaxToastState()
    var selectedTab by remember { mutableStateOf(PatientDetailTab.PROGRESS) }
    var selectedCheckIn by remember { mutableStateOf<CheckIn?>(null) }

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

    var showUnlinkDialog by remember { mutableStateOf(false) }
    var unlinkErrorMessage by remember { mutableStateOf<String?>(null) }
    var showUnlinkConfirmationDialog by remember { mutableStateOf(false) }

    if (showUnlinkConfirmationDialog) {
        com.relaxmind.app.ui.components.UnlinkNotificationDialog(
            type = com.relaxmind.app.ui.components.UnlinkDialogType.CONFIRMATION,
            otherPartyName = fullName,
            primaryColor = CaregiverPurple,
            onDismissRequest = {
                showUnlinkConfirmationDialog = false
                onNavigateBack()
            }
        )
    }

    if (showUnlinkDialog) {
        UnlinkPatientDialog(
            patientName = fullName,
            onDismiss = {
                showUnlinkDialog = false
                unlinkErrorMessage = null
            },
            onConfirm = { password, reason ->
                viewModel.unlinkPatient(
                    patientId = patientId,
                    passwordConfirm = password,
                    reason = reason,
                    onSuccess = {
                        showUnlinkDialog = false
                        unlinkErrorMessage = null
                        showUnlinkConfirmationDialog = true
                    },
                    onError = { errorMsg ->
                        unlinkErrorMessage = errorMsg
                    }
                )
            },
            isLoading = isLoading,
            errorMessage = unlinkErrorMessage
        )
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
                .background(
                    Brush.verticalGradient(
                        listOf(SoftLavender, Color(0xFFF8F8FD))
                    )
                )
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
                        },
                        onUnlinkClick = { showUnlinkDialog = true }
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
                                    patientName = fullName,
                                    isLoading = isPatientDetailLoading
                                )
                                PatientDetailTab.HISTORY -> PatientHistoryTab(
                                    checkIns = checkIns,
                                    alerts = alerts,
                                    onCheckInClick = { selectedCheckIn = it },
                                    isLoading = isPatientDetailLoading
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
                LoadingIndicator(isCaregiver = true)
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

    selectedCheckIn?.let { checkIn ->
        val relatedAlert = alerts.firstOrNull { alert ->
            alert.matchesCheckIn(checkIn)
        }
        CheckInDetailBottomSheet(
            checkIn = checkIn,
            relatedAlert = relatedAlert,
            onDismiss = { selectedCheckIn = null },
            onViewSosAlert = { alertId ->
                selectedCheckIn = null
                onSosAlertClick(alertId)
            }
        )
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
                    listOf(SoftLavender.copy(alpha = 0.18f), Color.Transparent)
                )
            )
    )
}

@Composable
private fun PatientDetailHeader(
    title: String,
    onBackClick: () -> Unit,
    onCallClick: () -> Unit,
    onUnlinkClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 20.dp, bottom = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        com.relaxmind.app.ui.components.RelaxBackButton(
            onClick = onBackClick,
            role = com.relaxmind.app.ui.components.AppRole.CAREGIVER
        )
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
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            HeaderIconButton(
                contentDescription = "Llamar paciente",
                onClick = onCallClick
            ) {
                Icon(
                    imageVector = Icons.Default.Phone,
                    contentDescription = null,
                    tint = CaregiverPurple,
                    modifier = Modifier.size(24.dp)
                )
            }
            HeaderIconButton(
                contentDescription = "Desvincular paciente",
                onClick = onUnlinkClick
            ) {
                Icon(
                    imageVector = Icons.Default.LinkOff,
                    contentDescription = null,
                    tint = Color(0xFFE11D48),
                    modifier = Modifier.size(24.dp)
                )
            }
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
        if (patient.avatarUrl.startsWith("relaxmind://avatar/")) {
            Image(
                painter = painterResource(id = getAvatarDrawableRes(patient.avatarUrl)),
                contentDescription = "Avatar de $fullName",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(116.dp)
                    .clip(CircleShape)
                    .border(4.dp, scoreStatus(latestScore).color.copy(alpha = 0.82f), CircleShape)
            )
        } else {
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
        }
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
            Spacer(modifier = Modifier.height(10.dp))
            PatientContactPill(
                icon = Icons.Default.Email,
                text = patient.email.ifBlank { "Sin correo registrado" }
            )
            Spacer(modifier = Modifier.height(6.dp))
            PatientContactPill(
                icon = Icons.Default.Phone,
                text = patient.phone.ifBlank { "Sin teléfono registrado" }
            )
        }
    }
}

@Composable
private fun PatientContactPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    val clipboardManager = LocalClipboardManager.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .pointerInput(text) {
                detectTapGestures(
                    onLongPress = {
                        if (!text.startsWith("Sin ")) {
                            clipboardManager.setText(AnnotatedString(text))
                        }
                    }
                )
            }
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = CaregiverPurple,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = text,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = TextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
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
    patientName: String,
    isLoading: Boolean
) {
    var selectedMonth by remember { mutableStateOf(YearMonth.now()) }
    var selectedDay by remember { mutableIntStateOf(LocalDate.now().dayOfMonth) }

    if (isLoading) {
        PatientDetailDataLoadingState()
    } else if (checkIns.isEmpty()) {
        ProgressEmptyState(patientName = patientName)
    } else {
        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
            ProgressChart(
                checkIns = checkIns,
                modifier = Modifier.fillMaxWidth(),
                accentColor = CaregiverPurple
            )
            PatientMonthlyCalendarCard(
                month = selectedMonth,
                checkIns = checkIns,
                selectedDay = selectedDay,
                onSelectedDayChange = { selectedDay = it },
                onPreviousMonth = { selectedMonth = selectedMonth.minusMonths(1) },
                onNextMonth = { selectedMonth = selectedMonth.plusMonths(1) }
            )
        }
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
            MonthNavButton(isPrevious = true, onClick = onPreviousMonth)
            Text(
                text = monthTitle(month),
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 19.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            MonthNavButton(isPrevious = false, onClick = onNextMonth)
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
    isPrevious: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(40.dp)
            .width(40.dp)
            .clip(CircleShape)
            .background(CaregiverPurple.copy(alpha = 0.10f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = if (isPrevious) {
                Icons.AutoMirrored.Filled.ArrowBack
            } else {
                Icons.AutoMirrored.Filled.KeyboardArrowRight
            },
            contentDescription = if (isPrevious) "Mes anterior" else "Mes siguiente",
            tint = CaregiverPurple,
            modifier = Modifier.size(if (isPrevious) 19.dp else 22.dp)
        )
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PatientHistoryTab(
    checkIns: List<CheckIn>,
    alerts: List<CaregiverAlert>,
    onCheckInClick: (CheckIn) -> Unit,
    isLoading: Boolean
) {
    if (isLoading) {
        PatientDetailDataLoadingState()
        return
    } else if (checkIns.isEmpty()) {
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
                generatedAlert = alerts.any { alert -> alert.matchesCheckIn(checkIn) },
                onClick = { onCheckInClick(checkIn) }
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
    val showAlert = generatedAlert || checkIn.score < 25

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClickLabel = "Ver detalle del check-in",
                onClick = onClick
            )
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
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
            Text(
                text = "${checkIn.relativeDateLabel()} · ${checkIn.timeLabel()}",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = TextPrimary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )
            AnimatedVisibility(visible = showAlert, enter = fadeIn(), exit = fadeOut()) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(PatientScoreStatus.CRITICAL.softColor)
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ReportProblem,
                        contentDescription = null,
                        tint = PatientScoreStatus.CRITICAL.color,
                        modifier = Modifier.size(12.dp)
                    )
                    Text(
                        text = "Alerta",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 10.sp,
                        color = PatientScoreStatus.CRITICAL.color,
                        maxLines = 1
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 46.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            PatientScoreChip(score = checkIn.score)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Text(
                    text = "Ver detalle",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = CaregiverPurple
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = null,
                    tint = CaregiverPurple,
                    modifier = Modifier.size(17.dp)
                )
            }
        }
    }
}
@Composable
private fun PatientScoreChip(score: Int) {
    val status = scoreStatus(score)
    Surface(
        shape = RoundedCornerShape(999.dp),
        color = status.softColor
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            Box(modifier = Modifier.size(7.dp).background(status.color, CircleShape))
            Text(
                text = "$score/100 ${status.label}",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 11.sp,
                color = status.color,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
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
private fun PatientDetailDataLoadingState() {
    SoftCard {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.48f)
                        .height(24.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .then(ShimmerEffect())
                )
                Box(
                    modifier = Modifier
                        .width(104.dp)
                        .height(32.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .then(ShimmerEffect())
                )
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.spacedBy(9.dp)
            ) {
                listOf(0.68f, 0.42f, 0.78f, 0.54f, 0.9f, 0.36f, 0.62f).forEach { fraction ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(fraction)
                            .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                            .then(ShimmerEffect())
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(4) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(16.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .then(ShimmerEffect())
                    )
                }
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

private fun CaregiverAlert.matchesCheckIn(checkIn: CheckIn): Boolean {
    if (patientId != checkIn.patientId) return false
    if (type.equals("sos", ignoreCase = true)) return false

    val alertDate = createdAt?.let {
        SimpleDateFormat("yyyy-MM-dd", Locale("es", "ES")).format(it)
    }.orEmpty()

    return createdAtText.contains(checkIn.date) || alertDate == checkIn.date
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckInDetailBottomSheet(
    checkIn: CheckIn,
    relatedAlert: CaregiverAlert?,
    onDismiss: () -> Unit,
    onViewSosAlert: (String) -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val status = scoreStatus(checkIn.score)
    val isSos = relatedAlert?.type?.equals("sos", ignoreCase = true) == true

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
            // Title
            Text(
                text = "Detalle del Check-in",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )

            // Date row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0xFFF6F4FF)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(LavenderPill, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = CaregiverPurple,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Column {
                        Text(
                            text = checkIn.relativeDateLabel(),
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = checkIn.timeLabel(),
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Score row
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = status.softColor
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier.size(40.dp).background(status.color, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${checkIn.score}",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Column {
                        Text(
                            text = "${checkIn.score}/100 — ${status.label}",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = status.color
                        )
                        Text(
                            text = "Rango: ${status.range}",
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            // Alert section
            if (relatedAlert != null) {
                val alertBg = if (isSos) Color(0xFFFFE8EA) else Color(0xFFFFF3DC)
                val alertColor = if (isSos) PatientScoreStatus.CRITICAL.color else PatientScoreStatus.LOW.color
                val alertLabel = if (isSos) "Alerta SOS enviada" else "Alerta de bienestar enviada"
                val alertDesc = if (isSos)
                    "Se activó una alerta de emergencia (SOS) para este check-in."
                else
                    "La puntuación baja activó una alerta de bienestar para este paciente."
                val alertStatusText = if (relatedAlert.resolved) "Atendida ✓" else "Pendiente"
                val alertStatusColor = if (relatedAlert.resolved) Color(0xFF38C172) else alertColor

                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = alertBg
                ) {
                    Column(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = if (isSos) Icons.Default.Warning else Icons.Default.ReportProblem,
                                contentDescription = null,
                                tint = alertColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = alertLabel,
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = alertColor
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Surface(
                                shape = RoundedCornerShape(50),
                                color = alertStatusColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = alertStatusText,
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = alertStatusColor,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                        Text(
                            text = alertDesc,
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary
                        )
                        if (isSos && !relatedAlert.resolved) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Ver alerta SOS →",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = alertColor,
                                modifier = Modifier.clickable { onViewSosAlert(relatedAlert.id) }
                            )
                        }
                    }
                }
            } else {
                // No alert generated
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = Color(0xFFE8F8EF)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF38C172),
                            modifier = Modifier.size(20.dp)
                        )
                        Text(
                            text = "No se generó ninguna alerta para este check-in.",
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

@Composable
fun UnlinkPatientDialog(
    patientName: String,
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null
) {
    var password by remember { mutableStateOf("") }
    var reason by remember { mutableStateOf("") }

    Dialog(onDismissRequest = { if (!isLoading) onDismiss() }) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFFFE4E6), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LinkOff,
                        contentDescription = "Desvincular",
                        tint = Color(0xFFE11D48),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Desvincular Paciente",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "¿Estás seguro que deseas desvincular a $patientName? Ya no tendrás acceso a su información de bienestar.",
                    fontFamily = LexendFontFamily,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                OutlinedTextField(
                    value = reason,
                    onValueChange = { reason = it },
                    label = { Text("Motivo (Opcional)", fontFamily = LexendFontFamily, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CaregiverPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedLabelColor = CaregiverPurple,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = CaregiverPurple
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = LexendFontFamily),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Tu contraseña actual", fontFamily = LexendFontFamily, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Password
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = CaregiverPurple,
                        unfocusedBorderColor = Color(0xFFE2E8F0),
                        focusedLabelColor = CaregiverPurple,
                        unfocusedLabelColor = TextSecondary,
                        cursorColor = CaregiverPurple
                    ),
                    textStyle = androidx.compose.ui.text.TextStyle(fontFamily = LexendFontFamily),
                    singleLine = true
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        fontFamily = LexendFontFamily,
                        color = Color.Red,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)),
                        enabled = !isLoading
                    ) {
                        Text(
                            text = "Cancelar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            color = TextSecondary
                        )
                    }

                    Button(
                        onClick = { onConfirm(password, reason) },
                        modifier = Modifier.weight(1.18f).height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        enabled = !isLoading && password.isNotBlank()
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                color = Color.White,
                                strokeWidth = 2.dp
                            )
                        } else {
                            Text(
                                text = "Desvincular",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 13.sp,
                                color = Color.White,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }
    }
}
