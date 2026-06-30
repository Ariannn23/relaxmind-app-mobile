package com.relaxmind.app.features.common

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.LexendTypography
import com.relaxmind.app.ui.themes.getWellnessPalette
import com.relaxmind.app.ui.themes.getWellnessStatusLabel
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

@Composable
fun DailyCheckInResultScreen(
    score: Int,
    onDismiss: () -> Unit,
    onNavigateToProgress: () -> Unit
) {
    WellnessResultScreen(
        score = score,
        title = "¡Check-in completado!",
        subtitle = "Tu nivel de bienestar de hoy",
        ctaText = "Ver mi dashboard",
        onDismiss = onDismiss,
        onNavigateToProgress = onNavigateToProgress,
        isInitialTest = false
    )
}

@Composable
fun InitialTestResultScreen(
    score: Int,
    onDismiss: () -> Unit,
    onNavigateToProgress: () -> Unit
) {
    WellnessResultScreen(
        score = score,
        title = "¡Test completado!",
        subtitle = "Ya tenemos una primera lectura de tu bienestar",
        ctaText = "Ir a mi dashboard",
        onDismiss = onDismiss,
        onNavigateToProgress = onNavigateToProgress,
        isInitialTest = true
    )
}

@Composable
private fun WellnessResultScreen(
    score: Int,
    title: String,
    subtitle: String,
    ctaText: String,
    onDismiss: () -> Unit,
    onNavigateToProgress: () -> Unit,
    isInitialTest: Boolean
) {
    val palette = getWellnessPalette(score)
    val statusLabel = getWellnessStatusLabel(score)

    // Dynamic support messages based on score
    val supportMessage = when {
        score < 40 -> if (isInitialTest) "Este resultado nos ayudará a cuidar de ti paso a paso. Lo importante es que estás aquí." else "Tu bienestar hoy está bajo. Está bien no estar bien siempre. Lo importante es que estás aquí."
        score < 75 -> if (isInitialTest) "Este resultado nos ayudará a personalizar mejor tu experiencia en RelaxMind." else "Hoy te encuentras en un punto intermedio. Sigue escuchándote y cuidando tu ritmo."
        else -> if (isInitialTest) "¡Qué gran comienzo! Este resultado nos ayudará a potenciar tu experiencia en RelaxMind." else "¡Vas muy bien! Hoy tu bienestar muestra una señal positiva. Sigue cultivando lo que te hace bien."
    }

    val bottomMessage = when {
        score < 40 -> "Gracias por tomarte un momento para ti. Hoy podría ayudarte respirar, escribir en tu diario o hablar con Lumi."
        score < 75 -> "Sigue así, cada pequeño paso cuenta."
        else -> "¡Excelente trabajo! Hoy has conectado muy bien contigo."
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = LexendTypography
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Top Section (Illustration & Titles)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.relaxmind.app.R.drawable.resultados),
                        contentDescription = "Resultados",
                        modifier = Modifier.size(200.dp)
                    )                    
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.primary,
                        maxLines = 1,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                }

                // Middle Section (Result Card)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    shape = RoundedCornerShape(32.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress Ring
                        Box(
                            modifier = Modifier.size(120.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator(
                                progress = { score / 100f },
                                modifier = Modifier.fillMaxSize(),
                                color = palette.primary,
                                trackColor = palette.ringTrack,
                                strokeWidth = 12.dp,
                                strokeCap = StrokeCap.Round
                            )
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = score.toString(),
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 48.sp),
                                    fontWeight = FontWeight.ExtraBold,
                                    color = palette.primary
                                )
                                Text(
                                    text = "/ 100",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.Gray
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Support Message Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.softBackground)
                                .padding(16.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .background(palette.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (score < 40) Icons.Filled.Favorite else if (score < 75) Icons.Filled.WbSunny else Icons.Filled.Eco,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                                Text(
                                    text = supportMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }

                // Bottom Section (State & CTA)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Filled.Eco, contentDescription = null, tint = palette.ringTrack, modifier = Modifier.size(16.dp))
                        Text(
                            text = "Estado actual",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = palette.primary
                        )
                        Icon(Icons.Filled.Eco, contentDescription = null, tint = palette.ringTrack, modifier = Modifier.size(16.dp))
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Ribbon Shape Canvas
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(44.dp)
                            .drawBehind {
                                val ribbonColor = palette.primary
                                val w = size.width
                                val h = size.height
                                val indent = 16.dp.toPx()
                                val path = androidx.compose.ui.graphics.Path().apply {
                                    moveTo(0f, 0f)
                                    lineTo(w, 0f)
                                    // Right cutout
                                    lineTo(w - indent, h / 2f)
                                    lineTo(w, h)
                                    lineTo(0f, h)
                                    // Left cutout
                                    lineTo(indent, h / 2f)
                                    close()
                                }
                                drawPath(path, ribbonColor)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = statusLabel,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = bottomMessage,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 24.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        RelaxButton(
                            text = ctaText,
                            onClick = onDismiss,
                            modifier = Modifier.weight(1f),
                            variant = ButtonVariant.OUTLINE,
                            role = AppRole.PATIENT
                        )
                        RelaxButton(
                            text = "Ver progreso",
                            onClick = onNavigateToProgress,
                            modifier = Modifier.weight(1f),
                            variant = ButtonVariant.PRIMARY,
                            role = AppRole.CAREGIVER
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}


