package com.relaxmind.app.features.patient

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.LinkOff
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.relaxmind.app.R
import com.relaxmind.app.MainActivity
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.FullScreenLoadingOverlay
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPatientScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val patient by viewModel.patient.collectAsState()
    val caregiver by viewModel.caregiver.collectAsState()

    var showLanguageBottomSheet by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showUnlinkDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = LexendTypography
    ) {
        Scaffold(
            containerColor = Color.White,
            bottomBar = {
                RelaxBottomNav(
                    selectedRoute = "patient/settings",
                    onNavigate = onNavigate,
                    role = AppRole.PATIENT
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Background decoration
                SoftGradientBackground(animateBlobs = true)

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 24.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // 1. Header
                    SettingsHeader(
                        onBackClick = { onNavigate("patient/dashboard") },
                        onNotificationClick = { /* No-op for now */ },
                        hasNotifications = true
                    )

                    patient?.let { currPatient ->
                        // 2. Profile Card
                        SettingsProfileCard(
                            userName = "${currPatient.name} ${currPatient.lastName}",
                            email = currPatient.email,
                            avatarUrl = currPatient.avatarUrl,
                            onClick = onNavigateToEditProfile
                        )

                        // 3. PREFERENCIAS Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SettingsSectionTitle("Preferencias")
                            SettingsGroupCard {
                                // Modo oscuro
                                SettingsToggleRow(
                                    label = "Modo oscuro",
                                    icon = Icons.Filled.DarkMode,
                                    checked = currPatient.darkMode,
                                    onToggle = { viewModel.updateDarkMode(it) }
                                )
                                SettingsDivider()

                                // Idioma
                                SettingsNavigationRow(
                                    label = "Idioma",
                                    icon = Icons.Filled.Language,
                                    trailingText = if (currPatient.language == "en") "English" else "Español",
                                    onClick = { showLanguageBottomSheet = true }
                                )
                                SettingsDivider()

                                // Notificaciones
                                SettingsToggleRow(
                                    label = "Notificaciones",
                                    icon = Icons.Filled.Notifications,
                                    checked = currPatient.notificationsEnabled,
                                    onToggle = { viewModel.updateNotificationsEnabled(it) }
                                )

                                if (currPatient.notificationsEnabled) {
                                    SettingsDivider()
                                    // Recordatorio check-in
                                    SettingsToggleRow(
                                        label = "Recordatorio de check-in",
                                        icon = Icons.Filled.AccessTime,
                                        checked = currPatient.checkInReminderEnabled,
                                        onToggle = { viewModel.updateCheckInReminderEnabled(it) }
                                    )
                                }
                                SettingsDivider()

                                // Biometría
                                SettingsToggleRow(
                                    label = "Inicio con biometría",
                                    icon = Icons.Filled.Fingerprint,
                                    checked = currPatient.biometricEnabled,
                                    onToggle = { viewModel.updateBiometricEnabled(it) }
                                )
                            }
                        }

                        // 4. CUENTA Section
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            SettingsSectionTitle("Cuenta")
                            SettingsGroupCard {
                                // Términos y condiciones
                                SettingsNavigationRow(
                                    label = "Términos y condiciones",
                                    icon = Icons.Filled.Description,
                                    onClick = { openTermsUrl(context) }
                                )
                                SettingsDivider()

                                // Desvincular cuidador
                                val hasCaregiver = currPatient.caregiverId != null
                                SettingsDangerRow(
                                    label = "Desvincular cuidador",
                                    icon = Icons.Filled.LinkOff,
                                    color = WarningOrange,
                                    bgColor = Color(0xFFFFF4EB),
                                    enabled = hasCaregiver,
                                    onClick = { showUnlinkDialog = true }
                                )
                                SettingsDivider()

                                // Borrar cuenta
                                SettingsDangerRow(
                                    label = "Borrar cuenta",
                                    icon = Icons.Filled.Delete,
                                    color = DangerRed,
                                    bgColor = Color(0xFFFEECEB),
                                    onClick = { showDeleteAccountDialog = true }
                                )
                                SettingsDivider()

                                // Cerrar sesión
                                SettingsDangerRow(
                                    label = "Cerrar sesión",
                                    icon = Icons.AutoMirrored.Filled.ExitToApp,
                                    color = DangerRed,
                                    bgColor = Color(0xFFFEECEB),
                                    onClick = { showLogoutDialog = true }
                                )
                            }
                        }
                    }

                    // 5. Version logo / branding
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.icono_plano2),
                            contentDescription = null,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Versión 1.0.0",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                    }
                }

                // Loading overlay
                if (isLoading) {
                    if (patient == null) {
                        FullScreenLoadingOverlay(overlayColor = Color.Transparent)
                    } else {
                        // Invisible overlay to block touches during background saves without darkening or showing a spinner
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable(
                                    onClick = {},
                                    indication = null,
                                    interactionSource = remember { MutableInteractionSource() }
                                )
                        )
                    }
                }
            }
        }
    }

    // Modal Bottom Sheet para idiomas
    if (showLanguageBottomSheet) {
        ModalBottomSheet(
            onDismissRequest = { showLanguageBottomSheet = false },
            sheetState = rememberModalBottomSheetState(),
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = "Selecciona tu idioma",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.updateLanguage("es")
                            showLanguageBottomSheet = false
                            relaunchActivity(context)
                        }
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Español (ES)",
                        fontFamily = LexendFontFamily,
                        fontWeight = if (patient?.language == "es") FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp,
                        color = if (patient?.language == "es") PatientGreen else TextPrimary
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable {
                            viewModel.updateLanguage("en")
                            showLanguageBottomSheet = false
                            relaunchActivity(context)
                        }
                        .padding(vertical = 14.dp, horizontal = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "English (EN)",
                        fontFamily = LexendFontFamily,
                        fontWeight = if (patient?.language == "en") FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp,
                        color = if (patient?.language == "en") PatientGreen else TextPrimary
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    // Custom dialog flows
    if (showUnlinkDialog) {
        var unlinkErrorMessage by remember { mutableStateOf<String?>(null) }
        UnlinkCaregiverDialog(
            caregiverName = caregiver?.let { "${it.name} ${it.lastName}" },
            onDismiss = {
                showUnlinkDialog = false
                unlinkErrorMessage = null
            },
            onConfirm = { password ->
                viewModel.unlinkCaregiver(
                    passwordConfirm = password,
                    onSuccess = {
                        showUnlinkDialog = false
                        unlinkErrorMessage = null
                    },
                    onError = { error ->
                        unlinkErrorMessage = error
                    }
                )
            },
            isLoading = isLoading,
            errorMessage = unlinkErrorMessage
        )
    }

    if (showDeleteAccountDialog) {
        var deleteErrorMessage by remember { mutableStateOf<String?>(null) }
        DeleteAccountDialog(
            onDismiss = {
                showDeleteAccountDialog = false
                deleteErrorMessage = null
            },
            onConfirm = { reason, password ->
                viewModel.deleteAccount(
                    reason = reason,
                    passwordConfirm = password,
                    onSuccess = {
                        showDeleteAccountDialog = false
                        deleteErrorMessage = null
                        onLogout()
                    },
                    onError = { error ->
                        deleteErrorMessage = error
                    }
                )
            },
            isLoading = isLoading,
            errorMessage = deleteErrorMessage
        )
    }

    if (showLogoutDialog) {
        LogoutConfirmationDialog(
            onDismiss = { showLogoutDialog = false },
            onConfirm = {
                showLogoutDialog = false
                viewModel.logout()
                onLogout()
            }
        )
    }
}

// ─────────────────────────────────────────────────────────────────────────────
// CUSTOM VISUAL COMPONENTS
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun RelaxSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val trackColor by animateColorAsState(if (checked) PatientGreen else Color(0xFFCBD5E0), label = "track")
    val thumbOffset by animateDpAsState(if (checked) 20.dp else 2.dp, label = "thumb")

    Box(
        modifier = modifier
            .size(width = 44.dp, height = 24.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(trackColor)
            .pointerInput(Unit) {
                detectTapGestures { onCheckedChange(!checked) }
            },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .padding(start = thumbOffset)
                .size(20.dp)
                .background(Color.White, CircleShape)
        )
    }
}

@Composable
fun SettingsHeader(
    onBackClick: () -> Unit,
    onNotificationClick: () -> Unit,
    hasNotifications: Boolean = true,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        contentAlignment = Alignment.Center
    ) {
        // Back Button
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .size(50.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
                )
                .background(Color.White, CircleShape)
                .clickable(
                    onClick = onBackClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = RelaxIcons.ArrowBack,
                contentDescription = "Atrás",
                tint = PatientGreen,
                modifier = Modifier.size(24.dp)
            )
        }

        // Title
        Text(
            text = "Ajustes",
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        // Notification Bell
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(50.dp)
                .shadow(
                    elevation = 4.dp,
                    shape = CircleShape,
                    ambientColor = Color(0xFF8A88A6).copy(alpha = 0.2f),
                    spotColor = Color(0xFF8A88A6).copy(alpha = 0.2f)
                )
                .background(Color.White, CircleShape)
                .clickable(
                    onClick = onNotificationClick,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = RelaxIcons.Notifications,
                contentDescription = "Notificaciones",
                tint = TextPrimary,
                modifier = Modifier.size(24.dp)
            )
            if (hasNotifications) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(PatientGreenLight)
                        .align(Alignment.TopEnd)
                        .offset(x = (-12).dp, y = 12.dp)
                )
            }
        }
    }
}

@Composable
fun SettingsProfileCard(
    userName: String,
    email: String,
    avatarUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 8.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.15f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.15f)
            )
            .background(Color.White, RoundedCornerShape(28.dp))
            .clickable(onClick = onClick)
            .padding(24.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            PatientAvatarLarge(avatarUrl = avatarUrl)
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = userName,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary
                )
            }
            
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun PatientAvatarLarge(avatarUrl: String) {
    val isCustomAvatar = avatarUrl.startsWith("relaxmind://avatar/")
    val modifier = Modifier
        .size(72.dp)
        .clip(CircleShape)
        .border(2.dp, PatientGreen.copy(alpha = 0.2f), CircleShape)

    if (isCustomAvatar) {
        val colors = getAvatarColors(avatarUrl)
        Box(
            modifier = modifier.background(Brush.linearGradient(colors))
        )
    } else {
        AsyncImage(
            model = avatarUrl.ifBlank { "https://ui-avatars.com/api/?name=P&background=0F6E56&color=fff" },
            contentDescription = "Perfil",
            modifier = modifier,
            contentScale = ContentScale.Crop
        )
    }
}

private fun getAvatarColors(url: String): List<Color> {
    return when (url) {
        "relaxmind://avatar/01" -> listOf(Color(0xFFA7F3D0), Color(0xFF0F6E56))
        "relaxmind://avatar/02" -> listOf(Color(0xFFFFD6A5), Color(0xFFED8936))
        "relaxmind://avatar/03" -> listOf(Color(0xFFD8B4FE), Color(0xFF7C3AED))
        "relaxmind://avatar/04" -> listOf(Color(0xFFA5F3FC), Color(0xFF0891B2))
        "relaxmind://avatar/05" -> listOf(Color(0xFFFBCFE8), Color(0xFFDB2777))
        "relaxmind://avatar/06" -> listOf(Color(0xFFBFDBFE), Color(0xFF2563EB))
        "relaxmind://avatar/07" -> listOf(Color(0xFFFEF3C7), Color(0xFFEAB308))
        "relaxmind://avatar/08" -> listOf(Color(0xFFFECACA), Color(0xFFEF4444))
        "relaxmind://avatar/09" -> listOf(Color(0xFFCCFBF1), Color(0xFF14B8A6))
        "relaxmind://avatar/10" -> listOf(Color(0xFFFED7AA), Color(0xFFEA580C))
        "relaxmind://avatar/11" -> listOf(Color(0xFFE9D5FF), Color(0xFFA855F7))
        "relaxmind://avatar/12" -> listOf(Color(0xFFFDE68A), Color(0xFFB45309))
        else -> listOf(Color(0xFFCBD5E0), Color(0xFF718096))
    }
}

@Composable
fun SettingsSectionTitle(title: String) {
    Text(
        text = title.uppercase(),
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = TextSecondary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp)
    )
}

@Composable
fun SettingsGroupCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = Color(0xFF8A88A6).copy(alpha = 0.12f),
                spotColor = Color(0xFF8A88A6).copy(alpha = 0.12f)
            )
            .background(Color.White, RoundedCornerShape(28.dp))
            .padding(vertical = 8.dp)
    ) {
        content()
    }
}

@Composable
fun SettingsToggleRow(
    label: String,
    icon: ImageVector,
    checked: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MintPill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PatientGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextPrimary
            )
        }
        
        RelaxSwitch(
            checked = checked,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun SettingsNavigationRow(
    label: String,
    icon: ImageVector,
    trailingText: String? = null,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MintPill),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PatientGreen,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = label,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 15.sp,
                color = TextPrimary
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun SettingsDangerRow(
    label: String,
    icon: ImageVector,
    color: Color,
    bgColor: Color,
    onClick: () -> Unit,
    enabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    val rowModifier = if (enabled) modifier.clickable(onClick = onClick) else modifier
    val alpha = if (enabled) 1f else 0.4f
    Row(
        modifier = rowModifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .alpha(alpha),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(bgColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column {
                Text(
                    text = label,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = color
                )
                if (!enabled) {
                    Text(
                        text = "Sin cuidador vinculado",
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 12.sp,
                        color = TextSecondary
                    )
                }
            }
        }
        
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = color.copy(alpha = 0.7f),
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        thickness = 1.dp,
        color = BorderSoft
    )
}

// ─────────────────────────────────────────────────────────────────────────────
// DIALOG COMPOSABLES
// ─────────────────────────────────────────────────────────────────────────────

@Composable
fun UnlinkCaregiverDialog(
    caregiverName: String?,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var password by remember { mutableStateOf("") }
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFFF4EB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.LinkOff,
                        contentDescription = null,
                        tint = WarningOrange,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Desvincular cuidador",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = if (caregiverName != null) {
                        "¿Seguro que deseas desvincularte de tu cuidador $caregiverName?"
                    } else {
                        "¿Seguro que deseas desvincularte de tu cuidador?"
                    },
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { 
                        Text(
                            text = "Contraseña para confirmar", 
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal
                        ) 
                    },
                    visualTransformation = PasswordVisualTransformation(),
                    isError = errorMessage != null,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PatientGreen,
                        unfocusedBorderColor = BorderSoft,
                        focusedLabelColor = PatientGreen
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        color = DangerRed,
                        fontSize = 12.sp,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PatientGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PatientGreen)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = { onConfirm(password) },
                        enabled = password.isNotBlank() && !isLoading,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = WarningOrange,
                            contentColor = Color.White,
                            disabledContainerColor = WarningOrange.copy(alpha = 0.5f)
                        )
                    ) {
                        Text(
                            text = "Desvincular",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DeleteAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, String) -> Unit,
    isLoading: Boolean,
    errorMessage: String?
) {
    var step by remember { mutableStateOf(1) }
    var selectedReason by remember { mutableStateOf("Ya no uso la app") }
    var otherReasonDetail by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    
    val reasons = listOf("Ya no uso la app", "Tengo otra cuenta", "Problemas con la app", "Otro")
    
    Dialog(
        onDismissRequest = onDismiss
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEECEB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Filled.Delete,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Borrar cuenta",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                when (step) {
                    1 -> {
                        Text(
                            text = "¿Por qué deseas eliminar tu cuenta?",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 15.sp,
                            color = TextPrimary,
                            textAlign = TextAlign.Center
                        )

                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            reasons.forEach { reason ->
                                val isSelected = selectedReason == reason
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) MintPill else Color.White)
                                        .border(
                                            width = 1.dp,
                                            color = if (isSelected) PatientGreen else BorderSoft,
                                            shape = RoundedCornerShape(12.dp)
                                        )
                                        .clickable { selectedReason = reason }
                                        .padding(horizontal = 16.dp, vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(20.dp)
                                            .clip(CircleShape)
                                            .border(
                                                width = 2.dp,
                                                color = if (isSelected) PatientGreen else Color.Gray,
                                                shape = CircleShape
                                            )
                                            .padding(3.dp)
                                    ) {
                                        if (isSelected) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(CircleShape)
                                                    .background(PatientGreen)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = reason,
                                        fontFamily = LexendFontFamily,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 14.sp,
                                        color = if (isSelected) PatientGreen else TextPrimary
                                    )
                                }
                            }
                        }

                        if (selectedReason == "Otro") {
                            OutlinedTextField(
                                value = otherReasonDetail,
                                onValueChange = { otherReasonDetail = it },
                                label = { Text("Especifica el motivo...", fontFamily = LexendFontFamily) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = PatientGreen,
                                    unfocusedBorderColor = BorderSoft,
                                    focusedLabelColor = PatientGreen
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, PatientGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PatientGreen)
                            ) {
                                Text(
                                    text = "Cancelar",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = { step = 2 },
                                enabled = selectedReason != "Otro" || otherReasonDetail.isNotBlank(),
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = PatientGreen, contentColor = Color.White)
                            ) {
                                Text(
                                    text = "Siguiente",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    2 -> {
                        Text(
                            text = "Ingresa tu contraseña para confirmar la eliminación de tu cuenta:",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("Contraseña", fontFamily = LexendFontFamily) },
                            visualTransformation = PasswordVisualTransformation(),
                            isError = errorMessage != null,
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PatientGreen,
                                unfocusedBorderColor = BorderSoft,
                                focusedLabelColor = PatientGreen
                            )
                        )

                        if (errorMessage != null) {
                            Text(
                                text = errorMessage,
                                fontFamily = LexendFontFamily,
                                fontWeight = FontWeight.Normal,
                                color = DangerRed,
                                fontSize = 12.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFFFFF9EF))
                                .border(1.dp, WarningOrange.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                                .padding(16.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Aviso importante",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = WarningOrange
                                )
                                Text(
                                    text = "Tu cuenta permanecerá inactiva. Podrás reactivarla en un plazo máximo de 7 días antes de su eliminación definitiva.",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Normal,
                                    fontSize = 12.sp,
                                    color = TextPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            OutlinedButton(
                                onClick = { step = 1 },
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, PatientGreen),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = PatientGreen)
                            ) {
                                Text(
                                    text = "Atrás",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }

                            Button(
                                onClick = {
                                    val finalReason = if (selectedReason == "Otro") {
                                        "Otro: $otherReasonDetail"
                                    } else {
                                        selectedReason
                                    }
                                    onConfirm(finalReason, password)
                                },
                                enabled = password.isNotBlank() && !isLoading,
                                modifier = Modifier.weight(1f).height(48.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                            ) {
                                Text(
                                    text = "Eliminar",
                                    fontFamily = LexendFontFamily,
                                    fontWeight = FontWeight.Medium,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LogoutConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFFEECEB)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ExitToApp,
                        contentDescription = null,
                        tint = DangerRed,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "¿Deseas cerrar sesión?",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tendrás que volver a introducir tus credenciales para acceder a la aplicación.",
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, PatientGreen),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = PatientGreen)
                    ) {
                        Text(
                            text = "Cancelar",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }

                    Button(
                        onClick = onConfirm,
                        modifier = Modifier.weight(1f).height(48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White)
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

private fun relaunchActivity(context: Context) {
    val intent = Intent(context, MainActivity::class.java).apply {
        addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
    }
    context.startActivity(intent)
    (context as? Activity)?.finish()
}

private fun openTermsUrl(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://relaxmind.com/terms"))
    context.startActivity(intent)
}
