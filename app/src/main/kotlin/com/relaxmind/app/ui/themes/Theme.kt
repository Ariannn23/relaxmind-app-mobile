package com.relaxmind.app.ui.themes

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// Light palette for the default patient-centered experience.
private val LightColorScheme = lightColorScheme(
    primary = PatientGreen,
    onPrimary = Color.White,
    secondary = CaregiverIndigo,
    onSecondary = Color.White,
    tertiary = SOSCoral,
    onTertiary = Color.White,
    background = BackgroundLight,
    onBackground = Color(0xFF1F2933),
    surface = SurfaceLight,
    onSurface = Color(0xFF1F2933),
    error = SOSCoral,
    onError = Color.White
)

// Dark palette keeps contrast high while preserving the RelaxMind identity.
private val DarkColorScheme = darkColorScheme(
    primary = PatientGreenLight,
    onPrimary = Color(0xFF062E24),
    secondary = CaregiverIndigo,
    onSecondary = Color.White,
    tertiary = SOSCoral,
    onTertiary = Color.White,
    background = BackgroundDark,
    onBackground = TextDarkPrimary,
    surface = SurfaceDark,
    onSurface = TextDarkPrimary,
    error = SOSCoral,
    onError = Color.White
)

// App theme entry point; darkTheme can be driven by settings in real time.
@Composable
fun RelaxMindTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = RelaxMindTypography,
        content = content
    )
}
