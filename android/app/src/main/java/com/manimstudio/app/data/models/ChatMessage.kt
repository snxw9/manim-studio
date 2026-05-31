package com.manimstudio.app.data.models

import android.graphics.Bitmap
import java.io.File
import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val type: MessageType,
    val content: String,
    val videoFile: File? = null,
    val thumbnailBitmap: Bitmap? = null,
    val renderTimeMs: Long? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val isError: Boolean = false,
)

enum class MessageType {
    USER_PROMPT,    // user typed this
    SYSTEM_STATUS,  // "Generating code...", "Rendering..."
    VIDEO_RESULT,   // MP4 output
    ERROR,          // something failed
    CODE_PREVIEW,   // expandable generated code block
}
