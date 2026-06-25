package com.relaxmind.app.features.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.data.model.DiaryEntry
import com.relaxmind.app.ui.themes.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.text.SimpleDateFormat
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun DiaryDayEntriesScreen(
    dateString: String, // YYYY-MM-DD
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onCreateEntry: () -> Unit
) {
    val allEntries by viewModel.diaryEntries.collectAsState()
    val dayEntries = allEntries.filter { it.date == dateString }

    LaunchedEffect(Unit) {
        viewModel.loadDiaryEntries()
    }

    val date = try {
        LocalDate.parse(dateString)
    } catch (e: Exception) {
        LocalDate.now()
    }
    
    val formattedDate = date.format(DateTimeFormatter.ofPattern("EEEE d 'de' MMMM", Locale("es", "ES"))).replaceFirstChar { it.uppercase() }

    Scaffold(
        containerColor = Color(0xFFFAFAFA),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(modifier = Modifier.fillMaxWidth()) {
                    IconButton(
                        onClick = onNavigateBack,
                        modifier = Modifier.align(Alignment.CenterStart).padding(start = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Volver",
                            tint = TextPrimary
                        )
                    }
                    
                    Text(
                        text = "Entradas del día",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                
                Text(
                    text = formattedDate,
                    fontFamily = LexendFontFamily,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateEntry,
                containerColor = Color(0xFFE8F5E9),
                contentColor = PatientGreen,
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Nueva entrada")
                Spacer(Modifier.width(8.dp))
                Text("Nueva entrada", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp),
            contentPadding = PaddingValues(bottom = 100.dp, top = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                DiaryDaySummaryCard(entriesList = dayEntries)
            }
            
            items(dayEntries) { entry ->
                DiaryEntryItemCard(entry)
            }
            
            if (dayEntries.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Aún no hay entradas para este día.",
                            fontFamily = LexendFontFamily,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryDaySummaryCard(entriesList: List<DiaryEntry> = emptyList()) {
    // Generate summary of emotions
    val emotionCounts = entriesList.groupingBy { it.emotion }.eachCount()
    val topEmotions = emotionCounts.entries.sortedByDescending { it.value }.take(3)
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Main icon
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = PatientGreen, modifier = Modifier.size(24.dp))
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(
                        text = "${entriesList.size} entradas registradas",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = TextPrimary
                    )
                    Text(
                        text = "Resumen de tu día",
                        fontFamily = LexendFontFamily,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
            
            // Emotion mini dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                topEmotions.forEach { (emotion, count) ->
                    val (color, icon) = getEmotionDetails(emotion)
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(color.copy(alpha = 0.2f), CircleShape)
                                .border(1.dp, color.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                        }
                        Text(
                            text = count.toString(),
                            fontFamily = LexendFontFamily,
                            fontSize = 10.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryEntryItemCard(entry: DiaryEntry) {
    val (iconColor, icon) = getEmotionDetails(entry.emotion)
    
    val timeString = entry.createdAt?.let {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        sdf.format(it)
    } ?: ""

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, iconColor.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emotion Icon
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(Color.White, iconColor.copy(alpha = 0.2f))
                                ),
                                shape = CircleShape
                            )
                            .border(1.dp, iconColor.copy(alpha = 0.1f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(icon, contentDescription = null, tint = iconColor, modifier = Modifier.size(24.dp))
                    }
                    
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    Column {
                        Text(
                            text = entry.emotion.ifEmpty { "Desconocido" },
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = entry.notes.ifEmpty { "Sin notas" },
                            fontFamily = LexendFontFamily,
                            fontSize = 13.sp,
                            color = TextSecondary,
                            maxLines = 2,
                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                        )
                    }
                }
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Category pill
                    Box(
                        modifier = Modifier
                            .background(iconColor.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = entry.category.ifEmpty { "General" },
                            fontFamily = LexendFontFamily,
                            fontSize = 11.sp,
                            color = iconColor.copy(alpha = 1f).copy(alpha = 0.8f),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    IconButton(onClick = { /* Opciones */ }, modifier = Modifier.size(24.dp).padding(start = 4.dp)) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Opciones", tint = TextSecondary)
                    }
                }
            }
            
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = BorderSoft,
                thickness = 0.5.dp
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (timeString.isNotEmpty()) {
                    Text(
                        text = timeString,
                        fontFamily = LexendFontFamily,
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // Small dot separator
                    Box(modifier = Modifier.size(3.dp).background(BorderSoft, CircleShape))
                    Spacer(modifier = Modifier.width(12.dp))
                }
                
                Text(
                    text = "Nota guardada exitosamente",
                    fontFamily = LexendFontFamily,
                    fontSize = 11.sp,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun getEmotionDetails(emotion: String): Pair<Color, ImageVector> {
    val lower = emotion.lowercase()
    return when {
        lower.contains("feliz") || lower.contains("alegre") -> Pair(Color(0xFFFFB74D), Icons.Default.Star)
        lower.contains("tranquil") || lower.contains("calma") -> Pair(PatientGreen, Icons.Default.Favorite)
        lower.contains("ansios") || lower.contains("nervios") -> Pair(Color(0xFF9575CD), Icons.Default.Cloud)
        lower.contains("triste") || lower.contains("melanc") -> Pair(Color(0xFF64B5F6), Icons.Default.Info)
        lower.contains("enojad") || lower.contains("frustrad") -> Pair(Color(0xFFE57373), Icons.Default.Warning)
        else -> Pair(PatientGreen, Icons.Default.Face)
    }
}
