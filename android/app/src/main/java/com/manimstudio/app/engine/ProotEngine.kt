package com.manimstudio.app.engine

import android.annotation.SuppressLint
import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class ProotEngine(val context: Context) {

    // CRITICAL: Hardcode /data/data/ — never use context.filesDir.absolutePath
    // context.filesDir returns /data/user/0/... but proot's internal realpath()
    // always resolves to /data/data/... on Android. Any mismatch causes every
    // temp file operation to fail with ENOENT. This is the root cause of all
    // previous failures. Termux itself hardcodes /data/data/ for the same reason.
    @SuppressLint("SdCardPath")
    private val base = "/data/data/${context.packageName}"

    val filesDir        = File("$base/files")
    val rootfsDir       = File("$base/files/rootfs")
    val homeDir         = File("$base/files/home/manim")
    val rendersDir      = File("$base/files/renders")
    private val tmpDir      = File("$base/files/tmp")
    private val prootTmpDir = File("$base/files/proot_tmp")

    val pythonBin = File(rootfsDir, "usr/bin/python3")
    val ffmpegBin = File(rootfsDir, "usr/bin/ffmpeg")
    val latexBin  = File(rootfsDir, "usr/bin/latex")

    val prootBin  = File(context.applicationInfo.nativeLibraryDir, "libproot.so")

    @SuppressLint("SetWorldReadable", "SetWorldWritable")
    fun ensureDirs() {
        listOf(rootfsDir, homeDir, rendersDir, tmpDir, prootTmpDir).forEach {
            it.mkdirs()
            it.setReadable(true, false)
            it.setWritable(true, false)
            it.setExecutable(true, false)
        }
    }

    fun buildProotCommand(
        command: List<String>,
        workDir: String = "/home/manim",
    ): List<String> = listOf(
        prootBin.absolutePath,
        "--kill-on-exit",
        "--link2symlink",
        "-0",
        "--kernel-release=5.4.0-faked",
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
        "HOME"             to "/home/manim",
        "PATH"             to "/usr/local/sbin:/usr/local/bin:/usr/sbin:/usr/bin:/sbin:/bin",
        "TERM"             to "xterm-256color",
        "LANG"             to "C.UTF-8",
        "LC_ALL"           to "C.UTF-8",
        "TMPDIR"           to "/tmp",
        "DEBIAN_FRONTEND"  to "noninteractive",
        // LD_LIBRARY_PATH: needed for Android's linker to find libtalloc.so
        // when proot itself starts. glibc binaries inside the rootfs won't
        // accidentally use Android's Bionic libs because their SONAMEs differ.
        "LD_LIBRARY_PATH"  to context.applicationInfo.nativeLibraryDir,
        // PROOT_TMP_DIR: must match what proot's realpath() returns.
        // /data/data/ IS what realpath() returns — now matches exactly.
        "PROOT_TMP_DIR"    to prootTmpDir.absolutePath,
        // PROOT_NO_SECCOMP: Android 10+ seccomp filters block syscalls proot
        // relies on. This disables that check.
        "PROOT_NO_SECCOMP" to "1",
    ) + extra

    suspend fun exec(
        command: List<String>,
        workDir: String = "/home/manim",
        env: Map<String, String> = emptyMap(),
        onOutput: (String) -> Unit = {},
    ): Pair<Int, String> = withContext(Dispatchers.IO) {
        ensureDirs()
        val fullCmd = buildProotCommand(command, workDir)

        android.util.Log.d("ProotEngine", "prootBin: ${prootBin.absolutePath}")
        android.util.Log.d("ProotEngine", "prootBin.exists: ${prootBin.exists()}")
        android.util.Log.d("ProotEngine", "prootTmpDir: ${prootTmpDir.absolutePath}")
        android.util.Log.d("ProotEngine", "prootTmpDir.exists: ${prootTmpDir.exists()}")
        android.util.Log.d("ProotEngine", "prootTmpDir.canWrite: ${prootTmpDir.canWrite()}")
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
                android.util.Log.d("ProotEngine", "proot: $line")
            }
        }
        val exit = process.waitFor()
        android.util.Log.d("ProotEngine", "exit: $exit")
        Pair(exit, output.toString())
    }
}
