package com.manimstudio.app.data.models

import kotlinx.serialization.Serializable

@Serializable
data class TemplateList(
    val version: String,
    val templates: List<Template>
)

@Serializable
data class Template(
    val id: String,
    val name: String,
    val category: String,
    val description: String,
    val code: String
)
