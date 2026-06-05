package com.mountaincrab.bookstore.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

/** Persists user preferences. Today just the selected theme name. */
class UserPreferencesRepository(private val context: Context) {

    val themeName: Flow<String?> = context.dataStore.data.map { it[THEME_KEY] }

    suspend fun setThemeName(name: String) {
        context.dataStore.edit { it[THEME_KEY] = name }
    }

    private companion object {
        val THEME_KEY = stringPreferencesKey("app_theme")
    }
}
