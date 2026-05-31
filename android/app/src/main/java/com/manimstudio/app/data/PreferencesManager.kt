package com.manimstudio.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Creates the DataStore singleton at the top level
val Context.dataStore by preferencesDataStore(name = "settings")

object PreferencesManager {
    private val THEME_KEY = stringPreferencesKey("theme_mode")

    fun getTheme(context: Context): Flow<String> {
        return context.dataStore.data.map { preferences ->
            preferences[THEME_KEY] ?: "System"
        }
    }

    suspend fun setTheme(context: Context, theme: String) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = theme
        }
    }
}
