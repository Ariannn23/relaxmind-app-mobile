package com.relaxmind.app.features.caregiver

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.getAvatarDrawableRes
import com.relaxmind.app.ui.themes.*
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SOSAlertScreen(
    alertId: String,
    onNavigateBack: () -> Unit,
    viewModel: SOSAlertViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var showResolveDialog by remember { mutableStateOf(false) }

    LaunchedEffect(alertId) {
        viewModel.loadAlert(alertId)
    }

    if (uiState.isLoading) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            LoadingIndicator()
        }
        return
    }

    val alert = uiState.alert
    if (alert == null) {
        SOSAlertMessageState(
            title = "Alerta no disponible",
            message = uiState.error ?: "No pudimos cargar esta alerta.",
            onNavigateBack = onNavigateBack
        )
        return
    }

    if (alert.resolved) {
        SOSAlertMessageState(
            title = "Alerta resuelta",
            message = "Esta emergencia ya fue marcada como resuelta.",
            onNavigateBack = onNavigateBack
        )
        return
    }

    val lat = alert.latitude
    val lng = alert.longitude
    val hasLocation = lat != null && lng != null

    val cameraPositionState = rememberCameraPositionState()
    LaunchedEffect(lat, lng) {
        if (hasLocation) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(LatLng(lat!!, lng!!), 16f)
        }
    }

    // Time calculations
    val createdAtTime = remember(alert.createdAtText) {
        // Try to parse "dd MMM yyyy, HH:mm" or just fallback
        try {
            val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
            val date = format.parse(alert.createdAtText)
            if (date != null) {
                val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val timeStr = timeFormat.format(date)
                
                val diffMills = Date().time - date.time
                val diffMins = (diffMills / (1000 * 60)).toInt()
                
                "Activo desde $timeStr · Hace $diffMins min"
            } else {
                "Activa: ${alert.createdAtText}"
            }
        } catch (e: Exception) {
            "Activa: ${alert.createdAtText}"
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // --- TOP HALF: CORAL BACKGROUND ---
        SOSCaregiverBackground()

        Column(modifier = Modifier.fillMaxSize()) {
            
            // HEADER
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                com.relaxmind.app.ui.components.RelaxBackButton(
                    onClick = onNavigateBack,
                    role = com.relaxmind.app.ui.components.AppRole.CAREGIVER
                )
                
                Spacer(modifier = Modifier.weight(1f))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Custom [SOS] box
                    Box(
                        modifier = Modifier
                            .border(1.5.dp, Color.White, RoundedCornerShape(8.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "SOS",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ALERTA SOS",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 24.sp,
                        color = Color.White,
                        letterSpacing = 1.sp
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                Spacer(modifier = Modifier.width(48.dp)) // Balance the back button
            }

            Spacer(modifier = Modifier.height(24.dp))

            // AVATAR & INFO
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Avatar with rings
                Box(
                    modifier = Modifier.size(160.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Subtle background rings for avatar
                    for (i in 1..2) {
                        Box(
                            modifier = Modifier
                                .size((90 + (i * 35)).dp)
                                .clip(CircleShape)
                                .background(Color.White.copy(alpha = 0.1f / i))
                        )
                    }
                    
                    val isCustomAvatar = uiState.patientAvatarUrl.startsWith("relaxmind://avatar/")
                    if (isCustomAvatar) {
                        Image(
                            painter = painterResource(id = getAvatarDrawableRes(uiState.patientAvatarUrl)),
                            contentDescription = "Avatar de ${alert.patientName}",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(3.dp, Color.White, CircleShape)
                        )
                    } else {
                        // Fallback Box if no custom avatar
                        Box(
                            modifier = Modifier
                                .size(90.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(3.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = alert.patientName.take(1).uppercase(),
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                color = SOSCoral
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = alert.patientName,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = Color.White
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = createdAtTime,
                    fontFamily = LexendFontFamily,
                    fontSize = 14.sp,
                    color = Color.White.copy(alpha = 0.9f)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- BOTTOM HALF: WHITE CARD ---
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                shape = RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp),
                color = Color.White,
                shadowElevation = 16.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 24.dp, vertical = 24.dp)
                ) {
                    // Call Button
                    com.relaxmind.app.ui.components.RelaxButton(
                        text = "LLAMAR AL PACIENTE",
                        onClick = {
                            val phone = uiState.patientPhone
                            if (phone.isNotBlank()) {
                                val intent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:$phone")
                                }
                                context.startActivity(intent)
                            }
                        },
                        variant = com.relaxmind.app.ui.components.ButtonVariant.DESTRUCTIVE,
                        icon = Icons.Default.Phone,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Map Container
                    if (hasLocation) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f) // Takes remaining space
                                .clip(RoundedCornerShape(16.dp))
                                .border(1.dp, Color(0xFFEEEEEE), RoundedCornerShape(16.dp))
                        ) {
                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState
                            ) {
                                val latLng = LatLng(lat!!, lng!!)
                                val markerState = MarkerState(position = latLng)
                                Marker(
                                    state = markerState,
                                    title = "${alert.patientName.split(" ").firstOrNull() ?: "El paciente"} está aquí"
                                )
                                // Force info window to show
                                LaunchedEffect(markerState) {
                                    markerState.showInfoWindow()
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        // Route and Share Buttons
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    val intent = Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$lat,$lng&travelmode=driving")
                                    )
                                    context.startActivity(intent)
                                },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CaregiverIndigo
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, CaregiverIndigo)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = "Ruta",
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = "VER RUTA",
                                        fontFamily = LexendFontFamily,
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp,
                                        letterSpacing = 0.5.sp
                                    )
                                }
                            }

                            OutlinedButton(
                                onClick = {
                                    val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(
                                            Intent.EXTRA_TEXT,
                                            "Alerta SOS de ${alert.patientName}. Ubicación: https://maps.google.com/?q=$lat,$lng"
                                        )
                                    }
                                    context.startActivity(Intent.createChooser(shareIntent, "Compartir ubicación"))
                                },
                                modifier = Modifier.size(56.dp),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = CaregiverIndigo
                                ),
                                border = androidx.compose.foundation.BorderStroke(1.5.dp, CaregiverIndigo),
                                contentPadding = PaddingValues(0.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Compartir ubicación",
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        // Fallback when no location
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFF5F5F5)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Ubicación no disponible",
                                fontFamily = LexendFontFamily,
                                color = TextSecondary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    // Resolve Button
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Marcar como resuelta",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textDecoration = TextDecoration.Underline,
                            modifier = Modifier
                                .clickable(
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ) {
                                    showResolveDialog = true
                                }
                                .padding(8.dp)
                        )
                    }
                }
            }
        }
    }

    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = {
                Text(
                    text = "Resolver Alerta",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
            },
            text = {
                Text(
                    text = "¿Estás seguro de que deseas marcar esta emergencia como resuelta?",
                    fontFamily = LexendFontFamily,
                    fontSize = 15.sp,
                    color = TextSecondary
                )
            },
            confirmButton = {
                com.relaxmind.app.ui.components.RelaxButton(
                    text = "Sí, resolver",
                    onClick = {
                        viewModel.markResolved(
                            onSuccess = {
                                showResolveDialog = false
                                onNavigateBack()
                            }
                        )
                    },
                    isLoading = uiState.isLoading,
                    role = com.relaxmind.app.ui.components.AppRole.CAREGIVER,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            dismissButton = {
                TextButton(onClick = { showResolveDialog = false }) {
                    Text(
                        text = "Cancelar",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Medium,
                        color = TextSecondary
                    )
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SOSAlertMessageState(
    title: String,
    message: String,
    onNavigateBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .systemBarsPadding()
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(SOSCoral.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "SOS",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.ExtraBold,
                    color = SOSCoral,
                    fontSize = 18.sp
                )
            }

            Text(
                text = title,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 24.sp,
                color = TextPrimary
            )

            Text(
                text = message,
                fontFamily = LexendFontFamily,
                fontSize = 15.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )

            Button(
                onClick = onNavigateBack,
                colors = ButtonDefaults.buttonColors(containerColor = CaregiverIndigo),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = "Volver",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun SOSCaregiverBackground() {
    val infiniteTransition = rememberInfiniteTransition(label = "BgPulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "BgAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        SOSCoral,
                        SOSOrange
                    )
                )
            )
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2, size.height * 0.25f)
            val maxRadius = size.width
            
            // Draw subtle concentric background rings
            for (i in 1..4) {
                drawCircle(
                    color = Color.White.copy(alpha = alphaAnim / i),
                    radius = (maxRadius / 3.5f) * i,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}
