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

    suspend fun generate(
        context: Context,
        prompt: String,
        model: String = "llama-3.3-70b-versatile"
    ): String = withContext(Dispatchers.IO) {
        val apiKey = ApiKeyManager.getGroqKey(context)
        if (apiKey.isBlank()) throw IllegalStateException("No Groq API key set")

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

        // Strip markdown fences
        if (code.startsWith("```python")) code = code.removePrefix("```python")
        if (code.startsWith("```")) code = code.removePrefix("```")
        if (code.endsWith("```")) code = code.removeSuffix("```")

        code.trim()
    }

    suspend fun testKey(context: Context): Boolean = withContext(Dispatchers.IO) {
        try {
            generate(context, "Write: from manim import *", "llama-3.1-8b-instant")
            true
        } catch (e: Exception) {
            false
        }
    }
}
