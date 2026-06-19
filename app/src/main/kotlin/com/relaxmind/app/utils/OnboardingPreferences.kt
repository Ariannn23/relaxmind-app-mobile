package com.relaxmind.app.utils

import android.content.Context

/**
 * Simple wrapper around SharedPreferences for the onboarding "seen" flag.
 *
 * Usage:
 *   OnboardingPreferences.markSeen(context)
 *   OnboardingPreferences.isSeen(context) // true after markSeen()
 */
object OnboardingPreferences {

    private const val PREFS_NAME = "relaxmind_prefs"
    private const val KEY_ONBOARDING_SEEN = "onboarding_seen"

    /** Returns true if the user has already completed the onboarding flow. */
    fun isSeen(context: Context): Boolean =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getBoolean(KEY_ONBOARDING_SEEN, false)

    /** Persists the flag so subsequent launches skip the onboarding. */
    fun markSeen(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_ONBOARDING_SEEN, true)
            .apply()
    }
}
