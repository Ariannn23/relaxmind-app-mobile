package com.relaxmind.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun RelaxBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    role: AppRole? = null,
    darkMode: Boolean = isSystemInDarkTheme()
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.9f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "back-button-scale"
    )

    // Determinar la paleta de colores según rol y modo
    // En modo oscuro, suele ser un fondo gris/negro muy oscuro con borde tenue
    // En modo claro, un fondo blanco con borde tenue
    // Si queremos tintar la flecha según el rol, podemos usar los colores primarios.
    
    val isCaregiver = role == AppRole.CAREGIVER
    
    val bgColor = Color.White
    
    val primaryColor = if (isCaregiver) Color(0xFF4338A8) else Color(0xFF0F6E56)
    val outlineColor = if (isCaregiver) Color(0xFF4338A8).copy(alpha = 0.25f) else Color(0xFF0F6E56).copy(alpha = 0.25f)

    Box(
        modifier = modifier
            .scale(scale)
            .size(44.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(1.5.dp, outlineColor, CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClickLabel = "Volver",
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
            contentDescription = null,
            tint = primaryColor,
            modifier = Modifier.size(22.dp)
        )
    }
}
