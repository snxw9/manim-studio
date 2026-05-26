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
    private val ALPINE_ROOTFS_URL =
        "https://dl-cdn.alpinelinux.org/alpine/v3.19/releases/aarch64/" +
        "alpine-minirootfs-3.19.1-aarch64.tar.gz"

    suspend fun install(): InstallResult = withContext(Dispatchers.IO) {
        try {
            engine.ensureDirs()

            // Stage 1: Download Alpine rootfs with real-time UI tracking
            onProgress(InstallProgress("Downloading Linux environment", 0, "Connecting..."))
            val rootfsTar = downloadFile(
                url = ALPINE_ROOTFS_URL, 
                filename = "alpine-rootfs.tar.gz",
                baseProgress = 0,
                progressRange = 30,
                stageName = "Downloading Linux environment"
            ) ?: return@withContext InstallResult.Error("Failed to download Alpine rootfs")

            // Stage 2: Extract rootfs
            onProgress(InstallProgress("Setting up Linux environment", 30, "Extracting files..."))
            extractTarGz(rootfsTar, engine.usrDir)
            rootfsTar.delete()
            Log.i(TAG, "Alpine rootfs extracted to ${engine.usrDir}")

            // Stage 3: Configure Alpine package manager & FIX DNS
            onProgress(InstallProgress("Configuring package manager", 35, "Setting up network and apk..."))
            setupAlpineRepos()

            // Stage 4: Install System Packages + Hidden Pango Dependencies
            onProgress(InstallProgress("Installing system packages", 45, "Downloading system libraries..."))
            val (pyExit, pyOut) = engine.exec(
                listOf("/sbin/apk", "add", "--no-cache",
                    "python3", "py3-pip",
                    "py3-setuptools", "py3-wheel",
                    "py3-cairo", "py3-cairo-dev",
                    "pango", "pango-dev",
                    "harfbuzz-dev", "freetype-dev", "glib-dev", // Pango's hidden C-dependencies
                    "cairo", "cairo-dev",
                    "ffmpeg",
                    "pkgconfig", "gcc", "g++", 
                    "python3-dev", "musl-dev",
                    "libffi-dev", "openssl-dev",
                    "font-dejavu"
                ),
                onOutput = { line ->
                    if (line.contains("Installing") || line.contains("Fetching")) {
                        // Keep UI progress bar moving during massive apk download
                    }
                }
            )
            if (pyExit != 0) {
                // Pass full string straight to the UI
                return@withContext InstallResult.Error("APK Error:\n$pyOut") 
            }

            // Stage 5: Pre-install build tools via pip globally
            onProgress(InstallProgress("Preparing Python builder", 60, "Installing Cython..."))
            val (toolsExit, toolsOut) = engine.exec(
                listOf("/usr/bin/pip3", "install", "--no-cache-dir", "--break-system-packages",
                    "Cython", "numpy<2.0.0"
                )
            )
            if (toolsExit != 0) {
                return@withContext InstallResult.Error("Cython Error:\n$toolsOut")
            }

            // Stage 6: Compile manimpango and install manim
            onProgress(InstallProgress("Installing Manim", 70, "Compiling manimpango..."))
            val (pipExit, pipOut) = engine.exec(
                listOf("/usr/bin/pip3", "install", "--no-cache-dir", "--break-system-packages",
                    "--no-build-isolation", // Force pip to use the Cython we just installed
                    "manimpango",
                    "manim"
                )
            )
            if (pipExit != 0) {
                return@withContext InstallResult.Error("Pip Manim Error:\n$pipOut")
            }

            // Stage 7: Verify installation
            onProgress(InstallProgress("Verifying installation", 90, "Testing Manim..."))
            val (verifyExit, verifyOut) = engine.exec(
                listOf("/usr/bin/python3", "-c", "import manim; print(manim.__version__)")
            )
            if (verifyExit != 0) {
                return@withContext InstallResult.Error("Manim verification failed: $verifyOut")
            }

            // Stage 8: Mark installation complete
            File(engine.filesDir, ".installed").writeText(verifyOut.trim())
            onProgress(InstallProgress("Complete", 100, "Manim ${verifyOut.trim()} ready"))

            InstallResult.Success

        } catch (e: Exception) {
            Log.e(TAG, "Installation failed", e)
            InstallResult.Error(e.message ?: "Unknown error", e)
        }
    }

    private suspend fun downloadFile(
        url: String, 
        filename: String,
        baseProgress: Int,
        progressRange: Int,
        stageName: String
    ): File? = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) return@withContext null

            val body = response.body ?: return@withContext null
            val totalBytes = body.contentLength()
            val file = File(engine.filesDir, filename)

            body.byteStream().use { input ->
                FileOutputStream(file).use { output ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesCopied = 0L
                    var bytes = input.read(buffer)
                    var lastPercent = -1

                    while (bytes >= 0) {
                        output.write(buffer, 0, bytes)
                        bytesCopied += bytes
                        
                        if (totalBytes > 0) {
                            val downloadedPct = ((bytesCopied.toDouble() / totalBytes.toDouble()) * 100).toInt()
                            // Only update UI every 2% to avoid overwhelming the Compose thread
                            if (downloadedPct != lastPercent && downloadedPct % 2 == 0) {
                                lastPercent = downloadedPct
                                val overallProgress = baseProgress + (downloadedPct * progressRange / 100)
                                val mbDownloaded = String.format("%.1f", bytesCopied.toDouble() / (1024 * 1024))
                                val mbTotal = String.format("%.1f", totalBytes.toDouble() / (1024 * 1024))
                                
                                onProgress(InstallProgress(
                                    stage = stageName, 
                                    percent = overallProgress, 
                                    detail = "$mbDownloaded MB / $mbTotal MB"
                                ))
                            }
                        }
                        bytes = input.read(buffer)
                    }
                }
            }
            file
        } catch (e: Exception) {
            Log.e(TAG, "Download failed: $url", e)
            null
        }
    }

    private fun extractTarGz(tarGz: File, destDir: File) {
        val process = ProcessBuilder(
            "tar", "xzf", tarGz.absolutePath,
            "-C", destDir.absolutePath,
            "--no-same-owner",
        ).start()
        process.waitFor()
    }

    private suspend fun setupAlpineRepos() {
        // FIX: Android doesn't have a standard /etc/resolv.conf, meaning PRoot has no DNS.
        // We MUST inject one so `apk` knows how to connect to the internet.
        val resolvFile = File(engine.usrDir, "etc/resolv.conf")
        resolvFile.parentFile?.mkdirs()
        resolvFile.writeText("nameserver 8.8.8.8\nnameserver 1.1.1.1\n")

        val reposFile = File(engine.usrDir, "etc/apk/repositories")
        reposFile.parentFile?.mkdirs()
        reposFile.writeText(
            "https://dl-cdn.alpinelinux.org/alpine/v3.19/main\n" +
            "https://dl-cdn.alpinelinux.org/alpine/v3.19/community\n"
        )
        
        // We also need to check the exit code. If the internet drops here, 
        // we want it to crash loudly, not fail silently.
        val (exitCode, output) = engine.exec(listOf("/sbin/apk", "update"))
        if (exitCode != 0) {
            throw Exception("Failed to update Alpine repositories. Check internet connection.\nOutput: $output")
        }
    }
}
