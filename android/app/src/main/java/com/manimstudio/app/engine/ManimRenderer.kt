package com.manimstudio.app.engine

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID

private const val TAG = "ManimRenderer"

data class RenderResult(
    val success: Boolean,
    val videoFile: File? = null,
    val errorMessage: String? = null,
    val renderTimeMs: Long = 0,
)

class ManimRenderer(
    private val context: Context,
    private val engine: ProotEngine,
) {
    suspend fun render(
        code: String,
        quality: String = "480p",
        onProgress: ((String) -> Unit)? = null,
    ): RenderResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        // Extract class name from code
        val classNameMatch = Regex("""class\s+(\w+)\s*\(""").find(code)
        if (classNameMatch == null) {
            return@withContext RenderResult(
                success = false,
                errorMessage = "No Scene class found in code"
            )
        }
        val className = classNameMatch.groupValues[1]
        Log.d(TAG, "Rendering class: $className at $quality")

        // Write scene file
        val sceneId = UUID.randomUUID().toString().take(8)
        val sceneFile = File(engine.homeDir, "scene_${sceneId}.py")
        sceneFile.writeText(code, Charsets.UTF_8)

        val qualityFlag = when (quality) {
            "480p" -> "-ql"
            "720p" -> "-qm"
            "1080p" -> "-qh"
            else -> "-ql"
        }

        // Build manim command
        val cmd = listOf(
            "/usr/bin/python3", "-m", "manim",
            qualityFlag,
            "--fps", "30",
            "--format", "mp4",
            "--media_dir", "/renders",
            "--disable_caching",
            "--progress_bar", "none",
            sceneFile.absolutePath,
            className,
        )

        try {
            val (exitCode, output) = engine.exec(
                cmd,
                workDir = engine.homeDir.absolutePath,
                onOutput = onProgress,
            )

            sceneFile.delete()

            if (exitCode != 0) {
                return@withContext RenderResult(
                    success = false,
                    errorMessage = "Manim error:\n${output.takeLast(2000)}",
                    renderTimeMs = System.currentTimeMillis() - startTime,
                )
            }

            // Find output video
            val videoFile = findOutputVideo(className)
                ?: return@withContext RenderResult(
                    success = false,
                    errorMessage = "Render completed but no video file found.\n$output",
                    renderTimeMs = System.currentTimeMillis() - startTime,
                )

            // Copy to a clean named location
            val outputName = "${className}_${quality}.mp4"
            val finalFile = File(engine.rendersDir, outputName)
            videoFile.copyTo(finalFile, overwrite = true)

            RenderResult(
                success = true,
                videoFile = finalFile,
                renderTimeMs = System.currentTimeMillis() - startTime,
            )

        } catch (e: Exception) {
            sceneFile.delete()
            RenderResult(
                success = false,
                errorMessage = "Unexpected error: ${e.message}",
                renderTimeMs = System.currentTimeMillis() - startTime,
            )
        }
    }

    private fun findOutputVideo(className: String): File? {
        return engine.rendersDir
            .walkTopDown()
            .filter { it.extension == "mp4" && it.name.contains(className) }
            .maxByOrNull { it.lastModified() }
    }
}
