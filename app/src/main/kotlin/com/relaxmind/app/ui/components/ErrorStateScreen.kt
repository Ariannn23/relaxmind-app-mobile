package com.relaxmind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.relaxmind.app.ui.themes.BackgroundWhite
import com.relaxmind.app.ui.themes.LexendTypography
import com.relaxmind.app.ui.themes.PatientGreen
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary
import com.relaxmind.app.ui.themes.ThemeState

@Composable
fun ErrorStateScreen(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    val darkMode by ThemeState.darkMode.collectAsState()
    
    val bgColor = if (darkMode) Color(0xFF071413) else BackgroundWhite
    val surfaceColor = if (darkMode) Color(0xFF0B211F) else Color(0xFFF4FAF7)
    val titleColor = if (darkMode) Color(0xFFE9EDF2) else TextPrimary
    val textColor = if (darkMode) Color(0xFFA1AEB7) else TextSecondary
    val iconColor = PatientGreen

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(bgColor)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Icon container
        Column(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(surfaceColor),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Warning,
                contentDescription = "Error",
                tint = iconColor,
                modifier = Modifier.size(48.dp)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "¡Ups! Algo salió mal",
            style = LexendTypography.headlineSmall,
            color = titleColor,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = message.ifBlank { "No pudimos cargar la información. Revisa tu conexión a internet e intenta nuevamente." },
            style = LexendTypography.bodyMedium,
            color = textColor,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 24.dp)
        )
        
        Spacer(modifier = Modifier.height(32.dp))
        
        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = iconColor,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier.height(52.dp)
        ) {
            Text(
                text = "Reintentar",
                style = LexendTypography.labelLarge,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }
    }
}
