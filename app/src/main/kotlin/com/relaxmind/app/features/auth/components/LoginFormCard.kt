package com.relaxmind.app.features.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.auth.RelaxMindAuthTextField
import com.relaxmind.app.ui.components.auth.RelaxOutlineButton
import com.relaxmind.app.ui.components.auth.RelaxPrimaryButton
import com.relaxmind.app.ui.themes.BorderSoft
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.SOSCoral
import com.relaxmind.app.ui.themes.SurfaceWhite
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

@Composable
fun LoginFormCard(
    email: String,
    onEmailChange: (String) -> Unit,
    password: String,
    onPasswordChange: (String) -> Unit,
    isPasswordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    keepSessionActive: Boolean,
    onKeepSessionChange: (Boolean) -> Unit,
    emailError: String?,
    globalError: String?,
    isFormValid: Boolean,
    isLoading: Boolean,
    onLogin: () -> Unit,
    onGoogleLogin: () -> Unit,
    onForgotPassword: () -> Unit,
    onCreateAccount: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(32.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = cardShape,
        color = SurfaceWhite,
        shadowElevation = 26.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, BorderSoft.copy(alpha = 0.9f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 28.dp)
        ) {
        RelaxMindAuthTextField(
            modifier = Modifier.fillMaxWidth(),
            value = email,
            onValueChange = onEmailChange,
            placeholder = "Correo electrónico",
            leadingIcon = RelaxIcons.Email,
            keyboardType = KeyboardType.Email,
            isError = emailError != null,
            errorMessage = emailError,
            contentDescription = "Campo de correo electrónico"
        )

        Spacer(modifier = Modifier.height(16.dp))

        RelaxMindAuthTextField(
            modifier = Modifier.fillMaxWidth(),
            value = password,
            onValueChange = onPasswordChange,
            placeholder = "Contraseña",
            leadingIcon = RelaxIcons.Lock,
            keyboardType = KeyboardType.Password,
            visualTransformation = if (isPasswordVisible) {
                VisualTransformation.None
            } else {
                PasswordVisualTransformation()
            },
            contentDescription = "Campo de contraseña",
            trailingContent = {
                val iconScale by animateFloatAsState(
                    targetValue = if (isPasswordVisible) 1.05f else 1f,
                    label = "password-eye-scale"
                )
                IconButton(
                    onClick = onTogglePasswordVisibility,
                    modifier = Modifier
                        .size(48.dp)
                        .scale(iconScale)
                ) {
                    Icon(
                        imageVector = if (isPasswordVisible) RelaxIcons.Eye else RelaxIcons.EyeOff,
                        contentDescription = if (isPasswordVisible) {
                            "Ocultar contraseña"
                        } else {
                            "Mostrar contraseña"
                        },
                        tint = TextSecondary,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(18.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        role = Role.Checkbox
                    ) { onKeepSessionChange(!keepSessionActive) },
                verticalAlignment = Alignment.CenterVertically
            ) {
                LoginCheckbox(
                    checked = keepSessionActive,
                    onCheckedChange = onKeepSessionChange
                )
                Text(
                    text = "Mantener sesión iniciada",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimary,
                    modifier = Modifier.padding(start = 10.dp)
                )
            }

            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.labelMedium.copy(
                    textDecoration = TextDecoration.None
                ),
                color = PatientGreen,
                textAlign = TextAlign.End,
                maxLines = 2,
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onForgotPassword)
                    .padding(start = 8.dp, top = 4.dp, bottom = 4.dp)
            )
        }

        if (!globalError.isNullOrBlank()) {
            Spacer(modifier = Modifier.height(12.dp))
            LoginErrorBanner(message = globalError)
        }

        Spacer(modifier = Modifier.height(24.dp))

        RelaxPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Iniciar sesión",
            onClick = onLogin,
            enabled = !isLoading,
            isLoading = isLoading
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            androidx.compose.material3.Divider(
                modifier = Modifier.weight(1f),
                color = BorderSoft
            )
            Text(
                text = "O continuar con",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 12.dp)
            )
            androidx.compose.material3.Divider(
                modifier = Modifier.weight(1f),
                color = BorderSoft
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        RelaxPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Continuar con Google",
            onClick = onGoogleLogin,
            backgroundColor = Color.White,
            textColor = Color.Black,
            iconRes = com.relaxmind.app.R.drawable.google
        )

        Spacer(modifier = Modifier.height(14.dp))

        RelaxPrimaryButton(
            modifier = Modifier.fillMaxWidth(),
            text = "Crear cuenta",
            onClick = onCreateAccount,
            icon = androidx.compose.material.icons.Icons.Default.Email
        )
        }
    }
}

@Composable
private fun LoginCheckbox(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor by animateColorAsState(
        targetValue = if (checked) PatientGreen else Color.Transparent,
        label = "checkbox-bg"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) PatientGreen else BorderSoft,
        label = "checkbox-border"
    )
    val scale by animateFloatAsState(
        targetValue = if (checked) 1.02f else 1f,
        label = "checkbox-scale"
    )

    Box(
        modifier = modifier
            .size(22.dp)
            .scale(scale)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor)
            .border(width = 1.5.dp, color = borderColor, shape = RoundedCornerShape(6.dp))
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.Center
    ) {
        if (checked) {
            Icon(
                imageVector = RelaxIcons.Check,
                contentDescription = "Sesión activa seleccionada",
                tint = Color.White,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun LoginErrorBanner(
    message: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SOSCoral.copy(alpha = 0.08f))
            .border(1.dp, SOSCoral.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Text(
            text = message,
            style = MaterialTheme.typography.bodySmall,
            color = SOSCoral
        )
    }
}
