package com.relaxmind.app.features.auth

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.RelaxToastState
import com.relaxmind.app.ui.components.rememberRelaxToastState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.ui.themes.SOSCoral
import kotlin.math.max

@Composable
fun EmailVerificationScreen(
    viewModel: AuthViewModel = viewModel(),
    email: String = "tu correo",
    autoSendCode: Boolean = true,
    onNavigateBack: () -> Unit,
    onVerified: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val timerSeconds by viewModel.timerSeconds.collectAsState()
    val resendCount by viewModel.resendCount.collectAsState()
    val toastState = rememberRelaxToastState()
    val digits = remember { mutableStateListOf("", "", "", "", "", "") }
    val focusRequesters = remember { List(6) { FocusRequester() } }
    val focusStates = remember { mutableStateListOf(false, false, false, false, false, false) }

    LaunchedEffect(Unit) {
        viewModel.clearSuccess()
        if (autoSendCode) viewModel.sendVerificationCode()
        focusRequesters.first().requestFocus()
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            viewModel.clearSuccess()
            onVerified()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            digits.indices.forEach { index -> digits[index] = "" }
            focusRequesters.first().requestFocus()
            toastState.showError("Código incorrecto. Intenta de nuevo.")
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = { RelaxTopBar(title = "", onBackClick = onNavigateBack) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                EmailHero()
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "Verifica tu correo",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ingresa el código de 6 dígitos enviado a $email",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(34.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    digits.forEachIndexed { index, digit ->
                        OtpBox(
                            value = digit,
                            focused = focusStates[index],
                            requester = focusRequesters[index],
                            onFocusChanged = { focusStates[index] = it },
                            onValueChange = { newValue ->
                                val nextDigit = newValue.filter(Char::isDigit).takeLast(1)
                                if (nextDigit.isNotEmpty()) {
                                    digits[index] = nextDigit
                                    if (index < digits.lastIndex) focusRequesters[index + 1].requestFocus()
                                } else {
                                    digits[index] = ""
                                }
                            },
                            onBackspace = {
                                if (digits[index].isBlank() && index > 0) {
                                    digits[index - 1] = ""
                                    focusRequesters[index - 1].requestFocus()
                                }
                            }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(34.dp))
                TimerText(timerSeconds = timerSeconds)
                Spacer(modifier = Modifier.height(18.dp))
                val resendLimitReached = resendCount >= 5
                val resendEnabled = timerSeconds == 0 && !resendLimitReached && !uiState.isLoading
                TextButton(
                    enabled = resendEnabled,
                    onClick = { viewModel.resendCode() }
                ) {
                    Text(
                        text = if (resendLimitReached) {
                            "Límite de reenvíos alcanzado."
                        } else {
                            "Reenviar código (${max(0, 5 - resendCount)}/5 intentos restantes)"
                        },
                        style = MaterialTheme.typography.labelLarge
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                RelaxButton(
                    text = "Verificar",
                    onClick = { viewModel.verifyCode(digits.joinToString("")) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY,
                    role = AppRole.PATIENT,
                    enabled = digits.all { it.isNotBlank() } && !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState.isLoading) FullScreenLoadingOverlay()
            RelaxToastHost(state = toastState)
        }
    }
}

@Composable
private fun EmailHero() {
    Box(modifier = Modifier.size(176.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(156.dp)
                .clip(CircleShape)
                .background(PatientGreen.copy(alpha = 0.09f))
        )
        Box(
            modifier = Modifier
                .size(112.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(Brush.verticalGradient(listOf(PatientGreen.copy(alpha = 0.92f), PatientGreen))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = RelaxIcons.Email,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(58.dp)
            )
        }
    }
}

@Composable
private fun OtpBox(
    value: String,
    focused: Boolean,
    requester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onValueChange: (String) -> Unit,
    onBackspace: () -> Unit
) {
    val borderColor = if (focused || value.isNotBlank()) PatientGreen else Color(0xFFCBD5E0)
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .size(52.dp)
            .focusRequester(requester)
            .onFocusChanged { onFocusChanged(it.isFocused) }
            .onPreviewKeyEvent {
                if (it.key == Key.Backspace) {
                    onBackspace()
                    false
                } else {
                    false
                }
            }
            .clip(RoundedCornerShape(12.dp))
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.5.dp, borderColor), RoundedCornerShape(12.dp)),
        textStyle = MaterialTheme.typography.headlineMedium.copy(
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center
        ),
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
        singleLine = true,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                innerTextField()
            }
        }
    )
}

@Composable
private fun TimerText(timerSeconds: Int) {
    val minutes = timerSeconds / 60
    val seconds = timerSeconds % 60
    Text(
        text = "El código expira en $minutes:${seconds.toString().padStart(2, '0')}",
        color = if (timerSeconds in 1..29) SOSCoral else MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
        style = MaterialTheme.typography.bodyLarge
    )
}

@Preview(name = "EmailVerificationScreen", showBackground = true, showSystemUi = true)
@Composable
private fun EmailVerificationScreenPreview() {
    RelaxMindTheme(darkTheme = false) {
        EmailVerificationScreen(
            email = "paciente@mail.com",
            autoSendCode = false,
            onNavigateBack = {},
            onVerified = {}
        )
    }
}
