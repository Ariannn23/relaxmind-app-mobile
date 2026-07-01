package com.relaxmind.app.features.auth

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.RadarLoadingOverlay
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.BackgroundWhite
import com.relaxmind.app.ui.themes.CaregiverPurple
import com.relaxmind.app.ui.themes.LexendTypography
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

import android.content.Context
import android.content.ContextWrapper

fun Context.findActivity(): FragmentActivity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is FragmentActivity) return context
        context = context.baseContext
    }
    return null
}
@Composable
fun BiometricLockScreen(
    role: AppRole = AppRole.PATIENT,
    onUnlockSuccess: () -> Unit,
    onLogoutClick: () -> Unit
) {
    val context = LocalContext.current
    var biometricError by remember { mutableStateOf<String?>(null) }
    var isAuthenticating by remember { mutableStateOf(false) }
    val isCaregiver = role == AppRole.CAREGIVER
    val accentColor = if (isCaregiver) CaregiverPurple else PatientGreen
    val iconBackground = accentColor.copy(alpha = if (isCaregiver) 0.14f else 0.10f)

    val authenticate = {
        isAuthenticating = true
        biometricError = null
        val activity = context.findActivity()
        if (activity != null) {
            val executor = ContextCompat.getMainExecutor(activity)
            val biometricPrompt = BiometricPrompt(
                activity,
                executor,
                object : BiometricPrompt.AuthenticationCallback() {
                    override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                        super.onAuthenticationError(errorCode, errString)
                        biometricError = "Error de autenticación: $errString"
                        isAuthenticating = false
                    }

                    override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                        super.onAuthenticationSucceeded(result)
                        isAuthenticating = false
                        onUnlockSuccess()
                    }

                    override fun onAuthenticationFailed() {
                        super.onAuthenticationFailed()
                        biometricError = "Huella o rostro no reconocido."
                        isAuthenticating = false
                    }
                }
            )

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle("Desbloquear RelaxMind")
                .setSubtitle("Inicia sesión usando tu biometría")
                .setNegativeButtonText("Cancelar")
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            biometricError = "No se pudo iniciar la autenticación biométrica."
            isAuthenticating = false
        }
    }

    LaunchedEffect(Unit) {
        authenticate()
    }

    Scaffold(containerColor = BackgroundWhite) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            SoftGradientBackground(role = role)

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(30.dp))
                        .background(iconBackground),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = "App Bloqueada",
                        modifier = Modifier.size(50.dp),
                        tint = accentColor
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "RelaxMind",
                    fontFamily = LexendTypography.headlineLarge.fontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Tu espacio seguro está bloqueado",
                    fontFamily = LexendTypography.bodyLarge.fontFamily,
                    fontSize = 16.sp,
                    color = TextSecondary
                )

                Spacer(modifier = Modifier.height(32.dp))

                if (biometricError != null) {
                    Text(
                        text = biometricError!!,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                }

                Button(
                    onClick = authenticate,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.Fingerprint,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Usar Biometría",
                        fontFamily = LexendTypography.labelLarge.fontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(
                    onClick = onLogoutClick
                ) {
                    Text(
                        text = "Cerrar sesión",
                        fontFamily = LexendTypography.labelLarge.fontFamily,
                        color = TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            RadarLoadingOverlay(
                visible = isAuthenticating,
                isCaregiver = isCaregiver,
                text = "Verificando..."
            )
        }
    }
}
