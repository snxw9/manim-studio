package com.manimstudio.app.engine

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "ProotEngine"

/**
 * Manages the proot Linux environment and Manim execution.
 */
class ProotEngine(private val context: Context) {

    val filesDir: File = context.filesDir
    val usrDir: File = File(filesDir, "usr")
    val homeDir: File = File(filesDir, "home/manim")
    val rendersDir: File = File(filesDir, "renders")
    val tmpDir: File = File(filesDir, "tmp")
    val prootBin: File = File(context.applicationInfo.nativeLibraryDir, "libproot.so")
    val pythonBin: File = File(usrDir, "usr/bin/python3")

    /**
     * Run a command inside the proot Linux environment.
     * Returns Pair(exitCode, output)
     */
    suspend fun exec(
        command: List<String>,
        env: Map<String, String> = emptyMap(),
        onOutput: ((String) -> Unit)? = null,
    ): Pair<Int, String> = withContext(Dispatchers.IO) {

        val prootCmd = buildProotCommand(command)

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

    private fun buildProotCommand(command: List<String>): List<String> {
        return listOf(
            prootBin.absolutePath,
            "--kill-on-exit",
            "--link2symlink",
            "-0",
            "-r", usrDir.absolutePath,
            "-b", "/dev",
            "-b", "/proc",
            "-b", "/sys",
            "-b", "${filesDir.absolutePath}/tmp:/tmp",
            "-b", "${rendersDir.absolutePath}:/renders",
            "-w", "/home/manim",
        ) + command
    }

    private fun buildEnvironment(extra: Map<String, String>): Map<String, String> {
        val nativeLibsDir = context.applicationInfo.nativeLibraryDir
        return mapOf(
            "HOME" to "/home/manim",
            "PATH" to "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "TERM" to "xterm-256color",
            "LANG" to "C.UTF-8",
            "LC_ALL" to "C.UTF-8",
            "TMPDIR" to "/tmp",
            "DEBIAN_FRONTEND" to "noninteractive",
            "PROOT_LOADER" to "$nativeLibsDir/libproot-loader.so"
        ) + extra
    }

    fun ensureDirs() {
        listOf(usrDir, homeDir, rendersDir, tmpDir,
               File(filesDir, "tmp")).forEach { it.mkdirs() }
    }
}
