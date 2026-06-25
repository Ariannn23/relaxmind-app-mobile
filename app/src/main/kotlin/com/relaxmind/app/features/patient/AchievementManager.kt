package com.relaxmind.app.features.patient

import com.relaxmind.app.data.model.UserAchievement
import com.relaxmind.app.data.remote.FirestoreRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.time.LocalDate
import java.util.UUID

object AchievementManager {
    private val _achievementUnlockedEvent = MutableSharedFlow<UserAchievement>()
    val achievementUnlockedEvent = _achievementUnlockedEvent.asSharedFlow()

    suspend fun checkAndUnlock(
        key: String,
        userId: String,
        unlockedAchievements: List<UserAchievement>,
        firestoreRepository: FirestoreRepository,
        onUnlocked: ((UserAchievement) -> Unit)? = null
    ) {
        if (unlockedAchievements.none { it.achievementKey == key }) {
            val catalogItem = AchievementCatalog.getByKey(key) ?: return
            val userAch = UserAchievement(
                id = UUID.randomUUID().toString(),
                patientId = userId,
                achievementKey = key,
                type = catalogItem.type,
                title = catalogItem.title,
                description = catalogItem.condition,
                iconUrl = catalogItem.defaultIconUrl,
                unlockedAt = LocalDate.now().toString()
            )
            firestoreRepository.unlockAchievement(userAch)
            _achievementUnlockedEvent.emit(userAch)
            onUnlocked?.invoke(userAch)
        }
    }
}
