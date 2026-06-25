package com.manimstudio.app.engine

import android.content.Context
import java.io.File

class ProotEngine(private val context: Context) {

    val filesDir: File = context.filesDir
    val rootfsDir: File = File(filesDir, "rootfs")
    val homeDir: File = File(filesDir, "home/manim")
    val rendersDir: File = File(filesDir, "renders")

    val pythonBin: File = File(rootfsDir, "usr/bin/python3")

    // proot binary — must be in nativeLibraryDir (exec-allowed by Android)
    val prootBin: File = File(
        context.applicationInfo.nativeLibraryDir, "libproot.so"
    )

    fun ensureDirs() {
        listOf(rootfsDir, homeDir, rendersDir,
               File(filesDir, "tmp")).forEach { it.mkdirs() }
    }


    fun buildProotCommand(
        command: List<String>,
        workDir: String = "/home/manim",
    ): List<String> {
        val usrLib   = File(rootfsDir, "usr/lib").absolutePath
        val usrBin   = File(rootfsDir, "usr/bin").absolutePath
        val usrSbin  = File(rootfsDir, "usr/sbin").absolutePath

        return listOf(
            prootBin.absolutePath,
            "--kill-on-exit",
            "--link2symlink",
            "-0",
            "-r", rootfsDir.absolutePath,

            // Debian usrmerge: bind real dirs so proot finds ELF interpreter
            "-b", "$usrLib:/lib",
            "-b", "$usrBin:/bin",
            "-b", "$usrSbin:/sbin",

            // System
            "-b", "/dev",
            "-b", "/proc",
            "-b", "/sys",

            // App directories
            "-b", "${File(filesDir, "tmp").absolutePath}:/tmp",
            "-b", "${rendersDir.absolutePath}:/renders",
            "-b", "${homeDir.absolutePath}:/home/manim",

            "-w", workDir,
        ) + command
    }

    fun buildEnvironment(extra: Map<String, String> = emptyMap()): Map<String, String> {
        val nativeDir = context.applicationInfo.nativeLibraryDir
        return mapOf(
            "HOME" to "/home/manim",
            "PATH" to "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
            "TERM" to "xterm-256color",
            "LANG" to "C.UTF-8",
            "LC_ALL" to "C.UTF-8",
            "TMPDIR" to "/tmp",
            "DEBIAN_FRONTEND" to "noninteractive",
            "LD_LIBRARY_PATH" to nativeDir,
        ) + extra
    }

    suspend fun exec(
        command: List<String>,
        workDir: String = "/home/manim",
        env: Map<String, String> = emptyMap(),
        onOutput: (String) -> Unit = {},
    ): Pair<Int, String> = kotlinx.coroutines.withContext(
        kotlinx.coroutines.Dispatchers.IO
    ) {
        ensureDirs()
        val fullCmd = buildProotCommand(command, workDir)
        android.util.Log.d("ProotEngine", "Running: ${fullCmd.joinToString(" ")}")
        android.util.Log.d("ProotEngine", "prootBin exists: ${prootBin.exists()}")
        android.util.Log.d("ProotEngine", "rootfsDir exists: ${rootfsDir.exists()}")

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
                android.util.Log.d("ProotEngine", "output: $line")
            }
        }
        val exit = process.waitFor()
        android.util.Log.d("ProotEngine", "exit code: $exit")
        Pair(exit, output.toString())
    }
}
