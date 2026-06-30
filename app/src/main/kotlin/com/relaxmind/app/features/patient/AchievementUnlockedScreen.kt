package com.relaxmind.app.features.patient

import androidx.compose.foundation.Image

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import nl.dionsegijn.konfetti.compose.KonfettiView
import nl.dionsegijn.konfetti.core.Party
import nl.dionsegijn.konfetti.core.Position
import nl.dionsegijn.konfetti.core.emitter.Emitter
import java.util.concurrent.TimeUnit
import com.relaxmind.app.data.model.UserAchievement
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import com.relaxmind.app.R

private fun getAchievementColor(key: String): Color {
    return when (key) {
        "first_checkin", "streak_3", "streak_7", "streak_14", "streak_30" -> Color(0xFFF9A471) // Naranja
        "first_meditation", "meditations_10" -> Color(0xFF71C9F9) // Azul
        "first_diary", "diary_7" -> Color(0xFF9B51E0) // Morado
        "score_80", "score_100" -> Color(0xFFF9D671) // Amarillo/Dorado
        "lumi_first" -> Color(0xFF68D391) // Verde
        else -> PatientGreen
    }
}

@Composable
fun AchievementUnlockedScreen(
    achievement: UserAchievement,
    onContinue: () -> Unit,
    onNavigateToLibrary: () -> Unit = onContinue
) {
    val achColor = getAchievementColor(achievement.achievementKey)
    val catalogItem = AchievementCatalog.getByKey(achievement.achievementKey)
    val iconResId = catalogItem?.iconResId ?: R.drawable.logro_primeros_pasos

    Dialog(
        onDismissRequest = onContinue,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnBackPress = true,
            dismissOnClickOutside = false
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
        ) {
            SoftGradientBackground(animateBlobs = true)
            
            // Confetti Animation
            KonfettiView(
                modifier = Modifier.fillMaxSize(),
                parties = listOf(
                    Party(
                        speed = 10f,
                        maxSpeed = 50f,
                        damping = 0.9f,
                        spread = 360,
                        colors = listOf(0x68D391, 0xF9D671, 0xF9A471, 0x71C9F9, 0xFF6B6B, 0x9B51E0),
                        emitter = Emitter(duration = 3, TimeUnit.SECONDS).perSecond(200),
                        position = Position.Relative(0.5, 0.2)
                    )
                )
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "¡Felicidades!",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Has desbloqueado un nuevo logro",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(48.dp))
                
                // Hexagon/Icon container
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .shadow(24.dp, shape = RoundedCornerShape(40.dp), spotColor = achColor)
                        .background(Color.White, RoundedCornerShape(40.dp))
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = iconResId),
                            contentDescription = "Achievement Icon",
                            modifier = Modifier.size(140.dp),
                            contentScale = ContentScale.Fit
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(48.dp))
                
                Text(
                    text = "LOGRO DESBLOQUEADO",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    letterSpacing = 1.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = achievement.title,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = achColor,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = achievement.description,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 16.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Info rows
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    AchievementInfoRow("Nuevo logro añadido a tu biblioteca", achColor)
                    AchievementInfoRow("Sigue avanzando en tu progreso", achColor)
                }
                
                Spacer(modifier = Modifier.height(32.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onContinue,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = achColor.copy(alpha = 0.15f)),
                        shape = RoundedCornerShape(100),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp)
                    ) {
                        Text(
                            text = "Continuar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = achColor
                        )
                    }
                    
                    Button(
                        onClick = onNavigateToLibrary,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp)
                            .shadow(16.dp, RoundedCornerShape(100), spotColor = achColor),
                        colors = ButtonDefaults.buttonColors(containerColor = achColor),
                        shape = RoundedCornerShape(100)
                    ) {
                        Text(
                            text = "Ver logros",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = Color.White
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
fun AchievementInfoRow(text: String, achColor: Color) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White, RoundedCornerShape(16.dp))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(achColor.copy(alpha = 0.15f), RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                tint = achColor,
                modifier = Modifier.size(24.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Text(
            text = text,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = TextPrimary,
            modifier = Modifier.weight(1f)
        )
    }
}
