package com.manimstudio.app.viewmodel

import android.app.Application
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.manimstudio.app.data.dataStore
import com.manimstudio.app.data.models.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SettingsViewModel(app: Application) : AndroidViewModel(app) {
    private val dataStore = app.dataStore

    private val _settings = MutableStateFlow(AppSettings())
    val settings: StateFlow<AppSettings> = _settings.asStateFlow()

    init {
        viewModelScope.launch {
            // Load saved settings from DataStore on init
            dataStore.data.collect { prefs ->
                _settings.value = AppSettings(
                    themeSettings = ThemeSettings(
                        useMaterialYou = prefs[booleanPreferencesKey("use_material_you")] ?: true,
                        themeColor = ThemeColor.valueOf(
                            prefs[stringPreferencesKey("theme_color")] ?: "ORANGE"
                        ),
                        themeMode = ThemeMode.valueOf(
                            prefs[stringPreferencesKey("theme_mode")] ?: "SYSTEM"
                        ),
                        pureBlackBackground = prefs[booleanPreferencesKey("pure_black")] ?: true,
                        fontOption = FontOption.valueOf(
                            prefs[stringPreferencesKey("font_option")] ?: "INTER"
                        )
                    ),
                    renderQuality = RenderQuality.valueOf(
                        prefs[stringPreferencesKey("quality")] ?: "MID"
                    ),
                    apiProvider = prefs[stringPreferencesKey("provider")] ?: "auto",
                    groqApiKey = prefs[stringPreferencesKey("groq_key")] ?: "",
                    geminiApiKey = prefs[stringPreferencesKey("gemini_key")] ?: "",
                    userName = prefs[stringPreferencesKey("user_name")] ?: "",
                )
            }
        }
    }

    fun updateThemeSettings(themeSettings: ThemeSettings) {
        _settings.value = _settings.value.copy(themeSettings = themeSettings)
        viewModelScope.launch {
            dataStore.edit { prefs ->
                prefs[booleanPreferencesKey("use_material_you")] = themeSettings.useMaterialYou
                prefs[stringPreferencesKey("theme_color")] = themeSettings.themeColor.name
                prefs[stringPreferencesKey("theme_mode")] = themeSettings.themeMode.name
                prefs[booleanPreferencesKey("pure_black")] = themeSettings.pureBlackBackground
                prefs[stringPreferencesKey("font_option")] = themeSettings.fontOption.name
            }
        }
    }

    fun updateQuality(quality: RenderQuality) = save("quality", quality.name) {
        _settings.value = _settings.value.copy(renderQuality = quality)
    }

    fun updateProvider(provider: String) = save("provider", provider) {
        _settings.value = _settings.value.copy(apiProvider = provider)
    }

    fun updateGroqKey(key: String) = save("groq_key", key) {
        _settings.value = _settings.value.copy(groqApiKey = key)
    }

    fun updateGeminiKey(key: String) = save("gemini_key", key) {
        _settings.value = _settings.value.copy(geminiApiKey = key)
    }

    fun updateUserName(name: String) = save("user_name", name) {
        _settings.value = _settings.value.copy(userName = name)
    }

    private fun save(key: String, value: String, update: () -> Unit) {
        update()
        viewModelScope.launch {
            dataStore.edit { it[stringPreferencesKey(key)] = value }
        }
    }
}
