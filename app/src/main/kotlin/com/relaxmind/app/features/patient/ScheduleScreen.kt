package com.relaxmind.app.features.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.Screen
import com.relaxmind.app.data.model.Appointment
import com.relaxmind.app.data.model.DiaryEntry
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxCard
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.PatientGreen
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val selectedDateAppointments by viewModel.selectedDateAppointments.collectAsState()
    val monthlyAppointments by viewModel.monthlyAppointments.collectAsState()
    val monthlyDiaryEntries by viewModel.monthlyDiaryEntries.collectAsState()

    var selectedTabIndex by remember { mutableStateOf(0) } // 0 = Semana, 1 = Mes
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var calendarYearMonth by remember { mutableStateOf(LocalDate.now()) }

    // Bottom sheet state for Month View
    var showBottomSheet by remember { mutableStateOf(false) }
    var bottomSheetDate by remember { mutableStateOf(LocalDate.now()) }
    val bottomSheetState = rememberModalBottomSheetState()

    LaunchedEffect(selectedDate) {
        viewModel.loadAppointmentsForDate(selectedDate.toString())
    }

    LaunchedEffect(calendarYearMonth) {
        viewModel.loadMonthlyEvents(calendarYearMonth.year, calendarYearMonth.monthValue)
    }

    val tabs = listOf("Semana", "Mes")

    Scaffold(
        topBar = {
            RelaxTopBar(title = "Agenda")
        },
        bottomBar = {
            RelaxBottomNav(
                selectedRoute = "patient/schedule",
                onNavigate = onNavigate,
                role = AppRole.PATIENT
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.CreateAppointment.route) },
                containerColor = PatientGreen,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo evento")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7FAFC))
        ) {
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = Color.White,
                contentColor = PatientGreen,
                indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = PatientGreen
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (selectedTabIndex == 0) {
                // VISTA SEMANAL
                val selectedDateDiaryEntry = monthlyDiaryEntries.find { it.date == selectedDate.toString() }
                WeeklyView(
                    selectedDate = selectedDate,
                    appointments = selectedDateAppointments,
                    diaryEntry = selectedDateDiaryEntry,
                    onDateSelected = { selectedDate = it },
                    onAppointmentClick = { onNavigate(Screen.AppointmentDetail.createRoute(it.id)) }
                )
            } else {
                // VISTA MENSUAL
                MonthlyView(
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

        if (showBottomSheet) {
            val dateStr = bottomSheetDate.toString()
            val dayAppointments = monthlyAppointments.filter { it.date == dateStr }.sortedBy { it.time }
            val dayDiary = monthlyDiaryEntries.find { it.date == dateStr }

            ModalBottomSheet(
                onDismissRequest = { showBottomSheet = false },
                sheetState = bottomSheetState,
                containerColor = Color.White
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
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
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
                                    color = Color.Gray,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    } else {
                        if (dayAppointments.isNotEmpty()) {
                            Text(
                                text = "Eventos del día",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 8.dp)
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
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.DarkGray,
                                modifier = Modifier.padding(bottom = 8.dp)
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
private fun WeeklyView(
    selectedDate: LocalDate,
    appointments: List<Appointment>,
    diaryEntry: DiaryEntry?,
    onDateSelected: (LocalDate) -> Unit,
    onAppointmentClick: (Appointment) -> Unit
) {
    val today = LocalDate.now()
    val monday = selectedDate.with(java.time.DayOfWeek.MONDAY)
    val weekDays = remember(monday) { (0..6).map { monday.plusDays(it.toLong()) } }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Week days selector row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            val weekdaysLabels = listOf("L", "M", "X", "J", "V", "S", "D")
            weekDays.forEachIndexed { index, date ->
                val isSelected = date == selectedDate
                val isToday = date == today

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = weekdaysLabels[index],
                        style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                        color = Color.Gray,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(
                                when {
                                    isSelected -> PatientGreen
                                    else -> Color.Transparent
                                }
                            )
                            .border(
                                width = if (isToday && !isSelected) 1.dp else 0.dp,
                                color = if (isToday && !isSelected) PatientGreen else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { onDateSelected(date) },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = date.dayOfMonth.toString(),
                            color = if (isSelected) Color.White else MaterialTheme.colorSurfaceForApp(),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Selected Date Summary Block
        val dayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }
        val dateFormatted = "${dayName}, ${selectedDate.dayOfMonth} de ${selectedDate.month.getDisplayName(TextStyle.FULL, Locale("es"))}"

        RelaxCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFE8F5F0)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = PatientGreen,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = dateFormatted,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PatientGreen
                    )
                    Text(
                        text = if (appointments.isEmpty()) "No tienes eventos programados" else "Tienes ${appointments.size} eventos programados",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Selected Date Events and Diary List
        if (appointments.isEmpty() && diaryEntry == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = Color.LightGray,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Sin eventos ni entradas de diario",
                        color = Color.DarkGray,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "¡Disfruta tu día!",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (appointments.isNotEmpty()) {
                    item {
                        Text(
                            text = "Eventos del día",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
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
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray,
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
private fun MaterialTheme.colorSurfaceForApp(): Color {
    return this.colorScheme.onSurface
}

@Composable
private fun MonthlyView(
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

    // Map events and diaries by day
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
            .padding(16.dp)
    ) {
        // Month Selector Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { onMonthChange(currentMonth.minusMonths(1)) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Mes anterior", tint = PatientGreen)
            }
            Text(
                text = "${currentMonth.month.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }} $year",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = PatientGreen
            )
            IconButton(onClick = { onMonthChange(currentMonth.plusMonths(1)) }) {
                Icon(imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Siguiente mes", tint = PatientGreen)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Calendar Weekdays Headers L M X J V S D
        val weekdays = listOf("L", "M", "X", "J", "V", "S", "D")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold,
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
            modifier = Modifier.fillMaxWidth(),
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
                            .background(Color.White)
                            .border(
                                width = if (isCellToday) 2.dp else 1.dp,
                                color = if (isCellToday) PatientGreen else Color(0xFFE2E8F0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { onDayClick(dayNumber) },
                        contentAlignment = Alignment.Center
                    ) {
                        // Background image for diary entry
                        if (diaryPhoto != null) {
                            AsyncImage(
                                model = diaryPhoto,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxSize()
                                    .alpha(0.4f)
                            )
                        }

                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(
                                text = dayNumber.toString(),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isCellToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isCellToday) PatientGreen else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            // Colored dots for appointments
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val hasCita = dayAppointments.any { it.type == "cita" }
                                val hasMed = dayAppointments.any { it.type == "medicacion" }
                                val hasRec = dayAppointments.any { it.type == "recordatorio" }

                                if (hasCita) Dot(color = Color(0xFF38A169)) // Green
                                if (hasMed) Dot(color = Color(0xFF3182CE)) // Blue
                                if (hasRec) Dot(color = Color(0xFFDD6B20)) // Orange
                                if (hasDiary) Dot(color = Color(0xFF805AD5)) // Purple for wellness/diary
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Grid Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            LegendItem(color = Color(0xFF38A169), text = "Citas")
            LegendItem(color = Color(0xFF3182CE), text = "Medicación")
            LegendItem(color = Color(0xFFDD6B20), text = "Recordatorios")
            LegendItem(color = Color(0xFF805AD5), text = "Bienestar")
        }
    }
}

@Composable
private fun Dot(color: Color) {
    Box(
        modifier = Modifier
            .size(5.dp)
            .background(color, CircleShape)
    )
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color.Gray
        )
    }
}

@Composable
private fun AppointmentItem(
    appointment: Appointment,
    onClick: () -> Unit
) {
    val dotColor = when (appointment.type) {
        "cita" -> Color(0xFF38A169) // Green
        "medicacion" -> Color(0xFF3182CE) // Blue
        else -> Color(0xFFDD6B20) // Orange
    }

    val typeLabel = when (appointment.type) {
        "cita" -> if (appointment.category.isNotBlank()) appointment.category else "Cita médica"
        "medicacion" -> "Medicación"
        else -> "Recordatorio"
    }

    val badgeBgColor = when (appointment.type) {
        "cita" -> Color(0xFFE8F5F0)
        "medicacion" -> Color(0xFFEBF8FF)
        else -> Color(0xFFFFFAF0)
    }

    val badgeTextColor = when (appointment.type) {
        "cita" -> Color(0xFF2F855A)
        "medicacion" -> Color(0xFF2B6CB0)
        else -> Color(0xFFC05621)
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Hour
                Text(
                    text = appointment.time,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (appointment.completed) Color.LightGray else MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(16.dp))
                // Colored dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(if (appointment.completed) Color.LightGray else dotColor, CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                // Title
                Text(
                    text = appointment.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (appointment.completed) Color.Gray else MaterialTheme.colorScheme.onSurface,
                    textDecoration = if (appointment.completed) TextDecoration.LineThrough else TextDecoration.None,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tag badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (appointment.completed) Color(0xFFF2F4F8) else badgeBgColor)
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = typeLabel,
                    color = if (appointment.completed) Color.Gray else badgeTextColor,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp
                )
            }
        }
    }
}

@Composable
private fun DiaryEntryCard(
    diaryEntry: DiaryEntry,
    modifier: Modifier = Modifier
) {
    val emoji = getEmotionEmoji(diaryEntry.emotion)
    RelaxCard(
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(PatientGreen.copy(alpha = 0.08f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = emoji, fontSize = 20.sp)
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Mi Diario",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PatientGreen
                        )
                        Text(
                            text = "Sintiéndome ${diaryEntry.emotion.lowercase()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray
                        )
                    }
                }
                
                // Tag Badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PatientGreen.copy(alpha = 0.08f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = diaryEntry.category,
                        color = PatientGreen,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Notes
            if (diaryEntry.notes.isNotBlank()) {
                Text(
                    text = diaryEntry.notes,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.DarkGray,
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
                    .clip(RoundedCornerShape(12.dp))
            )
        }
        size == 2 -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = photoUrls[0],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                AsyncImage(
                    model = photoUrls[1],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1f)
                        .height(120.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
            }
        }
        size == 3 -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = photoUrls[0],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1.5f)
                        .height(160.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = photoUrls[1],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    AsyncImage(
                        model = photoUrls[2],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(76.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
        size == 4 -> {
            Column(
                modifier = modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = photoUrls[0],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    AsyncImage(
                        model = photoUrls[1],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AsyncImage(
                        model = photoUrls[2],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                    AsyncImage(
                        model = photoUrls[3],
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }
            }
        }
        size >= 5 -> {
            Row(
                modifier = modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AsyncImage(
                    model = photoUrls[0],
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .weight(1.2f)
                        .height(180.dp)
                        .clip(RoundedCornerShape(12.dp))
                )
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = photoUrls[1],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .height(86.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        AsyncImage(
                            model = photoUrls[2],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .height(86.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AsyncImage(
                            model = photoUrls[3],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .height(86.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                        AsyncImage(
                            model = photoUrls[4],
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .weight(1f)
                                .height(86.dp)
                                .clip(RoundedCornerShape(12.dp))
                        )
                    }
                }
            }
        }
    }
}

private fun getEmotionEmoji(emotion: String): String {
    return when (emotion) {
        "Ansioso" -> "😟"
        "Tranquilo" -> "😌"
        "Feliz" -> "😊"
        "Triste" -> "😢"
        "Frustrado" -> "😤"
        "Emocionado" -> "🤩"
        else -> "📝"
    }
}

