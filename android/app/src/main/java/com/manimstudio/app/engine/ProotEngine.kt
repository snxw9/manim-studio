package com.manimstudio.app.engine

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

private const val TAG = "ProotEngine"

/**
 * Manages the proot Linux environment and Manim execution.
 * 
 * Architecture:
 *   /data/data/com.manimstudio.app/files/
 *     usr/          ← Alpine Linux rootfs unpacked here
 *       bin/proot   ← proot binary
 *       bin/python3 ← Python 3.11
 *       lib/        ← Cairo, Pango, etc
 *     home/manim/   ← Manim working directory
 *     renders/      ← Output videos saved here
 */
class ProotEngine(private val context: Context) {

    val filesDir: File = context.filesDir
    val usrDir: File = File(filesDir, "usr")
    val homeDir: File = File(filesDir, "home/manim")
    val rendersDir: File = File(filesDir, "renders")
    val prootBin: File = File(filesDir, "usr/bin/proot")
    val pythonBin: File = File(filesDir, "usr/bin/python3")

    val isInstalled: Boolean
        get() = prootBin.exists() && pythonBin.exists()

    /**
     * Run a command inside the proot Linux environment.
     * Returns Pair(exitCode, output)
     */
    suspend fun exec(
        command: List<String>,
        workDir: String = homeDir.absolutePath,
        env: Map<String, String> = emptyMap(),
        onOutput: ((String) -> Unit)? = null,
    ): Pair<Int, String> = withContext(Dispatchers.IO) {

        val prootCmd = buildProotCommand(command, workDir)

        Log.d(TAG, "exec: ${prootCmd.joinToString(" ")}")

        val processEnv = buildEnvironment(env)

        val process = ProcessBuilder(prootCmd)
            .directory(filesDir)
            .redirectErrorStream(true)
            .apply {
                environment().putAll(processEnv)
            }
            .start()

        val output = StringBuilder()
        val reader = process.inputStream.bufferedReader()

        reader.forEachLine { line ->
            output.appendLine(line)
            onOutput?.invoke(line)
            Log.d(TAG, line)
        }

        val exitCode = process.waitFor()
        Pair(exitCode, output.toString())
    }

    private fun buildProotCommand(
        command: List<String>,
        workDir: String,
    ): List<String> {
        return listOf(
            prootBin.absolutePath,
            "--kill-on-exit",
            "--link2symlink",
            "-0",                          // fake root
            "-r", usrDir.absolutePath,     // rootfs
            "-b", "/dev",
            "-b", "/proc",
            "-b", "/sys",
            "-b", "${rendersDir.absolutePath}:/renders",
            "-w", workDir,
        ) + command
    }

    private fun buildEnvironment(extra: Map<String, String>): Map<String, String> {
        return mapOf(
            "HOME" to homeDir.absolutePath,
            "PATH" to "/usr/local/bin:/usr/bin:/bin:/usr/local/sbin:/usr/sbin:/sbin",
            "TERM" to "xterm-256color",
            "LANG" to "en_US.UTF-8",
            "PREFIX" to usrDir.absolutePath,
            "TMPDIR" to "${usrDir.absolutePath}/tmp",
        ) + extra
    }

    fun ensureDirs() {
        listOf(usrDir, homeDir, rendersDir,
               File(usrDir, "tmp")).forEach { it.mkdirs() }
    }
}
