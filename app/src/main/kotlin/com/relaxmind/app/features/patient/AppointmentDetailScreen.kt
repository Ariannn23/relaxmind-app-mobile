package com.relaxmind.app.features.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun AppointmentDetailScreen(
    appointmentId: String,
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val appointment by viewModel.selectedAppointment.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(appointmentId) {
        viewModel.loadAppointmentDetail(appointmentId)
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

            if (appointment == null || isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = PatientGreen)
                }
            } else {
                val appt = appointment!!

                val dotColor = when (appt.type) {
                    "cita" -> Color(0xFF0F6E56) // MedicalGreen
                    "medicacion" -> Color(0xFF1E88E5) // MedicationBlue
                    else -> Color(0xFFFF9800) // ReminderOrange
                }

                val badgeBgColor = when (appt.type) {
                    "cita" -> SoftMint
                    "medicacion" -> Color(0xFFEBF8FF)
                    else -> SoftCream
                }

                val badgeTextColor = when (appt.type) {
                    "cita" -> PatientGreen
                    "medicacion" -> Color(0xFF1E88E5)
                    else -> Color(0xFFFF9800)
                }

                val icon = when (appt.type) {
                    "cita" -> Icons.Default.LocalHospital
                    "medicacion" -> Icons.Default.Medication
                    else -> Icons.Default.PushPin
                }

                val typeLabel = when (appt.type) {
                    "cita" -> "Cita médica"
                    "medicacion" -> "Medicación"
                    else -> "Recordatorio"
                }

                val dateLocal = LocalDate.parse(appt.date)
                val dayName = dateLocal.dayOfWeek.getDisplayName(TextStyle.FULL, Locale("es")).replaceFirstChar { it.uppercase() }
                val monthName = dateLocal.month.getDisplayName(TextStyle.FULL, Locale("es"))
                val dateFormatted = "${dayName} ${dateLocal.dayOfMonth} de $monthName"

                val timeLocal = LocalTime.parse(appt.time)
                val timeFormatted = timeLocal.format(DateTimeFormatter.ofPattern("hh:mm a"))

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
                                text = "Detalle del evento",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 24.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Información y opciones del evento",
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
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Main card with left border color
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
                                    .height(IntrinsicSize.Min)
                            ) {
                                // Left border indicator box
                                Box(
                                    modifier = Modifier
                                        .width(6.dp)
                                        .fillMaxHeight()
                                        .background(
                                            color = if (appt.completed) Color.LightGray else dotColor,
                                            shape = RoundedCornerShape(topStart = 26.dp, bottomStart = 26.dp)
                                        )
                                )

                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(20.dp),
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    // Row with large circular icon & tags
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .background(
                                                    if (appt.completed) Color(0xFFF2F4F8) else dotColor.copy(alpha = 0.08f),
                                                    CircleShape
                                                ),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = icon,
                                                contentDescription = null,
                                                tint = if (appt.completed) Color.Gray else dotColor,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }

                                        Row(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Badge(
                                                text = typeLabel,
                                                bgColor = if (appt.completed) Color(0xFFF2F4F8) else badgeBgColor,
                                                textColor = if (appt.completed) Color.Gray else badgeTextColor
                                            )
                                            if (appt.type == "cita" && appt.category.isNotBlank()) {
                                                Badge(
                                                    text = appt.category,
                                                    bgColor = if (appt.completed) Color(0xFFF2F4F8) else SoftMint,
                                                    textColor = if (appt.completed) Color.Gray else PatientGreen
                                                )
                                            }
                                        }
                                    }

                                    // Title
                                    Text(
                                        text = appt.title,
                                        fontFamily = LexendFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 22.sp,
                                        color = TextPrimary
                                    )

                                    // Divider
                                    HorizontalDivider(color = BorderSoft, thickness = 1.dp)

                                    // Date & Time Detail Row
                                    DetailRow(
                                        icon = Icons.Default.CalendarToday,
                                        text = "$dateFormatted  •  $timeFormatted"
                                    )

                                    // Reminder Detail Row
                                    DetailRow(
                                        icon = Icons.Default.PushPin,
                                        text = "Recordatorio ${appt.reminderTime} min antes"
                                    )
                                }
                            }
                        }

                        // NOTES SECTION
                        if (appt.notes.isNotBlank()) {
                            Column(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.AutoMirrored.Filled.Notes,
                                        contentDescription = null,
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Text(
                                        text = "Notas",
                                        fontFamily = LexendFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        color = TextPrimary
                                    )
                                }

                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(18.dp),
                                            ambientColor = Color(0xFF8A88A6).copy(alpha = 0.05f),
                                            spotColor = Color(0xFF8A88A6).copy(alpha = 0.05f)
                                        )
                                        .background(Color.White, RoundedCornerShape(18.dp))
                                        .border(1.dp, BorderSoft, RoundedCornerShape(18.dp))
                                        .padding(16.dp)
                                ) {
                                    Text(
                                        text = appt.notes,
                                        fontFamily = LexendFontFamily,
                                        fontSize = 14.sp,
                                        color = TextPrimary,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // ACTION BUTTONS
                        val isCompleted = appt.completed

                        // Complete/Incomplete Toggle Button (Outline green button)
                        OutlinedButton(
                            onClick = {
                                viewModel.updateAppointmentCompletion(
                                    appointmentId = appt.id,
                                    completed = !isCompleted,
                                    date = appt.date
                                )
                            },
                            border = androidx.compose.foundation.BorderStroke(1.5.dp, PatientGreen),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.05f),
                                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.05f)
                                ),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isCompleted) PatientGreen.copy(alpha = 0.05f) else Color.White
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = PatientGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = if (isCompleted) "Marcar como pendiente" else "Marcar como completado",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = PatientGreen
                                )
                            }
                        }

                        // Delete button (soft style, trash icon in red/coral)
                        Button(
                            onClick = { showDeleteDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSoft),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .shadow(
                                    elevation = 2.dp,
                                    shape = RoundedCornerShape(24.dp),
                                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.05f),
                                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.05f)
                                )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Eliminar",
                                    tint = SOSCoral,
                                    modifier = Modifier.size(18.dp)
                                )
                                Text(
                                    text = "Eliminar evento",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = SOSCoral
                                )
                            }
                        }
                    }
                }
            }
        }

        // CONFIRM DELETE DIALOG
        if (showDeleteDialog && appointment != null) {
            val appt = appointment!!
            AlertDialog(
                onDismissRequest = { showDeleteDialog = false },
                title = {
                    Text(
                        text = "¿Eliminar evento?",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Text(
                        text = "Esta acción borrará permanentemente este evento de tu agenda y no se podrá deshacer.",
                        fontFamily = LexendFontFamily,
                        fontSize = 14.sp
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showDeleteDialog = false
                            viewModel.deleteAppointment(
                                appointmentId = appt.id,
                                date = appt.date,
                                context = context,
                                onSuccess = {
                                    onNavigateBack()
                                }
                            )
                        }
                    ) {
                        Text(
                            text = "Eliminar",
                            fontFamily = LexendFontFamily,
                            color = SOSCoral,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = false }) {
                        Text(
                            text = "Cancelar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium
                        )
                    }
                },
                shape = RoundedCornerShape(24.dp),
                containerColor = Color.White
            )
        }
    }
}

@Composable
private fun Badge(text: String, bgColor: Color, textColor: Color) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = textColor
        )
    }
}

@Composable
private fun DetailRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Start
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PatientGreen,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontFamily = LexendFontFamily,
            fontSize = 14.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
