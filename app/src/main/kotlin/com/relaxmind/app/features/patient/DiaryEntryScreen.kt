package com.relaxmind.app.features.patient

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.FullScreenLoadingScreen
import com.relaxmind.app.ui.themes.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed

private data class EmotionOption(val label: String)
private data class CategoryOption(val label: String, val icon: ImageVector)

@OptIn(ExperimentalMaterial3Api::class)
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
    var showValidationError by remember { mutableStateOf(false) }

    val categories = listOf(
        CategoryOption("Estrés", Icons.Default.Cloud),
        CategoryOption("Familia", Icons.Default.Groups),
        CategoryOption("Trabajo", Icons.Default.Work),
        CategoryOption("Logro", Icons.Default.Star),
        CategoryOption("Otro", Icons.Default.MoreHoriz)
    )
    val emotions = listOf(
        EmotionOption("Ansioso"),
        EmotionOption("Tranquilo"),
        EmotionOption("Feliz"),
        EmotionOption("Triste"),
        EmotionOption("Frustrado")
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

    if (isLoading) {
        FullScreenLoadingScreen(text = "Guardando entrada...")
        return
    }

    Scaffold(
        containerColor = Color(0xFFFEFCF8),
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 24.dp)
            ) {
                Button(
                    onClick = {
                        if (notes.isBlank()) {
                            showValidationError = true
                        } else {
                            showValidationError = false
                            viewModel.createDiaryEntry(
                                category = selectedCategory,
                                emotion = selectedEmotion,
                                notes = notes,
                                localPhotoUris = selectedPhotoUris,
                                context = context,
                                onSuccess = {
                                    onSaved()
                                }
                            )
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = DiaryOrange),
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Icon(imageVector = Icons.Default.Edit, contentDescription = "Guardar", tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Guardar entrada",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = Color.White
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
            ) {
                // Header Area
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 20.dp)
                ) {
                    Box(modifier = Modifier.fillMaxWidth()) {
                        IconButton(
                            onClick = onNavigateBack,
                            modifier = Modifier.align(Alignment.CenterStart)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                                contentDescription = "Volver",
                                tint = TextPrimary
                            )
                        }
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.align(Alignment.Center)
                        ) {
                            Text(
                                text = "Nueva nota",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 20.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "Guarda un momento de tu día ✨",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )
                        }
                    }
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(28.dp)
                ) {
                    // CATEGORÍA CHIPS
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Label, contentDescription = null, tint = DiaryOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Categoría",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            categories.forEach { cat ->
                                val isSelected = cat.label == selectedCategory
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(if (isSelected) DiaryOrange else Color.White)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) DiaryOrange else BorderSoft,
                                            shape = RoundedCornerShape(20.dp)
                                        )
                                        .clickable { selectedCategory = cat.label }
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = cat.icon,
                                            contentDescription = cat.label,
                                            tint = if (isSelected) Color.White else TextSecondary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = cat.label,
                                            fontFamily = LexendFontFamily,
                                            color = if (isSelected) Color.White else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium,
                                            fontSize = 13.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // ESTADOS EMOCIONALES
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Favorite, contentDescription = null, tint = DiaryOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¿Cómo te sientes hoy?",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .horizontalScroll(rememberScrollState())
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            emotions.forEach { option ->
                                val isSelected = option.label == selectedEmotion
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (isSelected) DiaryOrange.copy(alpha = 0.1f) else Color.White
                                    ),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) DiaryOrange else BorderSoft
                                    ),
                                    shape = RoundedCornerShape(16.dp),
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
                                            tint = if (isSelected) DiaryOrange else Color(0xFF6B7280),
                                            modifier = Modifier.size(48.dp)
                                        )
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text(
                                            text = option.label,
                                            fontFamily = LexendFontFamily,
                                            color = if (isSelected) DiaryOrange else TextSecondary,
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.sp
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // NOTAS
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = null, tint = DiaryOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "¿Qué quieres recordar de hoy?",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(160.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.White)
                                .border(1.dp, DiaryOrange.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            TextField(
                                value = notes,
                                onValueChange = { 
                                    if (it.length <= 500) notes = it 
                                    if (notes.isNotBlank()) showValidationError = false
                                },
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(bottom = 24.dp),
                                colors = TextFieldDefaults.colors(
                                    focusedContainerColor = Color.Transparent,
                                    unfocusedContainerColor = Color.Transparent,
                                    focusedIndicatorColor = Color.Transparent,
                                    unfocusedIndicatorColor = Color.Transparent,
                                    cursorColor = DiaryOrange
                                ),
                                textStyle = androidx.compose.ui.text.TextStyle(
                                    fontFamily = LexendFontFamily,
                                    fontSize = 14.sp,
                                    color = TextPrimary
                                ),
                                placeholder = {
                                    Text(
                                        text = "Empieza a escribir aquí...",
                                        fontFamily = LexendFontFamily,
                                        color = TextSecondary,
                                        fontSize = 14.sp
                                    )
                                }
                            )
                            Text(
                                text = "${notes.length} / 500",
                                fontFamily = LexendFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary,
                                modifier = Modifier
                                    .align(Alignment.BottomEnd)
                                    .padding(end = 16.dp, bottom = 12.dp)
                            )
                        }
                        if (showValidationError) {
                            Text(
                                text = "La nota no puede estar vacía",
                                color = MaterialTheme.colorScheme.error,
                                fontSize = 12.sp,
                                fontFamily = LexendFontFamily,
                                modifier = Modifier.padding(top = 4.dp, start = 4.dp)
                            )
                        }
                    }

                    // FOTOS (REMOVIDO TEMPORALMENTE)
                    /*
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(imageVector = Icons.Default.Image, contentDescription = null, tint = DiaryOrange, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Fotos",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (selectedPhotoUris.size < 5) {
                                item {
                                    Box(
                                        modifier = Modifier
                                            .width(110.dp)
                                            .height(90.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(DiaryOrange.copy(alpha = 0.05f))
                                            .drawBehind {
                                                drawRoundRect(
                                                    color = DiaryOrange.copy(alpha = 0.5f),
                                                    style = Stroke(
                                                        width = 1.dp.toPx(),
                                                        pathEffect = androidx.compose.ui.graphics.PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                                                    ),
                                                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx(), 12.dp.toPx())
                                                )
                                            }
                                            .clickable {
                                                photoPickerLauncher.launch(
                                                    androidx.activity.result.PickVisualMediaRequest(
                                                        androidx.activity.result.contract.ActivityResultContracts.PickVisualMedia.ImageOnly
                                                    )
                                                )
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Icon(
                                                imageVector = Icons.Default.Add,
                                                contentDescription = "Agregar foto",
                                                tint = DiaryOrange
                                            )
                                            Spacer(modifier = Modifier.height(4.dp))
                                            Text(
                                                text = "Agregar foto",
                                                fontFamily = LexendFontFamily,
                                                fontSize = 12.sp,
                                                color = DiaryOrange,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }
                                }
                            }

                            itemsIndexed(selectedPhotoUris) { index, uri ->
                                Box(
                                    modifier = Modifier
                                        .width(90.dp)
                                        .height(90.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .border(1.dp, BorderSoft, RoundedCornerShape(12.dp))
                                ) {
                                    AsyncImage(
                                        model = uri,
                                        contentDescription = "Foto $index",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop
                                    )
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .size(24.dp)
                                            .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                            .clickable {
                                                selectedPhotoUris = selectedPhotoUris.filterIndexed { i, _ -> i != index }
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Eliminar",
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }

                        // Privacy Text
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 16.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(imageVector = Icons.Default.Security, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Tus fotos son privadas y solo tú puedes verlas.",
                                fontFamily = LexendFontFamily,
                                fontSize = 11.sp,
                                color = TextSecondary
                            )
                    }
                    */

                    Spacer(modifier = Modifier.height(100.dp))
                }
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
            else -> {
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
