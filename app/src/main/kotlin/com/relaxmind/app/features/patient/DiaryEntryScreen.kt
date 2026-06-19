package com.relaxmind.app.features.patient

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*

private data class EmotionOption(val label: String)

@Composable
fun DiaryEntryScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onSaved: () -> Unit = onNavigateBack
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMsg by viewModel.error.collectAsState()

    var selectedCategory by remember { mutableStateOf("Trabajo") }
    var selectedEmotion by remember { mutableStateOf("Tranquilo") }
    var notes by remember { mutableStateOf("") }
    var selectedPhotoUris by remember { mutableStateOf<List<Uri>>(emptyList()) }

    val categories = listOf("Estrés", "Familia", "Trabajo", "Logro", "Otro")
    val emotions = listOf(
        EmotionOption("Ansioso"),
        EmotionOption("Tranquilo"),
        EmotionOption("Feliz"),
        EmotionOption("Triste"),
        EmotionOption("Frustrado"),
        EmotionOption("Emocionado")
    )

    // Launch photo picker
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(5)
    ) { uris ->
        val currentCount = selectedPhotoUris.size
        val availableSlots = 5 - currentCount
        if (availableSlots > 0 && uris.isNotEmpty()) {
            selectedPhotoUris = selectedPhotoUris + uris.take(availableSlots)
        }
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
                            text = "Nueva entrada",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 24.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "Guarda un momento de tu día 🌿",
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
                    // CATEGORÍA CHIPS
                    Column {
                        Text(
                            text = "Categoría",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = cat == selectedCategory
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(
                                            if (isSelected) PatientGreen else Color.White
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) PatientGreen else BorderSoft,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { selectedCategory = cat }
                                        .padding(horizontal = 18.dp, vertical = 10.dp)
                                ) {
                                    Text(
                                        text = cat,
                                        fontFamily = LexendFontFamily,
                                        color = if (isSelected) Color.White else TextSecondary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }
                        }
                    }

                    // ESTADOS EMOCIONALES
                    Column {
                        Text(
                            text = "¿Cómo te sientes hoy?",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            emotions.forEach { option ->
                                val isSelected = option.label == selectedEmotion
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) MintPill else Color.White
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) PatientGreen else BorderSoft
                                    ),
                                    shape = RoundedCornerShape(18.dp),
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(110.dp)
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null
                                        ) { selectedEmotion = option.label }
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.Center,
                                        modifier = Modifier.fillMaxSize()
                                    ) {
                                        // Custom Drawn Emotion Sticker Face
                                        EmotionSticker(
                                            emotion = option.label,
                                            tint = if (isSelected) PatientGreen else TextSecondary,
                                            modifier = Modifier.size(46.dp)
                                        )

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = option.label,
                                            fontFamily = LexendFontFamily,
                                            color = if (isSelected) PatientGreen else TextPrimary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 11.sp
                                        )

                                        if (isSelected) {
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Box(
                                                modifier = Modifier
                                                    .width(28.dp)
                                                    .height(2.dp)
                                                    .background(PatientGreen, RoundedCornerShape(1.dp))
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // TEXT FIELD ¿QUÉ QUIERES RECORDAR DE HOY?
                    Column {
                        Text(
                            text = "¿Qué quieres recordar de hoy?",
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
                                onValueChange = { if (it.length <= 1000) notes = it },
                                placeholder = { Text("Hoy fue un día bastante tranquilo...", fontFamily = LexendFontFamily, color = Color.LightGray) },
                                shape = RoundedCornerShape(18.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(180.dp)
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
                                maxLines = 10
                            )
                            Text(
                                text = "${notes.length}/1000",
                                fontFamily = LexendFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(bottom = 8.dp, end = 12.dp)
                            )
                        }
                    }

                    // SECCIÓN DE FOTOS
                    Column {
                        Text(
                            text = "Fotos (opcional)",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            // Display selected photos
                            itemsIndexed(selectedPhotoUris) { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .size(110.dp)
                                        .clip(RoundedCornerShape(18.dp))
                                        .border(1.dp, BorderSoft, RoundedCornerShape(18.dp))
                                        .shadow(
                                            elevation = 2.dp,
                                            shape = RoundedCornerShape(18.dp),
                                            ambientColor = Color(0xFF8A88A6).copy(alpha = 0.05f),
                                            spotColor = Color(0xFF8A88A6).copy(alpha = 0.05f)
                                        )
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = null,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                    // Remove button (X)
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(6.dp)
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(Color.Black.copy(alpha = 0.6f))
                                            .clickable {
                                                selectedPhotoUris = selectedPhotoUris.toMutableList().apply {
                                                    removeAt(index)
                                                }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar foto",
                                            tint = Color.White,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }

                            // Add Photo Button (Dotted/dashed card style)
                            if (selectedPhotoUris.size < 5) {
                                item {
                                    Card(
                                        colors = CardDefaults.cardColors(containerColor = Color.White),
                                        border = BorderStroke(1.5.dp, PatientGreen),
                                        shape = RoundedCornerShape(18.dp),
                                        modifier = Modifier
                                            .size(110.dp)
                                            .clickable {
                                                photoPickerLauncher.launch(
                                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                                )
                                            }
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize(),
                                            verticalArrangement = Arrangement.Center,
                                            horizontalAlignment = Alignment.CenterHorizontally
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Agregar foto",
                                                tint = PatientGreen,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Agregar foto",
                                                fontFamily = LexendFontFamily,
                                                color = PatientGreen,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
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

                    Spacer(modifier = Modifier.height(16.dp))

                    // GUARDAR ENTRADA BUTTON (Pill-shaped green)
                    Button(
                        onClick = {
                            viewModel.createDiaryEntry(
                                category = selectedCategory,
                                emotion = selectedEmotion,
                                notes = notes,
                                localPhotoUris = selectedPhotoUris,
                                context = context,
                                onSuccess = {
                                    viewModel.clearError()
                                    onSaved()
                                }
                            )
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
                        Text(
                            text = "Guardar entrada",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 15.sp,
                            color = Color.White
                        )
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
private fun EmotionSticker(
    emotion: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val radius = w * 0.4f
        val cx = w / 2f
        val cy = h / 2f
        
        // Draw face background fill
        drawCircle(
            color = tint.copy(alpha = 0.08f),
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(cx, cy)
        )
        // Draw face outline
        drawCircle(
            color = tint,
            radius = radius,
            center = androidx.compose.ui.geometry.Offset(cx, cy),
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw features based on emotion
        when (emotion.lowercase()) {
            "ansioso" -> {
                // Worried eyes
                drawCircle(color = tint, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx - radius * 0.3f, cy - radius * 0.1f))
                drawCircle(color = tint, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx + radius * 0.3f, cy - radius * 0.1f))
                
                // Eyebrows slanted up in center
                drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(cx - radius * 0.45f, cy - radius * 0.3f), end = androidx.compose.ui.geometry.Offset(cx - radius * 0.15f, cy - radius * 0.2f), strokeWidth = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(cx + radius * 0.45f, cy - radius * 0.3f), end = androidx.compose.ui.geometry.Offset(cx + radius * 0.15f, cy - radius * 0.2f), strokeWidth = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                
                // Wavy mouth
                val path = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - radius * 0.3f, cy + radius * 0.25f)
                    quadraticBezierTo(cx - radius * 0.15f, cy + radius * 0.15f, cx, cy + radius * 0.25f)
                    quadraticBezierTo(cx + radius * 0.15f, cy + radius * 0.35f, cx + radius * 0.3f, cy + radius * 0.25f)
                }
                drawPath(path = path, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            "tranquilo" -> {
                // Closed happy eyes
                val pathLeft = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - radius * 0.4f, cy - radius * 0.05f)
                    quadraticBezierTo(cx - radius * 0.25f, cy - radius * 0.2f, cx - radius * 0.1f, cy - radius * 0.05f)
                }
                val pathRight = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx + radius * 0.1f, cy - radius * 0.05f)
                    quadraticBezierTo(cx + radius * 0.25f, cy - radius * 0.2f, cx + radius * 0.4f, cy - radius * 0.05f)
                }
                drawPath(path = pathLeft, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                drawPath(path = pathRight, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                
                // Smile
                val smile = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - radius * 0.25f, cy + radius * 0.2f)
                    quadraticBezierTo(cx, cy + radius * 0.35f, cx + radius * 0.25f, cy + radius * 0.2f)
                }
                drawPath(path = smile, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
            "feliz" -> {
                // Happy eyes
                drawCircle(color = tint, radius = 2.2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx - radius * 0.3f, cy - radius * 0.1f))
                drawCircle(color = tint, radius = 2.2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx + radius * 0.3f, cy - radius * 0.1f))
                
                // Smile
                val smile = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - radius * 0.35f, cy + radius * 0.15f)
                    quadraticBezierTo(cx, cy + radius * 0.5f, cx + radius * 0.35f, cy + radius * 0.15f)
                    quadraticBezierTo(cx, cy + radius * 0.22f, cx - radius * 0.35f, cy + radius * 0.15f)
                }
                drawPath(path = smile, color = tint)
            }
            "triste" -> {
                // Sad eyes
                val eyeLeft = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - radius * 0.4f, cy - radius * 0.15f)
                    quadraticBezierTo(cx - radius * 0.25f, cy - radius * 0.05f, cx - radius * 0.1f, cy - radius * 0.15f)
                }
                val eyeRight = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx + radius * 0.1f, cy - radius * 0.15f)
                    quadraticBezierTo(cx + radius * 0.25f, cy - radius * 0.05f, cx + radius * 0.4f, cy - radius * 0.15f)
                }
                drawPath(path = eyeLeft, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                drawPath(path = eyeRight, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))

                // Frown mouth
                val frown = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - radius * 0.25f, cy + radius * 0.32f)
                    quadraticBezierTo(cx, cy + radius * 0.15f, cx + radius * 0.25f, cy + radius * 0.32f)
                }
                drawPath(path = frown, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                
                // Small tear
                drawCircle(color = Color(0xFF1E88E5), radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx - radius * 0.35f, cy + radius * 0.1f))
            }
            "frustrado" -> {
                // Angry eyebrows/eyes
                drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(cx - radius * 0.45f, cy - radius * 0.25f), end = androidx.compose.ui.geometry.Offset(cx - radius * 0.15f, cy - radius * 0.1f), strokeWidth = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(cx + radius * 0.45f, cy - radius * 0.25f), end = androidx.compose.ui.geometry.Offset(cx + radius * 0.15f, cy - radius * 0.1f), strokeWidth = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
                
                drawCircle(color = tint, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx - radius * 0.3f, cy - radius * 0.05f))
                drawCircle(color = tint, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx + radius * 0.3f, cy - radius * 0.05f))

                // Straight line mouth
                drawLine(color = tint, start = androidx.compose.ui.geometry.Offset(cx - radius * 0.3f, cy + radius * 0.25f), end = androidx.compose.ui.geometry.Offset(cx + radius * 0.3f, cy + radius * 0.25f), strokeWidth = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round)
            }
            "emocionado" -> {
                // Wide open happy eyes
                drawCircle(color = tint, radius = 2.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx - radius * 0.3f, cy - radius * 0.15f))
                drawCircle(color = tint, radius = 2.5.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx + radius * 0.3f, cy - radius * 0.15f))
                
                // Raised eyebrows
                val ebLeft = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - radius * 0.45f, cy - radius * 0.3f)
                    quadraticBezierTo(cx - radius * 0.3f, cy - radius * 0.38f, cx - radius * 0.15f, cy - radius * 0.3f)
                }
                val ebRight = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx + 0.15f * radius, cy - radius * 0.3f)
                    quadraticBezierTo(cx + radius * 0.3f, cy - radius * 0.38f, cx + radius * 0.45f, cy - radius * 0.3f)
                }
                drawPath(path = ebLeft, color = tint, style = Stroke(width = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
                drawPath(path = ebRight, color = tint, style = Stroke(width = 1.5.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))

                // Open mouth
                drawCircle(
                    color = tint,
                    radius = radius * 0.2f,
                    center = androidx.compose.ui.geometry.Offset(cx, cy + radius * 0.22f)
                )
            }
            else -> {
                // Default calm face
                drawCircle(color = tint, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx - radius * 0.3f, cy - radius * 0.1f))
                drawCircle(color = tint, radius = 2.dp.toPx(), center = androidx.compose.ui.geometry.Offset(cx + radius * 0.3f, cy - radius * 0.1f))
                val smile = androidx.compose.ui.graphics.Path().apply {
                    moveTo(cx - radius * 0.25f, cy + radius * 0.2f)
                    quadraticBezierTo(cx, cy + radius * 0.35f, cx + radius * 0.25f, cy + radius * 0.2f)
                }
                drawPath(path = smile, color = tint, style = Stroke(width = 1.8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round))
            }
        }
    }
}
