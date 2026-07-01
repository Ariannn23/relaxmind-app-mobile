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
import com.relaxmind.app.ui.themes.PatientGreen
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
    modifier: Modifier = Modifier,
    accentColor: Color = PatientGreen
) {
    var selectedFilter by remember { mutableStateOf(ChartFilter.WEEKLY) }
    var selectedBarIndex by remember(selectedFilter) { mutableIntStateOf(-1) }

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
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                    .fillMaxWidth()
                    .background(accentColor.copy(alpha = 0.08f), RoundedCornerShape(20.dp))
                    .padding(4.dp)
            ) {
                ChartToggleButton(
                    text = "Semana",
                    selected = selectedFilter == ChartFilter.WEEKLY,
                    onClick = { selectedFilter = ChartFilter.WEEKLY },
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
                ChartToggleButton(
                    text = "Mes",
                    selected = selectedFilter == ChartFilter.MONTHLY,
                    onClick = { selectedFilter = ChartFilter.MONTHLY },
                    accentColor = accentColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        val selectedScore = chartData.getOrNull(selectedBarIndex)?.second
        val selectedLabel = chartData.getOrNull(selectedBarIndex)?.first.orEmpty()

        if (selectedScore != null) {
            Text(
                text = "$selectedLabel · $selectedScore/100",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 12.sp,
                color = accentColor,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(212.dp)
                .clip(RoundedCornerShape(18.dp))
                .background(Color(0xFFFBFAFF))
                .padding(start = 6.dp, top = 12.dp, end = 10.dp, bottom = 10.dp)
        ) {
            Row(modifier = Modifier.fillMaxSize()) {
                Column(
                    modifier = Modifier
                        .width(28.dp)
                        .height(150.dp),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.End
                ) {
                    listOf(100, 75, 50, 25, 0).forEach { value ->
                        Text(
                            text = value.toString(),
                            fontFamily = LexendFontFamily,
                            fontSize = 9.sp,
                            color = TextSecondary.copy(alpha = 0.72f),
                            maxLines = 1
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.SpaceBetween
                        ) {
                            repeat(5) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(Color(0xFFE8E3F8).copy(alpha = 0.62f))
                                )
                            }
                        }

                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.Bottom,
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            chartData.forEachIndexed { index, (_, score) ->
                                val targetHeight = if (score != null) (score / 100f).coerceIn(0.04f, 1f) else 0f
                                val animatedHeight by animateFloatAsState(
                                    targetValue = targetHeight,
                                    animationSpec = tween(durationMillis = 800, delayMillis = index * 20, easing = FastOutSlowInEasing),
                                    label = "barHeight"
                                )

                                val barColor = when {
                                    score == null -> Color.Transparent
                                    score >= 70 -> Color(0xFF22C55E)
                                    score >= 40 -> Color(0xFFF59E0B)
                                    else -> Color(0xFFE8582A)
                                }
                                val isSelected = selectedBarIndex == index

                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                        .clickable(
                                            interactionSource = remember { MutableInteractionSource() },
                                            indication = null,
                                            enabled = score != null,
                                            onClick = { selectedBarIndex = index }
                                        ),
                                    contentAlignment = Alignment.BottomCenter
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth(if (selectedFilter == ChartFilter.WEEKLY) 0.48f else 0.58f)
                                            .fillMaxHeight(animatedHeight)
                                            .clip(RoundedCornerShape(topStart = 7.dp, topEnd = 7.dp))
                                            .background(
                                                if (score != null) {
                                                    if (isSelected) accentColor else barColor
                                                } else {
                                                    Color.Transparent
                                                }
                                            )
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        chartData.forEach { (label, _) ->
                            Text(
                                text = label,
                                fontFamily = LexendFontFamily,
                                fontSize = if (selectedFilter == ChartFilter.WEEKLY) 11.sp else 9.sp,
                                color = TextSecondary,
                                textAlign = TextAlign.Center,
                                maxLines = 1,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ChartToggleButton(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
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
            color = if (selected) accentColor else TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
