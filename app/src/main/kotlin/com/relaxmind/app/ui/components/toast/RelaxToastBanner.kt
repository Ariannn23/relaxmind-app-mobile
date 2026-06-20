package com.relaxmind.app.ui.components.toast

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Emergency
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.Medication
import androidx.compose.material.icons.rounded.Mood
import androidx.compose.material.icons.rounded.NotificationsActive
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material.icons.rounded.WifiOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.relaxmind.app.ui.themes.LexendFontFamily

private val ToastSuccessBg = Color(0xFFBFEFD2)
private val ToastSuccessAccent = Color(0xFF0F6E56)

private val ToastErrorBg = Color(0xFFF7C6CC)
private val ToastErrorAccent = Color(0xFFE8582A)

private val ToastWarningBg = Color(0xFFFFF2C6)
private val ToastWarningAccent = Color(0xFFF59E0B)

private val ToastInfoBg = Color(0xFFD7ECFF)
private val ToastInfoAccent = Color(0xFF1E88E5)

private val ToastSOSBg = Color(0xFFFFD2C7)
private val ToastSOSAccent = Color(0xFFE8582A)

private val ToastNeutralBg = Color(0xFFEDEAF7)
private val ToastNeutralAccent = Color(0xFF7A7F8F)

private val TextPrimary = Color(0xFF1F2430)
private val TextSecondary = Color(0xFF4B5563)
private val SurfaceWhite = Color(0xFFFFFFFF)

@Composable
private fun getToastStyle(type: RelaxToastType): Pair<Color, Color> {
    return when(type) {
        RelaxToastType.Success -> ToastSuccessBg to ToastSuccessAccent
        RelaxToastType.Error -> ToastErrorBg to ToastErrorAccent
        RelaxToastType.Warning -> ToastWarningBg to ToastWarningAccent
        RelaxToastType.Info -> ToastInfoBg to ToastInfoAccent
        RelaxToastType.SOS, RelaxToastType.CaregiverAlert -> ToastSOSBg to ToastSOSAccent
        RelaxToastType.CheckInReminder -> ToastSuccessBg to ToastSuccessAccent
        RelaxToastType.AppointmentReminder -> ToastSuccessBg to ToastSuccessAccent
        RelaxToastType.MedicationReminder -> ToastInfoBg to ToastInfoAccent
        RelaxToastType.Offline -> ToastNeutralBg to ToastNeutralAccent
    }
}

@Composable
private fun getToastIcon(type: RelaxToastType): ImageVector {
    return when(type) {
        RelaxToastType.Success -> Icons.Rounded.CheckCircle
        RelaxToastType.Error -> Icons.Rounded.Error
        RelaxToastType.Warning -> Icons.Rounded.Warning
        RelaxToastType.Info -> Icons.Rounded.Info
        RelaxToastType.SOS -> Icons.Rounded.Emergency
        RelaxToastType.CaregiverAlert -> Icons.Rounded.NotificationsActive
        RelaxToastType.CheckInReminder -> Icons.Rounded.Mood
        RelaxToastType.AppointmentReminder -> Icons.Rounded.CalendarMonth
        RelaxToastType.MedicationReminder -> Icons.Rounded.Medication
        RelaxToastType.Offline -> Icons.Rounded.WifiOff
    }
}

@Composable
fun RelaxToastBanner(
    data: RelaxToastData,
    onDismiss: () -> Unit
) {
    val (bgColor, accentColor) = getToastStyle(data.type)
    val icon = getToastIcon(data.type)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 88.dp),
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Organic decorations (overlapping circles)
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .align(Alignment.TopStart)
                    .padding(start = 12.dp, top = 8.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.05f))
            )
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .align(Alignment.BottomStart)
                    .padding(start = 24.dp, bottom = 4.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.08f))
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Icon inside white circle
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(SurfaceWhite),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = data.type.name,
                        tint = accentColor,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                // Text Column
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = data.title,
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = data.message,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal
                        ),
                        color = TextSecondary
                    )
                    
                    if (data.actionLabel != null && data.onActionClick != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(16.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .clickable { data.onActionClick.invoke() }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = data.actionLabel,
                                style = MaterialTheme.typography.labelMedium.copy(
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.SemiBold
                                ),
                                color = accentColor
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Close Button
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Cerrar",
                    tint = TextSecondary.copy(alpha = 0.5f),
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .clickable { onDismiss() }
                )
            }
        }
    }
}
