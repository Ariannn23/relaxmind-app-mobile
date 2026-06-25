package com.relaxmind.app.features.auth.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.relaxmind.app.ui.components.RelaxBackButton

@Composable
fun RelaxAuthBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    RelaxBackButton(
        onClick = onClick,
        modifier = modifier
    )
}
