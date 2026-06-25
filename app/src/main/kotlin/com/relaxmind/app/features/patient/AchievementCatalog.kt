package com.relaxmind.app.features.patient

data class AchievementCatalogItem(
    val key: String,
    val title: String,
    val condition: String,
    val defaultIconUrl: String,
    val type: String
)

object AchievementCatalog {
    val items = listOf(
        AchievementCatalogItem("first_checkin", "Primeros pasos", "Primer check-in completado", "https://cdn-icons-png.flaticon.com/512/825/825590.png", "constancy"),
        AchievementCatalogItem("streak_3", "3 días seguidos", "Racha de 3 días", "https://cdn-icons-png.flaticon.com/512/785/785116.png", "constancy"),
        AchievementCatalogItem("streak_7", "7 días de calma", "Racha de 7 días", "https://cdn-icons-png.flaticon.com/512/785/785116.png", "constancy"),
        AchievementCatalogItem("streak_14", "Dos semanas imparable", "Racha de 14 días", "https://cdn-icons-png.flaticon.com/512/785/785116.png", "constancy"),
        AchievementCatalogItem("streak_30", "30 días seguidos", "Racha de 30 días", "https://cdn-icons-png.flaticon.com/512/3112/3112946.png", "constancy"),
        AchievementCatalogItem("first_meditation", "Enfoque total", "Primera meditación completada", "https://cdn-icons-png.flaticon.com/512/2913/2913520.png", "meditation"),
        AchievementCatalogItem("meditations_10", "Mente en calma", "10 meditaciones completadas", "https://cdn-icons-png.flaticon.com/512/414/414927.png", "meditation"),
        AchievementCatalogItem("first_diary", "Mi historia", "Primera entrada de diario", "https://cdn-icons-png.flaticon.com/512/3068/3068327.png", "diary"),
        AchievementCatalogItem("diary_7", "Una semana de notas", "7 entradas de diario", "https://cdn-icons-png.flaticon.com/512/3068/3068327.png", "diary"),
        AchievementCatalogItem("score_80", "Bienestar alto", "Check-in con 80+ puntos", "https://cdn-icons-png.flaticon.com/512/1828/1828884.png", "wellness"),
        AchievementCatalogItem("score_100", "Día perfecto", "Check-in con 100 puntos", "https://cdn-icons-png.flaticon.com/512/616/616489.png", "wellness"),
        AchievementCatalogItem("lumi_first", "Hola Lumi", "Primera conversación con Lumi", "https://cdn-icons-png.flaticon.com/512/134/134914.png", "ai")
    )

    fun getByKey(key: String): AchievementCatalogItem? {
        return items.find { it.key == key }
    }
}
