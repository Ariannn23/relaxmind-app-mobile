package com.relaxmind.app.features.patient

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.R
import com.relaxmind.app.Screen
import com.relaxmind.app.data.model.Appointment
import com.relaxmind.app.data.model.DiaryEntry
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*
import java.time.LocalDate
import java.time.format.TextStyle
import java.time.format.DateTimeFormatter
import java.util.Locale

// Event colors
private val MedicalGreen = Color(0xFF0F6E56)
private val MedicationBlue = Color(0xFF1E88E5)
private val ReminderOrange = Color(0xFFFF9800)
private val DiaryPurple = Color(0xFF8B5CF6)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val selectedDateAppointments by viewModel.selectedDateAppointments.collectAsState()
    val monthlyAppointments by viewModel.monthlyAppointments.collectAsState()
    val monthlyDiaryEntries by viewModel.monthlyDiaryEntries.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Semana, 1 = Calendario
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var calendarYearMonth by remember { mutableStateOf(LocalDate.now()) }

    // Bottom sheet state for Month Views
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetDate by remember { mutableStateOf(LocalDate.now()) }
    val bottomSheetState = rememberModalBottomSheetState()

    LaunchedEffect(selectedDate) {
        viewModel.loadAppointmentsForDate(selectedDate.toString())
        if (selectedDate.year != calendarYearMonth.year || selectedDate.monthValue != calendarYearMonth.monthValue) {
            calendarYearMonth = selectedDate.withDayOfMonth(1)
        }
    }

    LaunchedEffect(calendarYearMonth) {
        viewModel.loadMonthlyEvents(calendarYearMonth.year, calendarYearMonth.monthValue)
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner, selectedDate, calendarYearMonth) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                viewModel.loadAppointmentsForDate(selectedDate.toString())
                viewModel.loadMonthlyEvents(calendarYearMonth.year, calendarYearMonth.monthValue)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val tabs = listOf("Semana", "Calendario")

    Scaffold(
        containerColor = Color.Transparent,
        bottomBar = {
            RelaxBottomNav(
                selectedRoute = "patient/schedule",
                onNavigate = onNavigate,
                role = AppRole.PATIENT
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SoftGradientBackground(animateBlobs = true)

            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Header area with "+" button
                ScheduleHeader(
                    selectedTabIndex = selectedTabIndex,
                    onAddClick = { onNavigate(Screen.CreateAppointment.route) }
                )

                // Custom Pill-shaped Tab Selector (Soft UI)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp)
                        .background(Color(0xFFE6E8EF).copy(alpha = 0.5f), RoundedCornerShape(18.dp))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    tabs.forEachIndexed { index, title ->
                        val isSelected = selectedTabIndex == index
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(38.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (isSelected) Color.White else Color.Transparent)
                                .clickable { selectedTabIndex = index },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = title,
                                fontFamily = LexendFontFamily,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 13.sp,
                                color = if (isSelected) PatientGreen else TextSecondary
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Content switching
                when (selectedTabIndex) {
                    0 -> {
                        // WEEKLY VIEW
                        val selectedDateDiaryEntry = monthlyDiaryEntries.find { it.date == selectedDate.toString() }
                        WeeklyView(
                            selectedDate = selectedDate,
                            appointments = selectedDateAppointments,
                            monthlyAppointments = monthlyAppointments,
                            diaryEntry = selectedDateDiaryEntry,
                            monthlyDiaryEntries = monthlyDiaryEntries,
                            onDateSelected = { selectedDate = it },
                            onAppointmentClick = { onNavigate(Screen.AppointmentDetail.createRoute(it.id)) }
                        )
                    }
                    1 -> {
                        // MONTHLY VIEW WITH DIARY COLLAGE
                        MonthlyViewCollage(
                            currentMonth = calendarYearMonth,
                            appointments = monthlyAppointments,
                            diaryEntries = monthlyDiaryEntries,
                            onMonthChange = { calendarYearMonth = it },
                            onDayClick = { day ->
                                bottomSheetDate = calendarYearMonth.withDayOfMonth(day)
                                showBottomSheet = true
                            }
                        )
                    }
                }
            }
        }

        // Bottom Sheet showing events & diary entry for selected day in month views
        if (showBottomSheet) {
            val dateStr = bottomSheetDate.toString()
            val dayAppointments = monthlyAppointments.filter { it.date == dateStr }.sortedBy { it.time }
            val dayDiary = monthlyDiaryEntries.find { it.date == dateStr }

            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState,
                containerColor = Color.White,
                shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    val dayName = bottomSheetDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }
                    val dateFormatted = "${dayName}, ${bottomSheetDate.dayOfMonth} de ${bottomSheetDate.month.getDisplayName(TextStyle.FULL, Locale("es"))}"

                    Text(
                        text = dateFormatted,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    if (dayAppointments.isEmpty() && dayDiary == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 40.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    imageVector = Icons.Default.CalendarToday,
                                    contentDescription = null,
                                    tint = Color.LightGray,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Sin eventos ni entradas de diario",
                                    fontFamily = LexendFontFamily,
                                    color = TextSecondary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    } else {
                        if (dayAppointments.isNotEmpty()) {
                            Text(
                                text = "Eventos del día",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            Column(
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                dayAppointments.forEach { appt ->
                                    AppointmentItem(
                                        appointment = appt,
                                        onClick = {
                                            showBottomSheet = false
                                            onNavigate(Screen.AppointmentDetail.createRoute(appt.id))
                                        }
                                    )
                                }
                            }
                        }

                        if (dayDiary != null) {
                            if (dayAppointments.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(24.dp))
                            }
                            Text(
                                text = "Mi registro de bienestar",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary,
                                modifier = Modifier.padding(bottom = 12.dp)
                            )
                            DiaryEntryCard(diaryEntry = dayDiary)
                        }
                    }
                    Spacer(modifier = Modifier.height(40.dp))
                }
            }
        }
    }
}

@Composable
private fun ScheduleHeader(
    selectedTabIndex: Int,
    onAddClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Agenda",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (selectedTabIndex) {
                        0 -> "Tu semana, organizada y en calma"
                        else -> "Tus eventos, hábitos y momentos importantes"
                    },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
            
            // Circular Add Button (FAB styled, Soft UI green gradient)
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = PatientGreen.copy(alpha = 0.35f),
                        spotColor = PatientGreen.copy(alpha = 0.35f)
                    )
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(PatientGreenLight, PatientGreen)
                        ),
                        shape = CircleShape
                    )
                    .clickable(onClick = onAddClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Nuevo evento",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun WeeklyView(
    selectedDate: LocalDate,
    appointments: List<Appointment>,
    monthlyAppointments: List<Appointment>,
    diaryEntry: DiaryEntry?,
    monthlyDiaryEntries: List<DiaryEntry>,
    onDateSelected: (LocalDate) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    val today = LocalDate.now()
    val monday = selectedDate.with(java.time.DayOfWeek.MONDAY)
    val weekDays = remember(monday) { (0..6).map { monday.plusDays(it.toLong()) } }
    val eventDates = remember(monthlyAppointments) {
        monthlyAppointments.mapNotNull { appointment ->
            runCatching { LocalDate.parse(appointment.date) }.getOrNull()
        }.toSet()
    }
    val diaryDates = remember(monthlyDiaryEntries) {
        monthlyDiaryEntries.mapNotNull { entry ->
            runCatching { LocalDate.parse(entry.date) }.getOrNull()
        }.toSet()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Week days selector card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.15f),
                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 18.dp, horizontal = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val weekdaysLabels = listOf("L", "M", "X", "J", "V", "S", "D")
                weekDays.forEachIndexed { index, date ->
                    val isSelected = date == selectedDate
                    val isToday = date == today
                    val hasEvents = eventDates.contains(date)
                    val hasDiary = diaryDates.contains(date)

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .weight(1f)
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) { onDateSelected(date) }
                    ) {
                        Text(
                            text = weekdaysLabels[index],
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 12.sp,
                            color = if (isSelected) TextPrimary else TextSecondary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .background(
                                    color = if (isSelected) PatientGreen else Color.Transparent,
                                    shape = CircleShape
                                )
                                .border(
                                    width = if (isToday && !isSelected) 1.5.dp else 0.dp,
                                    color = if (isToday && !isSelected) PatientGreen else Color.Transparent,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = date.dayOfMonth.toString(),
                                fontFamily = LexendFontFamily,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 14.sp,
                                color = if (isSelected) Color.White else TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Box(
                            modifier = Modifier
                                .size(if (hasEvents || hasDiary) 6.dp else 4.dp)
                                .background(
                                    color = when {
                                        hasEvents -> PatientGreen
                                        hasDiary -> DiaryPurple
                                        isSelected -> PatientGreen.copy(alpha = 0.45f)
                                        else -> Color.Transparent
                                    },
                                    shape = CircleShape
                                )
                        )
                    }
                }
            }
        }

        // Selected Date Summary Block
        val dayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }
        val monthName = selectedDate.month.getDisplayName(TextStyle.FULL, Locale("es"))
        val dateFormatted = "${dayName} ${selectedDate.dayOfMonth} de ${monthName}"

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .background(SoftMint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = PatientGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = dateFormatted,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = if (appointments.isEmpty()) "Sin eventos programados" else "${appointments.size} eventos programados",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = PatientGreen
                )
            }
        }

        // Events list or placeholder empty state
        if (appointments.isEmpty() && diaryEntry == null) {
            EmptyEventsPlaceholder()
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                if (appointments.isNotEmpty()) {
                    items(appointments) { appt ->
                        AppointmentItem(
                            appointment = appt,
                            onClick = { onAppointmentClick(appt) }
                        )
                    }
                }

                if (diaryEntry != null) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Mi registro de bienestar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    item {
                        DiaryEntryCard(diaryEntry = diaryEntry)
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyEventsPlaceholder() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.05f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.05f)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 40.dp, horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val sizePx = size.width
                    drawRoundRect(
                        color = PatientGreen.copy(alpha = 0.2f),
                        size = androidx.compose.ui.geometry.Size(sizePx * 0.6f, sizePx * 0.6f),
                        topLeft = androidx.compose.ui.geometry.Offset(sizePx * 0.2f, sizePx * 0.25f),
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                        style = Stroke(width = 3.dp.toPx())
                    )
                    drawCircle(
                        color = PatientGreen.copy(alpha = 0.3f),
                        radius = 3.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(sizePx * 0.35f, sizePx * 0.22f)
                    )
                    drawCircle(
                        color = PatientGreen.copy(alpha = 0.3f),
                        radius = 3.dp.toPx(),
                        center = androidx.compose.ui.geometry.Offset(sizePx * 0.65f, sizePx * 0.22f)
                    )
                    val path = androidx.compose.ui.graphics.Path().apply {
                        moveTo(sizePx * 0.4f, sizePx * 0.55f)
                        lineTo(sizePx * 0.48f, sizePx * 0.63f)
                        lineTo(sizePx * 0.62f, sizePx * 0.45f)
                    }
                    drawPath(
                        path = path,
                        color = PatientGreen.copy(alpha = 0.4f),
                        style = Stroke(width = 3.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Sin eventos para el resto del día",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "Disfruta tu día",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun MonthlyViewSimple(
    currentMonth: LocalDate,
    appointments: List<Appointment>,
    diaryEntries: List<DiaryEntry>,
    onMonthChange: (LocalDate) -> Unit,
    onDayClick: (Int) -> Unit
) {
    val year = currentMonth.year
    val month = currentMonth.monthValue

    val firstDayOfMonth = remember(year, month) { LocalDate.of(year, month, 1) }
    val daysInMonth = remember(year, month) { firstDayOfMonth.lengthOfMonth() }
    val firstDayOfWeek = remember(year, month) { firstDayOfMonth.dayOfWeek.value } // 1 = Lunes, ..., 7 = Domingo
    val emptyCellsBefore = firstDayOfWeek - 1

    val today = remember { LocalDate.now() }

    val appointmentsByDay = remember(appointments) {
        appointments.groupBy { LocalDate.parse(it.date).dayOfMonth }
    }
    val diaryHasEntryByDay = remember(diaryEntries) {
        diaryEntries.associate { LocalDate.parse(it.date).dayOfMonth to true }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.15f),
                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Mes anterior",
                            tint = PatientGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }} $year",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Siguiente mes",
                            tint = PatientGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekdays headers
                val weekdays = listOf("L", "M", "X", "J", "V", "S", "D")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid
                val totalCells = emptyCellsBefore + daysInMonth
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(totalCells) { index ->
                        if (index < emptyCellsBefore) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val dayNumber = index - emptyCellsBefore + 1
                            val dayAppointments = appointmentsByDay[dayNumber] ?: emptyList()
                            val hasDiary = diaryHasEntryByDay[dayNumber] == true
                            val isCellToday = today.year == year && today.monthValue == month && today.dayOfMonth == dayNumber

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        color = if (isCellToday) PatientGreen else Color(0xFFF7FAFC)
                                    )
                                    .clickable { onDayClick(dayNumber) },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontFamily = LexendFontFamily,
                                        fontWeight = if (isCellToday) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isCellToday) Color.White else TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Dots row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val hasCita = dayAppointments.any { it.type == "cita" }
                                        val hasMed = dayAppointments.any { it.type == "medicacion" }
                                        val hasRec = dayAppointments.any { it.type == "recordatorio" }

                                        if (hasCita) Box(modifier = Modifier.size(4.dp).background(MedicalGreen, CircleShape))
                                        if (hasMed) Box(modifier = Modifier.size(4.dp).background(MedicationBlue, CircleShape))
                                        if (hasRec) Box(modifier = Modifier.size(4.dp).background(ReminderOrange, CircleShape))
                                        if (hasDiary) Box(modifier = Modifier.size(4.dp).background(DiaryPurple, CircleShape))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Toca un día hint & Legend
        CalendarInfoAndLegend()
    }
}

@Composable
private fun MonthlyViewCollage(
    currentMonth: LocalDate,
    appointments: List<Appointment>,
    diaryEntries: List<DiaryEntry>,
    onMonthChange: (LocalDate) -> Unit,
    onDayClick: (Int) -> Unit
) {
    val year = currentMonth.year
    val month = currentMonth.monthValue

    val firstDayOfMonth = remember(year, month) { LocalDate.of(year, month, 1) }
    val daysInMonth = remember(year, month) { firstDayOfMonth.lengthOfMonth() }
    val firstDayOfWeek = remember(year, month) { firstDayOfMonth.dayOfWeek.value } // 1 = Lunes, ..., 7 = Domingo
    val emptyCellsBefore = firstDayOfWeek - 1

    val today = remember { LocalDate.now() }

    val appointmentsByDay = remember(appointments) {
        appointments.groupBy { LocalDate.parse(it.date).dayOfMonth }
    }
    val diaryPhotoByDay = remember(diaryEntries) {
        diaryEntries
            .filter { it.photoUrls.isNotEmpty() }
            .associate { LocalDate.parse(it.date).dayOfMonth to it.photoUrls.first() }
    }
    val diaryHasEntryByDay = remember(diaryEntries) {
        diaryEntries.associate { LocalDate.parse(it.date).dayOfMonth to true }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(26.dp),
                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.15f),
                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.15f)
                ),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
            ) {
                // Month Selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Mes anterior",
                            tint = PatientGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }} $year",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                            contentDescription = "Siguiente mes",
                            tint = PatientGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weekdays headers
                val weekdays = listOf("L", "M", "X", "J", "V", "S", "D")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    weekdays.forEach { day ->
                        Text(
                            text = day,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Calendar Grid with Collage background images
                val totalCells = emptyCellsBefore + daysInMonth
                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(280.dp),
                    userScrollEnabled = false,
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(totalCells) { index ->
                        if (index < emptyCellsBefore) {
                            Box(modifier = Modifier.aspectRatio(1f))
                        } else {
                            val dayNumber = index - emptyCellsBefore + 1
                            val dayAppointments = appointmentsByDay[dayNumber] ?: emptyList()
                            val diaryPhoto = diaryPhotoByDay[dayNumber]
                            val hasDiary = diaryHasEntryByDay[dayNumber] == true
                            val isCellToday = today.year == year && today.monthValue == month && today.dayOfMonth == dayNumber

                            Box(
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(
                                        color = if (isCellToday) PatientGreen else Color(0xFFF7FAFC)
                                    )
                                    .clickable { onDayClick(dayNumber) },
                                contentAlignment = Alignment.Center
                            ) {
                                // Background image crop with white overlay for readability
                                if (diaryPhoto != null && !isCellToday) {
                                    AsyncImage(
                                        model = diaryPhoto,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .alpha(0.45f)
                                    )
                                }

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ) {
                                    Text(
                                        text = dayNumber.toString(),
                                        fontFamily = LexendFontFamily,
                                        fontWeight = if (isCellToday) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isCellToday) Color.White else TextPrimary
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    // Dots row
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(2.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val hasCita = dayAppointments.any { it.type == "cita" }
                                        val hasMed = dayAppointments.any { it.type == "medicacion" }
                                        val hasRec = dayAppointments.any { it.type == "recordatorio" }

                                        if (hasCita) Box(modifier = Modifier.size(4.dp).background(MedicalGreen, CircleShape))
                                        if (hasMed) Box(modifier = Modifier.size(4.dp).background(MedicationBlue, CircleShape))
                                        if (hasRec) Box(modifier = Modifier.size(4.dp).background(ReminderOrange, CircleShape))
                                        if (hasDiary) Box(modifier = Modifier.size(4.dp).background(DiaryPurple, CircleShape))
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Toca un día hint & Legend
        CalendarInfoAndLegend()
    }
}

@Composable
private fun CalendarInfoAndLegend() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Toca un día hint
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .background(SoftMint, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = PatientGreen,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Toca un día para ver sus eventos",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 12.sp,
                color = TextSecondary
            )
        }

        // Legend row
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(
                    elevation = 2.dp,
                    shape = RoundedCornerShape(16.dp),
                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.05f),
                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.05f)
                ),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = MedicalGreen, text = "Cita médica")
                LegendItem(color = MedicationBlue, text = "Medicación")
                LegendItem(color = ReminderOrange, text = "Recordatorio")
                LegendItem(color = DiaryPurple, text = "Diario")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun AppointmentItem(
    appointment: Appointment,
    onClick: () -> Unit
) {
    val dotColor = when (appointment.type) {
        "cita" -> MedicalGreen
        "medicacion" -> MedicationBlue
        else -> ReminderOrange
    }

    val typeLabel = when (appointment.type) {
        "cita" -> if (appointment.category.isNotBlank()) appointment.category else "Cita médica"
        "medicacion" -> "Medicación"
        else -> "Recordatorio"
    }

    val badgeBgColor = when (appointment.type) {
        "cita" -> SoftMint
        "medicacion" -> Color(0xFFEBF8FF)
        else -> SoftCream
    }

    val badgeTextColor = when (appointment.type) {
        "cita" -> MedicalGreen
        "medicacion" -> MedicationBlue
        else -> ReminderOrange
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.1f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.1f)
            )
            .background(Color.White, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
    ) {
        // Left border indicator box
        Box(
            modifier = Modifier
                .width(6.dp)
                .fillMaxHeight()
                .background(
                    color = if (appointment.completed) Color.LightGray else dotColor,
                    shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp)
                )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp, horizontal = 18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Hour text
                Text(
                    text = appointment.time,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = if (appointment.completed) Color.LightGray else TextPrimary
                )
                Spacer(modifier = Modifier.width(14.dp))
                // Colored dot indicator
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .background(if (appointment.completed) Color.LightGray else dotColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Title text
                Text(
                    text = appointment.title,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = if (appointment.completed) Color.Gray else TextPrimary,
                    textDecoration = if (appointment.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Chip showing type/category with a small icon next to it
            Row(
                modifier = Modifier
                    .background(
                        color = if (appointment.completed) Color(0xFFF2F4F8) else badgeBgColor,
                        shape = RoundedCornerShape(12.dp)
                    )
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Icon(
                    imageVector = when (appointment.type) {
                        "cita" -> Icons.Default.LocalHospital
                        "medicacion" -> Icons.Default.Medication
                        else -> Icons.Default.PushPin
                    },
                    contentDescription = null,
                    tint = if (appointment.completed) Color.Gray else badgeTextColor,
                    modifier = Modifier.size(12.dp)
                )
                Text(
                    text = typeLabel,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 11.sp,
                    color = if (appointment.completed) Color.Gray else badgeTextColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun DiaryEntryCard(
    diaryEntry: DiaryEntry,
    modifier: Modifier = Modifier
) {
    val emotionInitial = diaryEntry.emotion.firstOrNull()?.uppercase() ?: "D"
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(26.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.1f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(SoftMint, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emotionInitial,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = PatientGreen
                        )
                    }
                    Column {
                        Text(
                            text = "Mi Diario",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Sintiéndome ${diaryEntry.emotion.lowercase()}",
                            fontFamily = LexendFontFamily,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }
                
                // Category Tag
                Box(
                    modifier = Modifier
                        .background(SoftMint, RoundedCornerShape(10.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = diaryEntry.category,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 11.sp,
                        color = PatientGreen
                    )
                }
            }

            // Notes content text
            if (diaryEntry.notes.isNotBlank()) {
                Text(
                    text = diaryEntry.notes,
                    fontFamily = LexendFontFamily,
                    fontSize = 14.sp,
                    color = TextPrimary,
                    lineHeight = 20.sp
                )
            }

            // Photo collage
            if (diaryEntry.photoUrls.isNotEmpty()) {
                DiaryCollage(photoUrls = diaryEntry.photoUrls)
            }
        }
    }
}

@Composable
private fun DiaryCollage(
    photoUrls: List<String>,
    modifier: Modifier = Modifier
) {
    val size = photoUrls.size
    when {
        size == 1 -> {
            AsyncImage(
                model = photoUrls[0],
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(16.dp))
            )
        }
        size == 2 -> {
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = photoUrls[0],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                )
                AsyncImage(
                    model = photoUrls[1],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                )
            }
        }
        else -> {
            // More than 2 photos: show first two horizontally, second one gets overlay if more
            Row(
                modifier = modifier
                    .fillMaxWidth()
                    .height(140.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = photoUrls[0],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AsyncImage(
                        model = photoUrls[1],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                    if (size > 2) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "+${size - 1}",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}
