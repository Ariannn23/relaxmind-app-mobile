package com.relaxmind.app.features.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.R
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*
import com.relaxmind.app.utils.SoundPlayerManager

data class SoundItem(
    val id: String,
    val name: String,
    val resId: Int,
    val bgGradientColors: List<Color>,
    val emoji: String
)

val SoundCatalog = listOf(
    SoundItem("rain", "Lluvia", R.raw.sound_rain, listOf(Color(0xFFE0F2FE), Color(0xFF38BDF8)), "🌧️"),
    SoundItem("ocean", "Olas del Mar", R.raw.sound_ocean_waves, listOf(Color(0xFFE0F2FE), Color(0xFF06B6D4)), "🌊"),
    SoundItem("forest", "Bosque", R.raw.sound_forest, listOf(Color(0xFFDCFCE7), Color(0xFF22C55E)), "🌲"),
    SoundItem("white_noise", "Ruido Blanco", R.raw.sound_white_noise, listOf(Color(0xFFF1F5F9), Color(0xFF94A3B8)), "💨"),
    SoundItem("fireplace", "Chimenea", R.raw.sound_fireplace, listOf(Color(0xFFFFEDD5), Color(0xFFF97316)), "🔥"),
    SoundItem("wind", "Viento", R.raw.sound_wind, listOf(Color(0xFFF3E8FF), Color(0xFFA855F7)), "🍃")
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxSoundsScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val activeSoundIds by SoundPlayerManager.playingSoundIds.collectAsState()

    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        text = "Sonidos Relajantes",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                            contentDescription = "Volver",
                            tint = PatientGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
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
                    .padding(horizontal = 20.dp)
            ) {
                Text(
                    text = "Crea tu ambiente de paz",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                // Grid of Sound Cards
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(SoundCatalog) { item ->
                        val isPlaying = activeSoundIds.contains(item.id)
                        
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            border = if (isPlaying) BorderStroke(2.dp, PatientGreen) else BorderStroke(1.dp, BorderSoft),
                            elevation = CardDefaults.cardElevation(defaultElevation = if (isPlaying) 6.dp else 2.dp),
                            shape = RoundedCornerShape(24.dp),
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clickable {
                                    if (isPlaying) {
                                        SoundPlayerManager.stop(item.id)
                                    } else {
                                        SoundPlayerManager.play(context, item.id, item.resId)
                                    }
                                }
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Brush.verticalGradient(item.bgGradientColors))
                                    .padding(12.dp)
                            ) {
                                // Emoji / Icon representation of the element
                                Text(
                                    text = item.emoji,
                                    fontSize = 42.sp,
                                    modifier = Modifier.align(Alignment.Center)
                                )

                                // Play / Pause overlay indicator
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .align(Alignment.TopEnd)
                                        .background(Color.White.copy(alpha = 0.8f), CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isPlaying) {
                                        // Equalizer bars dummy animation represented by an equalizer emoji or small indicator
                                        Text(text = "⏸️", fontSize = 14.sp)
                                    } else {
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Reproducir",
                                            tint = TextPrimary,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                // Sound title
                                Text(
                                    text = item.name,
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = TextPrimary,
                                    modifier = Modifier
                                        .align(Alignment.BottomCenter)
                                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(12.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Sticky mixing control panel in the bottom
                if (activeSoundIds.isNotEmpty()) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                        shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
                        border = BorderStroke(1.dp, BorderSoft),
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(12.dp, RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Mezclador de Sonido",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 15.sp,
                                    color = TextPrimary
                                )
                                TextButton(
                                    onClick = { SoundPlayerManager.stopAll() },
                                    colors = ButtonDefaults.textButtonColors(contentColor = TextSecondary)
                                ) {
                                    Text(
                                        text = "Detener todo",
                                        fontFamily = LexendFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 13.sp
                                    )
                                }
                            }

                            // Active sound list with volume controls
                            activeSoundIds.forEach { soundId ->
                                val soundItem = SoundCatalog.find { it.id == soundId }
                                if (soundItem != null) {
                                    var volume by remember(soundId) { 
                                        mutableFloatStateOf(SoundPlayerManager.getVolume(soundId)) 
                                    }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "${soundItem.emoji} ${soundItem.name}",
                                            fontFamily = LexendFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            modifier = Modifier.width(90.dp)
                                        )

                                        Slider(
                                            value = volume,
                                            onValueChange = {
                                                volume = it
                                                SoundPlayerManager.setVolume(soundId, it)
                                            },
                                            colors = SliderDefaults.colors(
                                                thumbColor = PatientGreen,
                                                activeTrackColor = PatientGreen,
                                                inactiveTrackColor = BorderSoft
                                            ),
                                            modifier = Modifier.weight(1f)
                                        )

                                        Spacer(modifier = Modifier.width(8.dp))

                                        IconButton(
                                            onClick = { SoundPlayerManager.stop(soundId) },
                                            modifier = Modifier.size(28.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Detener",
                                                tint = TextSecondary,
                                                modifier = Modifier.size(16.dp)
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
}
