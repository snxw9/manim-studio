package com.manimstudio.app.engine

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProotEngine(val context: Context) {

    private val base = "/data/data/${context.packageName}"

    val filesDir    = File("$base/files")
    val rootfsDir   = File("$base/files/rootfs")
    val homeDir     = File("$base/files/home/manim")
    val rendersDir  = File("$base/files/renders")
    private val tmpDir = File("$base/files/tmp")

    val pythonBin = File(rootfsDir, "usr/bin/python3")
    val ffmpegBin = File(rootfsDir, "usr/local/bin/ffmpeg")
    val latexBin  = File(rootfsDir, "opt/TinyTeX/bin/aarch64-linux/latex")

    val prootBin    = File(context.applicationInfo.nativeLibraryDir, "libproot.so")
    // Pre-placed loader — proot uses this instead of extracting to tmp dir
    // This completely eliminates the chmod/PROOT_TMP_DIR issue
    val prootLoader = File(context.applicationInfo.nativeLibraryDir, "libproot-loader.so")

    fun ensureDirs() {
        listOf(rootfsDir, homeDir, rendersDir, tmpDir).forEach { it.mkdirs() }
    }

    fun buildProotCommand(
        command: List<String>,
        workDir: String = "/home/manim",
    ): List<String> = listOf(
        prootBin.absolutePath,
        "--kill-on-exit",
        "--link2symlink",
        "-0",
        "--kernel-release=4.9.0-faked",
        "-r", rootfsDir.absolutePath,
        "-b", "/dev",
        "-b", "/dev/urandom:/dev/random",
        "-b", "/proc",
        "-b", "/sys",
        "-b", "${tmpDir.absolutePath}:/tmp",
        "-b", "${rendersDir.absolutePath}:/renders",
        "-b", "${homeDir.absolutePath}:/home/manim",
        "-w", workDir,
    ) + command

    fun buildEnvironment(
        extra: Map<String, String> = emptyMap(),
    ): Map<String, String> = mapOf(
        "HOME"                    to "/home/manim",
        "PATH"                    to "/opt/TinyTeX/bin/aarch64-linux:/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
        "TERM"                    to "xterm-256color",
        "LANG"                    to "C.UTF-8",
        "LC_ALL"                  to "C.UTF-8",
        "TMPDIR"                  to "/tmp",
        "DEBIAN_FRONTEND"         to "noninteractive",
        "PROOT_LOADER"            to prootLoader.absolutePath,
        "LD_LIBRARY_PATH"         to context.applicationInfo.nativeLibraryDir,
        "PROOT_NO_SECCOMP"        to "1",
        // Prevent Python from writing .pyc files — reduces filesystem syscalls
        "PYTHONDONTWRITEBYTECODE" to "1",
        // Disable user site packages — simplifies import path resolution
        "PYTHONNOUSERSITE"        to "1",
        // Disable hash randomization — removes a getrandom() syscall on startup
        "PYTHONHASHSEED"          to "0",
    ) + extra

    suspend fun exec(
        command: List<String>,
        workDir: String = "/home/manim",
        env: Map<String, String> = emptyMap(),
        onOutput: (String) -> Unit = {},
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        ensureDirs()

        android.util.Log.d("ProotEngine", "prootBin exists: ${prootBin.exists()}")
        android.util.Log.d("ProotEngine", "prootLoader exists: ${prootLoader.exists()}")
        android.util.Log.d("ProotEngine", "prootLoader canExec: ${prootLoader.canExecute()}")

        val fullCmd = buildProotCommand(command, workDir)
        android.util.Log.d("ProotEngine", "CMD: ${fullCmd.joinToString(" ")}")

        val pb = ProcessBuilder(fullCmd).apply {
            environment().clear()
            environment().putAll(buildEnvironment(env))
            redirectErrorStream(true)
        }
        val process = pb.start()
        val output = StringBuilder()
        process.inputStream.bufferedReader().use { reader ->
            reader.forEachLine { line ->
                output.appendLine(line)
                onOutput(line)
                android.util.Log.d("ProotEngine", "out: $line")
            }
        }
        val exit = process.waitFor()
        android.util.Log.d("ProotEngine", "exit: $exit")
        Pair(exit, output.toString())
    }
}
