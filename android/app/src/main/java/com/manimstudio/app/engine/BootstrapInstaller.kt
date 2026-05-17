package com.manimstudio.app.engine

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.TimeUnit

private const val TAG = "BootstrapInstaller"

data class InstallProgress(
    val stage: String,
    val percent: Int,
    val detail: String = "",
)

sealed class InstallResult {
    object Success : InstallResult()
    data class Error(val message: String, val cause: Throwable? = null) : InstallResult()
}

class BootstrapInstaller(
    private val context: Context,
    private val engine: ProotEngine,
    private val onProgress: suspend (InstallProgress) -> Unit,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(300, TimeUnit.SECONDS)
        .build()

    // Alpine Linux minimal rootfs for arm64
    // This contains: musl libc, busybox, apk package manager
    private val ALPINE_ROOTFS_URL =
        "https://dl-cdn.alpinelinux.org/alpine/v3.19/releases/aarch64/" +
        "alpine-minirootfs-3.19.1-aarch64.tar.gz"

    // proot static binary for Android arm64
    // From the proot-distro project, tested on Android
    private val PROOT_URL =
        "https://github.com/termux/proot/releases/download/v5.3.0/" +
        "proot-aarch64"

    suspend fun install(): InstallResult = withContext(Dispatchers.IO) {
        try {
            engine.ensureDirs()

            // Stage 1: Download proot binary
            onProgress(InstallProgress("Downloading core files", 5, "proot binary"))
            val prootFile = downloadFile(PROOT_URL, "proot-aarch64")
                ?: return@withContext InstallResult.Error("Failed to download proot")
            prootFile.copyTo(engine.prootBin, overwrite = true)
            engine.prootBin.setExecutable(true)
            Log.i(TAG, "proot installed at ${engine.prootBin}")

            // Stage 2: Download Alpine rootfs
            onProgress(InstallProgress("Downloading Linux environment", 15, "Alpine Linux (~5MB)"))
            val rootfsTar = downloadFile(ALPINE_ROOTFS_URL, "alpine-rootfs.tar.gz")
                ?: return@withContext InstallResult.Error("Failed to download Alpine rootfs")

            // Stage 3: Extract rootfs
            onProgress(InstallProgress("Setting up Linux environment", 30, "Extracting files..."))
            extractTarGz(rootfsTar, engine.usrDir)
            rootfsTar.delete()
            Log.i(TAG, "Alpine rootfs extracted to ${engine.usrDir}")

            // Stage 4: Configure Alpine package manager
            onProgress(InstallProgress("Configuring package manager", 40, "Setting up apk..."))
            setupAlpineRepos()

            // Stage 5: Install Python + Cairo + Pango + FFmpeg
            onProgress(InstallProgress("Installing Python 3.11", 45, "This may take a few minutes..."))
            val (pyExit, pyOut) = engine.exec(
                listOf("/sbin/apk", "add", "--no-cache",
                    "python3", "py3-pip",
                    "cairo", "pango",
                    "ffmpeg",
                    "gcc", "python3-dev", "musl-dev",
                    "libffi-dev", "openssl-dev",
                    "font-dejavu",
                    "texmf-dist-latexrecommended",
                ),
                onOutput = { line ->
                    if (line.contains("Installing") || line.contains("Fetching")) {
                        Log.d(TAG, "apk: $line")
                    }
                }
            )
            if (pyExit != 0) {
                return@withContext InstallResult.Error("Failed to install packages: $pyOut")
            }
            Log.i(TAG, "System packages installed")

            // Stage 6: Install pycairo + manimpango
            onProgress(InstallProgress("Installing Cairo Python bindings", 65,
                "pycairo + manimpango..."))
            val (cairoExit, cairoOut) = engine.exec(
                listOf("/usr/bin/pip3", "install", "--no-cache-dir",
                    "pycairo", "manimpango")
            )
            if (cairoExit != 0) {
                return@withContext InstallResult.Error("Failed to install pycairo: $cairoOut")
            }

            // Stage 7: Install Manim
            onProgress(InstallProgress("Installing Manim", 75,
                "Mathematical animation engine..."))
            val (manimExit, manimOut) = engine.exec(
                listOf("/usr/bin/pip3", "install", "--no-cache-dir", "manim")
            )
            if (manimExit != 0) {
                return@withContext InstallResult.Error("Failed to install Manim: $manimOut")
            }
            Log.i(TAG, "Manim installed successfully")

            // Stage 8: Verify installation
            onProgress(InstallProgress("Verifying installation", 90, "Testing Manim..."))
            val (verifyExit, verifyOut) = engine.exec(
                listOf("/usr/bin/python3", "-c", "import manim; print(manim.__version__)")
            )
            if (verifyExit != 0) {
                return@withContext InstallResult.Error("Manim verification failed: $verifyOut")
            }
            Log.i(TAG, "Manim version: $verifyOut")

            // Stage 9: Mark installation complete
            File(engine.filesDir, ".installed").writeText(verifyOut.trim())
            onProgress(InstallProgress("Complete", 100, "Manim ${verifyOut.trim()} ready"))

            InstallResult.Success

        } catch (e: Exception) {
            Log.e(TAG, "Installation failed", e)
            InstallResult.Error(e.message ?: "Unknown error", e)
        }
    }

    private fun downloadFile(url: String, filename: String): File? {
        return try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return null

            val file = File(engine.filesDir, filename)
            response.body?.byteStream()?.use { input ->
                FileOutputStream(file).use { output ->
                    input.copyTo(output)
                }
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: $url", e)
            null
        }
    }

    private fun extractTarGz(tarGz: File, destDir: File) {
        // Use Android's built-in tar via Runtime
        // Alpine rootfs must be extracted preserving symlinks
        val process = ProcessBuilder(
            "tar", "xzf", tarGz.absolutePath,
            "-C", destDir.absolutePath,
            "--no-same-owner",
        ).start()
        process.waitFor()
    }

    private suspend fun setupAlpineRepos() {
        val reposFile = File(engine.usrDir, "etc/apk/repositories")
        reposFile.parentFile?.mkdirs()
        reposFile.writeText(
            "https://dl-cdn.alpinelinux.org/alpine/v3.19/main\n" +
            "https://dl-cdn.alpinelinux.org/alpine/v3.19/community\n"
        )
        // Update package list
        engine.exec(listOf("/sbin/apk", "update"))
    }
}
