package com.manimstudio.app.data

import android.content.Context
import com.manimstudio.app.data.models.Template
import com.manimstudio.app.data.models.TemplateList
import kotlinx.serialization.json.Json
import java.io.IOException

class TemplateRepository(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }

    fun getTemplates(): List<Template> {
        return try {
            val jsonString = context.assets.open("templates.json").bufferedReader().use { it.readText() }
            val templateList = json.decodeFromString<TemplateList>(jsonString)
            templateList.templates
        } catch (e: IOException) {
            e.printStackTrace()
            emptyList()
        } catch (e: Exception) {
            e.printStackTrace()
            emptyList()
        }
    }

    fun getTemplateById(id: String): Template? {
        return getTemplates().find { it.id == id }
    }
}
