package com.relaxmind.app.features.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.features.auth.components.LoginAlignedContent
import com.relaxmind.app.features.auth.components.LoginFormCard
import com.relaxmind.app.features.auth.components.LoginHeader
import com.relaxmind.app.features.auth.components.LoginHeroIllustration
import com.relaxmind.app.features.auth.components.LoginScreenHorizontalPadding
import com.relaxmind.app.features.auth.components.LoginWelcomeText
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.BackgroundWhite
import com.relaxmind.app.ui.themes.LexendTypography
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import com.relaxmind.app.utils.GoogleAuthHelper
import com.relaxmind.app.utils.ValidationUtils
import com.relaxmind.app.utils.toUserFriendlyMessage
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.rememberRelaxToastState
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToPatientDashboard: () -> Unit,
    onNavigateToCaregiverDashboard: () -> Unit,
    onNavigateToOnboarding: () -> Unit = {},
    onBiometricLogin: () -> Unit = {},
    biometricEnabled: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val toastState = rememberRelaxToastState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var keepSession by remember { mutableStateOf(false) }
    var displayError by remember { mutableStateOf<String?>(null) }
    var showRoleDialog by remember { mutableStateOf(false) }

    val emailError = when {
        email.isEmpty() -> null
        else -> ValidationUtils.validateEmail(email)
    }
    val isFormValid = emailError == null && email.isNotEmpty() && password.isNotEmpty()

    LaunchedEffect(uiState.success, userRole) {
        if (uiState.success) {
            if (uiState.isNewUser) {
                onNavigateToOnboarding()
            } else {
                when (userRole) {
                    "caregiver" -> onNavigateToCaregiverDashboard()
                    "patient" -> onNavigateToPatientDashboard()
                    null -> {
                        // This happens when a new user signs in with Google
                        // We must ask them to pick a role.
                        showRoleDialog = true
                    }
                    else -> {}
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            displayError = msg
            toastState.showError(msg)
            viewModel.clearError()
        }
    }

    LaunchedEffect(email, password) {
        if (displayError != null) displayError = null
    }

    LoginTheme {
        Scaffold(
            containerColor = BackgroundWhite
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                SoftGradientBackground()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = LoginScreenHorizontalPadding),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(12.dp))

                    LoginHeader()

                    Spacer(modifier = Modifier.height(4.dp))

                    LoginAlignedContent {
                        LoginHeroIllustration()

                        Spacer(modifier = Modifier.height(8.dp))

                        LoginWelcomeText()

                        Spacer(modifier = Modifier.height(18.dp))

                        AnimatedVisibility(
                            visible = true,
                            enter = fadeIn(tween(500)) + slideInVertically(
                                initialOffsetY = { it / 4 },
                                animationSpec = tween(500)
                            )
                        ) {
                            LoginFormCard(
                                email = email,
                                onEmailChange = { email = it },
                                password = password,
                                onPasswordChange = { password = it },
                                isPasswordVisible = passwordVisible,
                                onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                                keepSessionActive = keepSession,
                                onKeepSessionChange = { keepSession = it },
                                emailError = emailError,
                                globalError = displayError,
                                isFormValid = isFormValid,
                                isLoading = uiState.isLoading,
                                onLogin = {
                                    if (email.isEmpty() || password.isEmpty()) {
                                        displayError = "Por favor, ingresa tu correo y contraseña"
                                    } else if (emailError != null) {
                                        displayError = emailError
                                    } else {
                                        com.relaxmind.app.utils.SecurityPreferences.setKeepLoggedIn(context, keepSession)
                                        viewModel.login(email, password)
                                    }
                                },
                                onGoogleLogin = {
                                    com.relaxmind.app.utils.SecurityPreferences.setKeepLoggedIn(context, true)
                                    scope.launch {
                                        val result = GoogleAuthHelper.getGoogleIdToken(context)
                                        result.onSuccess { token ->
                                            viewModel.loginWithGoogle(token)
                                        }.onFailure { error ->
                                            displayError = error.toUserFriendlyMessage("Error al iniciar sesión con Google.")
                                        }
                                    }
                                },
                                onForgotPassword = onNavigateToForgotPassword,
                                onCreateAccount = onNavigateToRegister
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
                RelaxToastHost(state = toastState)
            }

            if (showRoleDialog) {
                GoogleRoleSelectionDialog(
                    onDismiss = { showRoleDialog = false },
                    onConfirm = { role ->
                        showRoleDialog = false
                        viewModel.finishGoogleRegistration(role)
                    }
                )
            }
        }
    }
}

/** Local theme wrapper — Lexend typography + login light palette only on this screen. */
@Composable
private fun LoginTheme(content: @Composable () -> Unit) {
    val loginColorScheme = lightColorScheme(
        primary = PatientGreen,
        onPrimary = BackgroundWhite,
        background = BackgroundWhite,
        onBackground = TextPrimary,
        surface = BackgroundWhite,
        onSurface = TextPrimary,
        onSurfaceVariant = TextSecondary
    )

    androidx.compose.material3.MaterialTheme(
        colorScheme = loginColorScheme,
        typography = LexendTypography,
        content = content
    )
}

@Preview(name = "LoginScreen", showBackground = true, showSystemUi = true)
@Composable
private fun LoginScreenPreview() {
    LoginScreen(
        onNavigateToRegister = {},
        onNavigateToForgotPassword = {},
        onNavigateToPatientDashboard = {},
        onNavigateToCaregiverDashboard = {},
        biometricEnabled = true
    )
}

@Composable
fun GoogleRoleSelectionDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var selectedRole by remember { mutableStateOf<String?>(null) }
    val accentColor = when (selectedRole) {
        "caregiver" -> com.relaxmind.app.ui.themes.CaregiverPurple
        else -> com.relaxmind.app.ui.themes.PatientGreen
    }

    androidx.compose.ui.window.Dialog(
        onDismissRequest = onDismiss,
        properties = androidx.compose.ui.window.DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        androidx.compose.material3.Surface(
            shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 32.dp, topEnd = 32.dp, bottomStart = 24.dp, bottomEnd = 24.dp),
            color = BackgroundWhite,
            modifier = androidx.compose.ui.Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = androidx.compose.ui.Modifier.padding(horizontal = 24.dp, vertical = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Handle pill
                androidx.compose.foundation.layout.Box(
                    modifier = androidx.compose.ui.Modifier
                        .size(width = 40.dp, height = 4.dp)
                        .clip(androidx.compose.foundation.shape.RoundedCornerShape(50))
                        .background(com.relaxmind.app.ui.themes.BorderSoft)
                )

                Spacer(modifier = androidx.compose.ui.Modifier.height(20.dp))

                // Google icon + title row
                androidx.compose.foundation.layout.Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = com.relaxmind.app.ui.components.RelaxIcons.Groups,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = androidx.compose.ui.Modifier.size(28.dp)
                    )
                    Spacer(modifier = androidx.compose.ui.Modifier.width(10.dp))
                    Text(
                        text = "Configura tu cuenta",
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontFamily = com.relaxmind.app.ui.themes.LexendFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = androidx.compose.ui.unit.TextUnit(22f, androidx.compose.ui.unit.TextUnitType.Sp)
                        ),
                        color = TextPrimary
                    )
                }

                Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))

                Text(
                    text = "Hemos creado tu cuenta con Google. ¿Cómo quieres usar RelaxMind?",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = com.relaxmind.app.ui.themes.LexendFontFamily
                    ),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))

                // Role selectors — reuse the same RoleCard from RegisterFormCard
                androidx.compose.foundation.layout.Row(
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    com.relaxmind.app.features.auth.components.RoleCard(
                        label = "Paciente",
                        sublabel = "Busco mejorar mi bienestar mental",
                        icon = com.relaxmind.app.ui.components.RelaxIcons.Person,
                        isSelected = selectedRole == "patient",
                        selectedBorderColor = com.relaxmind.app.ui.themes.PatientGreen,
                        selectedBgColor = com.relaxmind.app.ui.themes.SoftMint,
                        modifier = androidx.compose.ui.Modifier.weight(1f),
                        onClick = { selectedRole = "patient" }
                    )
                    com.relaxmind.app.features.auth.components.RoleCard(
                        label = "Cuidador",
                        sublabel = "Acompaño el bienestar de otra persona",
                        icon = com.relaxmind.app.ui.components.RelaxIcons.Groups,
                        isSelected = selectedRole == "caregiver",
                        selectedBorderColor = com.relaxmind.app.ui.themes.CaregiverPurple,
                        selectedBgColor = com.relaxmind.app.ui.themes.SoftLavender,
                        modifier = androidx.compose.ui.Modifier.weight(1f),
                        onClick = { selectedRole = "caregiver" }
                    )
                }

                Spacer(modifier = androidx.compose.ui.Modifier.height(24.dp))

                // Primary CTA — Crear cuenta
                com.relaxmind.app.ui.components.auth.RelaxPrimaryButton(
                    text = "Crear cuenta",
                    onClick = {
                        selectedRole?.let { onConfirm(it) }
                    },
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth(),
                    enabled = selectedRole != null,
                    backgroundColor = accentColor
                )

                Spacer(modifier = androidx.compose.ui.Modifier.height(12.dp))

                // Secondary — cancel
                TextButton(
                    onClick = onDismiss,
                    modifier = androidx.compose.ui.Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "Cancelar",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontFamily = com.relaxmind.app.ui.themes.LexendFontFamily,
                            fontWeight = FontWeight.Medium
                        ),
                        color = TextSecondary
                    )
                }
            }
        }
    }
}
