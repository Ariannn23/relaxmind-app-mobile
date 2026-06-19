package com.relaxmind.app.features.patient

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Calendar
import java.util.Locale

@Composable
fun CreateAppointmentScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.error.collectAsState()

    var title by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf("cita") } // "cita" | "medicacion" | "recordatorio"
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(10, 30)) }
    var notes by remember { mutableStateOf("") }
    var reminderTimeMinutes by remember { mutableStateOf(15) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringDays by remember { mutableStateOf<List<Int>>(emptyList()) }

    var isTitleError by remember { mutableStateOf(false) }

    // DatePickerDialog launcher
    val datePickerDialog = remember {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // TimePickerDialog launcher
    val timePickerDialog = remember {
        TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                selectedTime = LocalTime.of(hourOfDay, minute)
            },
            selectedTime.hour,
            selectedTime.minute,
            false // 12-hour format or not. True for 24h, False for 12h with AM/PM
        )
    }

    Scaffold(
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SoftGradientBackground(animateBlobs = true)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Back Button & Header Area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .shadow(
                                elevation = 4.dp,
                                shape = CircleShape,
                                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.15f),
                                spotColor = Color(0xFF8A88A6).copy(alpha = 0.15f)
                            )
                            .background(Color.White, CircleShape)
                            .clickable(onClick = onNavigateBack),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Volver",
                            tint = PatientGreen,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Nuevo evento",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Crea un nuevo evento en tu agenda 🌿",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(20.dp)
                ) {
                    // TÍTULO DEL EVENTO
                    Column {
                        Text(
                            text = "Título del evento",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = title,
                            onValueChange = {
                                title = it
                                if (it.isNotBlank()) isTitleError = false
                            },
                            placeholder = { Text("Ej: Cita con neuróloga", fontFamily = LexendFontFamily, color = Color.LightGray) },
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(
                                    elevation = 3.dp,
                                    shape = RoundedCornerShape(18.dp),
                                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.08f),
                                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.08f)
                                ),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PatientGreen,
                                unfocusedBorderColor = BorderSoft,
                                focusedContainerColor = Color.White,
                                unfocusedContainerColor = Color.White,
                                cursorColor = PatientGreen
                            ),
                            singleLine = true,
                            isError = isTitleError
                        )
                        if (isTitleError) {
                            Text(
                                text = "El título es obligatorio",
                                fontFamily = LexendFontFamily,
                                color = SOSCoral,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
                            )
                        }
                    }

                    // TIPO DE EVENTO SELECTOR
                    Column {
                        Text(
                            text = "Tipo de evento",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            EventTypeCard(
                                title = "Cita médica",
                                icon = Icons.Default.LocalHospital,
                                isSelected = selectedType == "cita",
                                onClick = { selectedType = "cita" },
                                modifier = Modifier.weight(1f)
                            )
                            EventTypeCard(
                                title = "Medicación",
                                icon = Icons.Default.Medication,
                                isSelected = selectedType == "medicacion",
                                onClick = { selectedType = "medicacion" },
                                modifier = Modifier.weight(1f)
                            )
                            EventTypeCard(
                                title = "Recordatorio",
                                icon = Icons.Default.PushPin,
                                isSelected = selectedType == "recordatorio",
                                onClick = { selectedType = "recordatorio" },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    // FECHA Y HORA SELECTORS
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Date Button
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Fecha",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val dayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }
                            val dateFormatted = "${dayName}, ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale("es"))} ${selectedDate.year}"

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(
                                        elevation = 3.dp,
                                        shape = RoundedCornerShape(18.dp),
                                        ambientColor = Color(0xFF8A88A6).copy(alpha = 0.08f),
                                        spotColor = Color(0xFF8A88A6).copy(alpha = 0.08f)
                                    )
                                    .background(Color.White, RoundedCornerShape(18.dp))
                                    .border(1.dp, BorderSoft, RoundedCornerShape(18.dp))
                                    .clickable { datePickerDialog.show() }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = null,
                                        tint = PatientGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = dateFormatted,
                                        fontFamily = LexendFontFamily,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Time Button
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Hora",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            val timeFormatted = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a"))

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(56.dp)
                                    .shadow(
                                        elevation = 3.dp,
                                        shape = RoundedCornerShape(18.dp),
                                        ambientColor = Color(0xFF8A88A6).copy(alpha = 0.08f),
                                        spotColor = Color(0xFF8A88A6).copy(alpha = 0.08f)
                                    )
                                    .background(Color.White, RoundedCornerShape(18.dp))
                                    .border(1.dp, BorderSoft, RoundedCornerShape(18.dp))
                                    .clickable { timePickerDialog.show() }
                                    .padding(horizontal = 16.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Schedule,
                                        contentDescription = null,
                                        tint = PatientGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = timeFormatted,
                                        fontFamily = LexendFontFamily,
                                        fontSize = 13.sp,
                                        color = TextPrimary
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                    contentDescription = null,
                                    tint = TextSecondary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    // REPETIR RECORDATORIO SWITCH
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Repetir recordatorio",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Recordar automáticamente ciertos días",
                                fontFamily = LexendFontFamily,
                                fontSize = 12.sp,
                                color = TextSecondary
                            )
                        }
                        Switch(
                            checked = isRecurring,
                            onCheckedChange = { isRecurring = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PatientGreen,
                                uncheckedThumbColor = Color.Gray,
                                uncheckedTrackColor = Color(0xFFE2E8F0)
                            )
                        )
                    }

                    if (isRecurring) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = "Días a repetir",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = TextPrimary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                val weekdaysList = listOf(
                                    Pair(1, "L"),
                                    Pair(2, "M"),
                                    Pair(3, "M"),
                                    Pair(4, "J"),
                                    Pair(5, "V"),
                                    Pair(6, "S"),
                                    Pair(7, "D")
                                )
                                weekdaysList.forEach { (dayVal, label) ->
                                    val isSelected = recurringDays.contains(dayVal)
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .clip(CircleShape)
                                            .background(
                                                if (isSelected) PatientGreen else Color(0xFFF7FAFC)
                                            )
                                            .border(
                                                width = 1.dp,
                                                color = if (isSelected) PatientGreen else BorderSoft,
                                                shape = CircleShape
                                            )
                                            .clickable {
                                                recurringDays = if (isSelected) {
                                                    recurringDays - dayVal
                                                } else {
                                                    recurringDays + dayVal
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = label,
                                            fontFamily = LexendFontFamily,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // NOTAS
                    Column {
                        Text(
                            text = "Notas",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = notes,
                                onValueChange = { if (it.length <= 500) notes = it },
                                placeholder = { Text("Detalles opcionales...", fontFamily = LexendFontFamily, color = Color.LightGray) },
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(120.dp)
                                    .shadow(
                                        elevation = 3.dp,
                                        shape = RoundedCornerShape(18.dp),
                                        ambientColor = Color(0xFF8A88A6).copy(alpha = 0.08f),
                                        spotColor = Color(0xFF8A88A6).copy(alpha = 0.08f)
                                    ),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PatientGreen,
                                    unfocusedBorderColor = BorderSoft,
                                    focusedContainerColor = Color.White,
                                    unfocusedContainerColor = Color.White,
                                    cursorColor = PatientGreen
                                ),
                                maxLines = 4
                            )
                            Text(
                                text = "${notes.length}/500",
                                fontFamily = LexendFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 8.dp, end = 12.dp)
                            )
                        }
                    }

                    if (errorMsg != null) {
                        Text(
                            text = errorMsg ?: "",
                            fontFamily = LexendFontFamily,
                            color = SOSCoral,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // GUARDAR EVENTO BUTTON (Pill-shaped green with calendar icon)
                    Button(
                        onClick = {
                            if (title.isBlank()) {
                                isTitleError = true
                            } else {
                                viewModel.createAppointment(
                                    title = title,
                                    type = selectedType,
                                    category = "", // Category is completely removed as requested
                                    date = selectedDate.toString(),
                                    time = selectedTime.toString(),
                                    reminderMinutes = reminderTimeMinutes,
                                    notes = notes,
                                    recurring = isRecurring,
                                    recurringDays = recurringDays,
                                    context = context,
                                    onNavigateBack
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PatientGreen),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp)
                            .shadow(
                                elevation = 6.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = PatientGreen.copy(alpha = 0.25f),
                                spotColor = PatientGreen.copy(alpha = 0.25f)
                            )
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Text(
                                text = "Guardar evento",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = Color.White
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(30.dp))
                }
            }

            if (isLoading) {
                FullScreenLoadingOverlay()
            }
        }
    }
}

@Composable
private fun EventTypeCard(
    title: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) MintPill else Color.White
        ),
        border = BorderStroke(
            width = if (isSelected) 1.5.dp else 1.dp,
            color = if (isSelected) PatientGreen else BorderSoft
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .height(100.dp)
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(if (isSelected) PatientGreen.copy(alpha = 0.08f) else Color(0xFFF7FAFC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) PatientGreen else TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontFamily = LexendFontFamily,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                fontSize = 11.sp,
                color = if (isSelected) PatientGreen else TextPrimary,
                maxLines = 1
            )
        }
    }
}
