package com.relaxmind.app.features.patient

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.RepeatOne
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.relaxmind.app.R
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*
import com.relaxmind.app.utils.SoundPlayerManager

data class SoundItem(
    val id: String,
    val name: String,
    val resId: Int,
    val imageResId: Int
)

val SoundCatalog = listOf(
    SoundItem("1", "Viento", R.raw.wind_chimes_sound, R.drawable.wind_chimes),
    SoundItem("2", "Olas", R.raw.waves_sound, R.drawable.waves),
    SoundItem("3", "Cascada", R.raw.waterfall_sound, R.drawable.waterfall),
    SoundItem("4", "Cuenco", R.raw.tibetian_bowl_sound, R.drawable.tibetian_bowl),
    SoundItem("5", "Lluvia", R.raw.rain_sound, R.drawable.rain),
    SoundItem("6", "Piano", R.raw.piano_sound, R.drawable.piano),
    SoundItem("7", "Noche", R.raw.night_sound, R.drawable.night),
    SoundItem("8", "Arpa", R.raw.harp_sound, R.drawable.harpa),
    SoundItem("9", "Bosque", R.raw.forest_sound, R.drawable.forest),
    SoundItem("10", "Fuego", R.raw.fireplace_sound, R.drawable.firaplace)
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
                    verticalArrangement = Arrangement.spacedBy(24.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(SoundCatalog) { item ->
                        val isPlaying = activeSoundIds.contains(item.id)
                        
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier
                                    .aspectRatio(1f)
                                    .shadow(if (isPlaying) 8.dp else 4.dp, RoundedCornerShape(24.dp))
                                    .border(
                                        width = if (isPlaying) 3.dp else 0.dp,
                                        color = if (isPlaying) PatientGreen else Color.Transparent,
                                        shape = RoundedCornerShape(24.dp)
                                    )
                                    .clip(RoundedCornerShape(24.dp))
                                    .clickable(
                                        interactionSource = remember { MutableInteractionSource() },
                                        indication = null
                                    ) {
                                        if (isPlaying) {
                                            SoundPlayerManager.stop(item.id)
                                        } else {
                                            SoundPlayerManager.play(context, item.id, item.resId)
                                        }
                                    }
                            ) {
                                Box(modifier = Modifier.fillMaxSize()) {
                                    // Image background
                                    Image(
                                        painter = painterResource(id = item.imageResId),
                                        contentDescription = item.name,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Play / Pause overlay indicator
                                    Box(
                                        modifier = Modifier
                                            .size(40.dp)
                                            .align(Alignment.TopEnd)
                                            .padding(8.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (isPlaying) {
                                            Icon(
                                                painter = painterResource(id = android.R.drawable.ic_media_pause),
                                                contentDescription = "Pausar",
                                                tint = Color.White,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.PlayArrow,
                                                contentDescription = "Reproducir",
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                        }
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Sound title below the image in a pill shape
                            Box(
                                modifier = Modifier
                                    .background(
                                        color = if (isPlaying) PatientGreen else Color.White,
                                        shape = CircleShape
                                    )
                                    .border(
                                        width = 1.dp,
                                        color = if (isPlaying) PatientGreen else BorderSoft,
                                        shape = CircleShape
                                    )
                                    .padding(horizontal = 16.dp, vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = item.name,
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 14.sp,
                                    color = if (isPlaying) Color.White else TextPrimary
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

                                    var isLooping by remember(soundId) { mutableStateOf(true) }

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = soundItem.name,
                                            fontFamily = LexendFontFamily,
                                            fontWeight = FontWeight.Medium,
                                            fontSize = 13.sp,
                                            color = TextPrimary,
                                            modifier = Modifier.width(70.dp)
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
                                        
                                        Spacer(modifier = Modifier.width(4.dp))
                                        
                                        // Loop button
                                        IconButton(
                                            onClick = {
                                                isLooping = !isLooping
                                                SoundPlayerManager.setLooping(soundId, isLooping)
                                            },
                                            modifier = Modifier.size(36.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isLooping) Icons.Default.RepeatOne else Icons.Default.Repeat,
                                                contentDescription = "Repetir",
                                                tint = if (isLooping) PatientGreen else TextSecondary,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }

                                        Spacer(modifier = Modifier.width(4.dp))

                                        // Stop button
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(CircleShape)
                                                .clickable(
                                                    interactionSource = remember { MutableInteractionSource() },
                                                    indication = null
                                                ) {
                                                    SoundPlayerManager.stop(soundId)
                                                },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Close,
                                                contentDescription = "Detener",
                                                tint = Color(0xFFD32F2F),
                                                modifier = Modifier.size(28.dp)
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
