package com.relaxmind.app.features.auth.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.auth.RelaxMindAuthTextField
import com.relaxmind.app.ui.components.auth.RelaxPrimaryButton
import com.relaxmind.app.ui.themes.BorderSoft
import com.relaxmind.app.ui.themes.CaregiverBlue
import com.relaxmind.app.ui.themes.SoftBlue
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.SOSCoral
import com.relaxmind.app.ui.themes.SoftLavender
import com.relaxmind.app.ui.themes.SoftMint
import com.relaxmind.app.ui.themes.SurfaceWhite
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

@Composable
fun RegisterFormCard(
    name: String,
    onNameChange: (String) -> Unit,
    nameError: String?,
    lastName: String,
    onLastNameChange: (String) -> Unit,
    lastNameError: String?,
    birthDate: String,
    onBirthDateClick: () -> Unit,
    birthDateError: String?,
    email: String,
    onEmailChange: (String) -> Unit,
    emailError: String?,
    password: String,
    onPasswordChange: (String) -> Unit,
    passwordError: String?,
    confirmPassword: String,
    onConfirmPasswordChange: (String) -> Unit,
    confirmPasswordError: String?,
    passwordVisible: Boolean,
    onTogglePasswordVisibility: () -> Unit,
    confirmPasswordVisible: Boolean,
    onToggleConfirmPasswordVisibility: () -> Unit,
    selectedRole: String,
    onRoleSelected: (String) -> Unit,
    termsAccepted: Boolean,
    onTermsAcceptedChange: (Boolean) -> Unit,
    onNavigateToTerms: () -> Unit,
    isFormValid: Boolean,
    isLoading: Boolean,
    onSubmit: () -> Unit,
    onGoogleRegister: () -> Unit,
    onNavigateToLogin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cardShape = RoundedCornerShape(34.dp)
    val accentColor = if (selectedRole == "caregiver") CaregiverBlue else PatientGreen
    val iconColor = accentColor
    val iconBgColor = if (selectedRole == "caregiver") SoftBlue else SoftMint

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
                .padding(horizontal = 22.dp, vertical = 28.dp)
        ) {
            // Nombre
            RelaxMindAuthTextField(
                modifier = Modifier.fillMaxWidth(),
                value = name,
                onValueChange = onNameChange,
                placeholder = "Nombre",
                leadingIcon = RelaxIcons.Person,
                isError = nameError != null,
                errorMessage = nameError,
                contentDescription = "Campo de nombre",
                iconColor = iconColor,
                iconBgColor = iconBgColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Apellidos
            RelaxMindAuthTextField(
                modifier = Modifier.fillMaxWidth(),
                value = lastName,
                onValueChange = onLastNameChange,
                placeholder = "Apellidos",
                leadingIcon = RelaxIcons.Person,
                isError = lastNameError != null,
                errorMessage = lastNameError,
                contentDescription = "Campo de apellidos",
                iconColor = iconColor,
                iconBgColor = iconBgColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Fecha de Nacimiento
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onBirthDateClick)
            ) {
                RelaxMindAuthTextField(
                    modifier = Modifier.fillMaxWidth(),
                    value = birthDate,
                    onValueChange = {}, // read-only directly
                    placeholder = "Fecha de nacimiento",
                    leadingIcon = RelaxIcons.Calendar,
                    isError = birthDateError != null,
                    errorMessage = birthDateError,
                    contentDescription = "Campo de fecha de nacimiento",
                    iconColor = iconColor,
                    iconBgColor = iconBgColor,
                    trailingContent = {
                        IconButton(onClick = onBirthDateClick) {
                            Icon(
                                imageVector = RelaxIcons.Calendar,
                                contentDescription = "Seleccionar fecha",
                                tint = accentColor,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                )
                // Overlay invisible to intercept clicks on the text field
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .clickable(onClick = onBirthDateClick)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Correo Electrónico
            RelaxMindAuthTextField(
                modifier = Modifier.fillMaxWidth(),
                value = email,
                onValueChange = onEmailChange,
                placeholder = "Correo electrónico",
                leadingIcon = RelaxIcons.Email,
                keyboardType = KeyboardType.Email,
                isError = emailError != null,
                errorMessage = emailError,
                contentDescription = "Campo de correo electrónico",
                iconColor = iconColor,
                iconBgColor = iconBgColor
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Contraseña
            RelaxMindAuthTextField(
                modifier = Modifier.fillMaxWidth(),
                value = password,
                onValueChange = onPasswordChange,
                placeholder = "Contraseña",
                leadingIcon = RelaxIcons.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = passwordError != null,
                errorMessage = passwordError,
                contentDescription = "Campo de contraseña",
                iconColor = iconColor,
                iconBgColor = iconBgColor,
                trailingContent = {
                    val iconScale by animateFloatAsState(
                        targetValue = if (passwordVisible) 1.05f else 1f,
                        label = "register-password-scale"
                    )
                    IconButton(
                        onClick = onTogglePasswordVisibility,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(iconScale)
                    ) {
                        Icon(
                            imageVector = if (passwordVisible) RelaxIcons.Eye else RelaxIcons.EyeOff,
                            contentDescription = if (passwordVisible) "Ocultar" else "Mostrar",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )

            // Password strength meter
            PasswordStrengthMeter(
                password = password,
                accentColor = accentColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp, vertical = 6.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Confirmar Contraseña
            RelaxMindAuthTextField(
                modifier = Modifier.fillMaxWidth(),
                value = confirmPassword,
                onValueChange = onConfirmPasswordChange,
                placeholder = "Confirmar contraseña",
                leadingIcon = RelaxIcons.Lock,
                keyboardType = KeyboardType.Password,
                visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                isError = confirmPasswordError != null,
                errorMessage = confirmPasswordError,
                contentDescription = "Campo de confirmar contraseña",
                iconColor = iconColor,
                iconBgColor = iconBgColor,
                trailingContent = {
                    val iconScale by animateFloatAsState(
                        targetValue = if (confirmPasswordVisible) 1.05f else 1f,
                        label = "register-confirm-password-scale"
                    )
                    IconButton(
                        onClick = onToggleConfirmPasswordVisibility,
                        modifier = Modifier
                            .size(48.dp)
                            .scale(iconScale)
                    ) {
                        Icon(
                            imageVector = if (confirmPasswordVisible) RelaxIcons.Eye else RelaxIcons.EyeOff,
                            contentDescription = if (confirmPasswordVisible) "Ocultar" else "Mostrar",
                            tint = TextSecondary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Selector de Rol
            Text(
                text = "¿Cómo te identificarías en RelaxMind?",
                style = MaterialTheme.typography.labelMedium.copy(
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary
                ),
                modifier = Modifier.padding(bottom = 12.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                RoleCard(
                    label = "Paciente",
                    sublabel = "Busco mejorar mi bienestar mental",
                    icon = RelaxIcons.Person,
                    isSelected = selectedRole == "patient",
                    selectedBorderColor = PatientGreen,
                    selectedBgColor = SoftMint,
                    modifier = Modifier.weight(1f),
                    onClick = { onRoleSelected("patient") }
                )
                RoleCard(
                    label = "Cuidador",
                    sublabel = "Acompaño el bienestar de otra persona",
                    icon = RelaxIcons.Groups,
                    isSelected = selectedRole == "caregiver",
                    selectedBorderColor = CaregiverBlue,
                    selectedBgColor = SoftBlue,
                    modifier = Modifier.weight(1f),
                    onClick = { onRoleSelected("caregiver") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Términos y condiciones
            TermsCheckboxRow(
                checked = termsAccepted,
                onCheckedChange = onTermsAcceptedChange,
                onClickTerms = onNavigateToTerms,
                accentColor = accentColor,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Botón de Registro
            RelaxPrimaryButton(
                text = "Registrarme",
                onClick = onSubmit,
                enabled = isFormValid && !isLoading,
                isLoading = isLoading,
                backgroundColor = accentColor
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
                    text = "O regístrate con",
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

            com.relaxmind.app.ui.components.auth.RelaxOutlineButton(
                modifier = Modifier.fillMaxWidth(),
                text = "Continuar con Google",
                onClick = onGoogleRegister
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Link a Login
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(SoftMint.copy(alpha = 0.5f))
                    .clickable(onClick = onNavigateToLogin)
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("¿Ya tienes cuenta? ")
                        withStyle(
                            SpanStyle(
                                color = PatientGreen,
                                fontWeight = FontWeight.Bold,
                                textDecoration = TextDecoration.None
                            )
                        ) {
                            append("Iniciar sesión")
                        }
                    },
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = LexendFontFamily
                    ),
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun RoleCard(
    label: String,
    sublabel: String,
    icon: ImageVector,
    isSelected: Boolean,
    selectedBorderColor: Color,
    selectedBgColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (isSelected) 1.02f else 1f,
        label = "role-card-scale-$label"
    )
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) selectedBorderColor else BorderSoft,
        animationSpec = tween(durationMillis = 250),
        label = "role-border-$label"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) selectedBgColor else SurfaceWhite,
        animationSpec = tween(durationMillis = 250),
        label = "role-bg-$label"
    )

    val unselectedIconBgColor = if (label == "Cuidador") SoftBlue else SoftMint

    Card(
        modifier = modifier
            .scale(scale)
            .height(150.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (isSelected) 2.dp else 1.dp,
            color = borderColor
        ),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) Color.White else unselectedIconBgColor),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        tint = if (isSelected) selectedBorderColor else TextSecondary,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontFamily = LexendFontFamily,
                        fontSize = 11.sp,
                        lineHeight = 13.sp
                    ),
                    color = TextSecondary,
                    maxLines = 3
                )
            }

            // Check circular
            if (isSelected) {
                Box(
                    modifier = Modifier
                        .padding(10.dp)
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(selectedBorderColor)
                        .align(Alignment.TopEnd),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = RelaxIcons.Check,
                        contentDescription = "Seleccionado",
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TermsCheckboxRow(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onClickTerms: () -> Unit,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val checkboxBgColor by animateColorAsState(
        targetValue = if (checked) accentColor else Color.Transparent,
        label = "terms-checkbox-bg"
    )
    val checkboxBorderColor by animateColorAsState(
        targetValue = if (checked) accentColor else BorderSoft,
        label = "terms-checkbox-border"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(SoftMint.copy(alpha = 0.3f))
            .border(1.dp, BorderSoft.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(22.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(checkboxBgColor)
                .border(width = 1.5.dp, color = checkboxBorderColor, shape = RoundedCornerShape(6.dp))
                .clickable { onCheckedChange(!checked) },
            contentAlignment = Alignment.Center
        ) {
            if (checked) {
                Icon(
                    imageVector = RelaxIcons.Check,
                    contentDescription = "Aceptado",
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        Text(
            text = buildAnnotatedString {
                append("Acepto los ")
                withStyle(
                    SpanStyle(
                        color = PatientGreen,
                        fontWeight = FontWeight.Bold,
                        textDecoration = TextDecoration.Underline
                    )
                ) {
                    append("términos y condiciones")
                }
            },
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = LexendFontFamily
            ),
            color = TextPrimary,
            modifier = Modifier.clickable(onClick = onClickTerms)
        )
    }
}

@Composable
private fun PasswordStrengthMeter(
    password: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val score = remember(password) {
        listOf(
            password.length >= 8,
            password.any { it.isDigit() },
            password.any { it.isUpperCase() },
            password.any { it.isLowerCase() },
            password.any { !it.isLetterOrDigit() }
        ).count { it }
    }
    val strengthText = when {
        password.isBlank() -> "Ingresa una contraseña segura"
        score <= 2 -> "Contraseña débil"
        score <= 4 -> "Contraseña media"
        else -> "Contraseña fuerte"
    }
    val strengthColor = when {
        password.isBlank() -> BorderSoft
        score <= 2 -> SOSCoral
        score <= 4 -> Color(0xFFED8936)
        else -> accentColor
    }

    Column(modifier = modifier) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            repeat(5) { index ->
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(50))
                        .background(
                            if (password.isNotBlank() && index < score) strengthColor
                            else BorderSoft.copy(alpha = 0.5f)
                        )
                )
            }
        }
        Text(
            text = strengthText,
            modifier = Modifier.padding(top = 6.dp, start = 4.dp),
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = LexendFontFamily
            ),
            color = strengthColor
        )
    }
}
