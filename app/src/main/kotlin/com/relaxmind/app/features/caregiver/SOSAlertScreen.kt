package com.relaxmind.app.features.caregiver

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.themes.SOSCoral

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
    if (alert == null || alert.resolved) {
        LaunchedEffect(Unit) {
            onNavigateBack()
        }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(SOSCoral)
            .padding(start = 24.dp, end = 24.dp, top = 40.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.weight(1f))
            BlinkingAlertText()
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = RelaxIcons.Close,
                    contentDescription = "Cerrar",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        AsyncImage(
            model = uiState.patientAvatarUrl.ifBlank { "https://ui-avatars.com/api/?name=${alert.patientName}&background=fff&color=E8582A" },
            contentDescription = "Patient Avatar",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .border(2.dp, Color.White, CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = alert.patientName,
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp
            ),
            color = Color.White
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (hasLocation) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(MaterialTheme.shapes.medium)
            ) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState
                ) {
                    val latLng = LatLng(lat!!, lng!!)
                    Marker(
                        state = MarkerState(position = latLng),
                        title = "Ubicación del paciente"
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Lat: $lat, Lng: $lng",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.8f)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Button(
                    onClick = {
                        val phone = uiState.patientPhone
                        if (phone.isNotBlank()) {
                            val intent = Intent(Intent.ACTION_DIAL).apply {
                                data = Uri.parse("tel:$phone")
                            }
                            context.startActivity(intent)
                        }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = SOSCoral
                    )
                ) {
                    Text(
                        text = "LLAMAR",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }

                Button(
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
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = SOSCoral
                    )
                ) {
                    Text(
                        text = "VER RUTA",
                        style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                    )
                }
            }
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .background(Color.White.copy(alpha = 0.2f), MaterialTheme.shapes.medium),
                contentAlignment = Alignment.Center
            ) {
                Text("Obteniendo ubicación...", color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    val phone = uiState.patientPhone
                    if (phone.isNotBlank()) {
                        val intent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:$phone")
                        }
                        context.startActivity(intent)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = SOSCoral
                )
            ) {
                Text(
                    text = "LLAMAR AL PACIENTE",
                    style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        TextButton(
            onClick = { showResolveDialog = true }
        ) {
            Text(
                text = "Marcar como resuelta",
                color = Color.White,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }

    if (showResolveDialog) {
        AlertDialog(
            onDismissRequest = { showResolveDialog = false },
            title = {
                Text(text = "Resolver Alerta")
            },
            text = {
                Text(text = "¿Estás seguro de que deseas marcar esta alerta como resuelta?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showResolveDialog = false
                        viewModel.markResolved(onSuccess = {
                            onNavigateBack()
                        })
                    }
                ) {
                    Text("Confirmar")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showResolveDialog = false }
                ) {
                    Text("Cancelar")
                }
            }
        )
    }
}

@Composable
fun BlinkingAlertText() {
    val infiniteTransition = rememberInfiniteTransition(label = "Blinking")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Text(
        text = "ALERTA SOS",
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = Color.White,
        modifier = Modifier.alpha(alpha)
    )
}
