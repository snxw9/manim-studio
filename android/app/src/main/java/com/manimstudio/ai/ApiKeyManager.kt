package com.manimstudio.ai

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

object ApiKeyManager {

    private fun getPrefs(context: Context) = EncryptedSharedPreferences.create(
        context,
        "api_keys",
        MasterKey.Builder(context).setKeyScheme(MasterKey.KeyScheme.AES256_GCM).build(),
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun setGroqKey(context: Context, key: String) =
        getPrefs(context).edit().putString("groq_api_key", key).apply()

    fun getGroqKey(context: Context): String =
        getPrefs(context).getString("groq_api_key", "") ?: ""

    fun setGeminiKey(context: Context, key: String) =
        getPrefs(context).edit().putString("gemini_api_key", key).apply()

    fun getGeminiKey(context: Context): String =
        getPrefs(context).getString("gemini_api_key", "") ?: ""

    fun setOpenAIKey(context: Context, key: String) =
        getPrefs(context).edit().putString("openai_api_key", key).apply()

    fun getOpenAIKey(context: Context): String =
        getPrefs(context).getString("openai_api_key", "") ?: ""

    fun hasAnyKey(context: Context): Boolean {
        return getGroqKey(context).isNotBlank() || 
               getGeminiKey(context).isNotBlank() || 
               getOpenAIKey(context).isNotBlank()
    }
}
