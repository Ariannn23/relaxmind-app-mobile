package com.relaxmind.app.ui.themes

import androidx.compose.ui.graphics.Color

/**
 * Defines the semantic colors for the wellness status across the app.
 */
data class WellnessStatusPalette(
    val primary: Color,
    val secondary: Color,
    val softBackground: Color,
    val ringTrack: Color,
    val textOnPrimary: Color = Color.White
)

val WellnessPaletteVeryLow = WellnessStatusPalette(
    primary = Color(0xFFF97360),
    secondary = Color(0xFFFF8A65),
    softBackground = Color(0xFFFFF1EC),
    ringTrack = Color(0xFFF7B7A3)
)

val WellnessPaletteModerate = WellnessStatusPalette(
    primary = Color(0xFFE9B949),
    secondary = Color(0xFFF2C94C),
    softBackground = Color(0xFFFFF8E1),
    ringTrack = Color(0xFFF6E7A8)
)

val WellnessPaletteGood = WellnessStatusPalette(
    primary = Color(0xFF0F6E56),
    secondary = Color(0xFF1C8C6A),
    softBackground = Color(0xFFEAF8F1),
    ringTrack = Color(0xFFCFEFE3)
)

/**
 * Categorizes a wellness score (0-100) into a palette.
 */
fun getWellnessPalette(score: Int?): WellnessStatusPalette {
    if (score == null) return WellnessPaletteModerate // Fallback

    return when {
        score < 40 -> WellnessPaletteVeryLow
        score < 75 -> WellnessPaletteModerate
        else -> WellnessPaletteGood
    }
}

/**
 * Categorizes a wellness score into a status string.
 */
fun getWellnessStatusLabel(score: Int?): String {
    if (score == null) return "Sin datos"

    return when {
        score < 40 -> "Muy bajo"
        score < 75 -> "Normal"
        else -> "Alto"
    }
}
