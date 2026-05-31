package com.manimstudio.app.data.models

data class SuggestionCard(
    val title: String,
    val description: String,
    val prompt: String,
)

data class RecentChat(
    val id: String,
    val title: String,
)
