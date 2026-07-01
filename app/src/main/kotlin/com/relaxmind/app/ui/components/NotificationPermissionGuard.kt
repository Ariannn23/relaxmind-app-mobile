package com.relaxmind.app.ui.components

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.NotificationsOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.relaxmind.app.ui.themes.CaregiverPurple
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.SOSCoral
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

fun hasNotificationPermission(context: Context): Boolean {
    return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
        ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
}

fun openNotificationSettings(context: Context) {
    val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS).apply {
            putExtra(Settings.EXTRA_APP_PACKAGE, context.packageName)
        }
    } else {
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = Uri.parse("package:${context.packageName}")
        }
    }
    context.startActivity(intent)
}

@Composable
fun rememberNotificationPermissionStatus(): Boolean {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var granted by remember { mutableStateOf(hasNotificationPermission(context)) }

    DisposableEffect(lifecycleOwner, context) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                granted = hasNotificationPermission(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    return granted
}

@Composable
fun rememberNotificationPermissionLauncher(
    onResult: (Boolean) -> Unit
): ManagedActivityResultLauncher<String, Boolean> {
    return rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = onResult
    )
}

fun requestNotificationPermission(
    launcher: ManagedActivityResultLauncher<String, Boolean>,
    onAlreadyGranted: () -> Unit
) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        launcher.launch(Manifest.permission.POST_NOTIFICATIONS)
    } else {
        onAlreadyGranted()
    }
}

@Composable
fun NotificationPermissionDialog(
    role: AppRole,
    title: String = "Activa tus notificaciones",
    message: String = "RelaxMind necesita notificaciones para recordarte tus check-ins, avisarte de eventos y mantener activas las alertas importantes.",
    primaryText: String = "Activar notificaciones",
    secondaryText: String = "Ahora no",
    onPrimaryClick: () -> Unit,
    onDismiss: () -> Unit,
    showSettingsButton: Boolean = true
) {
    val accent = if (role == AppRole.CAREGIVER) CaregiverPurple else PatientGreen

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE8EAF2), RoundedCornerShape(30.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(accent.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsActive,
                    contentDescription = null,
                    tint = accent,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = title,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = message,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Button(
                onClick = onPrimaryClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(18.dp),
                colors = ButtonDefaults.buttonColors(containerColor = accent, contentColor = Color.White)
            ) {
                Text(
                    text = primaryText,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold
                )
            }

            if (showSettingsButton) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
                ) {
                    Text(
                        text = secondaryText,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
fun DisableNotificationsWarningDialog(
    role: AppRole,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val accent = if (role == AppRole.CAREGIVER) CaregiverPurple else PatientGreen

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(30.dp))
                .background(Color.White)
                .border(1.dp, SOSCoral.copy(alpha = 0.22f), RoundedCornerShape(30.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(SOSCoral.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.NotificationsOff,
                    contentDescription = null,
                    tint = SOSCoral,
                    modifier = Modifier.size(34.dp)
                )
            }

            Text(
                text = "Desactivar notificaciones",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )

            Text(
                text = "Si las desactivas, podrías perder recordatorios de check-in, citas y alertas importantes. Te recomendamos mantenerlas activas.",
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, accent.copy(alpha = 0.28f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = accent)
                ) {
                    Text("Mantener", fontFamily = LexendFontFamily, fontWeight = FontWeight.SemiBold)
                }

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = SOSCoral, contentColor = Color.White)
                ) {
                    Text("Desactivar", fontFamily = LexendFontFamily, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}
