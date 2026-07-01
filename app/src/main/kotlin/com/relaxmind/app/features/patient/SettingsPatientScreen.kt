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
import com.relaxmind.app.ui.components.DisableNotificationsWarningDialog
import com.relaxmind.app.ui.components.NotificationPermissionDialog
import com.relaxmind.app.ui.components.RelaxBottomNav
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.getAvatarDrawableRes
import com.relaxmind.app.ui.components.hasNotificationPermission
import com.relaxmind.app.ui.components.openNotificationSettings
import com.relaxmind.app.ui.components.rememberNotificationPermissionLauncher
import com.relaxmind.app.ui.components.rememberNotificationPermissionStatus
import com.relaxmind.app.ui.components.requestNotificationPermission
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.components.SettingsSkeleton
import com.relaxmind.app.ui.components.ErrorStateScreen
import com.relaxmind.app.ui.components.ScreenHeader
import com.relaxmind.app.ui.components.auth.SoftGradientBackground
import com.relaxmind.app.ui.themes.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPatientScreen(
    viewModel: PatientViewModel = viewModel(),
    onNavigateToEditProfile: () -> Unit,
    onLogout: () -> Unit,
    onNavigate: (String) -> Unit,
    showBottomNav: Boolean = true
) {
    val context = LocalContext.current
    val isLoading by viewModel.isLoading.collectAsState()
    val error by viewModel.error.collectAsState()
    val patient by viewModel.patient.collectAsState()
    val caregiver by viewModel.caregiver.collectAsState()

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showUnlinkDialog by remember { mutableStateOf(false) }
    var showDeleteAccountDialog by remember { mutableStateOf(false) }
    var showNotificationPermissionDialog by remember { mutableStateOf(false) }
    var showDisableNotificationsDialog by remember { mutableStateOf(false) }
    val notificationsPermissionGranted = rememberNotificationPermissionStatus()
    val notificationPermissionLauncher = rememberNotificationPermissionLauncher { granted ->
        viewModel.updateNotificationsEnabled(granted)
        showNotificationPermissionDialog = !granted
        if (granted) {
            patient?.let {
                com.relaxmind.app.utils.ReminderManager.scheduleReminder(context, it.checkInReminderTime)
            }
        }
    }

    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    // App theme state
    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val bgColor = if (isDark) com.relaxmind.app.ui.themes.BackgroundDark else Color.White

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = LexendTypography
    ) {
        Scaffold(
            containerColor = bgColor,
            bottomBar = {
                if (showBottomNav) {
                    RelaxBottomNav(
                        selectedRoute = "patient/settings",
                        onNavigate = onNavigate,
                        role = AppRole.PATIENT,
                        darkMode = isDark
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                // Background decoration
                if (!isDark) {
                    SoftGradientBackground(animateBlobs = true)
                }

                if (isLoading && patient == null && error == null) {
                    SettingsSkeleton(modifier = Modifier.align(Alignment.Center))
                } else if (error != null && patient == null) {
                    ErrorStateScreen(
                        message = error ?: "",
                        onRetry = { viewModel.loadDashboardData() }
                    )
                } else {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(start = 24.dp, end = 24.dp, bottom = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                    // 1. Header
                    ScreenHeader(
                        title = "Ajustes",
                        subtitle = "Configura tu cuenta y preferencias",
                        horizontalPadding = 0.dp
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
                                // Notificaciones
                                SettingsToggleRow(
                                    label = "Notificaciones",
                                    icon = Icons.Filled.Notifications,
                                    checked = currPatient.notificationsEnabled && notificationsPermissionGranted,
                                    onToggle = { enabled ->
                                        if (enabled) {
                                            if (hasNotificationPermission(context)) {
                                                viewModel.updateNotificationsEnabled(true)
                                                com.relaxmind.app.utils.ReminderManager.scheduleReminder(context, currPatient.checkInReminderTime)
                                            } else {
                                                showNotificationPermissionDialog = true
                                            }
                                        } else {
                                            showDisableNotificationsDialog = true
                                        }
                                    }
                                )

                                val context = androidx.compose.ui.platform.LocalContext.current
                                
                                if (currPatient.notificationsEnabled && notificationsPermissionGranted) {
                                    SettingsDivider()
                                    // Recordatorio check-in
                                    SettingsToggleRow(
                                        label = "Recordatorio de check-in",
                                        icon = Icons.Filled.AccessTime,
                                        checked = currPatient.checkInReminderEnabled,
                                        onToggle = { 
                                            viewModel.updateCheckInReminderEnabled(it) 
                                            if (it) {
                                                com.relaxmind.app.utils.ReminderManager.scheduleReminder(context, currPatient.checkInReminderTime)
                                            } else {
                                                com.relaxmind.app.utils.ReminderManager.cancelReminder(context)
                                            }
                                        }
                                    )
                                    
                                    if (currPatient.checkInReminderEnabled) {
                                        val timeParts = currPatient.checkInReminderTime.split(":")
                                        val initHour = timeParts.getOrNull(0)?.toIntOrNull() ?: 20
                                        val initMin = timeParts.getOrNull(1)?.toIntOrNull() ?: 0
                                        
                                        SettingsClickableRow(
                                            label = "Hora del recordatorio",
                                            value = currPatient.checkInReminderTime,
                                            icon = Icons.Filled.AccessTime,
                                            onClick = {
                                                android.app.TimePickerDialog(
                                                    context,
                                                    { _, hour, minute ->
                                                        val timeStr = String.format(java.util.Locale.getDefault(), "%02d:%02d", hour, minute)
                                                        viewModel.updateCheckInReminderTime(timeStr)
                                                        com.relaxmind.app.utils.ReminderManager.scheduleReminder(context, timeStr)
                                                    },
                                                    initHour,
                                                    initMin,
                                                    true // is24HourView
                                                ).show()
                                            }
                                        )
                                    }
                                }
                                SettingsDivider()

                                // Biometría
                                SettingsToggleRow(
                                    label = "Inicio con biometría",
                                    icon = Icons.Filled.Fingerprint,
                                    checked = currPatient.biometricEnabled,
                                    onToggle = { 
                                        viewModel.updateBiometricEnabled(it)
                                        com.relaxmind.app.utils.SecurityPreferences.setBiometricEnabled(context, it)
                                    }
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
                                    onClick = { onNavigate("common/terms-and-conditions/patient") }
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
                            modifier = Modifier.size(130.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val versionColor = if (isDark) com.relaxmind.app.ui.themes.TextDarkSecondary else TextSecondary
                        Text(
                            text = "Versión 1.0.0",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = versionColor
                        )
                    }
                    }
                }

                // Invisible overlay to block touches during background saves
                if (isLoading && patient != null) {
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

    // Language bottom sheet removed

    if (showNotificationPermissionDialog) {
        NotificationPermissionDialog(
            role = AppRole.PATIENT,
            title = "Activa tus notificaciones",
            message = "Las notificaciones son necesarias para tus check-ins, recordatorios y alertas SOS. Actívalas para que RelaxMind pueda acompañarte a tiempo.",
            primaryText = "Activar ahora",
            secondaryText = "Ir a ajustes",
            onPrimaryClick = {
                if (hasNotificationPermission(context)) {
                    viewModel.updateNotificationsEnabled(true)
                    showNotificationPermissionDialog = false
                } else {
                    requestNotificationPermission(
                        launcher = notificationPermissionLauncher,
                        onAlreadyGranted = {
                            viewModel.updateNotificationsEnabled(true)
                            showNotificationPermissionDialog = false
                        }
                    )
                }
            },
            onDismiss = {
                openNotificationSettings(context)
                showNotificationPermissionDialog = false
            }
        )
    }

    if (showDisableNotificationsDialog) {
        DisableNotificationsWarningDialog(
            role = AppRole.PATIENT,
            onConfirm = {
                showDisableNotificationsDialog = false
                viewModel.updateNotificationsEnabled(false)
                com.relaxmind.app.utils.ReminderManager.cancelReminder(context)
            },
            onDismiss = { showDisableNotificationsDialog = false }
        )
    }

    // Custom dialog flows
    var showUnlinkConfirmationDialog by remember { mutableStateOf(false) }
    var unlinkedCaregiverName by remember { mutableStateOf("") }

    if (showUnlinkConfirmationDialog) {
        com.relaxmind.app.ui.components.UnlinkNotificationDialog(
            type = com.relaxmind.app.ui.components.UnlinkDialogType.CONFIRMATION,
            otherPartyName = unlinkedCaregiverName,
            primaryColor = com.relaxmind.app.ui.themes.PatientGreen,
            onDismissRequest = {
                showUnlinkConfirmationDialog = false
            }
        )
    }

    if (showUnlinkDialog) {
        var unlinkErrorMessage by remember { mutableStateOf<String?>(null) }
        UnlinkCaregiverDialog(
            caregiverName = caregiver?.let { "${it.name} ${it.lastName}" },
            onDismiss = {
                showUnlinkDialog = false
                unlinkErrorMessage = null
            },
            onConfirm = { password ->
                unlinkedCaregiverName = caregiver?.let { "${it.name} ${it.lastName}" } ?: "tu cuidador"
                viewModel.unlinkCaregiver(
                    passwordConfirm = password,
                    onSuccess = {
                        showUnlinkDialog = false
                        unlinkErrorMessage = null
                        showUnlinkConfirmationDialog = true
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
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                onCheckedChange(!checked)
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
fun SettingsProfileCard(
    userName: String,
    email: String,
    avatarUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val cardColor = if (isDark) com.relaxmind.app.ui.themes.SurfaceDark else Color.White
    val shadowColor = if (isDark) Color(0xFF68D391).copy(alpha = 0.05f) else androidx.compose.ui.graphics.DefaultShadowColor
    val textColorPrimary = if (isDark) com.relaxmind.app.ui.themes.TextDarkPrimary else TextPrimary
    val textColorSecondary = if (isDark) com.relaxmind.app.ui.themes.TextDarkSecondary else TextSecondary

    Box(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 12.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .background(cardColor, RoundedCornerShape(28.dp))
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
                    color = textColorPrimary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = email,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = textColorSecondary
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
        Image(
            painter = painterResource(id = getAvatarDrawableRes(avatarUrl)),
            contentDescription = "Perfil",
            contentScale = ContentScale.Crop,
            modifier = modifier.background(Color(0xFFF3F4F6))
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

@Composable
fun SettingsSectionTitle(title: String) {
    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val textColorSecondary = if (isDark) com.relaxmind.app.ui.themes.TextDarkSecondary else TextSecondary
    Text(
        text = title.uppercase(),
        fontFamily = LexendFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = textColorSecondary,
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
    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val cardColor = if (isDark) com.relaxmind.app.ui.themes.SurfaceDark else Color.White
    val shadowColor = if (isDark) Color(0xFF68D391).copy(alpha = 0.05f) else Color(0xFF8A88A6).copy(alpha = 0.12f)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = 6.dp,
                shape = RoundedCornerShape(28.dp),
                ambientColor = shadowColor,
                spotColor = shadowColor
            )
            .background(cardColor, RoundedCornerShape(28.dp))
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
    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val textColorPrimary = if (isDark) com.relaxmind.app.ui.themes.TextDarkPrimary else TextPrimary
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
                color = textColorPrimary
            )
        }
        
        RelaxSwitch(
            checked = checked,
            onCheckedChange = onToggle
        )
    }
}

@Composable
fun SettingsClickableRow(
    label: String,
    value: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val textColorPrimary = if (isDark) com.relaxmind.app.ui.themes.TextDarkPrimary else TextPrimary
    val textColorSecondary = if (isDark) com.relaxmind.app.ui.themes.TextDarkSecondary else TextSecondary
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
            Column {
                Text(
                    text = label,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                    color = textColorPrimary
                )
                if (value.isNotEmpty()) {
                    Text(
                        text = value,
                        fontFamily = LexendFontFamily,
                        fontWeight = FontWeight.Normal,
                        fontSize = 13.sp,
                        color = textColorSecondary
                    )
                }
            }
        }
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
    val isDark by com.relaxmind.app.ui.themes.ThemeState.darkMode.collectAsState()
    val textColorPrimary = if (isDark) com.relaxmind.app.ui.themes.TextDarkPrimary else TextPrimary
    val textColorSecondary = if (isDark) com.relaxmind.app.ui.themes.TextDarkSecondary else TextSecondary
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
                color = textColorPrimary
            )
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (trailingText != null) {
                Text(
                    text = trailingText,
                    fontFamily = LexendFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                    color = textColorSecondary,
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
                                    fontWeight = FontWeight.SemiBold,
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
                        modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed, contentColor = Color.White),
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
                    ) {
                        Text(
                            text = "Cerrar sesión",
                            fontFamily = LexendFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp,
                            maxLines = 1
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
