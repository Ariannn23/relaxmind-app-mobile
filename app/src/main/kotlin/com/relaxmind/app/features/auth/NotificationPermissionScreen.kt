package com.relaxmind.app.features.auth

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import com.relaxmind.app.ui.components.RelaxToastHost
import com.relaxmind.app.ui.components.rememberRelaxToastState
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.FullScreenLoadingScreen
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxIcons
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import kotlinx.coroutines.delay

@Composable
fun NotificationPermissionScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onContinuePatient: () -> Unit,
    onContinueCaregiver: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val toastState = rememberRelaxToastState()
    var submitted by remember { mutableStateOf(false) }
    var showSavingScreen by remember { mutableStateOf(false) }
    var savingStartedAt by remember { mutableStateOf(0L) }

    val notificationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            submitted = true
            showSavingScreen = true
            savingStartedAt = System.currentTimeMillis()
            viewModel.setNotificationPermission(granted)
        }
    )

    LaunchedEffect(Unit) {
        viewModel.clearSuccess()
    }

    LaunchedEffect(uiState.success, submitted, userRole) {
        if (uiState.success && submitted) {
            val elapsed = System.currentTimeMillis() - savingStartedAt
            delay((1_000L - elapsed).coerceAtLeast(0L))
            viewModel.clearSuccess()
            if (userRole == "caregiver") onContinueCaregiver() else onContinuePatient()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            showSavingScreen = false
            toastState.showError(it)
            viewModel.clearError()
        }
    }

    if (showSavingScreen) {
        FullScreenLoadingScreen(
            text = "Guardando tus preferencias...",
            backgroundColor = Color.White,
            indicatorColor = PatientGreen
        )
        return
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
                    .padding(horizontal = 28.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(52.dp))
                NotificationHero()
                Spacer(modifier = Modifier.height(38.dp))
                Text(
                    text = "Mantente al día",
                    style = MaterialTheme.typography.headlineLarge,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(20.dp))
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    BenefitRow("Recordatorio de check-in diario")
                    BenefitRow("Alertas antes de tus citas médicas")
                    BenefitRow("Avisos importantes de tu cuidador")
                    BenefitRow("Notificaciones de seguridad SOS")
                }
                Spacer(modifier = Modifier.weight(1f))
                RelaxButton(
                    text = "Permitir notificaciones",
                    onClick = {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                        } else {
                            submitted = true
                            showSavingScreen = true
                            savingStartedAt = System.currentTimeMillis()
                            viewModel.setNotificationPermission(true)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY,
                    role = AppRole.PATIENT,
                    enabled = !uiState.isLoading
                )
                TextButton(
                    enabled = !uiState.isLoading,
                    onClick = {
                        submitted = true
                        showSavingScreen = true
                        savingStartedAt = System.currentTimeMillis()
                        viewModel.setNotificationPermission(false)
                    }
                ) {
                    Text(
                        text = "Ahora no",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
                    )
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            RelaxToastHost(state = toastState)
        }
    }
}

@Composable
private fun NotificationHero() {
    Box(modifier = Modifier.size(214.dp), contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(176.dp)
                .clip(CircleShape)
                .background(PatientGreen.copy(alpha = 0.08f))
        )
        Box(
            modifier = Modifier
                .size(132.dp)
                .clip(CircleShape)
                .background(Brush.verticalGradient(listOf(PatientGreen.copy(alpha = 0.75f), PatientGreen))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = RelaxIcons.Notifications,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(68.dp)
            )
        }
    }
}

@Composable
private fun BenefitRow(text: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Icon(
            imageVector = RelaxIcons.Check,
            contentDescription = null,
            tint = PatientGreen,
            modifier = Modifier.size(26.dp)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.68f)
        )
    }
}

@Preview(name = "NotificationPermissionScreen", showBackground = true, showSystemUi = true)
@Composable
private fun NotificationPermissionScreenPreview() {
    RelaxMindTheme(darkTheme = false) {
        NotificationPermissionScreen(
            onNavigateBack = {},
            onContinuePatient = {},
            onContinueCaregiver = {}
        )
    }
}
