package com.relaxmind.app.features.auth

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
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.relaxmind.app.ui.components.AppRole
import com.relaxmind.app.ui.components.ButtonVariant
import com.relaxmind.app.ui.components.RelaxButton
import com.relaxmind.app.ui.components.RelaxTopBar
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.RelaxMindTheme
import kotlinx.coroutines.delay

private const val DEFAULT_AVATAR_URL = "relaxmind://avatar/default"

private data class AvatarOption(
    val url: String,
    val colors: List<Color>
)

private val avatarOptions = listOf(
    AvatarOption("relaxmind://avatar/01", listOf(Color(0xFFA7F3D0), Color(0xFF0F6E56))),
    AvatarOption("relaxmind://avatar/02", listOf(Color(0xFFFFD6A5), Color(0xFFED8936))),
    AvatarOption("relaxmind://avatar/03", listOf(Color(0xFFD8B4FE), Color(0xFF7C3AED))),
    AvatarOption("relaxmind://avatar/04", listOf(Color(0xFFA5F3FC), Color(0xFF0891B2))),
    AvatarOption("relaxmind://avatar/05", listOf(Color(0xFFFBCFE8), Color(0xFFDB2777))),
    AvatarOption("relaxmind://avatar/06", listOf(Color(0xFFBFDBFE), Color(0xFF2563EB))),
    AvatarOption("relaxmind://avatar/07", listOf(Color(0xFFFEF3C7), Color(0xFFEAB308))),
    AvatarOption("relaxmind://avatar/08", listOf(Color(0xFFFECACA), Color(0xFFEF4444))),
    AvatarOption("relaxmind://avatar/09", listOf(Color(0xFFCCFBF1), Color(0xFF14B8A6))),
    AvatarOption("relaxmind://avatar/10", listOf(Color(0xFFFED7AA), Color(0xFFEA580C))),
    AvatarOption("relaxmind://avatar/11", listOf(Color(0xFFE9D5FF), Color(0xFFA855F7))),
    AvatarOption("relaxmind://avatar/12", listOf(Color(0xFFFDE68A), Color(0xFFB45309)))
)

@Composable
fun AvatarSetupScreen(
    viewModel: AuthViewModel = viewModel(),
    onNavigateBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var selectedAvatarUrl by remember { mutableStateOf(avatarOptions.first().url) }
    var submitted by remember { mutableStateOf(false) }
    var showSavingScreen by remember { mutableStateOf(false) }
    var savingStartedAt by remember { mutableStateOf(0L) }

    LaunchedEffect(Unit) {
        viewModel.clearSuccess()
    }

    LaunchedEffect(uiState.success, submitted) {
        if (uiState.success && submitted) {
            val elapsed = System.currentTimeMillis() - savingStartedAt
            delay((1_000L - elapsed).coerceAtLeast(0L))
            viewModel.clearSuccess()
            onContinue()
        }
    }

    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            showSavingScreen = false
            snackbarHostState.showSnackbar(it)
            viewModel.clearError()
        }
    }

    if (showSavingScreen) {
        AvatarSavingScreen()
        return
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
                horizontalAlignment = Alignment.Start
            ) {
                Spacer(modifier = Modifier.height(34.dp))
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
                    items(avatarOptions) { avatar ->
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
        }
    }
}

@Composable
private fun AvatarSavingScreen() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(color = PatientGreen)
            Text(
                text = "Guardando tu avatar...",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.62f)
            )
        }
    }
}

@Composable
private fun AvatarBubble(
    option: AvatarOption,
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
            .background(
                brush = Brush.linearGradient(option.colors),
                shape = CircleShape
            )
    )
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
