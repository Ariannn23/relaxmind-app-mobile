package com.relaxmind.app.features.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
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
import com.relaxmind.app.ui.components.auth.RelaxMindAuthTextField
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import kotlinx.coroutines.delay

@Composable
fun ForgotPasswordScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var email by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.clearSuccess()
        viewModel.clearError()
        focusRequester.requestFocus()
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.success) {
        if (uiState.success) {
            delay(5000)
            viewModel.clearSuccess()
            onNavigateBack()
        }
    }

    Scaffold(
        topBar = { RelaxTopBar(title = "", onBackClick = onNavigateBack) },
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
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp))
                PasswordHero()
                Spacer(modifier = Modifier.height(30.dp))
                Text(
                    text = "Recuperar contraseña",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "Ingresa tu correo electrónico registrado y te enviaremos un enlace para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f),
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(34.dp))

                RelaxMindAuthTextField(
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    value = email,
                    onValueChange = { 
                        email = it 
                        viewModel.clearError()
                        viewModel.clearSuccess()
                    },
                    placeholder = "Correo electrónico",
                    leadingIcon = RelaxIcons.Email,
                    keyboardType = KeyboardType.Email,
                    isError = uiState.emailError != null,
                    errorMessage = uiState.emailError,
                    contentDescription = "Campo de correo para recuperar contraseña"
                )

                AnimatedVisibility(
                    visible = uiState.success,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = PatientGreen.copy(alpha = 0.1f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Info,
                                contentDescription = "Info",
                                tint = PatientGreen,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "Revisa tu bandeja de entrada o de spam para encontrar el enlace de recuperación.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.8f)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.weight(1f))
                RelaxButton(
                    text = "Enviar enlace",
                    onClick = { viewModel.resetPassword(email.trim()) },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY,
                    role = AppRole.PATIENT,
                    enabled = !uiState.isLoading
                )
                Spacer(modifier = Modifier.height(24.dp))
            }

            if (uiState.isLoading) FullScreenLoadingOverlay()
        }
    }
}

@Composable
private fun PasswordHero() {
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
                imageVector = RelaxIcons.Email, // Using Email icon for sending reset link
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(58.dp)
            )
        }
    }
}

@Preview(name = "ForgotPasswordScreen", showBackground = true, showSystemUi = true)
@Composable
private fun ForgotPasswordScreenPreview() {
    RelaxMindTheme(darkTheme = false) {
        ForgotPasswordScreen(
            onNavigateBack = {}
        )
    }
}
