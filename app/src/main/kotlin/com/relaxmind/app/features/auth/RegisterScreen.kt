package com.relaxmind.app.features.auth

import android.app.DatePickerDialog
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.LoadingIndicator
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxInputField
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.CaregiverIndigo
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.utils.ValidationUtils
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar

// ---------------------------------------------------------------------------
// Screen
// ---------------------------------------------------------------------------

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToEmailVerification: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

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
    val nameError = if (name.isNotEmpty()) {
        if (name.length < 2) "Mínimo 2 caracteres." else null
    } else null

    val lastNameError = if (lastName.isNotEmpty()) {
        if (lastName.length < 2) "Mínimo 2 caracteres." else null
    } else null

    val birthDateError = if (birthDate.isNotEmpty()) {
        validateAge(birthDate)
    } else null

    val emailError = if (email.isNotEmpty()) ValidationUtils.validateEmail(email) else null

    val passwordError = if (password.isNotEmpty()) {
        when {
            password.length < 8 -> "Mínimo 8 caracteres."
            !password.any { it.isDigit() } -> "Debe contener al menos un número."
            else -> null
        }
    } else null

    val confirmPasswordError = if (confirmPassword.isNotEmpty()) {
        if (password != confirmPassword) "Las contraseñas no coinciden." else null
    } else null

    val isFormValid = name.isNotBlank() && lastName.isNotBlank() &&
            birthDate.isNotBlank() && email.isNotBlank() &&
            password.isNotBlank() && confirmPassword.isNotBlank() &&
            nameError == null && lastNameError == null && birthDateError == null &&
            emailError == null && passwordError == null && confirmPasswordError == null &&
            termsAccepted

    // ── Side effects ─────────────────────────────────────────────────────────
    LaunchedEffect(uiState.success) {
        if (uiState.success) onNavigateToEmailVerification()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = {
            RelaxTopBar(
                title = "Crear cuenta",
                onBackClick = onNavigateBack
            )
        },
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
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {

                // ── Name ─────────────────────────────────────────────────
                RelaxInputField(
                    value = name,
                    onValueChange = { name = it },
                    label = "Nombre",
                    leadingIcon = Icons.Filled.Person,
                    isError = nameError != null,
                    errorMessage = nameError,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Last name ─────────────────────────────────────────────
                RelaxInputField(
                    value = lastName,
                    onValueChange = { lastName = it },
                    label = "Apellidos",
                    leadingIcon = Icons.Filled.Person,
                    isError = lastNameError != null,
                    errorMessage = lastNameError,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Birth date (tap to open DatePicker) ───────────────────
                RelaxInputField(
                    value = birthDate,
                    onValueChange = {},
                    label = "Fecha de nacimiento",
                    leadingIcon = Icons.Filled.CalendarMonth,
                    trailingIcon = {
                        IconButton(onClick = { datePickerDialog.show() }) {
                            Icon(
                                imageVector = Icons.Filled.CalendarMonth,
                                contentDescription = "Seleccionar fecha",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    isError = birthDateError != null,
                    errorMessage = birthDateError,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { datePickerDialog.show() }
                )

                // ── Email ─────────────────────────────────────────────────
                RelaxInputField(
                    value = email,
                    onValueChange = { email = it },
                    label = "Correo electrónico",
                    leadingIcon = Icons.Filled.Email,
                    keyboardType = KeyboardType.Email,
                    isError = emailError != null,
                    errorMessage = emailError,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Password ──────────────────────────────────────────────
                RelaxInputField(
                    value = password,
                    onValueChange = { password = it },
                    label = "Contraseña",
                    leadingIcon = Icons.Filled.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (passwordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    isError = passwordError != null,
                    errorMessage = passwordError,
                    modifier = Modifier.fillMaxWidth()
                )

                // ── Confirm password ──────────────────────────────────────
                RelaxInputField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = "Confirmar contraseña",
                    leadingIcon = Icons.Filled.Lock,
                    keyboardType = KeyboardType.Password,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                    else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                imageVector = if (confirmPasswordVisible) Icons.Filled.Visibility
                                else Icons.Filled.VisibilityOff,
                                contentDescription = if (confirmPasswordVisible) "Ocultar" else "Mostrar",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    },
                    isError = confirmPasswordError != null,
                    errorMessage = confirmPasswordError,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(4.dp))

                // ── Role selector ─────────────────────────────────────────
                Text(
                    text = "¿Cuál es tu rol?",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    RoleCard(
                        label = "Paciente",
                        emoji = "🌿",
                        isSelected = selectedRole == "patient",
                        selectedBorderColor = PatientGreen,
                        selectedBgColor = PatientGreen.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f),
                        onClick = { selectedRole = "patient" }
                    )
                    RoleCard(
                        label = "Cuidador",
                        emoji = "🤝",
                        isSelected = selectedRole == "caregiver",
                        selectedBorderColor = CaregiverIndigo,
                        selectedBgColor = CaregiverIndigo.copy(alpha = 0.08f),
                        modifier = Modifier.weight(1f),
                        onClick = { selectedRole = "caregiver" }
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                // ── Terms checkbox ────────────────────────────────────────
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val checkColor = if (selectedRole == "caregiver") CaregiverIndigo else PatientGreen
                    Checkbox(
                        checked = termsAccepted,
                        onCheckedChange = { termsAccepted = it },
                        colors = CheckboxDefaults.colors(
                            checkedColor = checkColor,
                            uncheckedColor = MaterialTheme.colorScheme.outline
                        )
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    TextButton(onClick = { /* TODO: open terms */ }) {
                        Text(
                            text = buildAnnotatedString {
                                append("Acepto los ")
                                withStyle(
                                    SpanStyle(
                                        color = if (selectedRole == "caregiver") CaregiverIndigo else PatientGreen,
                                        textDecoration = TextDecoration.Underline
                                    )
                                ) {
                                    append("términos y condiciones")
                                }
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ── Register button (color adapts to role) ─────────────────
                RelaxButton(
                    text = "Registrarme",
                    onClick = {
                        viewModel.register(
                            name = name,
                            lastName = lastName,
                            birthDate = birthDate,
                            email = email,
                            password = password,
                            confirmPassword = confirmPassword,
                            role = selectedRole
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY,
                    role = if (selectedRole == "caregiver") AppRole.CAREGIVER else AppRole.PATIENT,
                    enabled = isFormValid && !uiState.isLoading
                )

                Spacer(modifier = Modifier.height(24.dp))
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

// ---------------------------------------------------------------------------
// Role card composable
// ---------------------------------------------------------------------------

@Composable
private fun RoleCard(
    label: String,
    emoji: String,
    isSelected: Boolean,
    selectedBorderColor: Color,
    selectedBgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) selectedBorderColor
        else Color(0xFFCBD5E0),
        animationSpec = tween(durationMillis = 250),
        label = "role-border-$label"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) selectedBgColor else Color.White,
        animationSpec = tween(durationMillis = 250),
        label = "role-bg-$label"
    )

    Card(
        modifier = modifier
            .height(90.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(text = emoji, fontSize = 24.sp)
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = if (isSelected) borderColor
                else MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// ---------------------------------------------------------------------------
// Age validation helper (must be > 13 years)
// ---------------------------------------------------------------------------

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

// ---------------------------------------------------------------------------
// Preview
// ---------------------------------------------------------------------------

@Preview(name = "RegisterScreen", showBackground = true, showSystemUi = true)
@Composable
private fun RegisterScreenPreview() {
    RelaxMindTheme(darkTheme = false) {
        RegisterScreen(
            onNavigateBack = {},
            onNavigateToEmailVerification = {}
        )
    }
}
