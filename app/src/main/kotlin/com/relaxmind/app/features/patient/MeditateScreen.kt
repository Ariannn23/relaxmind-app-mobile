package com.relaxmind.app.features.patient

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.Screen
import com.relaxmind.app.data.model.MeditationExercise
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxCard
import com.relaxmind.app.ui.themes.PatientGreen

private data class IconInfo(val emoji: String, val bgColor: Color)

private fun getExerciseIconInfo(title: String, type: String): IconInfo {
    val titleLower = title.lowercase()
    return when {
        titleLower.contains("caja") -> IconInfo("📦", Color(0xFF2B6CB0))
        titleLower.contains("gratitud") -> IconInfo("💖", Color(0xFF319795))
        titleLower.contains("diafragmática") || titleLower.contains("diafragmatica") -> IconInfo("🌿", Color(0xFF38A169))
        type == "respiracion" -> IconInfo("🫁", Color(0xFF0F6E56))
        type == "mindfulness" -> IconInfo("🧘", Color(0xFF805AD5))
        else -> IconInfo("🌿", Color(0xFF38A169)) // Default relaxation
    }
}

@Composable
fun MeditateScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigate: (String) -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val exercises by viewModel.meditationExercises.collectAsState()
    val dailyGoal by viewModel.dailyGoal.collectAsState()
    val dailyGoalExercise by viewModel.dailyGoalExercise.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
        viewModel.loadMeditationExercises()
    }

    Scaffold(
        bottomBar = {
            RelaxBottomNav(
                selectedRoute = "patient/meditate",
                onNavigate = onNavigate,
                role = AppRole.PATIENT
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(Color(0xFFF7FAFC))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp, vertical = 24.dp)
            ) {
                // Header Title
                Text(
                    text = "Meditar",
                    style = MaterialTheme.typography.headlineLarge.copy(fontSize = 26.sp),
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(20.dp))

                if (isLoading && exercises.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = PatientGreen)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Section: Daily Goal if not completed
                        val showDailyGoal = dailyGoalExercise != null && dailyGoal?.completed != true
                        if (showDailyGoal) {
                            item {
                                dailyGoalExercise?.let { exercise ->
                                    RelaxCard(
                                        modifier = Modifier.fillMaxWidth(),
                                        onClick = { onNavigate(Screen.MeditationDetail.createRoute(exercise.id)) },
                                        containerColor = PatientGreen
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(48.dp)
                                                    .clip(CircleShape)
                                                    .background(Color.White.copy(alpha = 0.2f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Filled.Star,
                                                    contentDescription = null,
                                                    tint = Color(0xFFECC94B), // Gold star
                                                    modifier = Modifier.size(24.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(16.dp))
                                            Column {
                                                Text(
                                                    text = "Tu meta de hoy:",
                                                    style = MaterialTheme.typography.bodyMedium.copy(
                                                        color = Color.White.copy(alpha = 0.85f),
                                                        fontWeight = FontWeight.Medium
                                                    )
                                                )
                                                Text(
                                                    text = exercise.title,
                                                    style = MaterialTheme.typography.titleMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    color = Color.White
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Section: Exercises Catalog list
                        items(exercises) { exercise ->
                            val isMetaDeHoy = dailyGoalExercise?.id == exercise.id
                            val iconInfo = getExerciseIconInfo(exercise.title, exercise.type)
                            val categoryLabel = when (exercise.type) {
                                "respiracion" -> "Respiración"
                                "mindfulness" -> "Mindfulness"
                                else -> "Relajación"
                            }

                            RelaxCard(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onNavigate(Screen.MeditationDetail.createRoute(exercise.id)) }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        modifier = Modifier.weight(1f)
                                    ) {
                                        // Left Icon Box
                                        Box(
                                            modifier = Modifier
                                                .size(48.dp)
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(iconInfo.bgColor),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = iconInfo.emoji,
                                                fontSize = 22.sp
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column {
                                            Text(
                                                text = exercise.title,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.SemiBold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = "$categoryLabel  •  ${exercise.durationMinutes} min",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = Color.Gray
                                            )
                                        }
                                    }
                                    
                                    // Right Meta De Hoy Badge or Arrow
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (isMetaDeHoy && dailyGoal?.completed != true) {
                                            Box(
                                                modifier = Modifier
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(Color(0xFFE8F5F0))
                                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                            ) {
                                                Text(
                                                    text = "META DE HOY",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = PatientGreen,
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 9.sp
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                        }
                                        Icon(
                                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                            contentDescription = null,
                                            tint = Color.Gray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
