package com.relaxmind.app.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.PatientGreenLight
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

@Composable
fun AchievementUnlockedDialog(
    title: String,
    iconUrl: String?,
    onDismiss: () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    val cardScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.5f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "achievement-dialog-scale"
    )
    val iconScale by animateFloatAsState(
        targetValue = if (visible) 1f else 0.35f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "achievement-icon-scale"
    )
    val composition by rememberLottieComposition(LottieCompositionSpec.JsonString(ConfettiLottieJson))
    val progress by animateLottieCompositionAsState(
        composition = composition,
        iterations = 1,
        restartOnPlay = true
    )

    LaunchedEffect(Unit) {
        visible = true
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.46f)),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp)
                    .scale(cardScale),
                shape = RoundedCornerShape(30.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp)
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    LottieAnimation(
                        composition = composition,
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(132.dp)
                    )

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp, vertical = 30.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .scale(iconScale)
                                .clip(CircleShape)
                                .background(PatientGreenLight.copy(alpha = 0.22f)),
                            contentAlignment = Alignment.Center
                        ) {
                            AsyncImage(
                                model = iconUrl,
                                contentDescription = "Icono de logro",
                                modifier = Modifier
                                    .size(64.dp)
                                    .clip(CircleShape)
                            )
                        }

                        Spacer(modifier = Modifier.height(18.dp))

                        Text(
                            text = "¡Logro desbloqueado!",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary,
                            style = MaterialTheme.typography.titleLarge
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = title,
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 24.sp,
                            color = PatientGreen,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = "Cada pequeño avance cuenta.",
                            fontFamily = LexendFontFamily,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = PatientGreen),
                            shape = RoundedCornerShape(18.dp)
                        ) {
                            Text(
                                text = "¡Genial!",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

private const val ConfettiLottieJson = """
{
  "v":"5.7.4","fr":30,"ip":0,"op":58,"w":400,"h":180,"nm":"confetti","ddd":0,
  "assets":[],
  "layers":[
    {"ddd":0,"ind":1,"ty":4,"nm":"green","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":1,"k":[{"t":0,"s":[0]},{"t":58,"s":[360]}]},"p":{"a":1,"k":[{"t":0,"s":[80,30,0]},{"t":58,"s":[130,170,0]}]},"a":{"a":0,"k":[0,0,0]},"s":{"a":0,"k":[100,100,100]}},"shapes":[{"ty":"gr","it":[{"ty":"el","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[16,16]}},{"ty":"fl","c":{"a":0,"k":[0.058,0.431,0.337,1]},"o":{"a":0,"k":100}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}],"ip":0,"op":58,"st":0,"bm":0},
    {"ddd":0,"ind":2,"ty":4,"nm":"yellow","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":1,"k":[{"t":0,"s":[0]},{"t":58,"s":[-360]}]},"p":{"a":1,"k":[{"t":0,"s":[210,35,0]},{"t":58,"s":[250,160,0]}]},"a":{"a":0,"k":[0,0,0]},"s":{"a":0,"k":[100,100,100]}},"shapes":[{"ty":"gr","it":[{"ty":"rc","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[18,18]},"r":{"a":0,"k":4}},{"ty":"fl","c":{"a":0,"k":[0.925,0.788,0.294,1]},"o":{"a":0,"k":100}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}],"ip":0,"op":58,"st":0,"bm":0},
    {"ddd":0,"ind":3,"ty":4,"nm":"coral","sr":1,"ks":{"o":{"a":0,"k":100},"r":{"a":1,"k":[{"t":0,"s":[0]},{"t":58,"s":[270]}]},"p":{"a":1,"k":[{"t":0,"s":[315,25,0]},{"t":58,"s":[285,175,0]}]},"a":{"a":0,"k":[0,0,0]},"s":{"a":0,"k":[100,100,100]}},"shapes":[{"ty":"gr","it":[{"ty":"rc","p":{"a":0,"k":[0,0]},"s":{"a":0,"k":[14,22]},"r":{"a":0,"k":3}},{"ty":"fl","c":{"a":0,"k":[0.91,0.345,0.165,1]},"o":{"a":0,"k":100}},{"ty":"tr","p":{"a":0,"k":[0,0]},"a":{"a":0,"k":[0,0]},"s":{"a":0,"k":[100,100]},"r":{"a":0,"k":0},"o":{"a":0,"k":100}}]}],"ip":0,"op":58,"st":0,"bm":0}
  ]
}
"""
