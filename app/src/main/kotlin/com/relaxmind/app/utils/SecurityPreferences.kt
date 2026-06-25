package com.relaxmind.app.utils

import android.content.Context

object SecurityPreferences {
    private const val PREFS_NAME = "relaxmind_security_prefs"
    private const val KEY_KEEP_LOGGED_IN = "keep_logged_in"
    private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

    fun setKeepLoggedIn(context: Context, keepLoggedIn: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_KEEP_LOGGED_IN, keepLoggedIn).apply()
    }

    fun isKeepLoggedIn(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        // Default to true for existing users to avoid unexpected logouts
        return prefs.getBoolean(KEY_KEEP_LOGGED_IN, true)
    }

    fun setBiometricEnabled(context: Context, enabled: Boolean) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    fun isBiometricEnabled(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, false)
    }
}
