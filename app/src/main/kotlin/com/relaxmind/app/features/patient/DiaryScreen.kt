package com.relaxmind.app.features.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.data.model.DiaryEntry
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.*
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DiaryScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onCreateEntry: () -> Unit,
    onDayClick: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val entries by viewModel.diaryEntries.collectAsState()
    
    var currentMonth by remember { mutableStateOf(YearMonth.now()) }

    LaunchedEffect(Unit) {
        viewModel.loadDiaryEntries()
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            RelaxTopBar(
                title = "Mi Diario",
                onBackClick = onNavigateBack
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onCreateEntry,
                containerColor = DiaryOrange.copy(alpha = 0.2f),
                contentColor = DiaryOrange,
                shape = RoundedCornerShape(24.dp),
                elevation = FloatingActionButtonDefaults.elevation(0.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Nueva entrada")
                Spacer(Modifier.width(8.dp))
                Text("Nueva entrada", fontFamily = LexendFontFamily, fontWeight = FontWeight.Bold)
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
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DiaryCalendarGrid(
                    currentMonth = currentMonth,
                    entries = entries,
                    onMonthPrevious = { currentMonth = currentMonth.minusMonths(1) },
                    onMonthNext = { currentMonth = currentMonth.plusMonths(1) },
                    onDayClick = { date ->
                        val dayEntries = entries.filter { runCatching { LocalDate.parse(it.date) }.getOrNull() == date }
                        if (dayEntries.isNotEmpty()) {
                            onDayClick(date.toString())
                        }
                    }
                )
            }

            if (isLoading) {
                FullScreenLoadingOverlay()
            }
        }
    }
}

@Composable
private fun DiaryCalendarGrid(
    currentMonth: YearMonth,
    entries: List<DiaryEntry>,
    onMonthPrevious: () -> Unit,
    onMonthNext: () -> Unit,
    onDayClick: (LocalDate) -> Unit
) {
    val firstDayOfMonth = currentMonth.atDay(1)
    val daysInMonth = currentMonth.lengthOfMonth()
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value
    val emptyCellsBefore = firstDayOfWeek - 1
    val totalCells = emptyCellsBefore + daysInMonth
    val rows = (0 until totalCells).toList().chunked(7)

    val monthName = currentMonth.month.getDisplayName(java.time.format.TextStyle.FULL, Locale("es", "ES")).replaceFirstChar { it.uppercase() }
    val year = currentMonth.year

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(24.dp),
                ambientColor = DiaryOrange.copy(alpha = 0.08f),
                spotColor = DiaryOrange.copy(alpha = 0.08f)
            )
            .border(1.dp, BorderSoft, RoundedCornerShape(24.dp)),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onMonthPrevious) {
                    Icon(Icons.Default.ChevronLeft, contentDescription = "Mes anterior", tint = DiaryOrange)
                }
                Text(
                    text = "$monthName $year",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = DiaryOrange
                )
                IconButton(onClick = onMonthNext) {
                    Icon(Icons.Default.ChevronRight, contentDescription = "Mes siguiente", tint = DiaryOrange)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            val weekdays = listOf("L", "M", "X", "J", "V", "S", "D")
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                weekdays.forEach { day ->
                    Text(
                        text = day,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            val today = LocalDate.now()

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                rows.forEach { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        for (i in 0 until 7) {
                            val cellIndex = row.getOrNull(i)
                            if (cellIndex == null || cellIndex < emptyCellsBefore) {
                                Box(modifier = Modifier.weight(1f).aspectRatio(0.85f))
                            } else {
                                val dayNum = cellIndex - emptyCellsBefore + 1
                                val date = currentMonth.atDay(dayNum)
                                val dayEntries = entries.filter { runCatching { LocalDate.parse(it.date) }.getOrNull() == date }
                                
                                DiaryCalendarCell(
                                    date = date,
                                    isToday = date == today,
                                    entries = dayEntries,
                                    onClick = { onDayClick(date) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryCalendarCell(
    date: LocalDate,
    isToday: Boolean,
    entries: List<DiaryEntry>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val hasEntries = entries.isNotEmpty()
    val photoUrl = entries.firstOrNull { it.photoUrls.isNotEmpty() }?.photoUrls?.firstOrNull()
    val emotion = entries.firstOrNull()?.emotion?.lowercase() ?: ""
    
    val dotColor = when {
        emotion.contains("triste") || emotion.contains("ansios") -> Color(0xFF90CDF4)
        emotion.contains("feliz") || emotion.contains("alegre") -> ScoreGreenLight
        emotion.contains("enojad") || emotion.contains("frustrad") -> ScoreRed.copy(alpha = 0.6f)
        else -> ScoreYellow
    }

    Box(
        modifier = modifier
            .aspectRatio(0.85f)
            .padding(4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isToday) DiaryOrange.copy(alpha = 0.15f) else Color(0xFFF7FAFC))
            .then(
                if (isToday && !hasEntries) Modifier.border(1.dp, DiaryOrange, RoundedCornerShape(12.dp))
                else Modifier
            )
            .clickable(enabled = hasEntries) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        if (photoUrl != null) {
            AsyncImage(
                model = photoUrl,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay gradient to ensure day number is visible
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Black.copy(alpha = 0.3f), Color.Transparent, Color.Black.copy(alpha = 0.4f))
                        )
                    )
            )
        }
        
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "${date.dayOfMonth}",
                fontFamily = LexendFontFamily,
                fontWeight = if (hasEntries) FontWeight.Bold else FontWeight.Normal,
                fontSize = 14.sp,
                color = if (photoUrl != null) Color.White else if (hasEntries) TextPrimary else TextSecondary.copy(alpha = 0.5f)
            )
            if (hasEntries && photoUrl == null) {
                Spacer(modifier = Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .background(dotColor, CircleShape)
                )
            }
        }
        
        if (isToday && photoUrl != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .border(2.dp, DiaryPeach, RoundedCornerShape(12.dp))
            )
        }
    }
}

@Composable
private fun DiaryHistoryCard(entry: DiaryEntry) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            // Photo at the top if it exists (Polaroid style)
            if (entry.photoUrls.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 12.dp, end = 12.dp, top = 12.dp, bottom = 4.dp)
                ) {
                    DiaryPhotoCollage(
                        photoUrls = entry.photoUrls.take(5),
                        modifier = Modifier.clip(RoundedCornerShape(8.dp))
                    )
                }
            }

            // Text and info below
            Column(
                modifier = Modifier.padding(
                    start = 20.dp, 
                    end = 20.dp, 
                    bottom = 24.dp, 
                    top = if (entry.photoUrls.isEmpty()) 24.dp else 12.dp
                ),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                if (entry.notes.isNotBlank()) {
                    Text(
                        text = "\"${entry.notes}\"",
                        fontFamily = LexendFontFamily,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp,
                        color = TextPrimary.copy(alpha = 0.85f),
                        lineHeight = 24.sp
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Emotion
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        val emotionLabel = entry.emotion.lowercase()
                        val dotColor = when {
                            emotionLabel.contains("triste") || emotionLabel.contains("ansios") -> Color(0xFF90CDF4)
                            emotionLabel.contains("feliz") || emotionLabel.contains("alegre") -> ScoreGreenLight
                            emotionLabel.contains("enojad") || emotionLabel.contains("frustrad") -> ScoreRed.copy(alpha = 0.6f)
                            else -> ScoreYellow
                        }
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .background(dotColor, CircleShape)
                        )
                        Text(
                            text = entry.emotion,
                            fontFamily = LexendFontFamily,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                    }

                    // Category Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFFF7FAFC))
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = entry.category,
                            fontFamily = LexendFontFamily,
                            fontSize = 11.sp,
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DiaryPhotoCollage(
    photoUrls: List<String>,
    modifier: Modifier = Modifier
) {
    when (photoUrls.size) {
        1 -> CollageImage(photoUrls[0], modifier.fillMaxWidth().height(190.dp))
        2 -> Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CollageImage(photoUrls[0], Modifier.weight(1f).height(132.dp))
            CollageImage(photoUrls[1], Modifier.weight(1f).height(132.dp))
        }
        3 -> Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CollageImage(photoUrls[0], Modifier.weight(1.4f).height(172.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                CollageImage(photoUrls[1], Modifier.fillMaxWidth().height(82.dp))
                CollageImage(photoUrls[2], Modifier.fillMaxWidth().height(82.dp))
            }
        }
        4 -> Column(modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CollageImage(photoUrls[0], Modifier.weight(1f).height(104.dp))
                CollageImage(photoUrls[1], Modifier.weight(1f).height(104.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CollageImage(photoUrls[2], Modifier.weight(1f).height(104.dp))
                CollageImage(photoUrls[3], Modifier.weight(1f).height(104.dp))
            }
        }
        else -> Row(modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            CollageImage(photoUrls[0], Modifier.weight(1.2f).height(184.dp))
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CollageImage(photoUrls[1], Modifier.weight(1f).height(88.dp))
                    CollageImage(photoUrls[2], Modifier.weight(1f).height(88.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    CollageImage(photoUrls[3], Modifier.weight(1f).height(88.dp))
                    CollageImage(photoUrls[4], Modifier.weight(1f).height(88.dp))
                }
            }
        }
    }
}

@Composable
private fun CollageImage(
    url: String,
    modifier: Modifier = Modifier
) {
    AsyncImage(
        model = url,
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = modifier.clip(RoundedCornerShape(14.dp))
    )
}
