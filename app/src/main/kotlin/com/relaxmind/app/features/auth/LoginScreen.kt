package com.relaxmind.app.features.auth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Scaffold
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.RelaxToastState
import com.relaxmind.app.ui.components.rememberRelaxToastState
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.features.auth.components.BiometricHint
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
import com.relaxmind.app.utils.ValidationUtils
import com.relaxmind.app.utils.GoogleAuthHelper
import com.relaxmind.app.utils.toUserFriendlyMessage
@Composable
fun LoginScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit,
    onNavigateToPatientDashboard: () -> Unit,
    onNavigateToCaregiverDashboard: () -> Unit,
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
                                onLogin = { viewModel.login(email, password) },
                                onGoogleLogin = {
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

                    Spacer(modifier = Modifier.height(28.dp))

                    BiometricHint(
                        enabled = biometricEnabled,
                        onClick = onBiometricLogin
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }
                RelaxToastHost(state = toastState)
            }

            if (showRoleDialog) {
                AlertDialog(
                    onDismissRequest = { /* Must select */ },
                    title = { Text("Configuración de Cuenta", color = TextPrimary) },
                    text = { Text("Hemos creado tu cuenta con Google. ¿Deseas usar la aplicación como Paciente o como Cuidador?", color = TextSecondary) },
                    confirmButton = {
                        TextButton(onClick = {
                            showRoleDialog = false
                            viewModel.finishGoogleRegistration("patient")
                        }) {
                            Text("Soy Paciente", color = PatientGreen)
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = {
                            showRoleDialog = false
                            viewModel.finishGoogleRegistration("caregiver")
                        }) {
                            Text("Soy Cuidador", color = PatientGreen)
                        }
                    },
                    containerColor = BackgroundWhite
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
