package com.relaxmind.app.features.patient

import android.app.DatePickerDialog
import android.app.TimePickerDialog
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxInputField
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.SOSCoral
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
    var category by remember { mutableStateOf("Neurología") }
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var selectedTime by remember { mutableStateOf(LocalTime.of(10, 30)) }
    var notes by remember { mutableStateOf("") }
    var reminderTimeMinutes by remember { mutableStateOf(15) }
    var isRecurring by remember { mutableStateOf(false) }
    var recurringDays by remember { mutableStateOf<List<Int>>(emptyList()) }

    var categoryDropdownExpanded by remember { mutableStateOf(false) }
    val categoriesList = listOf("Neurología", "Psicología", "Psiquiatría", "Medicina General", "Otro")

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
        topBar = {
            RelaxTopBar(
                title = "Nuevo evento",
                onBackClick = onNavigateBack
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                // TÍTULO DEL EVENTO
                Text(
                    text = "Título del evento",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                RelaxInputField(
                    value = title,
                    onValueChange = {
                        title = it
                        if (it.isNotBlank()) isTitleError = false
                    },
                    label = "Ej: Cita con neuróloga",
                    isError = isTitleError,
                    errorMessage = if (isTitleError) "El título es obligatorio" else null
                )

                // TIPO DE EVENTO SELECTOR
                Text(
                    text = "Tipo de evento",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Cita Médica Card
                    EventTypeCard(
                        title = "Cita médica",
                        icon = Icons.Default.LocalHospital,
                        isSelected = selectedType == "cita",
                        color = Color(0xFF38A169),
                        onClick = { selectedType = "cita" },
                        modifier = Modifier.weight(1f)
                    )
                    // Medicación Card
                    EventTypeCard(
                        title = "Medicación",
                        icon = Icons.Default.Medication,
                        isSelected = selectedType == "medicacion",
                        color = Color(0xFF3182CE),
                        onClick = { selectedType = "medicacion" },
                        modifier = Modifier.weight(1f)
                    )
                    // Recordatorio Card
                    EventTypeCard(
                        title = "Recordatorio",
                        icon = Icons.Default.PushPin,
                        isSelected = selectedType == "recordatorio",
                        color = Color(0xFFDD6B20),
                        onClick = { selectedType = "recordatorio" },
                        modifier = Modifier.weight(1f)
                    )
                }

                // CATEGORÍA (Sólo si es tipo Cita)
                if (selectedType == "cita") {
                    Text(
                        text = "Categoría",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.DarkGray
                    )
                    Box(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = {},
                            readOnly = true,
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .shadow(3.dp, RoundedCornerShape(18.dp))
                                .clickable { categoryDropdownExpanded = true },
                            enabled = false,
                            colors = OutlinedTextFieldDefaults.colors(
                                disabledTextColor = MaterialTheme.colorScheme.onSurface,
                                disabledBorderColor = PatientGreen,
                                disabledLabelColor = PatientGreen,
                                disabledContainerColor = Color.White
                            ),
                            trailingIcon = {
                                IconButton(onClick = { categoryDropdownExpanded = true }) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = "Desplegar",
                                        tint = PatientGreen
                                    )
                                }
                            }
                        )
                        DropdownMenu(
                            expanded = categoryDropdownExpanded,
                            onDismissRequest = { categoryDropdownExpanded = false },
                            modifier = Modifier.fillMaxWidth(0.9f)
                        ) {
                            categoriesList.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        category = cat
                                        categoryDropdownExpanded = false
                                    }
                                )
                            }
                        }
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
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val dayName = selectedDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }
                        val dateFormatted = "${dayName}, ${selectedDate.dayOfMonth} ${selectedDate.month.getDisplayName(TextStyle.SHORT, Locale("es"))} ${selectedDate.year}"
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(1.dp, PatientGreen, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { datePickerDialog.show() }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.CalendarToday, contentDescription = null, tint = PatientGreen, modifier = Modifier.size(20.dp))
                            Text(
                                text = dateFormatted,
                                style = MaterialTheme.typography.bodyMedium,
                                maxLines = 1,
                                fontSize = 13.sp
                            )
                        }
                    }

                    // Time Button
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Hora",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        val timeFormatted = selectedTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
                        
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .border(1.dp, PatientGreen, RoundedCornerShape(18.dp))
                                .clip(RoundedCornerShape(18.dp))
                                .clickable { timePickerDialog.show() }
                                .padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(imageVector = Icons.Default.Schedule, contentDescription = null, tint = PatientGreen, modifier = Modifier.size(20.dp))
                            Text(text = timeFormatted, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }

                // REPETIR RECORDATORIO SWITCH Y DÍAS DE LA SEMANA SELECTOR
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Repetir recordatorio",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
                        )
                        Text(
                            text = "Recordar automáticamente ciertos días",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                    androidx.compose.material3.Switch(
                        checked = isRecurring,
                        onCheckedChange = { isRecurring = it },
                        colors = androidx.compose.material3.SwitchDefaults.colors(
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
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.DarkGray
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
                                            color = if (isSelected) PatientGreen else Color(0xFFE2E8F0),
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
                                        color = if (isSelected) Color.White else Color.Gray,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 14.sp
                                    )
                                }
                            }
                        }
                    }
                }

                // NOTAS OPCIONALES
                Text(
                    text = "Notas",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray
                )
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Detalles opcionales...") },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .shadow(3.dp, RoundedCornerShape(18.dp)),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PatientGreen,
                        unfocusedBorderColor = PatientGreen.copy(alpha = 0.6f),
                        focusedLabelColor = PatientGreen,
                        unfocusedLabelColor = Color.Gray,
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color.White
                    ),
                    maxLines = 4
                )

                if (errorMsg != null) {
                    Text(
                        text = errorMsg ?: "",
                        color = SOSCoral,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // GUARDAR EVENTO BUTTON
                RelaxButton(
                    text = "Guardar evento",
                    onClick = {
                        if (title.isBlank()) {
                            isTitleError = true
                        } else {
                            viewModel.createAppointment(
                                title = title,
                                type = selectedType,
                                category = if (selectedType == "cita") category else "",
                                date = selectedDate.toString(),
                                time = selectedTime.toString(),
                                reminderMinutes = reminderTimeMinutes,
                                notes = notes,
                                recurring = isRecurring,
                                recurringDays = recurringDays,
                                context = context,
                                onSuccess = {
                                    viewModel.clearError()
                                    onNavigateBack()
                                }
                            )
                        }
                    },
                    variant = ButtonVariant.PRIMARY,
                    role = AppRole.PATIENT,
                    modifier = Modifier.fillMaxWidth()
                )
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
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    color: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) color.copy(alpha = 0.08f) else Color.White
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = if (isSelected) color else Color(0xFFE2E8F0)
        ),
        shape = RoundedCornerShape(16.dp),
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
                    .background(if (isSelected) color.copy(alpha = 0.16f) else Color(0xFFF7FAFC)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isSelected) color else Color.Gray,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = title,
                fontSize = 11.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) color else Color.Gray,
                maxLines = 1
            )
        }
    }
}
