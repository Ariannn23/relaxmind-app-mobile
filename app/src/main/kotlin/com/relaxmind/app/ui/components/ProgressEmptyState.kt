package com.relaxmind.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

@Composable
fun ProgressEmptyState(
    patientName: String? = null,
    modifier: Modifier = Modifier
) {
    val title = if (patientName.isNullOrBlank()) {
        "Aún no hay registros"
    } else {
        "Sin actividad reciente"
    }

    val description = if (patientName.isNullOrBlank()) {
        "Aún no has registrado ninguna actividad en tu diario. Tus registros de bienestar aparecerán aquí para que puedas ver tu progreso."
    } else {
        "${patientName.split(" ").first()} aún no ha registrado ninguna actividad en su diario."
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp, horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color(0xFFF3F4F6)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.MenuBook,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = Color(0xFF9CA3AF)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = title,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        Text(
            text = description,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 14.sp,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
