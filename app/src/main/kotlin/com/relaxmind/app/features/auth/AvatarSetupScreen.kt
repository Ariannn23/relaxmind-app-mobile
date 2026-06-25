package com.relaxmind.app.features.auth

import androidx.compose.foundation.Image
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.relaxmind.app.ui.components.LocalRelaxAvatars
import com.relaxmind.app.ui.components.RelaxAvatar

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.FullScreenLoadingScreen
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import kotlinx.coroutines.delay

private const val DEFAULT_AVATAR_URL = "relaxmind://avatar/default"

@Composable
fun AvatarSetupScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onContinue: (String?) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val toastState = rememberRelaxToastState()
    var selectedAvatarUrl by remember { mutableStateOf(LocalRelaxAvatars.first().url) }
    var submitted by remember { mutableStateOf(false) }
    var showSavingScreen by remember { mutableStateOf(false) }
    var savingStartedAt by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.ensureCurrentRole()
        viewModel.clearSuccess()
    }

    LaunchedEffect(uiState.success, submitted) {
        if (uiState.success && submitted) {
            val elapsed = System.currentTimeMillis() - savingStartedAt
            delay((1_000L - elapsed).coerceAtLeast(0L))
            viewModel.clearSuccess()
            onContinue(userRole)
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
            text = "Guardando tu avatar...",
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
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(34.dp))
                if (userRole == "patient") {
                    Box(
                        modifier = Modifier
                            .align(Alignment.End)
                            .background(
                                color = PatientGreen.copy(alpha = 0.10f),
                                shape = RoundedCornerShape(999.dp)
                            )
                            .padding(horizontal = 14.dp, vertical = 7.dp)
                    ) {
                        Text(
                            text = "Paso 1 de 2",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = PatientGreen
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
                Text(
                    text = "Elige tu avatar",
                    style = MaterialTheme.typography.headlineLarge
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Puedes cambiarlo después en ajustes",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.55f)
                )
                Spacer(modifier = Modifier.height(34.dp))
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    items(LocalRelaxAvatars) { avatar ->
                        AvatarBubble(
                            option = avatar,
                            selected = selectedAvatarUrl == avatar.url,
                            onClick = { selectedAvatarUrl = avatar.url }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
                RelaxButton(
                    text = "Continuar",
                    onClick = {
                        submitted = true
                        showSavingScreen = true
                        savingStartedAt = System.currentTimeMillis()
                        viewModel.updateAvatar(selectedAvatarUrl)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    variant = ButtonVariant.PRIMARY,
                    role = AppRole.PATIENT,
                    enabled = !uiState.isLoading
                )
                TextButton(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    enabled = !uiState.isLoading,
                    onClick = {
                        submitted = true
                        showSavingScreen = true
                        savingStartedAt = System.currentTimeMillis()
                        viewModel.updateAvatar(DEFAULT_AVATAR_URL)
                    }
                ) {
                    Text(
                        text = "Omitir",
                        style = MaterialTheme.typography.labelLarge,
                        color = PatientGreen
                    )
                }
                Spacer(modifier = Modifier.height(16.dp))
            }
            RelaxToastHost(state = toastState)
        }
    }
}

@Composable
private fun AvatarBubble(
    option: RelaxAvatar,
    selected: Boolean,
    onClick: () -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (selected) 1.08f else 1f,
        label = "avatar-scale"
    )
    Box(
        modifier = Modifier
            .size(70.dp)
            .scale(scale)
            .clickable(onClick = onClick)
            .border(
                border = BorderStroke(if (selected) 3.dp else 0.dp, PatientGreen),
                shape = CircleShape
            )
            .padding(if (selected) 5.dp else 0.dp)
            .clip(CircleShape)
            .background(Color(0xFFF3F4F6))
    ) {
        Image(
            painter = painterResource(id = option.drawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
    }
}

@Preview(name = "AvatarSetupScreen", showBackground = true, showSystemUi = true)
@Composable
private fun AvatarSetupScreenPreview() {
    RelaxMindTheme(darkTheme = false) {
        AvatarSetupScreen(
            onNavigateBack = {},
            onContinue = {}
        )
    }
}
