package com.relaxmind.app.ui.components

import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.relaxmind.app.ui.components.toast.LocalRelaxToast
import com.relaxmind.app.ui.components.toast.RelaxToastHostState
import com.relaxmind.app.ui.components.toast.RelaxToastType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

// ── Bridge classes to migrate old RelaxToast to the new global RelaxToast ──

enum class RelaxToastTypeLegacy {
    Success, Error, Warning, Info
}

class RelaxToastState(
    private val hostState: RelaxToastHostState,
    private val scope: CoroutineScope
) {
    fun showSuccess(message: String) {
        scope.launch {
            hostState.showToast(type = RelaxToastType.Success, title = "¡Listo!", message = message)
        }
    }

    fun showError(message: String) {
        scope.launch {
            hostState.showToast(type = RelaxToastType.Error, title = "Algo salió mal", message = message)
        }
    }

    fun showWarning(message: String) {
        scope.launch {
            hostState.showToast(type = RelaxToastType.Warning, title = "Advertencia", message = message)
        }
    }

    fun showInfo(message: String) {
        scope.launch {
            hostState.showToast(type = RelaxToastType.Info, title = "Información", message = message)
        }
    }

    fun dismiss() {
        hostState.dismissCurrent()
    }
}

@Composable
fun rememberRelaxToastState(): RelaxToastState {
    val hostState = LocalRelaxToast.current
    val scope = rememberCoroutineScope()
    return remember(hostState, scope) {
        RelaxToastState(hostState, scope)
    }
}

// ── Toast host (Empty bridge) ───────────────────────────────────────────────────
@Composable
fun RelaxToastHost(
    state: RelaxToastState,
    modifier: Modifier = Modifier
) {
    // Intentionally empty. 
    // The actual UI drawing is handled globally by the new RelaxToastHost 
    // located in MainActivity.kt inside the CompositionLocalProvider.
}
