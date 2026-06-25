package com.relaxmind.app.features.auth

import android.app.DatePickerDialog
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import com.relaxmind.app.utils.GoogleAuthHelper
import com.relaxmind.app.utils.toUserFriendlyMessage
import com.relaxmind.app.features.auth.components.RegisterFormCard
import com.relaxmind.app.features.auth.components.RegisterHeader
import com.relaxmind.app.features.auth.components.RelaxAuthBackButton
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.BackgroundWhite
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.LexendTypography
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import com.relaxmind.app.utils.ValidationUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEmailVerification: () -> Unit,
    onNavigateToPatientDashboard: () -> Unit,
    onNavigateToCaregiverDashboard: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val toastState = rememberRelaxToastState()
    val userRole by viewModel.userRole.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ── Form state ──────────────────────────────────────────────────────────
    var name by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("patient") }
    var termsAccepted by remember { mutableStateOf(false) }
    var phone by remember { mutableStateOf("") }

    // ── Date picker ─────────────────────────────────────────────────────────
    val calendar = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                birthDate = "%02d/%02d/%04d".format(day, month + 1, year)
            },
            calendar.get(Calendar.YEAR) - 18,
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).apply {
            // Restrict max date: user must be at least 13 years old
            datePicker.maxDate = Calendar.getInstance().apply {
                add(Calendar.YEAR, -13)
            }.timeInMillis
        }
    }

    // ── Real-time validations ────────────────────────────────────────────────
    val nameError = if (name.isNotEmpty()) ValidationUtils.validateName(name) else null
    val lastNameError = if (lastName.isNotEmpty()) ValidationUtils.validateLastName(lastName) else null
    val birthDateError = if (birthDate.isNotEmpty()) {
        validateAge(birthDate)
    } else null
    val emailError = if (email.isNotEmpty()) ValidationUtils.validateEmail(email) else null
    val passwordError = if (password.isNotEmpty()) ValidationUtils.validatePassword(password) else null
    val confirmPasswordError = if (confirmPassword.isNotEmpty()) {
        if (password != confirmPassword) "Las contraseñas no coinciden." else null
    } else null
    val phoneError = if (phone.isNotEmpty()) ValidationUtils.validatePhone(phone) else null

    val isFormValid = name.isNotBlank() && lastName.isNotBlank() &&
            birthDate.isNotBlank() && email.isNotBlank() &&
            password.isNotBlank() && confirmPassword.isNotBlank() && phone.isNotBlank() &&
            nameError == null && lastNameError == null && birthDateError == null &&
            emailError == null && passwordError == null && confirmPasswordError == null && phoneError == null &&
            termsAccepted

    // ── Side effects ─────────────────────────────────────────────────────────
    LaunchedEffect(uiState.success, userRole) {
        if (uiState.success) {
            // Si el userRole es nulo, significa que acaba de registrarse con Google y falta asignar el rol
            if (userRole == null) {
                viewModel.finishGoogleRegistration(selectedRole)
            } else {
                // Ya tiene rol, decidir a dónde navegar
                // Si fue un registro manual, el email verification es el siguiente paso. 
                // Pero si fue Google Auth, ya está verificado y vamos al dashboard.
                // Sin embargo, para no complicar, asumo que Google = verificado. Si FirebaseAuthService dice que current user tiene emailVerified, omitimos EmailVerification.
                val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
                if (currentUser?.isEmailVerified == true || currentUser?.providerData?.any { it.providerId == "google.com" } == true) {
                    if (userRole == "caregiver") {
                        onNavigateToCaregiverDashboard()
                    } else {
                        onNavigateToPatientDashboard()
                    }
                } else {
                    onNavigateToEmailVerification()
                }
            }
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            toastState.showError(msg)
            viewModel.clearError()
        }
    }

    RegisterTheme {
        Scaffold(
            containerColor = BackgroundWhite
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Soft gradient background
                SoftGradientBackground()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                        .padding(horizontal = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Spacer(modifier = Modifier.height(16.dp))

                    // Back button aligned left
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        RelaxAuthBackButton(onClick = onNavigateBack)
                    }

                    Spacer(modifier = Modifier.height(18.dp))

                    // Header (Logo, title, subtitle & 3D illustration)
                    RegisterHeader()

                    Spacer(modifier = Modifier.height(20.dp))

                    // Main register form card
                    RegisterFormCard(
                        name = name,
                        onNameChange = { name = it.onlyLettersAndSpaces() },
                        nameError = nameError,
                        lastName = lastName,
                        onLastNameChange = { lastName = it.onlyLettersAndSpaces() },
                        lastNameError = lastNameError,
                        birthDate = birthDate,
                        onBirthDateClick = { datePickerDialog.show() },
                        birthDateError = birthDateError,
                        email = email,
                        onEmailChange = { email = it },
                        emailError = emailError,
                        password = password,
                        onPasswordChange = { password = it },
                        passwordError = passwordError,
                        confirmPassword = confirmPassword,
                        onConfirmPasswordChange = { confirmPassword = it },
                        confirmPasswordError = confirmPasswordError,
                        passwordVisible = passwordVisible,
                        onTogglePasswordVisibility = { passwordVisible = !passwordVisible },
                        confirmPasswordVisible = confirmPasswordVisible,
                        onToggleConfirmPasswordVisibility = { confirmPasswordVisible = !confirmPasswordVisible },
                        phone = phone,
                        onPhoneChange = { phone = it },
                        phoneError = phoneError,
                        selectedRole = selectedRole,
                        onRoleSelected = { selectedRole = it },
                        termsAccepted = termsAccepted,
                        onTermsAcceptedChange = { termsAccepted = it },
                        onNavigateToTerms = { /* TODO: open terms screen or URL if exists */ },
                        isFormValid = isFormValid,
                        isLoading = uiState.isLoading,
                        onSubmit = {
                            viewModel.register(
                                name = name,
                                lastName = lastName,
                                birthDate = birthDate,
                                email = email,
                                password = password,
                                confirmPassword = confirmPassword,
                                role = selectedRole,
                                phone = phone
                            )
                        },
                        onGoogleRegister = {
                            scope.launch {
                                val result = GoogleAuthHelper.getGoogleIdToken(context)
                                result.onSuccess { token ->
                                    viewModel.loginWithGoogle(token)
                                }.onFailure { error ->
                                    toastState.showError(error.toUserFriendlyMessage("Error al registrar con Google."))
                                }
                            }
                        },
                        onNavigateToLogin = onNavigateBack
                    )

                    Spacer(modifier = Modifier.height(32.dp))
                }

                RelaxToastHost(state = toastState)

                // Loading overlay
                if (uiState.isLoading) {
                    FullScreenLoadingOverlay()
                }
            }
        }
    }
}

/** Local theme wrapper — Lexend typography + register light palette only on this screen. */
@Composable
private fun RegisterTheme(content: @Composable () -> Unit) {
    val registerColorScheme = lightColorScheme(
        primary = PatientGreen,
        onPrimary = BackgroundWhite,
        background = BackgroundWhite,
        onBackground = TextPrimary,
        surface = BackgroundWhite,
        onSurface = TextPrimary,
        onSurfaceVariant = TextSecondary
    )

    androidx.compose.material3.MaterialTheme(
        colorScheme = registerColorScheme,
        typography = LexendTypography,
        content = content
    )
}

private fun validateAge(birthDate: String): String? {
    return try {
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val date = LocalDate.parse(birthDate, formatter)
        val minDate = LocalDate.now().minusYears(13)
        if (date.isAfter(minDate)) "Debes tener al menos 13 años." else null
    } catch (_: Exception) {
        "Formato de fecha inválido (dd/MM/yyyy)."
    }
}

private fun String.onlyLettersAndSpaces(): String =
    filter { it.isLetter() || it.isWhitespace() }.replace(Regex("\\s{2,}"), " ")

@Preview(name = "RegisterScreen", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    RegisterScreen(
        onNavigateBack = {},
        onNavigateToEmailVerification = {},
        onNavigateToPatientDashboard = {},
        onNavigateToCaregiverDashboard = {}
    )
}
