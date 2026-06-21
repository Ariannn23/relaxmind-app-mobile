package com.relaxmind.app.features.patient

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.relaxmind.app.data.model.NearbyHealthCenter
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxLoadingContent
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyHealthScreen(
    onNavigateBack: () -> Unit,
    viewModel: NearbyHealthViewModel = viewModel()
) {
    val context = LocalContext.current
    val userLocation by viewModel.userLocation.collectAsState()
    val healthCenters by viewModel.healthCenters.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val hasPermission by viewModel.hasLocationPermission.collectAsState()

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            if (granted) viewModel.onPermissionGranted()
            else viewModel.onPermissionDenied()
        }
    )

    LaunchedEffect(Unit) {
        if (hasPermission == null) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Centros de Salud", fontFamily = LexendFontFamily, fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        containerColor = Color(0xFFF7F9FC)
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasPermission == false) {
                PermissionDeniedState {
                    val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                        data = Uri.fromParts("package", context.packageName, null)
                    }
                    context.startActivity(intent)
                }
            } else {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Map Section
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(250.dp)
                            .clip(RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp))
                    ) {
                        if (userLocation != null) {
                            val userLatLng = LatLng(userLocation!!.lat, userLocation!!.lng)
                            val cameraPositionState = rememberCameraPositionState {
                                position = CameraPosition.fromLatLngZoom(userLatLng, 13f)
                            }

                            // Keep camera updated if user moves
                            LaunchedEffect(userLatLng) {
                                cameraPositionState.animate(CameraUpdateFactory.newLatLng(userLatLng))
                            }

                            GoogleMap(
                                modifier = Modifier.fillMaxSize(),
                                cameraPositionState = cameraPositionState,
                                properties = MapProperties(isMyLocationEnabled = true),
                                uiSettings = MapUiSettings(zoomControlsEnabled = false)
                            ) {
                                healthCenters.forEach { center ->
                                    Marker(
                                        state = MarkerState(position = LatLng(center.lat, center.lng)),
                                        title = center.name,
                                        snippet = center.typeName
                                    )
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.LightGray.copy(alpha = 0.3f)),
                                contentAlignment = Alignment.Center
                            ) {
                                LoadingIndicator()
                            }
                        }
                    }

                    // List Header
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp, vertical = 16.dp)
                    ) {
                        Text(
                            text = "Centros disponibles",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "${healthCenters.size} encontrados",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }

                    // Loading State
                    if (isLoading && healthCenters.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(260.dp)
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            RelaxLoadingContent(compact = true)
                        }
                    }

                    // List of Centers
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        items(healthCenters) { center ->
                            HealthCenterCard(
                                center = center,
                                onOpenRoute = {
                                    val uri = Uri.parse("google.navigation:q=${center.lat},${center.lng}")
                                    val intent = Intent(Intent.ACTION_VIEW, uri)
                                    intent.setPackage("com.google.android.apps.maps")
                                    if (intent.resolveActivity(context.packageManager) != null) {
                                        context.startActivity(intent)
                                    } else {
                                        // Fallback to browser
                                        val webUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=${center.lat},${center.lng}")
                                        context.startActivity(Intent(Intent.ACTION_VIEW, webUri))
                                    }
                                },
                                onCall = {
                                    if (!center.phoneNumber.isNullOrBlank()) {
                                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${center.phoneNumber}"))
                                        context.startActivity(intent)
                                    }
                                }
                            )
                        }
                        item {
                            Spacer(modifier = Modifier.height(30.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HealthCenterCard(
    center: NearbyHealthCenter,
    onOpenRoute: () -> Unit,
    onCall: () -> Unit
) {
    val isMinsa = center.name.contains("Hospital", ignoreCase = true) || center.name.contains("MINSA", ignoreCase = true)
    
    val iconBgColor = if (isMinsa) Color(0xFFFFEAEA) else Color(0xFFE4F6F0)
    val iconColor = if (isMinsa) Color(0xFFE53935) else PatientGreen
    val iconVector = if (isMinsa) Icons.Default.Warning else Icons.Default.LocationOn // Approximate icons
    
    val pillText = if (center.isPublic) "SIS" else "Info"
    val pillBgColor = if (isMinsa) Color(0xFFFFEAEA) else Color(0xFFE4F6F0)
    val pillTextColor = if (isMinsa) Color(0xFFE53935) else PatientGreen

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color.LightGray.copy(alpha = 0.5f),
                spotColor = Color.LightGray.copy(alpha = 0.5f)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                // Icon
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(iconBgColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = iconVector,
                        contentDescription = null,
                        tint = iconColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Texts
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Text(
                            text = center.name,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // SIS/Info Pill
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(50))
                                .background(pillBgColor)
                                .padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = pillText,
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp,
                                color = pillTextColor
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = center.typeName + if (isMinsa) " (MINSA)" else "",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = center.schedule ?: "Horario no disponible",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (center.schedule == "24/7") Color(0xFFE53935) else PatientGreen
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Ver ruta Button
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
                        .clickable(onClick = onOpenRoute),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocationOn, // Change icon accordingly
                            contentDescription = null,
                            tint = PatientGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Text(
                            text = "Ver ruta",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = PatientGreen
                        )
                    }
                }

                // Llamar Button
                val canCall = !center.phoneNumber.isNullOrBlank()
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .background(
                            color = if (canCall) PatientGreen else Color.LightGray,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clip(RoundedCornerShape(12.dp))
                        .clickable(enabled = canCall, onClick = onCall),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Call,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Llamar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            color = Color.White
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Ver detalle text
            Text(
                text = "Ver detalle",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = PatientGreen,
                modifier = Modifier.clickable { /* Future implementation */ }
            )
        }
    }
}

@Composable
fun PermissionDeniedState(onOpenSettings: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Default.LocationOn,
            contentDescription = null,
            tint = TextSecondary,
            modifier = Modifier.size(64.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Permiso de ubicación denegado",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "Necesitamos acceso a tu ubicación para encontrar centros de salud cercanos a ti.",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = onOpenSettings,
            colors = ButtonDefaults.buttonColors(containerColor = PatientGreen),
            shape = RoundedCornerShape(50)
        ) {
            Text("Ir a Configuración", fontFamily = LexendFontFamily, fontWeight = FontWeight.Medium)
        }
    }
}
