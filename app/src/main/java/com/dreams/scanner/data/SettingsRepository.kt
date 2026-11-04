package com.dreams.scanner.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import java.io.IOException

enum class ThemeMode(val key: String) {
    SYSTEM("system"), LIGHT("light"), DARK("dark");

    companion object {
        fun fromKey(key: String?): ThemeMode = entries.firstOrNull { it.key == key } ?: SYSTEM
    }
}

enum class SortOrder(val key: String) {
    DATE("date"), NAME("name");

    companion object {
        fun fromKey(key: String?): SortOrder = entries.firstOrNull { it.key == key } ?: DATE
    }
}

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class SettingsRepository(context: Context) {
    private val store = context.applicationContext.settingsDataStore

    private object Keys {
        val THEME = stringPreferencesKey("theme_mode")
        val SORT = stringPreferencesKey("sort_order")
    }

    private val data: Flow<Preferences> = store.data.catch { e ->
        if (e is IOException) emit(emptyPreferences()) else throw e
    }

    val themeMode: Flow<ThemeMode> = data.map { ThemeMode.fromKey(it[Keys.THEME]) }.distinctUntilChanged()
    val sortOrder: Flow<SortOrder> = data.map { SortOrder.fromKey(it[Keys.SORT]) }.distinctUntilChanged()

    suspend fun setThemeMode(mode: ThemeMode) {
        store.edit { it[Keys.THEME] = mode.key }
    }

    suspend fun setSortOrder(order: SortOrder) {
        store.edit { it[Keys.SORT] = order.key }
    }
}
