package com.lawrence.justsitting

import android.content.Context
import android.content.SharedPreferences

object ProgressManager {
    private const val PREFS_NAME = "wisdom_progress"

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Salvăm titlul învățăturii ca fiind citită
    fun markAsRead(context: Context, title: String) {
        getPrefs(context).edit().putBoolean(title, true).apply()
    }

    // Verificăm dacă o învățătură a fost citită
    fun isRead(context: Context, title: String): Boolean {
        return getPrefs(context).getBoolean(title, false)
    }

    // Șterge tot istoricul de citire
    fun resetAllProgress(context: Context) {
        getPrefs(context).edit().clear().apply()
    }
}