package com.relaxmind.app.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import com.relaxmind.app.ui.themes.*
import kotlinx.coroutines.delay

// ── Toast type ────────────────────────────────────────────────────────────────
enum class RelaxToastType {
    Success, Error, Warning, Info
}

// ── Toast state ────────────────────────────────────────────────────────────────
class RelaxToastState {
    var currentMessage by mutableStateOf<RelaxToastData?>(null)
        private set

    fun showSuccess(message: String) {
        currentMessage = RelaxToastData(message, RelaxToastType.Success)
    }

    fun showError(message: String) {
        currentMessage = RelaxToastData(message, RelaxToastType.Error)
    }

    fun showWarning(message: String) {
        currentMessage = RelaxToastData(message, RelaxToastType.Warning)
    }

    fun showInfo(message: String) {
        currentMessage = RelaxToastData(message, RelaxToastType.Info)
    }

    fun dismiss() {
        currentMessage = null
    }
}

data class RelaxToastData(
    val message: String,
    val type: RelaxToastType
)

@Composable
fun rememberRelaxToastState(): RelaxToastState = remember { RelaxToastState() }

// ── Visual config per type ────────────────────────────────────────────────────
private data class ToastVisual(
    val accentColor: Color,
    val iconBg: Color,
    val icon: ImageVector,
    val iconTint: Color
)

private fun toastVisual(type: RelaxToastType): ToastVisual = when (type) {
    RelaxToastType.Success -> ToastVisual(
        accentColor = Color(0xFF0F6E56),
        iconBg = Color(0xFFEAF8F1),
        icon = Icons.Default.CheckCircle,
        iconTint = Color(0xFF0F6E56)
    )
    RelaxToastType.Error -> ToastVisual(
        accentColor = Color(0xFFE8582A),
        iconBg = Color(0xFFFEECEB),
        icon = Icons.Default.Cancel,
        iconTint = Color(0xFFE8582A)
    )
    RelaxToastType.Warning -> ToastVisual(
        accentColor = Color(0xFFF59E0B),
        iconBg = Color(0xFFFFFBEB),
        icon = Icons.Default.Warning,
        iconTint = Color(0xFFF59E0B)
    )
    RelaxToastType.Info -> ToastVisual(
        accentColor = Color(0xFF4338A8),
        iconBg = Color(0xFFF1EDFF),
        icon = Icons.Default.Info,
        iconTint = Color(0xFF4338A8)
    )
}

// ── Host composable ────────────────────────────────────────────────────────────
/**
 * Drop this inside the topmost Box of any screen, AFTER the Scaffold content.
 * It floats at the top with a spring slide-in animation.
 *
 * Usage:
 * ```kotlin
 * val toastState = rememberRelaxToastState()
 * Box(Modifier.fillMaxSize()) {
 *     Scaffold(...) { ... }
 *     RelaxToastHost(state = toastState)
 * }
 * toastState.showSuccess("Guardado correctamente")
 * ```
 */
@Composable
fun RelaxToastHost(
    state: RelaxToastState,
    modifier: Modifier = Modifier,
    durationMs: Long = 3500
) {
    val message = state.currentMessage

    LaunchedEffect(message) {
        if (message != null) {
            delay(durationMs)
            state.dismiss()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .zIndex(999f)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        AnimatedVisibility(
            visible = message != null,
            enter = slideInVertically(
                initialOffsetY = { -it - 120 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMediumLow
                )
            ) + fadeIn(animationSpec = tween(250)),
            exit = slideOutVertically(
                targetOffsetY = { -it - 120 },
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioNoBouncy,
                    stiffness = Spring.StiffnessMedium
                )
            ) + fadeOut(animationSpec = tween(200))
        ) {
            message?.let { msg ->
                RelaxToastCard(
                    data = msg,
                    onDismiss = { state.dismiss() }
                )
            }
        }
    }
}

// ── Card UI — Soft UI white card with colored left accent ─────────────────────
@Composable
private fun RelaxToastCard(
    data: RelaxToastData,
    onDismiss: () -> Unit
) {
    val visual = toastVisual(data.type)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 20.dp,
                shape = RoundedCornerShape(20.dp),
                ambientColor = visual.accentColor.copy(alpha = 0.12f),
                spotColor = visual.accentColor.copy(alpha = 0.18f)
            )
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White)
            .clickable(
                indication = null,
                interactionSource = remember { MutableInteractionSource() },
                onClick = onDismiss
            )
    ) {
        // Colored left accent strip
        Box(
            modifier = Modifier
                .width(4.dp)
                .fillMaxHeight()
                .background(visual.accentColor)
                .align(Alignment.CenterStart)
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 14.dp, top = 14.dp, bottom = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Icon bubble
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(visual.iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = visual.icon,
                    contentDescription = null,
                    tint = visual.iconTint,
                    modifier = Modifier.size(22.dp)
                )
            }

            // Message
            Text(
                text = data.message,
                fontFamily = LexendFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = TextPrimary,
                modifier = Modifier.weight(1f),
                lineHeight = 19.sp
            )

            // Close X
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFF4F5F7))
                    .clickable(
                        indication = null,
                        interactionSource = remember { MutableInteractionSource() },
                        onClick = onDismiss
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar",
                    tint = TextSecondary,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
    }
}
