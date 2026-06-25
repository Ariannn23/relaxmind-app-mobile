package com.relaxmind.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.relaxmind.app.ui.themes.LexendFontFamily
import com.relaxmind.app.ui.themes.RelaxMindTheme
import com.relaxmind.app.ui.themes.TextPrimary
import com.relaxmind.app.ui.themes.TextSecondary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RelaxTopBar(
    title: String,
    onBackClick: (() -> Unit)? = null,
    actions: @Composable RowScope.() -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontFamily = com.relaxmind.app.ui.themes.LexendFontFamily,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = com.relaxmind.app.ui.themes.TextPrimary
                )
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                Box(modifier = Modifier.padding(start = 8.dp)) {
                    com.relaxmind.app.ui.components.RelaxBackButton(onClick = onBackClick)
                }
            }
        },
        actions = actions
    )
}

@Preview(name = "RelaxTopBar Light", showBackground = true)
@Composable
private fun RelaxTopBarLightPreview() {
    RelaxMindTheme(darkTheme = false) {
        RelaxTopBar(title = "RelaxMind", onBackClick = {})
    }
}

@Preview(name = "RelaxTopBar Dark", showBackground = true)
@Composable
private fun RelaxTopBarDarkPreview() {
    RelaxMindTheme(darkTheme = true) {
        RelaxTopBar(title = "Progreso")
    }
}

@Composable
fun ScreenHeader(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    horizontalPadding: Dp = 24.dp
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = horizontalPadding, vertical = 16.dp)
    ) {
        Text(
            text = title,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 32.sp,
            color = TextPrimary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            fontFamily = LexendFontFamily,
            fontWeight = FontWeight.Normal,
            fontSize = 13.sp,
            color = TextSecondary
        )
    }
}
