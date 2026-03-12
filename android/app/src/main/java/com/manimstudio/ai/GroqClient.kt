package com.manimstudio.ai

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GroqClient {

    private const val ENGINE_URL = "http://10.0.2.2:8000" // Default for Android Emulator to host PC

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val SYSTEM_PROMPT = """
        Manim Community Edition v0.18 expert. Output ONLY valid Python code.
        - from manim import *
        - class NAME(Scene): def construct(self):
        - Raw strings for LaTeX: r"\alpha" never "\alpha"
        - Sector(radius=r) never Sector(outer_radius=r)
        - No explanation, no markdown fences
    """.trimIndent()

    suspend fun generate(context: Context, prompt: String): String {
        val userKey = ApiKeyManager.getGroqKey(context)
        
        return if (userKey.isNotBlank()) {
            // User has their own key — call Groq directly (fastest, no middleman)
            generateDirect(prompt, userKey)
        } else {
            // No user key — call the hosted engine (uses server's free quota)
            generateViaEngine(context, prompt)
        }
    }

    private suspend fun generateDirect(
        prompt: String, 
        apiKey: String,
        model: String = "llama-3.3-70b-versatile"
    ): String = withContext(Dispatchers.IO) {
        val body = JSONObject().apply {
            put("model", model)
            put("temperature", 0.2)
            put("max_tokens", 4096)
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", SYSTEM_PROMPT)
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", prompt)
                })
            })
        }

        val request = Request.Builder()
            .url("https://api.groq.com/openai/v1/chat/completions")
            .header("Authorization", "Bearer $apiKey")
            .header("Content-Type", "application/json")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() 
            ?: throw Exception("Empty response from Groq")

        if (!response.isSuccessful) {
            val error = JSONObject(responseBody)
            val msg = error.optJSONObject("error")?.optString("message") ?: responseBody
            throw Exception("Groq error ${response.code}: $msg")
        }

        var code = JSONObject(responseBody)
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
            .trim()

        cleanCode(code)
    }

    private suspend fun generateViaEngine(context: Context, prompt: String): String = withContext(Dispatchers.IO) {
        // Forward other keys if they exist
        val userKeys = JSONObject().apply {
            val gemini = ApiKeyManager.getGeminiKey(context)
            if (gemini.isNotBlank()) put("gemini", gemini)
            val openai = ApiKeyManager.getOpenAIKey(context)
            if (openai.isNotBlank()) put("openai", openai)
        }

        val body = JSONObject().apply {
            put("prompt", prompt)
            put("user_keys", userKeys)
        }

        val request = Request.Builder()
            .url("$ENGINE_URL/generate")
            .post(body.toString().toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string() 
            ?: throw Exception("Empty response from engine")

        if (!response.isSuccessful) {
            val error = JSONObject(responseBody)
            val detail = error.opt("detail")
            val msg = if (detail is JSONObject) detail.optString("message") else detail?.toString() ?: responseBody
            throw Exception(msg)
        }

        val data = JSONObject(responseBody)
        data.getString("code")
    }

    suspend fun getPoolStatus(): JSONObject? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url("$ENGINE_URL/pool/status")
                .get()
                .build()
            val response = client.newCall(request).execute()
            val body = response.body?.string() ?: return@withContext null
            if (response.isSuccessful) JSONObject(body) else null
        } catch (e: Exception) {
            null
        }
    }

    private fun cleanCode(code: String): String {
        var cleaned = code.trim()
        if (cleaned.startsWith("```python")) cleaned = cleaned.removePrefix("```python")
        if (cleaned.startsWith("```")) cleaned = cleaned.removePrefix("```")
        if (cleaned.endsWith("```")) cleaned = cleaned.removeSuffix("```")
        return cleaned.trim()
    }

    suspend fun testKey(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            val key = ApiKeyManager.getGroqKey(context)
            if (key.isBlank()) return@withContext false
            generateDirect("Say OK", key, "llama-3.1-8b-instant")
            true
        } catch (e: Exception) {
            false
        }
    }
}
