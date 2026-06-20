package com.relaxmind.app.ui.components.toast

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Stable
class RelaxToastHostState {
    private val mutex = Mutex()

    var currentToastData by mutableStateOf<RelaxToastData?>(null)
        private set

    suspend fun showToast(
        type: RelaxToastType,
        title: String,
        message: String,
        durationMillis: Long = 3500L,
        actionLabel: String? = null,
        onActionClick: (() -> Unit)? = null
    ) {
        val data = RelaxToastData(type, title, message, durationMillis, actionLabel, onActionClick)
        
        mutex.withLock {
            try {
                currentToastData = data
                delay(durationMillis)
            } finally {
                // Only clear if it hasn't been overwritten (though withLock prevents concurrent writes)
                if (currentToastData == data) {
                    currentToastData = null
                }
            }
        }
    }

    fun dismissCurrent() {
        currentToastData = null
    }
}

val LocalRelaxToast = staticCompositionLocalOf<RelaxToastHostState> {
    error("No RelaxToastHostState provided")
}
