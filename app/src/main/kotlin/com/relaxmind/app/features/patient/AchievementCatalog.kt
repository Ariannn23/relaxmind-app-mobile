package com.relaxmind.app.features.patient

import com.relaxmind.app.R

data class AchievementCatalogItem(
    val key: String,
    val title: String,
    val condition: String,
    val iconResId: Int,
    val type: String
)

object AchievementCatalog {
    val items = listOf(
        AchievementCatalogItem("first_checkin", "Primeros pasos", "Primer check-in completado", R.drawable.logro_primeros_pasos, "constancy"),
        AchievementCatalogItem("streak_3", "3 días seguidos", "Racha de 3 días", R.drawable.logro_3_dias, "constancy"),
        AchievementCatalogItem("streak_7", "7 días de calma", "Racha de 7 días", R.drawable.logro_7_dias, "constancy"),
        AchievementCatalogItem("streak_14", "Dos semanas imparable", "Racha de 14 días", R.drawable.logro_2_semanas, "constancy"),
        AchievementCatalogItem("streak_30", "30 días seguidos", "Racha de 30 días", R.drawable.logro_30_dias, "constancy"),
        AchievementCatalogItem("first_meditation", "Enfoque total", "Primera meditación completada", R.drawable.logro_enfoque_total, "meditation"),
        AchievementCatalogItem("meditations_10", "Mente en calma", "10 meditaciones completadas", R.drawable.logro_mente_calma, "meditation"),
        AchievementCatalogItem("first_diary", "Mi historia", "Primera entrada de diario", R.drawable.logro_mi_historia, "diary"),
        AchievementCatalogItem("diary_7", "Una semana de notas", "7 entradas de diario", R.drawable.logro_semana_notas, "diary"),
        AchievementCatalogItem("score_80", "Bienestar alto", "Check-in con 80+ puntos", R.drawable.logro_bienestar_alto, "wellness"),
        AchievementCatalogItem("score_100", "Día perfecto", "Check-in con 100 puntos", R.drawable.logro_dia_perfecto, "wellness"),
        AchievementCatalogItem("lumi_first", "Hola Lumi", "Primera conversación con Lumi", R.drawable.logro_hola_lumi, "ai")
    )

    fun getByKey(key: String): AchievementCatalogItem? {
        return items.find { it.key == key }
    }
}
