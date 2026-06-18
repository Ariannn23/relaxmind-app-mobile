package com.relaxmind.app.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.relaxmind.app.ui.themes.RelaxMindTheme

@Composable
fun RelaxCard(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    elevation: Dp = 2.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    val clickableModifier = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }

    Card(
        modifier = clickableModifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Column(modifier = Modifier.padding(16.dp), content = content)
    }
}

@Preview(name = "RelaxCard Light", showBackground = true)
@Composable
private fun RelaxCardLightPreview() {
    RelaxMindTheme(darkTheme = false) {
        RelaxCard {
            Text(text = "Meta de hoy", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Respiracion 4-7-8", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(name = "RelaxCard Dark", showBackground = true)
@Composable
private fun RelaxCardDarkPreview() {
    RelaxMindTheme(darkTheme = true) {
        RelaxCard {
            Text(text = "Proximo recordatorio", style = MaterialTheme.typography.headlineSmall)
            Text(text = "Terapia a las 6:00 PM", style = MaterialTheme.typography.bodyMedium)
        }
    }
}
