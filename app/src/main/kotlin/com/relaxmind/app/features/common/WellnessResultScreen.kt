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
import androidx.compose.ui.text.style.TextOverflow
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
        title = "Check-in completado",
        subtitle = "Tu nivel de bienestar de hoy",
        ctaText = "Inicio",
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
        title = "Test completado",
        subtitle = "Ya tenemos una primera lectura de tu bienestar",
        ctaText = "Inicio",
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
        score < 40 -> if (isInitialTest) "Este resultado nos ayuda a cuidarte paso a paso." else "Tu bienestar esta bajo. Gracias por estar aqui."
        score < 75 -> if (isInitialTest) "Con esto personalizamos mejor tu experiencia." else "Estas en un punto intermedio. Sigue escuchandote."
        else -> if (isInitialTest) "Gran comienzo. Seguiremos acompanandote." else "Vas muy bien. Sigue cultivando lo que te hace bien."
    }

    val bottomMessage = when {
        score < 40 -> "Respirar, escribir o hablar con Lumi podria ayudarte hoy."
        score < 75 -> "Sigue asi, cada pequeno paso cuenta."
        else -> "Excelente trabajo. Hoy conectaste contigo."
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
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Top Section (Illustration & Titles)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Image(
                        painter = androidx.compose.ui.res.painterResource(id = com.relaxmind.app.R.drawable.resultados),
                        contentDescription = "Resultados",
                        modifier = Modifier.size(150.dp)
                    )                    
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = palette.primary,
                        maxLines = 2,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFFEFC)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Progress Ring
                        Box(
                            modifier = Modifier.size(106.dp),
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
                                    style = MaterialTheme.typography.displayLarge.copy(fontSize = 42.sp),
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

                        Spacer(modifier = Modifier.height(12.dp))

                        // Support Message Box
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(palette.softBackground.copy(alpha = 0.55f))
                                .padding(horizontal = 14.dp, vertical = 12.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .background(palette.primary, CircleShape),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (score < 40) Icons.Filled.Favorite else if (score < 75) Icons.Filled.WbSunny else Icons.Filled.Eco,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(21.dp)
                                    )
                                }
                                Text(
                                    text = supportMessage,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray,
                                    lineHeight = 18.sp,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
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
                            variant = ButtonVariant.PRIMARY,
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


