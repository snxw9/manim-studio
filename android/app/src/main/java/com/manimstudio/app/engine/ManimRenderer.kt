package com.manimstudio.app.engine

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class RenderResult(
    val success: Boolean,
    val videoFile: File? = null,
    val errorMessage: String? = null,
    val renderTimeMs: Long = 0,
)

class ManimRenderer(
    private val engine: ProotEngine,
) {
    suspend fun render(
        code: String,
        quality: String = "720p",
        onProgress: (String) -> Unit = {},
    ): RenderResult = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        // Validate code before sending to proot
        val classMatch =
            Regex("""class\s+(\w+)\s*\(""").find(code) ?: return@withContext RenderResult(
                success = false,
                errorMessage = "No Scene class found in code. " +
                        "Every Manim animation needs a class like:\n" +
                        "class MyScene(Scene):",
            )
        val className = classMatch.groupValues[1]

        val qualityFlag = when (quality) {
            "480p" -> "-ql"
            "720p" -> "-qm"
            "1080p" -> "-qh"
            else -> "-qm"
        }

        // Write scene file to the shared home directory
        val sceneId = java.util.UUID.randomUUID().toString().take(8)
        val sceneFile = File(engine.homeDir, "scene_${sceneId}.py")
        try {
            // Strip control characters before writing
            val cleanCode = code.replace(Regex("[\\x00-\\x08\\x0B\\x0C\\x0E-\\x1F]"), "")
            sceneFile.writeText(cleanCode, Charsets.UTF_8)

            val cmd = listOf(
                "/usr/bin/python3", "-m", "manim",
                qualityFlag,
                "--fps", "30",
                "--format", "mp4",
                "--media_dir", "/renders",
                "--disable_caching",
                "--progress_bar", "none",
                "/home/manim/${sceneFile.name}",
                className,
            )

            val (exitCode, output) = engine.exec(
                command = cmd,
                onOutput = { line ->
                    onProgress(line)
                },
            )

            sceneFile.delete()

            if (exitCode != 0) {
                return@withContext RenderResult(
                    success = false,
                    errorMessage = buildFriendlyError(output),
                    renderTimeMs = System.currentTimeMillis() - startTime,
                )
            }

            // Find the output file
            val videoFile = findOutputVideo(className)
                ?: return@withContext RenderResult(
                    success = false,
                    errorMessage = "Render completed but no video file was produced.\n$output",
                    renderTimeMs = System.currentTimeMillis() - startTime,
                )

            // Copy to a friendly name
            val friendlyName = "${className}_${quality}.mp4"
            val finalFile = File(engine.rendersDir, friendlyName)
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

    private fun buildFriendlyError(output: String): String {
        return when {
            output.contains("ModuleNotFoundError") || output.contains("ImportError") ->
                "A Python package is missing from the engine. " +
                "Try reinstalling the engine from Settings."
            output.contains("latex failed") || output.contains("LaTeX Error") ->
                "LaTeX failed to render the math equation. " +
                "Check your MathTex syntax — use raw strings: MathTex(r\"\\alpha\")"
            output.contains("AttributeError") && output.contains("has no attribute") -> {
                val match = Regex("""'(\w+)' object has no attribute '(\w+)'""").find(output)
                if (match != null) {
                    "The method '${match.groupValues[2]}' doesn't exist on " +
                    "'${match.groupValues[1]}'. Check the Manim documentation."
                } else "A Manim object method was used incorrectly.\n$output"
            }
            output.contains("NameError") -> {
                val match = Regex("""name '(\w+)' is not defined""").find(output)
                "Unknown name '${match?.groupValues?.get(1) ?: "unknown"}'. " +
                "This Manim class or function doesn't exist in this version."
            }
            output.contains("SyntaxError") ->
                "Python syntax error in the code. Check for missing colons, " +
                "brackets, or indentation problems."
            else -> "Manim error:\n${output.takeLast(2000)}"
        }
    }

    private fun findOutputVideo(className: String): File? {
        return engine.rendersDir
            .walkTopDown()
            .filter { it.extension == "mp4" && it.name.contains(className) }
            .maxByOrNull { it.lastModified() }
    }
}
