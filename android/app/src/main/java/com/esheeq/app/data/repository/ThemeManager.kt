package com.esheeq.app.data.repository

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AppThemeMode(val storageValue: String) {
    SYSTEM("system"),
    LIGHT("light"),
    DARK("dark");

    companion object {
        fun fromStorage(value: String?): AppThemeMode =
            entries.firstOrNull { it.storageValue == value } ?: SYSTEM
    }
}

/**
 * Manages persisted user theme preference (System / Light / Dark).
 */
object ThemeManager {

    private const val PREFS_NAME = "esheeq_prefs"
    private const val KEY_THEME_MODE = "app_theme_mode"

    private var prefs: SharedPreferences? = null

    private val _themeModeState = MutableStateFlow(AppThemeMode.SYSTEM)
    val themeModeState: StateFlow<AppThemeMode> = _themeModeState.asStateFlow()

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
        val saved = prefs?.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.storageValue)
        _themeModeState.value = AppThemeMode.fromStorage(saved)
    }

    fun setThemeMode(context: Context, mode: AppThemeMode) {
        if (prefs == null) init(context)
        prefs?.edit()?.putString(KEY_THEME_MODE, mode.storageValue)?.apply()
        _themeModeState.value = mode
    }
}
