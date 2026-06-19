package com.relaxmind.app.features.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxInputField
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.utils.ValidationUtils

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToPatientDashboard: () -> Unit,
    onNavigateToCaregiverDashboard: () -> Unit,
    biometricEnabled: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Local form state
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var keepSession by remember { mutableStateOf(false) }

    // Real-time validation
    val emailError = if (email.isNotEmpty()) ValidationUtils.validateEmail(email) else null
    val isFormValid = emailError == null && email.isNotEmpty() && password.isNotEmpty()

    // React to success → navigate by role
    LaunchedEffect(uiState.success, userRole) {
        if (uiState.success) {
            when (userRole) {
                "caregiver" -> onNavigateToCaregiverDashboard()
                else -> onNavigateToPatientDashboard()
            }
        }
    }

    // Show error snackbar
    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .statusBarsPadding()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(40.dp))

                // ── Logo placeholder ─────────────────────────────────────
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape)
                        .background(PatientGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "RM",
                        color = Color.White,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "RelaxMind",
                    style = MaterialTheme.typography.headlineSmall,
                    color = PatientGreen
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Titles ───────────────────────────────────────────────
                Text(
                    text = "Bienvenido de nuevo",
                    style = MaterialTheme.typography.headlineMedium.copy(fontSize = 28.sp),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Ingresa a tu cuenta",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(32.dp))

                // ── Email field ──────────────────────────────────────────
                RelaxInputField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo electrónico",
                    leadingIcon = RelaxIcons.Email,
                    keyboardType = KeyboardType.Email,
                    isError = emailError != null,
                    errorMessage = emailError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ── Password field ───────────────────────────────────────
                RelaxInputField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    leadingIcon = RelaxIcons.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) RelaxIcons.Eye
                                else RelaxIcons.EyeOff,
                                contentDescription = if (passwordVisible) "Ocultar contraseña"
                                else "Mostrar contraseña",
                                tint = PatientGreen
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Keep session switch ──────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    KeepSessionToggle(
                        checked = keepSession,
                        onCheckedChange = { keepSession = it }
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Mantener sesión iniciada",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ── Login button ─────────────────────────────────────────
                RelaxButton(
                    text = "Iniciar sesión",
                    onClick = { viewModel.login(email, password) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY,
                    role = AppRole.PATIENT,
                    enabled = isFormValid && !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // ── Forgot password ──────────────────────────────────────
                TextButton(onClick = onNavigateToForgotPassword) {
                    Text(
                        text = "¿Olvidaste tu contraseña?",
                        color = PatientGreen,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Divider "o" ──────────────────────────────────────────
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HorizontalDivider(modifier = Modifier.weight(1f))
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 8.dp)
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "o",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    HorizontalDivider(modifier = Modifier.weight(1f))
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ── Create account ───────────────────────────────────────
                RelaxButton(
                    text = "Crear cuenta",
                    onClick = onNavigateToRegister,
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.OUTLINE,
                    role = AppRole.PATIENT
                )

                // ── Biometric hint ───────────────────────────────────────
                if (biometricEnabled) {
                    Spacer(modifier = Modifier.height(20.dp))
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(PatientGreen.copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = RelaxIcons.Fingerprint,
                            contentDescription = "Inicio biométrico",
                            tint = PatientGreen,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // ── Loading overlay ──────────────────────────────────────────
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f)),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingIndicator()
                }
            }
        }
    }
}

@Composable
private fun KeepSessionToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(
        targetValue = if (checked) PatientGreen.copy(alpha = 0.16f) else Color(0xFFF0F1F3),
        label = "keep-session-track"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) PatientGreen else Color(0xFFCBD5E0),
        label = "keep-session-border"
    )
    val thumbColor by animateColorAsState(
        targetValue = if (checked) PatientGreen else Color(0xFF7A7480),
        label = "keep-session-thumb"
    )
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 28.dp else 4.dp,
        label = "keep-session-thumb-offset"
    )

    Box(
        modifier = modifier
            .width(58.dp)
            .height(32.dp)
            .clip(RoundedCornerShape(50))
            .background(trackColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(50))
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(24.dp)
                .shadow(
                    elevation = if (checked) 5.dp else 2.dp,
                    shape = CircleShape,
                    ambientColor = PatientGreen.copy(alpha = 0.26f),
                    spotColor = PatientGreen.copy(alpha = 0.22f)
                )
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(name = "LoginScreen", showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    RelaxMindTheme(darkTheme = false) {
        LoginScreen(
            onNavigateToRegister = {},
            onNavigateToForgotPassword = {},
            onNavigateToPatientDashboard = {},
            onNavigateToCaregiverDashboard = {},
            biometricEnabled = true
        )
    }
}
