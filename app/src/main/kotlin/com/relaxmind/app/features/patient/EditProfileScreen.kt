package com.relaxmind.app.features.patient

import android.app.DatePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.FullScreenLoadingScreen
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.RelaxToastState
import com.relaxmind.app.ui.components.rememberRelaxToastState
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*
import java.util.Calendar


// ── Avatar color data (same palette as AvatarSetupScreen) ────────────────────
private data class AvatarColorOption(val url: String, val colors: List<Color>)
private val avatarColorOptions = listOf(
    AvatarColorOption("relaxmind://avatar/01", listOf(Color(0xFFA7F3D0), Color(0xFF0F6E56))),
    AvatarColorOption("relaxmind://avatar/02", listOf(Color(0xFFFFD6A5), Color(0xFFED8936))),
    AvatarColorOption("relaxmind://avatar/03", listOf(Color(0xFFD8B4FE), Color(0xFF7C3AED))),
    AvatarColorOption("relaxmind://avatar/04", listOf(Color(0xFFA5F3FC), Color(0xFF0891B2))),
    AvatarColorOption("relaxmind://avatar/05", listOf(Color(0xFFFBCFE8), Color(0xFFDB2777))),
    AvatarColorOption("relaxmind://avatar/06", listOf(Color(0xFFBFDBFE), Color(0xFF2563EB))),
    AvatarColorOption("relaxmind://avatar/07", listOf(Color(0xFFFEF3C7), Color(0xFFEAB308))),
    AvatarColorOption("relaxmind://avatar/08", listOf(Color(0xFFFECACA), Color(0xFFEF4444))),
    AvatarColorOption("relaxmind://avatar/09", listOf(Color(0xFFCCFBF1), Color(0xFF14B8A6))),
    AvatarColorOption("relaxmind://avatar/10", listOf(Color(0xFFFED7AA), Color(0xFFEA580C))),
    AvatarColorOption("relaxmind://avatar/11", listOf(Color(0xFFE9D5FF), Color(0xFFA855F7))),
    AvatarColorOption("relaxmind://avatar/12", listOf(Color(0xFFFDE68A), Color(0xFFB45309)))
)

private fun getAvatarColorsForEdit(url: String): List<Color> {
    return avatarColorOptions.find { it.url == url }?.colors
        ?: listOf(Color(0xFFA7F3D0), Color(0xFF0F6E56))
}

private val sexOptions = listOf("Masculino", "Femenino", "No binario", "Prefiero no decirlo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateBack: () -> Unit
) {
    val patient by viewModel.patient.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val context = LocalContext.current

    // ── Form state (pre-filled from patient) ─────────────────────────────────
    var name by remember(patient) { mutableStateOf(patient?.name ?: "") }
    var lastName by remember(patient) { mutableStateOf(patient?.lastName ?: "") }
    var birthDate by remember(patient) { mutableStateOf(patient?.birthDate ?: "") }
    var sex by remember(patient) { mutableStateOf(patient?.sex ?: "") }
    var phone by remember(patient) { mutableStateOf(patient?.phone ?: "") }
    var condition by remember(patient) { mutableStateOf(patient?.condition ?: "") }
    var selectedAvatarUrl by remember(patient) { mutableStateOf(patient?.avatarUrl ?: "relaxmind://avatar/01") }

    var sexExpanded by remember { mutableStateOf(false) }

    val toastState = rememberRelaxToastState()

    // Real-time validation
    val nameError = if (name.isNotEmpty() && name.trim().length < 2) "El nombre debe tener al menos 2 caracteres." else null
    val lastNameError = if (lastName.isNotEmpty() && lastName.trim().length < 2) "El apellido debe tener al menos 2 caracteres." else null
    val phoneError = if (phone.isNotEmpty() && (phone.length < 9 || !phone.all { it.isDigit() })) "El teléfono debe tener 9 dígitos numéricos." else null
    val isFormValid = name.isNotBlank() && lastName.isNotBlank() && phone.isNotBlank() &&
            nameError == null && lastNameError == null && phoneError == null

    // Date picker
    val cal = Calendar.getInstance()
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _, year, month, day ->
                birthDate = "%02d/%02d/%04d".format(day, month + 1, year)
            },
            cal.get(Calendar.YEAR) - 25,
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).apply {
            datePicker.maxDate = Calendar.getInstance().apply { add(Calendar.YEAR, -13) }.timeInMillis
        }
    }

    LaunchedEffect(patient) {
        if (patient == null) viewModel.loadDashboardData()
    }

    // Full-screen loading during save
    if (isLoading && patient == null) {
        FullScreenLoadingScreen(text = "Cargando perfil...")
        return
    }

    MaterialTheme(colorScheme = MaterialTheme.colorScheme, typography = LexendTypography) {
        Scaffold(
            containerColor = Color.Transparent
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                SoftGradientBackground(animateBlobs = false)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .statusBarsPadding()
                        .navigationBarsPadding()
                        .imePadding()
                ) {
                    // ── Header bar ────────────────────────────────────────────
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .shadow(4.dp, CircleShape, ambientColor = Color(0x18000000))
                                .clip(CircleShape)
                                .background(Color.White)
                                .clickable(
                                    onClick = onNavigateBack,
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Volver",
                                tint = PatientGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Text(
                            text = "Editar Perfil",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 20.sp,
                            color = TextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // ── Avatar picker card ─────────────────────────────────────
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        shadowElevation = 10.dp,
                        tonalElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Tu avatar",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            // Large avatar preview
                            val previewIsCustom = selectedAvatarUrl.startsWith("relaxmind://avatar/")
                            Box(
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(CircleShape)
                                    .border(3.dp, PatientGreen.copy(alpha = 0.4f), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                if (previewIsCustom) {
                                    val previewColors = getAvatarColorsForEdit(selectedAvatarUrl)
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Brush.linearGradient(previewColors))
                                    )
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFFD4F3E5))
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Text(
                                text = "Elige un color",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = TextSecondary
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            // Avatar color grid (2 rows of 6)
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                for (rowIdx in 0..1) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        for (colIdx in 0..5) {
                                            val option = avatarColorOptions[rowIdx * 6 + colIdx]
                                            val isSelected = selectedAvatarUrl == option.url
                                            val scale by animateFloatAsState(
                                                targetValue = if (isSelected) 1.1f else 1f,
                                                label = "avatar-scale-${option.url}"
                                            )
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .scale(scale)
                                                    .border(
                                                        width = if (isSelected) 2.5.dp else 0.dp,
                                                        color = if (isSelected) PatientGreen else Color.Transparent,
                                                        shape = CircleShape
                                                    )
                                                    .padding(if (isSelected) 3.dp else 0.dp)
                                                    .clip(CircleShape)
                                                    .background(Brush.linearGradient(option.colors))
                                                    .clickable(
                                                        indication = null,
                                                        interactionSource = remember { MutableInteractionSource() }
                                                    ) { selectedAvatarUrl = option.url }
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // ── Profile fields card ────────────────────────────────────
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(28.dp),
                        color = Color.White,
                        shadowElevation = 10.dp,
                        tonalElevation = 0.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(horizontal = 22.dp, vertical = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Text(
                                text = "Información Personal",
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 15.sp,
                                color = TextPrimary
                            )

                            // Nombre
                            ProfileTextField(
                                value = name,
                                onValueChange = { if (it.all { c -> c.isLetter() || c.isWhitespace() }) name = it },
                                label = "Nombre",
                                leadingIcon = { EditProfileIcon(RelaxIcons.Person) },
                                isError = nameError != null,
                                errorMessage = nameError
                            )

                            // Apellidos
                            ProfileTextField(
                                value = lastName,
                                onValueChange = { if (it.all { c -> c.isLetter() || c.isWhitespace() }) lastName = it },
                                label = "Apellidos",
                                leadingIcon = { EditProfileIcon(RelaxIcons.Person) },
                                isError = lastNameError != null,
                                errorMessage = lastNameError
                            )

                            // Fecha de nacimiento (tappable)
                            Box(modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() }) {
                                ProfileTextField(
                                    value = birthDate,
                                    onValueChange = {},
                                    label = "Fecha de nacimiento",
                                    leadingIcon = { EditProfileIcon(RelaxIcons.Calendar) },
                                    isError = false,
                                    errorMessage = null,
                                    enabled = false,
                                    trailingContent = {
                                        Icon(
                                            imageVector = RelaxIcons.Calendar,
                                            contentDescription = null,
                                            tint = PatientGreen,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                )
                                Box(modifier = Modifier.matchParentSize().clickable { datePickerDialog.show() })
                            }

                            // Sexo (ExposedDropdownMenu)
                            ExposedDropdownMenuBox(
                                expanded = sexExpanded,
                                onExpandedChange = { sexExpanded = it }
                            ) {
                                ProfileTextField(
                                    value = sex,
                                    onValueChange = {},
                                    label = "Sexo",
                                    leadingIcon = {
                                        Box(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(MintPill),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = null,
                                                tint = PatientGreen,
                                                modifier = Modifier.size(18.dp)
                                            )
                                        }
                                    },
                                    isError = false,
                                    errorMessage = null,
                                    enabled = false,
                                    trailingContent = {
                                        ExposedDropdownMenuDefaults.TrailingIcon(expanded = sexExpanded)
                                    },
                                    modifier = Modifier.menuAnchor()
                                )
                                ExposedDropdownMenu(
                                    expanded = sexExpanded,
                                    onDismissRequest = { sexExpanded = false }
                                ) {
                                    sexOptions.forEach { option ->
                                        DropdownMenuItem(
                                            text = {
                                                Text(
                                                    text = option,
                                                    fontFamily = LexendFontFamily,
                                                    fontSize = 14.sp,
                                                    color = TextPrimary
                                                )
                                            },
                                            onClick = {
                                                sex = option
                                                sexExpanded = false
                                            }
                                        )
                                    }
                                }
                            }

                            // Teléfono (numeric, max 9 digits)
                            ProfileTextField(
                                value = phone,
                                onValueChange = { if (it.length <= 9 && it.all { c -> c.isDigit() }) phone = it },
                                label = "Teléfono (9 dígitos)",
                                leadingIcon = { EditProfileIcon(RelaxIcons.Phone) },
                                isError = phoneError != null,
                                errorMessage = phoneError,
                                keyboardType = KeyboardType.Number
                            )

                            // Condición / notas de salud (multiline)
                            Column {
                                Text(
                                    text = "Condición o notas de salud",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 13.sp,
                                    color = TextSecondary,
                                    modifier = Modifier.padding(bottom = 6.dp)
                                )
                                BasicProfileTextField(
                                    value = condition,
                                    onValueChange = { condition = it },
                                    placeholder = "Ej. Ansiedad leve, hipertensión...",
                                    minLines = 3
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // ── Save button ────────────────────────────────────────────
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                    ) {
                        Button(
                            onClick = {
                                viewModel.updateProfile(
                                    name = name.trim(),
                                    lastName = lastName.trim(),
                                    birthDate = birthDate,
                                    sex = sex,
                                    phone = phone,
                                    condition = condition.trim(),
                                    onSuccess = { toastState.showSuccess("Perfil guardado correctamente") },
                                    onError = { toastState.showError(it) }
                                )
                            },
                            enabled = isFormValid && !isLoading,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(54.dp),
                            shape = RoundedCornerShape(18.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = PatientGreen,
                                disabledContainerColor = PatientGreen.copy(alpha = 0.38f)
                            )
                        ) {
                            if (isLoading) {
                                CircularProgressIndicator(
                                    color = Color.White,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(22.dp)
                                )
                            } else {
                                Text(
                                    text = "Guardar cambios",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }

                RelaxToastHost(state = toastState)
            }
        }
    }
}

// ── Reusable sub-composables ──────────────────────────────────────────────────

@Composable
private fun EditProfileIcon(icon: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MintPill),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = PatientGreen,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
private fun ProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    leadingIcon: @Composable () -> Unit,
    isError: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardType: KeyboardType = KeyboardType.Text,
    trailingContent: @Composable (() -> Unit)? = null
) {
    val shape = RoundedCornerShape(18.dp)
    val borderColor = when {
        isError -> SOSCoral
        else -> BorderSoft
    }

    Column {
        Text(
            text = label,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Medium,
            fontSize = 12.sp,
            color = TextSecondary,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Surface(
            modifier = modifier
                .fillMaxWidth()
                .border(1.5.dp, borderColor, shape),
            shape = shape,
            color = if (enabled) SurfaceWhite else Color(0xFFF8FAFB),
            tonalElevation = 0.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(horizontal = 14.dp)
                    .height(54.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leadingIcon()
                Spacer(modifier = Modifier.width(12.dp))
                if (value.isBlank() && !enabled) {
                    Text(
                        text = label,
                        fontFamily = LexendFontFamily,
                        fontSize = 14.sp,
                        color = TextSecondary.copy(alpha = 0.6f),
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    BasicTextField(
                        value = value,
                        onValueChange = onValueChange,
                        modifier = Modifier.weight(1f),
                        textStyle = TextStyle(
                            fontFamily = LexendFontFamily,
                            fontSize = 14.sp,
                            color = TextPrimary
                        ),
                        singleLine = true,
                        enabled = enabled,
                        keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
                    )
                }
                trailingContent?.let {
                    Spacer(modifier = Modifier.width(8.dp))
                    it()
                }
            }
        }

        if (isError && errorMessage != null) {
            Text(
                text = errorMessage,
                fontFamily = LexendFontFamily,
                fontSize = 11.sp,
                color = SOSCoral,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun BasicProfileTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    minLines: Int = 3
) {
    val shape = RoundedCornerShape(18.dp)
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, BorderSoft, shape),
        shape = shape,
        color = SurfaceWhite,
        tonalElevation = 0.dp
    ) {
        Box(modifier = Modifier.padding(14.dp)) {
            if (value.isBlank()) {
                Text(
                    text = placeholder,
                    fontFamily = LexendFontFamily,
                    fontSize = 14.sp,
                    color = TextSecondary.copy(alpha = 0.6f)
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                modifier = Modifier.fillMaxWidth(),
                textStyle = TextStyle(
                    fontFamily = LexendFontFamily,
                    fontSize = 14.sp,
                    color = TextPrimary
                ),
                minLines = minLines
            )
        }
    }
}
