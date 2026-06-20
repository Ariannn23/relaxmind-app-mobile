package com.relaxmind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.ui.themes.ScoreGray
import com.relaxmind.app.ui.themes.ScoreGreenDark
import com.relaxmind.app.ui.themes.ScoreGreenLight
import com.relaxmind.app.ui.themes.ScoreOrange
import com.relaxmind.app.ui.themes.ScoreRed
import com.relaxmind.app.ui.themes.ScoreYellow
import com.relaxmind.app.utils.WellnessScoreCalculator
import java.time.LocalDate

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WellnessCalendarGrid(
    year: Int,
    month: Int,
    checkIns: Map<Int, Int>, // day of month -> score
    modifier: Modifier = Modifier
) {
    val firstDayOfMonth = remember(year, month) { LocalDate.of(year, month, 1) }
    val daysInMonth = remember(year, month) { firstDayOfMonth.lengthOfMonth() }
    val firstDayOfWeek = remember(year, month) { firstDayOfMonth.dayOfWeek.value } // 1 = Monday, ..., 7 = Sunday
    val emptyCellsBefore = firstDayOfWeek - 1

    var selectedDayInfo by remember { mutableStateOf<Pair<Int, Int>?>(null) } // day to score

    Column(modifier = modifier.fillMaxWidth()) {
        // Headers L M X J V S D
        val weekdays = listOf("L", "M", "X", "J", "V", "S", "D")
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            weekdays.forEach { day ->
                Text(
                    text = day,
                    style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.sp),
                    color = Color.Gray,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Total grid items = emptyCells + daysInMonth
        val totalCells = emptyCellsBefore + daysInMonth

        // Render calendar grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp), // Fix height to prevent infinite measurement issues inside scroll views
            verticalArrangement = Arrangement.spacedBy(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(totalCells) { index ->
                if (index < emptyCellsBefore) {
                    // Empty cell
                    Box(modifier = Modifier.size(36.dp))
                } else {
                    val dayNumber = index - emptyCellsBefore + 1
                    val score = checkIns[dayNumber]
                    val cellColor = WellnessScoreCalculator.getScoreColor(score)

                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(cellColor)
                            .clickable(enabled = score != null) {
                                score?.let { selectedDayInfo = Pair(dayNumber, it) }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = dayNumber.toString(),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = if (score != null) FontWeight.Bold else FontWeight.Normal,
                                color = if (score != null && cellColor != ScoreYellow && cellColor != ScoreGray && cellColor != ScoreGreenLight) Color.White else MaterialTheme.colorScheme.onSurface
                            ),
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Leyenda
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            LegendItem(color = ScoreRed, text = "Muy bajo")
            LegendItem(color = ScoreOrange, text = "Bajo")
            LegendItem(color = ScoreYellow, text = "Moderado")
            LegendItem(color = ScoreGreenLight, text = "Bueno")
            LegendItem(color = ScoreGreenDark, text = "Excelente")
        }
    }

    // Detail Pop-up / Dialog
    if (selectedDayInfo != null) {
        val (day, score) = selectedDayInfo!!
        val category = WellnessScoreCalculator.getCategory(score)
        AlertDialog(
            onDismissRequest = { selectedDayInfo = null },
            title = { Text("Registro del día $day") },
            text = {
                Text(
                    text = "Puntaje: $score / 100\nCategoría: $category",
                    style = MaterialTheme.typography.bodyLarge
                )
            },
            confirmButton = {
                TextButton(onClick = { selectedDayInfo = null }) {
                    Text("Cerrar")
                }
            }
        )
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
            color = Color.Gray
        )
    }
}
