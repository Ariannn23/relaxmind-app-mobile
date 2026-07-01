package com.relaxmind.app.ui.components

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object ScrollToTopEvents {
    private val _requests = MutableSharedFlow<String>(extraBufferCapacity = 16)
    val requests = _requests.asSharedFlow()

    fun request(route: String) {
        _requests.tryEmit(route)
    }
}
