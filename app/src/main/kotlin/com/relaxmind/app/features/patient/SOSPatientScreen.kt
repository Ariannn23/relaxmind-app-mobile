package com.relaxmind.app.features.patient

import android.Manifest
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.border
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.location.LocationServices
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.NotificationPermissionDialog
import com.relaxmind.app.ui.components.hasNotificationPermission
import com.relaxmind.app.ui.components.openNotificationSettings
import com.relaxmind.app.ui.components.rememberNotificationPermissionLauncher
import com.relaxmind.app.ui.components.requestNotificationPermission
import com.relaxmind.app.ui.components.toast.LocalRelaxToast
import com.relaxmind.app.ui.components.toast.RelaxToastType
import com.relaxmind.app.ui.themes.*
import kotlinx.coroutines.launch

@Composable
fun SOSPatientScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val factory = object : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            @Suppress("UNCHECKED_CAST")
            return SOSPatientViewModel(fusedLocationClient = fusedLocationClient) as T
        }
    }
    val viewModel: SOSPatientViewModel = viewModel(factory = factory)
    val uiState by viewModel.uiState.collectAsState()

    var showCancelDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    val relaxToast = LocalRelaxToast.current
    val coroutineScope = rememberCoroutineScope()

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocation = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocation = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false
        if (fineLocation || coarseLocation) {
            viewModel.activateSOS()
        } else {
            // Permission denied, but we still activate SOS (location will just not be updated)
            viewModel.activateSOS()
        }
    }
    val notificationPermissionLauncher = rememberNotificationPermissionLauncher { granted ->
        if (granted) {
            viewModel.updateNotificationsEnabled(true)
            showNotificationPermissionDialog = false
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        } else {
            showNotificationPermissionDialog = true
        }
    }

    LaunchedEffect(uiState.isDataLoaded) {
        if (uiState.isDataLoaded && !uiState.isSOSActive) {
            if (uiState.notificationsEnabled && hasNotificationPermission(context)) {
                permissionLauncher.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            } else {
                showNotificationPermissionDialog = true
            }
        }
    }

    // Main layout
    Box(
        modifier = modifier
            .fillMaxSize()
            .systemBarsPadding()
    ) {
        // Background gradient and waves
        SOSBackground()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            
            Spacer(modifier = Modifier.weight(0.5f))

            // Pulse indicator
            SOSPulseIndicator(isSending = !uiState.isSOSActive)

            Spacer(modifier = Modifier.height(32.dp))

            // Main Status Text
            SOSStatusMessage(
                isSOSActive = uiState.isSOSActive,
                caregiverName = uiState.caregiverName
            )

            Spacer(modifier = Modifier.weight(1f))

            // Call Button
            CallCaregiverButton(
                onClick = {
                    val phone = uiState.caregiverPhone
                    if (phone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phone")
                        }
                        context.startActivity(intent)
                    } else {
                        coroutineScope.launch {
                            relaxToast.showToast(
                                type = RelaxToastType.Error,
                                title = "Número no disponible",
                                message = "Tu cuidador no tiene un número registrado"
                            )
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel Button
            CancelSOSButton(onClick = { showCancelDialog = true })
            
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Dialogs
        if (showCancelDialog) {
            CancelSOSDialog(
                onConfirm = {
                    showCancelDialog = false
                    viewModel.cancelSOS()
                    onNavigateBack()
                },
                onDismiss = { showCancelDialog = false }
            )
        }

        if (showNotificationPermissionDialog) {
            NotificationPermissionDialog(
                role = AppRole.PATIENT,
                title = "Activa notificaciones para SOS",
                message = "Para enviar alertas SOS con seguridad, RelaxMind necesita que las notificaciones estén activadas antes de iniciar la alerta.",
                primaryText = "Activar ahora",
                secondaryText = "Ir a ajustes",
                onPrimaryClick = {
                    if (hasNotificationPermission(context)) {
                        viewModel.updateNotificationsEnabled(true)
                        showNotificationPermissionDialog = false
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    } else {
                        requestNotificationPermission(
                            launcher = notificationPermissionLauncher,
                            onAlreadyGranted = {
                                viewModel.updateNotificationsEnabled(true)
                                showNotificationPermissionDialog = false
                                permissionLauncher.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            }
                        )
                    }
                },
                onDismiss = {
                    openNotificationSettings(context)
                    showNotificationPermissionDialog = false
                }
            )
        }

        uiState.error?.let { errorMessage ->
            AlertDialog(
                onDismissRequest = {
                    viewModel.clearError()
                    onNavigateBack()
                },
                title = { 
                    Text(
                        text = "Aviso", 
                        fontFamily = LexendFontFamily, 
                        fontWeight = FontWeight.Bold 
                    ) 
                },
                text = { 
                    Text(
                        text = errorMessage,
                        fontFamily = LexendFontFamily
                    ) 
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.clearError()
                            onNavigateBack()
                        }
                    ) {
                        Text("Entendido", fontFamily = LexendFontFamily, fontWeight = FontWeight.SemiBold)
                    }
                }
            )
        }
    }
}

@Composable
private fun SOSBackground() {
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
            val center = Offset(size.width / 2, size.height * 0.4f)
            val maxRadius = size.width
            
            // Draw subtle concentric background rings
            for (i in 1..4) {
                drawCircle(
                    color = Color.White.copy(alpha = alphaAnim / i),
                    radius = (maxRadius / 4) * i,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )
            }
        }
    }
}

@Composable
private fun SOSPulseIndicator(isSending: Boolean) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse")
    
    val ringScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingScale"
    )
    
    val ringAlpha by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RingAlpha"
    )

    // Inner icon pulse
    val iconScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "IconScale"
    )

    Box(
        modifier = Modifier.size(200.dp),
        contentAlignment = Alignment.Center
    ) {
        // Expanding ring
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(ringScale)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = ringAlpha))
        )

        // Main circle with white border
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(iconScale)
                .shadow(16.dp, CircleShape, ambientColor = SOSCoralDark)
                .clip(CircleShape)
                .background(SOSCoralDark.copy(alpha = 0.4f))
                .border(4.dp, Color.White, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = "Alerta SOS activa",
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
        }
    }
}

@Composable
private fun SOSStatusMessage(isSOSActive: Boolean, caregiverName: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Main Title
        Text(
            text = "SOS\nACTIVADO",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 42.sp,
            color = TextOnSOS,
            textAlign = TextAlign.Center,
            letterSpacing = 2.sp,
            lineHeight = 48.sp
        )

        // Status description
        AnimatedVisibility(
            visible = true,
            enter = fadeIn() + slideInVertically(initialOffsetY = { 20 }),
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (isSOSActive) "Tu cuidador ha sido notificado" else "Enviando alerta...",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 18.sp,
                    color = TextOnSOS,
                    textAlign = TextAlign.Center
                )

                if (isSOSActive && caregiverName.isNotBlank()) {
                    Text(
                        text = "$caregiverName está en camino",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 16.sp,
                        color = TextOnSOSMuted,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun CallCaregiverButton(onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .shadow(8.dp, RoundedCornerShape(32.dp), spotColor = SOSCoralDark),
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = SOSWhite,
            contentColor = SOSCoral
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Phone,
                contentDescription = "Llamar Cuidador",
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "LLAMAR A CUIDADOR",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
private fun CancelSOSButton(onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(24.dp))
            .clickable(
                onClick = onClick,
                indication = null,
                interactionSource = remember { MutableInteractionSource() }
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Cancelar",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
            color = TextOnSOS.copy(alpha = 0.9f)
        )
    }
}

@Composable
private fun CancelSOSDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cancelar SOS",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary
            )
        },
        text = {
            Text(
                text = "¿Estás seguro de que deseas cancelar la alerta de emergencia? Tu cuidador recibirá un aviso de que estás bien.",
                fontFamily = LexendFontFamily,
                fontSize = 15.sp,
                color = TextSecondary
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(containerColor = SOSCoral),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = "Sí, cancelar",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "No, mantener activa",
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
