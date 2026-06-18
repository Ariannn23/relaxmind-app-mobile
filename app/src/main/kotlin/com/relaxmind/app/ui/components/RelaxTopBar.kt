package com.relaxmind.app.ui.components

import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.relaxmind.app.ui.themes.RelaxMindTheme

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
                style = MaterialTheme.typography.headlineSmall
            )
        },
        navigationIcon = {
            if (onBackClick != null) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Volver"
                    )
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
