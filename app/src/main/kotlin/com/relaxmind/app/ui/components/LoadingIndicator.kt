package com.relaxmind.app.ui.components

import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.relaxmind.app.ui.themes.RelaxMindTheme

@Composable
fun LoadingIndicator(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        modifier = modifier,
        color = MaterialTheme.colorScheme.primary
    )
}

@Preview(name = "LoadingIndicator Light", showBackground = true)
@Composable
private fun LoadingIndicatorLightPreview() {
    RelaxMindTheme(darkTheme = false) {
        LoadingIndicator()
    }
}

@Preview(name = "LoadingIndicator Dark", showBackground = true, backgroundColor = 0xFF1A1A2E)
@Composable
private fun LoadingIndicatorDarkPreview() {
    RelaxMindTheme(darkTheme = true) {
        LoadingIndicator()
    }
}
