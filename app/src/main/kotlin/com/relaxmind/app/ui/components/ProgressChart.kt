package com.relaxmind.app.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.data.model.CheckIn
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import com.relaxmind.app.ui.components.RelaxCard
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

enum class ChartFilter {
    WEEKLY, MONTHLY
}

@Composable
fun ProgressChart(
    checkIns: List<CheckIn>,
    modifier: Modifier = Modifier
) {
    var selectedFilter by remember { mutableStateOf(ChartFilter.WEEKLY) }

    // Parse checkIns and map them by date
    val checkInMap = remember(checkIns) {
        checkIns.mapNotNull { checkIn ->
            runCatching {
                LocalDate.parse(checkIn.date) to checkIn.score
            }.getOrNull()
        }.toMap()
    }

    // Determine the date range
    val today = LocalDate.now()
    val daysToShow = if (selectedFilter == ChartFilter.WEEKLY) 7 else 30
    
    val chartData = remember(selectedFilter, checkInMap) {
        val data = mutableListOf<Pair<String, Int?>>()
        for (i in (daysToShow - 1) downTo 0) {
            val date = today.minusDays(i.toLong())
            val score = checkInMap[date]
            val label = if (selectedFilter == ChartFilter.WEEKLY) {
                // Return day of week: "Lun", "Mar", etc.
                when (date.dayOfWeek.value) {
                    1 -> "Lun"
                    2 -> "Mar"
                    3 -> "Mié"
                    4 -> "Jue"
                    5 -> "Vie"
                    6 -> "Sáb"
                    7 -> "Dom"
                    else -> ""
                }
            } else {
                // Every 5 days show label, otherwise empty to save space
                if (date.dayOfMonth % 5 == 0 || date == today) date.dayOfMonth.toString() else ""
            }
            data.add(label to score)
        }
        data
    }

    RelaxCard(modifier = modifier) {
        // Toggle Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Tendencia de Bienestar",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 17.sp,
                color = TextPrimary
            )

            Row(
                modifier = Modifier
                    .background(Color(0xFFF3F4F6), RoundedCornerShape(20.dp))
                    .padding(4.dp)
            ) {
                ChartToggleButton(
                    text = "Semana",
                    selected = selectedFilter == ChartFilter.WEEKLY,
                    onClick = { selectedFilter = ChartFilter.WEEKLY }
                )
                ChartToggleButton(
                    text = "Mes",
                    selected = selectedFilter == ChartFilter.MONTHLY,
                    onClick = { selectedFilter = ChartFilter.MONTHLY }
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Bar Chart Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp),
            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            chartData.forEachIndexed { index, (label, score) ->
                val targetHeight = if (score != null) (score / 100f).coerceIn(0.05f, 1f) else 0f
                val animatedHeight by animateFloatAsState(
                    targetValue = targetHeight,
                    animationSpec = tween(durationMillis = 800, delayMillis = index * 20, easing = FastOutSlowInEasing),
                    label = "barHeight"
                )

                // Bar color logic
                val barColor = when {
                    score == null -> Color.Transparent
                    score >= 70 -> Color(0xFF22C55E) // Green
                    score >= 40 -> Color(0xFFF59E0B) // Orange
                    else -> Color(0xFFE8582A) // Red
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Bottom,
                    modifier = Modifier.weight(1f).fillMaxHeight()
                ) {
                    // The Bar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(if (selectedFilter == ChartFilter.WEEKLY) 0.6f else 0.8f)
                            .fillMaxHeight(animatedHeight.coerceAtLeast(0.02f))
                            .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                            .background(if (score != null) barColor else Color.Transparent)
                    )

                    // Spacer for label
                    Spacer(modifier = Modifier.height(8.dp))

                    // Label
                    Text(
                        text = label,
                        fontFamily = LexendFontFamily,
                        fontSize = if (selectedFilter == ChartFilter.WEEKLY) 12.sp else 10.sp,
                        color = TextSecondary,
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        modifier = Modifier.height(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ChartToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(if (selected) Color.White else Color.Transparent)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            fontFamily = LexendFontFamily,
            fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
            fontSize = 12.sp,
            color = if (selected) TextPrimary else TextSecondary
        )
    }
}
