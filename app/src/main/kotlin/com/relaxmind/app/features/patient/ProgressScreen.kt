package com.relaxmind.app.features.patient

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.Spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.R
import com.relaxmind.app.data.model.CheckIn
import com.relaxmind.app.data.model.UserAchievement
import com.relaxmind.app.ui.components.AchievementUnlockedDialog
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.ScreenHeader
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.BorderSoft
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.LexendTypography
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.ScoreGray
import com.relaxmind.app.ui.themes.ScoreGreenDark
import com.relaxmind.app.ui.themes.ScoreGreenLight
import com.relaxmind.app.ui.themes.ScoreOrange
import com.relaxmind.app.ui.themes.ScoreRed
import com.relaxmind.app.ui.themes.ScoreYellow
import com.relaxmind.app.ui.themes.SoftMint
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import com.relaxmind.app.utils.WellnessScoreCalculator
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

private data class AchievementCatalogItem(
    val key: String,
    val title: String,
    val condition: String,
    val defaultIconUrl: String
)

private val AchievementCatalog = listOf(
    AchievementCatalogItem("first_checkin", "Primeros pasos", "Primer check-in completado", "https://cdn-icons-png.flaticon.com/512/825/825590.png"),
    AchievementCatalogItem("streak_3", "3 dí­as seguidos", "Racha de 3 dí­as", "https://cdn-icons-png.flaticon.com/512/785/785116.png"),
    AchievementCatalogItem("streak_7", "7 dí­as de calma", "Racha de 7 dí­as", "https://cdn-icons-png.flaticon.com/512/785/785116.png"),
    AchievementCatalogItem("streak_14", "Dos semanas imparable", "Racha de 14 dí­as", "https://cdn-icons-png.flaticon.com/512/785/785116.png"),
    AchievementCatalogItem("streak_30", "30 dí­as seguidos", "Racha de 30 dí­as", "https://cdn-icons-png.flaticon.com/512/3112/3112946.png"),
    AchievementCatalogItem("first_meditation", "Enfoque total", "Primera meditación completada", "https://cdn-icons-png.flaticon.com/512/2913/2913520.png"),
    AchievementCatalogItem("meditations_10", "Mente en calma", "10 meditaciones completadas", "https://cdn-icons-png.flaticon.com/512/414/414927.png"),
    AchievementCatalogItem("first_diary", "Mi historia", "Primera entrada de diario", "https://cdn-icons-png.flaticon.com/512/3068/3068327.png"),
    AchievementCatalogItem("diary_7", "Una semana de notas", "7 entradas de diario", "https://cdn-icons-png.flaticon.com/512/3068/3068327.png"),
    AchievementCatalogItem("score_80", "Bienestar alto", "Check-in con 80+ puntos", "https://cdn-icons-png.flaticon.com/512/1828/1828884.png"),
    AchievementCatalogItem("score_100", "Día perfecto", "Check-in con 100 puntos", "https://cdn-icons-png.flaticon.com/512/616/616489.png"),
    AchievementCatalogItem("lumi_first", "Hola Lumi", "Primera conversación con Lumi", "https://cdn-icons-png.flaticon.com/512/134/134914.png")
)

private enum class AchievementIconType {
    SPROUT,
    MEDITATION,
    TARGET,
    TROPHY
}

@Composable
fun ProgressScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigate: (String) -> Unit,
    showBottomNav: Boolean = true
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val streakData by viewModel.streak.collectAsState()
    val unlockedAchievements by viewModel.achievements.collectAsState()
    val allCheckIns by viewModel.allCheckIns.collectAsState()
    val selectedMonth by viewModel.selectedMonth.collectAsState()
    val selectedYear by viewModel.selectedYear.collectAsState()
    var observedAchievementCount by remember { mutableStateOf<Int?>(null) }
    var achievementDialog by remember { mutableStateOf<UserAchievement?>(null) }

    val currentStreak = streakData?.currentStreak ?: 0
    val longestStreak = streakData?.longestStreak ?: 0

    LaunchedEffect(Unit) {
        viewModel.loadProgressData()
    }

    LaunchedEffect(unlockedAchievements) {
        val previousCount = observedAchievementCount
        if (previousCount != null && unlockedAchievements.size > previousCount) {
            achievementDialog = unlockedAchievements.maxByOrNull { it.unlockedAt }
        }
        observedAchievementCount = unlockedAchievements.size
    }

    // Filter check-ins by selected month/year
    val monthlyCheckIns = remember(allCheckIns, selectedMonth, selectedYear) {
        allCheckIns.filter { checkIn ->
            runCatching {
                val date = LocalDate.parse(checkIn.date)
                date.monthValue == selectedMonth && date.year == selectedYear
            }.getOrDefault(false)
        }
    }

    // Map dayOfMonth to score
    val checkInsMap = remember(monthlyCheckIns) {
        monthlyCheckIns.associate { checkIn ->
            runCatching {
                val date = LocalDate.parse(checkIn.date)
                date.dayOfMonth to checkIn.score
            }.getOrDefault(1 to 0)
        }
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = LexendTypography
    ) {
        Scaffold(
            containerColor = Color.White,
            bottomBar = {
                if (showBottomNav) {
                    RelaxBottomNav(
                        selectedRoute = "patient/progress",
                        onNavigate = onNavigate,
                        role = AppRole.PATIENT
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Background gradient blobs
                SoftGradientBackground(animateBlobs = true)

                if (isLoading && allCheckIns.isEmpty() && streakData == null) {
                    LoadingIndicator()
                } else {
                    var selectedDayInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal = 24.dp),
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // 1. Header
                        ProgressHeader()

                        // 2. Streak Card
                        StreakCard(
                            currentStreak = currentStreak,
                            longestStreak = longestStreak
                        )

                        // 3. Monthly Progress Card
                        MonthlyProgressCard(
                            year = selectedYear,
                            month = selectedMonth,
                            checkIns = checkInsMap,
                            onMonthPrevious = { viewModel.selectPreviousMonth() },
                            onMonthNext = { viewModel.selectNextMonth() },
                            onDayClick = { day, score ->
                                selectedDayInfo = Pair(day, score)
                            }
                        )

                        // 4. Achievements Section
                        AchievementsSection(
                            unlockedAchievements = unlockedAchievements,
                            catalog = AchievementCatalog
                        )

                        // 5. History Section
                        HistorySection(
                            history = allCheckIns
                        )

                        // Spacing for floating bottom capsule bar
                        Spacer(modifier = Modifier.height(100.dp))
                    }

                    // Detail Pop-up / Dialog
                    if (selectedDayInfo != null) {
                        val (day, score) = selectedDayInfo!!
                        val category = WellnessScoreCalculator.getCategory(score)
                        AlertDialog(
                            onDismissRequest = { selectedDayInfo = null },
                            title = { Text("Registro del día $day", fontFamily = LexendFontFamily) },
                            text = {
                                Text(
                                    text = "Puntaje: $score / 100\nCategoría: $category",
                                    fontFamily = LexendFontFamily,
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            },
                            confirmButton = {
                                TextButton(onClick = { selectedDayInfo = null }) {
                                    Text("Cerrar", fontFamily = LexendFontFamily, color = PatientGreen)
                                }
                            }
                        )
                    }

                    achievementDialog?.let { achievement ->
                        AchievementUnlockedDialog(
                            title = achievement.title.ifBlank { "Nuevo logro" },
                            iconUrl = achievement.iconUrl,
                            onDismiss = { achievementDialog = null }
                        )
                    }
                }
            }
        }
    }
}

// -----------------------------------------------------------------------------
@Composable
private fun ProgressHeader(
    modifier: Modifier = Modifier
) {
    ScreenHeader(
        title = "Progreso",
        subtitle = "Tu evolución y bienestar día a día",
        modifier = modifier,
        horizontalPadding = 0.dp
    )
}

// -----------------------------------------------------------------------------
// 2. STREAK CARD
// -----------------------------------------------------------------------------
@Composable
private fun StreakCard(
    currentStreak: Int,
    longestStreak: Int
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "streak-scale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFFF97316).copy(alpha = 0.15f),
                spotColor = Color(0xFFF97316).copy(alpha = 0.15f)
            )
            .border(1.2.dp, Color(0xFFFFEDD5), RoundedCornerShape(28.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFFFFF7ED), Color(0xFFFFEDD5)),
                    start = androidx.compose.ui.geometry.Offset.Zero,
                    end = androidx.compose.ui.geometry.Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                ),
                shape = RoundedCornerShape(28.dp)
            )
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isPressed = !isPressed }
            )
    ) {
        // Background floating stars
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
        ) {
            val w = size.width
            val h = size.height
            val starColor = Color(0xFFFDBA74).copy(alpha = 0.6f)

            fun drawStar(center: androidx.compose.ui.geometry.Offset, radius: Float) {
                val path = androidx.compose.ui.graphics.Path()
                val points = 5
                val angle = Math.PI / points
                for (i in 0 until points * 2) {
                    val r = if (i % 2 == 0) radius else radius / 2.2f
                    val a = i * angle - Math.PI / 2
                    val x = center.x + r * Math.cos(a).toFloat()
                    val y = center.y + r * Math.sin(a).toFloat()
                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                }
                path.close()
                drawPath(path, starColor)
            }

            drawStar(androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.25f), 14.dp.toPx())
            drawStar(androidx.compose.ui.geometry.Offset(w * 0.88f, h * 0.5f), 22.dp.toPx())
            drawStar(androidx.compose.ui.geometry.Offset(w * 0.65f, h * 0.7f), 10.dp.toPx())
            drawStar(androidx.compose.ui.geometry.Offset(w * 0.92f, h * 0.8f), 12.dp.toPx())
            
            // Draw a few small circular particles
            drawCircle(starColor, 4.dp.toPx(), androidx.compose.ui.geometry.Offset(w * 0.8f, h * 0.1f))
            drawCircle(starColor, 3.dp.toPx(), androidx.compose.ui.geometry.Offset(w * 0.7f, h * 0.5f))
            drawCircle(starColor, 5.dp.toPx(), androidx.compose.ui.geometry.Offset(w * 0.85f, h * 0.85f))
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Large circular container for the flame icon
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                FlameIcon(modifier = Modifier.size(46.dp))
            }

            Spacer(modifier = Modifier.width(24.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    verticalAlignment = Alignment.Bottom
                ) {
                    Text(
                        text = "$currentStreak",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 48.sp,
                        color = Color(0xFFEA580C), // Deep Orange
                        lineHeight = 48.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "dí­as seguidos",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary,
                        modifier = Modifier.padding(bottom = 6.dp)
                    )
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Mejor racha: $longestStreak días",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun FlameIcon(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        // Styled vector path for outer orange flame
        val outerPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.05f)
            cubicTo(w * 0.2f, h * 0.35f, w * 0.1f, h * 0.65f, w * 0.25f, h * 0.85f)
            cubicTo(w * 0.35f, h * 0.98f, w * 0.65f, h * 0.98f, w * 0.75f, h * 0.85f)
            cubicTo(w * 0.9f, h * 0.65f, w * 0.8f, h * 0.35f, w * 0.5f, h * 0.05f)
            close()
        }
        drawPath(
            path = outerPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFF97316), Color(0xFFDC2626)) // Orange to Red
            )
        )

        // Light inner core
        val innerPath = androidx.compose.ui.graphics.Path().apply {
            moveTo(w * 0.5f, h * 0.35f)
            cubicTo(w * 0.35f, h * 0.5f, w * 0.3f, h * 0.7f, w * 0.4f, h * 0.82f)
            cubicTo(w * 0.45f, h * 0.88f, w * 0.55f, h * 0.88f, w * 0.6f, h * 0.82f)
            cubicTo(w * 0.7f, h * 0.7f, w * 0.65f, h * 0.5f, w * 0.5f, h * 0.35f)
            close()
        }
        drawPath(
            path = innerPath,
            brush = Brush.verticalGradient(
                colors = listOf(Color(0xFFFEF08A), Color(0xFFF97316)) // Yellow to Orange
            )
        )
    }
}

// -----------------------------------------------------------------------------
// 3. MONTHLY PROGRESS CARD
// -----------------------------------------------------------------------------
@Composable
private fun MonthlyProgressCard(
    year: Int,
    month: Int,
    checkIns: Map<Int, Int>,
    onMonthPrevious: () -> Unit,
    onMonthNext: () -> Unit,
    onDayClick: (day: Int, score: Int) -> Unit
) {
    val firstDayOfMonth = remember(year, month) { LocalDate.of(year, month, 1) }
    val daysInMonth = remember(year, month) { firstDayOfMonth.lengthOfMonth() }
    val firstDayOfWeek = remember(year, month) { firstDayOfMonth.dayOfWeek.value } // 1 = Mon, 7 = Sun
    val emptyCellsBefore = firstDayOfWeek - 1
    val totalCells = emptyCellsBefore + daysInMonth
    val rows = remember(totalCells) { (0 until totalCells).toList().chunked(7) }

    val prevMonthName = remember(month) { getMonthNameInSpanish(if (month == 1) 12 else month - 1) }
    val nextMonthName = remember(month) { getMonthNameInSpanish(if (month == 12) 1 else month + 1) }
    val currentMonthName = remember(month) { getMonthNameInSpanish(month) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 10.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = PatientGreen.copy(alpha = 0.08f),
                spotColor = PatientGreen.copy(alpha = 0.08f)
            )
            .border(1.2.dp, BorderSoft, RoundedCornerShape(28.dp)),
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Month Selector Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "< $prevMonthName",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PatientGreenLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMonthPrevious() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )

                Text(
                    text = "$currentMonthName $year",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )

                Text(
                    text = "$nextMonthName >",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = PatientGreenLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onMonthNext() }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Weekday Headings
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

            Spacer(modifier = Modifier.height(16.dp))

            // Calendar Matrix
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                rows.forEach { rowCells ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        rowCells.forEach { cellIndex ->
                            if (cellIndex < emptyCellsBefore) {
                                Spacer(modifier = Modifier.size(34.dp))
                            } else {
                                val dayNumber = cellIndex - emptyCellsBefore + 1
                                val score = checkIns[dayNumber]
                                val isCellToday = LocalDate.now().year == year &&
                                                  LocalDate.now().monthValue == month &&
                                                  LocalDate.now().dayOfMonth == dayNumber
                                val cellColor = WellnessScoreCalculator.getScoreColor(score)
                                val hasScore = score != null

                                Box(
                                    modifier = Modifier
                                        .size(38.dp)
                                        .clip(CircleShape)
                                        .background(if (hasScore) cellColor else Color(0xFFF4F7F5))
                                        .then(
                                            if (isCellToday) {
                                                Modifier.border(1.5.dp, PatientGreen.copy(alpha = 0.45f), CircleShape)
                                            } else Modifier
                                        )
                                        .clickable(enabled = score != null) {
                                            score?.let { onDayClick(dayNumber, it) }
                                        },
                                    contentAlignment = Alignment.Center
                                ) {
                                    val isLightBackground = !hasScore ||
                                        cellColor == ScoreGray ||
                                        cellColor == ScoreYellow ||
                                        cellColor == ScoreGreenLight
                                    Text(
                                        text = dayNumber.toString(),
                                        fontFamily = LexendFontFamily,
                                        fontWeight = if (isCellToday || hasScore) FontWeight.Bold else FontWeight.Medium,
                                        fontSize = 13.sp,
                                        color = when {
                                            hasScore && !isLightBackground -> Color.White
                                            hasScore -> TextPrimary
                                            isCellToday -> PatientGreen
                                            else -> TextSecondary
                                        }
                                    )
                                }
                            }
                        }
                        // Padding row if short
                        if (rowCells.size < 7) {
                            repeat(7 - rowCells.size) {
                                Spacer(modifier = Modifier.size(38.dp))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Legend below grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = ScoreRed, text = "Muy bajo")
                LegendItem(color = ScoreOrange, text = "Bajo")
                LegendItem(color = ScoreYellow, text = "Moderado")
                LegendItem(color = ScoreGreenLight, text = "Bueno")
                LegendItem(color = ScoreGreenDark, text = "Excelente")
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 11.sp,
            color = TextSecondary
        )
    }
}

// -----------------------------------------------------------------------------
// 4. ACHIEVEMENTS SECTION
// -----------------------------------------------------------------------------
@Composable
private fun AchievementsSection(
    unlockedAchievements: List<UserAchievement>,
    catalog: List<AchievementCatalogItem>
) {
    var showAll by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.progress_achievements),
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            Text(
                text = if (showAll) "Ver destacados" else "Ver todos",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = PatientGreenLight,
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .clickable { showAll = !showAll }
                    .padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (showAll) {
            // Chunked list of all achievements for vertical display inside ScrollView
            val columns = 3
            val chunked = remember(catalog) { catalog.chunked(columns) }

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                chunked.forEach { rowItems ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        rowItems.forEach { item ->
                            val isUnlocked = unlockedAchievements.any { it.achievementKey == item.key }
                            val iconType = when (item.key) {
                                "first_checkin" -> AchievementIconType.SPROUT
                                "streak_7" -> AchievementIconType.MEDITATION
                                "first_meditation" -> AchievementIconType.TARGET
                                "streak_30" -> AchievementIconType.TROPHY
                                else -> {
                                    if (item.key.contains("streak")) AchievementIconType.TROPHY
                                    else if (item.key.contains("meditation")) AchievementIconType.MEDITATION
                                    else if (item.key.contains("diary")) AchievementIconType.SPROUT
                                    else AchievementIconType.TARGET
                                }
                            }
                            AchievementCard(
                                title = item.title,
                                isUnlocked = isUnlocked,
                                iconType = iconType
                            )
                        }
                        if (rowItems.size < columns) {
                            repeat(columns - rowItems.size) {
                                Spacer(modifier = Modifier.width(76.dp))
                            }
                        }
                    }
                }
            }
        } else {
            // Row of 4 featured achievements
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AchievementCard(
                    title = "Primeros pasos",
                    isUnlocked = unlockedAchievements.any { it.achievementKey == "first_checkin" },
                    iconType = AchievementIconType.SPROUT
                )
                AchievementCard(
                    title = "7 días de calma",
                    isUnlocked = unlockedAchievements.any { it.achievementKey == "streak_7" },
                    iconType = AchievementIconType.MEDITATION
                )
                AchievementCard(
                    title = "Enfoque total",
                    isUnlocked = unlockedAchievements.any { it.achievementKey == "first_meditation" },
                    iconType = AchievementIconType.TARGET
                )
                AchievementCard(
                    title = "30 dí­as seguidos",
                    isUnlocked = unlockedAchievements.any { it.achievementKey == "streak_30" },
                    iconType = AchievementIconType.TROPHY
                )
            }
        }
    }
}

@Composable
private fun AchievementCard(
    title: String,
    isUnlocked: Boolean,
    iconType: AchievementIconType
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "ach-scale"
    )

    val cardBg = if (isUnlocked) {
        when (iconType) {
            AchievementIconType.SPROUT -> Color(0xFFF4FBF7)
            AchievementIconType.MEDITATION -> Color(0xFFF1EDFF)
            AchievementIconType.TARGET -> Color(0xFFEBF8FF)
            AchievementIconType.TROPHY -> Color(0xFFFFF9EF)
        }
    } else {
        Color(0xFFF7F7F7)
    }

    val borderStrokeColor = if (isUnlocked) BorderSoft else Color(0xFFE6E8EF)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .scale(scale)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isPressed = !isPressed }
            )
    ) {
        Box(
            modifier = Modifier
                .size(68.dp)
                .shadow(
                    elevation = if (isUnlocked) 4.dp else 1.dp,
                    shape = RoundedCornerShape(20.dp),
                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.1f),
                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.1f)
                )
                .border(1.2.dp, borderStrokeColor, RoundedCornerShape(20.dp))
                .background(cardBg, RoundedCornerShape(20.dp)),
            contentAlignment = Alignment.Center
        ) {
            AchievementVectorIcon(
                iconType = iconType,
                isUnlocked = isUnlocked,
                modifier = Modifier.size(40.dp)
            )

            if (!isUnlocked) {
                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.BottomEnd)
                        .offset(x = 2.dp, y = 2.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(1.dp, Color(0xFFE2F3EB), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Lock,
                        contentDescription = "Bloqueado",
                        tint = TextSecondary,
                        modifier = Modifier.size(11.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = if (isUnlocked) TextPrimary else TextSecondary,
            textAlign = TextAlign.Center,
            maxLines = 2,
            lineHeight = 13.sp,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(2.dp))

        Text(
            text = if (isUnlocked) "Completado" else "Bloqueada",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 10.sp,
            color = if (isUnlocked) PatientGreen else TextSecondary.copy(alpha = 0.6f),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun AchievementVectorIcon(
    iconType: AchievementIconType,
    isUnlocked: Boolean,
    modifier: Modifier = Modifier
) {
    when (iconType) {
        AchievementIconType.SPROUT -> {
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                val primaryColor = if (isUnlocked) Color(0xFF0F6E56) else Color(0xFF7A7F8F)
                val leafColor = if (isUnlocked) Color(0xFF68D391) else Color(0xFFCBD5E0)

                val stem = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.5f, h * 0.85f)
                    quadraticBezierTo(w * 0.5f, h * 0.45f, w * 0.46f, h * 0.25f)
                }
                drawPath(
                    path = stem,
                    color = primaryColor,
                    style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
                )

                val leftLeaf = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.48f, h * 0.52f)
                    quadraticBezierTo(w * 0.26f, h * 0.40f, w * 0.22f, h * 0.46f)
                    quadraticBezierTo(w * 0.32f, h * 0.62f, w * 0.48f, h * 0.52f)
                    close()
                }
                drawPath(path = leftLeaf, color = leafColor)

                val rightLeaf = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.48f, h * 0.42f)
                    quadraticBezierTo(w * 0.70f, h * 0.28f, w * 0.74f, h * 0.34f)
                    quadraticBezierTo(w * 0.64f, h * 0.52f, w * 0.48f, h * 0.42f)
                    close()
                }
                drawPath(path = rightLeaf, color = leafColor)
            }
        }
        AchievementIconType.MEDITATION -> {
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                val primaryColor = if (isUnlocked) Color(0xFF7C3AED) else Color(0xFF7A7F8F)
                val bodyColor = if (isUnlocked) Color(0xFFC084FC) else Color(0xFFCBD5E0)

                drawCircle(
                    color = primaryColor,
                    radius = w * 0.12f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.25f)
                )

                val body = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.5f, h * 0.38f)
                    lineTo(w * 0.36f, h * 0.45f)
                    quadraticBezierTo(w * 0.22f, h * 0.60f, w * 0.32f, h * 0.70f)
                    lineTo(w * 0.68f, h * 0.70f)
                    quadraticBezierTo(w * 0.78f, h * 0.60f, w * 0.64f, h * 0.45f)
                    close()
                }
                drawPath(path = body, color = bodyColor)
                drawPath(path = body, color = primaryColor, style = Stroke(width = 1.5.dp.toPx(), join = StrokeJoin.Round))
            }
        }
        AchievementIconType.TARGET -> {
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                val primaryColor = if (isUnlocked) Color(0xFF1E88E5) else Color(0xFF7A7F8F)
                val ringColor = if (isUnlocked) Color(0xFF90CAF9) else Color(0xFFCBD5E0)
                val centerColor = if (isUnlocked) Color(0xFFE53E3E) else Color(0xFF90A4AE)

                drawCircle(
                    color = primaryColor,
                    radius = w * 0.38f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = ringColor,
                    radius = w * 0.25f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f),
                    style = Stroke(width = 2.dp.toPx())
                )
                drawCircle(
                    color = centerColor,
                    radius = w * 0.12f,
                    center = androidx.compose.ui.geometry.Offset(w * 0.5f, h * 0.5f)
                )

                drawLine(
                    color = primaryColor,
                    start = androidx.compose.ui.geometry.Offset(w * 0.15f, h * 0.85f),
                    end = androidx.compose.ui.geometry.Offset(w * 0.75f, h * 0.25f),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )

                val tip = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.75f, h * 0.25f)
                    lineTo(w * 0.64f, h * 0.22f)
                    lineTo(w * 0.78f, h * 0.22f)
                    lineTo(w * 0.78f, h * 0.36f)
                    close()
                }
                drawPath(path = tip, color = primaryColor)
            }
        }
        AchievementIconType.TROPHY -> {
            Canvas(modifier = modifier) {
                val w = size.width
                val h = size.height
                val primaryColor = if (isUnlocked) Color(0xFFD84315) else Color(0xFF7A7F8F)
                val cupColor = if (isUnlocked) Color(0xFFFFB74D) else Color(0xFFCBD5E0)

                drawRect(
                    color = primaryColor,
                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.32f, h * 0.75f),
                    size = androidx.compose.ui.geometry.Size(w * 0.36f, h * 0.08f)
                )
                drawRect(
                    color = primaryColor,
                    topLeft = androidx.compose.ui.geometry.Offset(w * 0.46f, h * 0.62f),
                    size = androidx.compose.ui.geometry.Size(w * 0.08f, h * 0.13f)
                )

                val cup = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.25f, h * 0.20f)
                    lineTo(w * 0.75f, h * 0.20f)
                    quadraticBezierTo(w * 0.70f, h * 0.62f, w * 0.50f, h * 0.62f)
                    quadraticBezierTo(w * 0.30f, h * 0.62f, w * 0.25f, h * 0.20f)
                    close()
                }
                drawPath(path = cup, color = cupColor)
                drawPath(path = cup, color = primaryColor, style = Stroke(width = 2.dp.toPx()))

                val leftHandle = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.25f, h * 0.28f)
                    quadraticBezierTo(w * 0.12f, h * 0.30f, w * 0.18f, h * 0.46f)
                    quadraticBezierTo(w * 0.23f, h * 0.50f, w * 0.26f, h * 0.44f)
                }
                drawPath(path = leftHandle, color = primaryColor, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))

                val rightHandle = androidx.compose.ui.graphics.Path().apply {
                    moveTo(w * 0.75f, h * 0.28f)
                    quadraticBezierTo(w * 0.88f, h * 0.30f, w * 0.82f, h * 0.46f)
                    quadraticBezierTo(w * 0.77f, h * 0.50f, w * 0.74f, h * 0.44f)
                }
                drawPath(path = rightHandle, color = primaryColor, style = Stroke(width = 1.5.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}

// -----------------------------------------------------------------------------
// 5. HISTORY SECTION
// -----------------------------------------------------------------------------
@Composable
private fun HistorySection(
    history: List<CheckIn>
) {
    var showFullHistory by remember { mutableStateOf(false) }
    val visibleHistory = remember(history) { history.take(3) }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = R.string.progress_history),
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = TextPrimary
            )
            if (history.size > 3) {
                Text(
                    text = stringResource(id = R.string.progress_view_all),
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp,
                    color = PatientGreenLight,
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .clickable { showFullHistory = true }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (visibleHistory.isEmpty()) {
            Text(
                text = "No tienes registros de check-in históricos.",
                fontFamily = LexendFontFamily,
                fontSize = 14.sp,
                color = TextSecondary,
                modifier = Modifier.padding(vertical = 12.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                visibleHistory.forEach { checkIn ->
                    HistoryItemRow(checkIn = checkIn)
                }
            }
        }

        if (showFullHistory) {
            FullHistoryDialog(
                history = history,
                onDismiss = { showFullHistory = false }
            )
        }
    }
}

@Composable
private fun FullHistoryDialog(
    history: List<CheckIn>,
    onDismiss: () -> Unit
) {
    val availableMonths = remember(history) {
        history.mapNotNull { checkIn ->
            runCatching { YearMonth.from(LocalDate.parse(checkIn.date)) }.getOrNull()
        }.distinct().sortedDescending()
    }
    var selectedMonth by remember(availableMonths) { mutableStateOf(availableMonths.firstOrNull()) }
    val filteredHistory = remember(history, selectedMonth) {
        if (selectedMonth == null) {
            history
        } else {
            history.filter { checkIn ->
                runCatching { YearMonth.from(LocalDate.parse(checkIn.date)) == selectedMonth }.getOrDefault(false)
            }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cerrar", fontFamily = LexendFontFamily, color = PatientGreen)
            }
        },
        title = {
            Text(
                text = "Historial completo",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(520.dp)
            ) {
                if (availableMonths.isNotEmpty()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        availableMonths.forEach { month ->
                            val selected = month == selectedMonth
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(50))
                                    .background(if (selected) PatientGreen else Color.White)
                                    .border(
                                        width = 1.dp,
                                        color = if (selected) PatientGreen else BorderSoft,
                                        shape = RoundedCornerShape(50)
                                    )
                                    .clickable { selectedMonth = month }
                                    .padding(horizontal = 14.dp, vertical = 9.dp)
                            ) {
                                Text(
                                    text = "${getMonthNameInSpanish(month.monthValue)} ${month.year}",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 12.sp,
                                    color = if (selected) Color.White else TextPrimary
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (filteredHistory.isEmpty()) {
                        Text(
                            text = "No hay registros para este mes.",
                            fontFamily = LexendFontFamily,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.padding(vertical = 20.dp)
                        )
                    } else {
                        filteredHistory.forEach { checkIn ->
                            HistoryItemRow(checkIn = checkIn)
                        }
                    }
                }
            }
        },
        containerColor = Color.White,
        shape = RoundedCornerShape(28.dp)
    )
}

@Composable
private fun HistoryItemRow(
    checkIn: CheckIn
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(stiffness = Spring.StiffnessMedium),
        label = "row-scale"
    )

    val parsedDate = remember(checkIn.date) {
        runCatching { LocalDate.parse(checkIn.date) }.getOrNull()
    }
    val dayStr = parsedDate?.dayOfMonth?.toString() ?: ""
    val monthStr = parsedDate?.format(DateTimeFormatter.ofPattern("MMM", Locale("es", "ES")))?.uppercase() ?: ""

    val chipColor = WellnessScoreCalculator.getScoreColor(checkIn.score)
    val isLightBackground = chipColor == Color(0xFFECC94B) || chipColor == ScoreGray || chipColor == ScoreGreenLight
    val textColor = if (isLightBackground) Color(0xFF2D3748) else Color.White

    val title = if (checkIn.type == "initial_test") "Evaluación Inicial" else "Check-in Diario"
    val categoryText = if (checkIn.type == "initial_test") "Test completo" else "Seguimiento diario"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale)
            .shadow(
                elevation = 4.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.05f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.05f)
            )
            .border(1.dp, BorderSoft, RoundedCornerShape(20.dp))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = { isPressed = !isPressed }
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Badge (vertical styled dates)
            Column(
                modifier = Modifier
                    .size(width = 54.dp, height = 54.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF4FBF7))
                    .border(1.dp, Color(0xFFE2F3EB), RoundedCornerShape(12.dp)),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = dayStr,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = monthStr.replace(".", ""), // remove dot from spanish abbreviations like "may."
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 10.sp,
                    color = PatientGreen
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = categoryText,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Score Chip Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(chipColor)
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${checkIn.score}%",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = textColor
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Detalles",
                tint = TextSecondary.copy(alpha = 0.6f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// -----------------------------------------------------------------------------
// HELPERS
// -----------------------------------------------------------------------------
private fun getMonthNameInSpanish(month: Int): String {
    return when (month) {
        1 -> "Enero"
        2 -> "Febrero"
        3 -> "Marzo"
        4 -> "Abril"
        5 -> "Mayo"
        6 -> "Junio"
        7 -> "Julio"
        8 -> "Agosto"
        9 -> "Septiembre"
        10 -> "Octubre"
        11 -> "Noviembre"
        else -> "Diciembre"
    }
}
