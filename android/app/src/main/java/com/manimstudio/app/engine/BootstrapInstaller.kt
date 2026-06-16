package com.manimstudio.app.engine

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

private const val TAG = "BootstrapInstaller"
private const val MANIFEST_URL =
    "https://raw.githubusercontent.com/snxw9/manim-studio-bootstrap/main/bootstrap-manifest.json"
private const val RELEASE_MANIFEST_URL =
    "https://github.com/snxw9/manim-studio-bootstrap/releases/latest/download/bootstrap-manifest.json"

@Serializable
data class BootstrapManifest(
    val version: String,
    val manim_version: String,
    val python_version: String,
    val arch: String,
    val min_app_version: Int,
    val release_url: String,
    val bootstrap_url: String,
    val bootstrap_size_bytes: Long,
    val bootstrap_sha256: String,
    val proot_url: String,
    val proot_sha256: String,
    val changelog: String,
    val built_at: String = "",
)

data class InstallProgress(
    val stage: String,
    val percent: Int,
    val detail: String = "",
    val bytesDownloaded: Long = 0L,
    val bytesTotal: Long = 0L,
)

sealed class InstallResult {
    object Success : InstallResult()
    data class Error(val message: String, val cause: Throwable? = null) : InstallResult()
    object WifiRequired : InstallResult()
}

class BootstrapInstaller(
    private val context: Context,
    private val engine: ProotEngine,
    private val onProgress: suspend (InstallProgress) -> Unit,
) {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(0, TimeUnit.SECONDS) // no timeout for large downloads
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    // ── Public API ────────────────────────────────────────────────────────────

    suspend fun install(allowMobileData: Boolean = false): InstallResult =
        withContext(Dispatchers.IO) {
            try {
                engine.ensureDirs()

                // Check connectivity
                val connectivityResult = checkConnectivity(allowMobileData)
                if (connectivityResult != null) return@withContext connectivityResult

                // Fetch manifest
                onProgress(InstallProgress("Checking latest version", 2))
                val manifest = fetchManifest()
                    ?: return@withContext InstallResult.Error(
                        "Could not fetch bootstrap manifest. Check your internet connection."
                    )

                Log.i(TAG, "Installing bootstrap v${manifest.version}")

                // Download and extract rootfs
                val archiveFile = File(engine.filesDir, "bootstrap.tar.gz")
                val downloadResult = downloadWithProgress(
                    url = manifest.bootstrap_url,
                    dest = archiveFile,
                    expectedSize = manifest.bootstrap_size_bytes,
                    expectedSha256 = manifest.bootstrap_sha256,
                    stageLabel = "Downloading Manim bootstrap",
                    startPercent = 5,
                    endPercent = 75,
                )
                if (downloadResult != null) return@withContext downloadResult

                // Extract
                onProgress(InstallProgress("Extracting files", 76,
                    "This may take a few minutes..."))
                val extractResult = extractTarGz(archiveFile, engine.usrDir)
                if (extractResult != null) return@withContext extractResult
                archiveFile.delete()

                // Inject DNS
                onProgress(InstallProgress("Configuring network", 90))
                injectDns()

                // Verify
                onProgress(InstallProgress("Verifying installation", 94,
                    "Testing Manim..."))
                val verifyResult = verify()
                if (verifyResult != null) return@withContext verifyResult

                // Write version marker
                val marker = File(engine.filesDir, ".installed")
                marker.writeText(manifest.version)

                onProgress(InstallProgress("Complete", 100,
                    "Manim ${manifest.manim_version} ready"))

                Log.i(TAG, "Bootstrap installation complete: v${manifest.version}")
                InstallResult.Success

            } catch (e: Exception) {
                Log.e(TAG, "Installation failed", e)
                InstallResult.Error(e.message ?: "Unknown error during installation", e)
            }
        }

    /**
     * Check if a newer bootstrap version is available.
     * Returns the new manifest if an update is available, null otherwise.
     * Does not block — intended for background checks.
     */
    suspend fun checkForUpdate(): BootstrapManifest? = withContext(Dispatchers.IO) {
        try {
            val installedVersion = getInstalledVersion() ?: return@withContext null
            val manifest = fetchManifest() ?: return@withContext null
            if (isNewerVersion(manifest.version, installedVersion)) manifest else null
        } catch (e: Exception) {
            Log.w(TAG, "Update check failed", e)
            null
        }
    }

    fun getInstalledVersion(): String? {
        val marker = File(engine.filesDir, ".installed")
        return if (marker.exists()) marker.readText().trim() else null
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private fun checkConnectivity(allowMobileData: Boolean): InstallResult? {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE)
            as ConnectivityManager
        val network = cm.activeNetwork ?: return InstallResult.Error(
            "No internet connection. Connect to Wi-Fi or mobile data and try again."
        )
        val caps = cm.getNetworkCapabilities(network) ?: return InstallResult.Error(
            "Could not determine network type."
        )

        val isWifi = caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        val isMobile = caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        val isEthernet = caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)

        if (!isWifi && !isMobile && !isEthernet) {
            return InstallResult.Error("No usable network connection found.")
        }

        if (!isWifi && !isEthernet && !allowMobileData) {
            return InstallResult.WifiRequired
        }

        return null // all good
    }

    private fun fetchManifest(): BootstrapManifest? {
        // Try release manifest first, fall back to repo manifest
        for (url in listOf(RELEASE_MANIFEST_URL, MANIFEST_URL)) {
            try {
                val request = Request.Builder().url(url).build()
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    val body = response.body.string()
                    return json.decodeFromString<BootstrapManifest>(body)
                }
            } catch (e: Exception) {
                Log.w(TAG, "Failed to fetch manifest from $url: ${e.message}")
            }
        }
        return null
    }

    private suspend fun downloadWithProgress(
        url: String,
        dest: File,
        expectedSize: Long,
        expectedSha256: String,
        stageLabel: String,
        startPercent: Int,
        endPercent: Int,
    ): InstallResult? {
        val range = endPercent - startPercent

        try {
            val request = Request.Builder().url(url).build()
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                return InstallResult.Error(
                    "Download failed: HTTP ${response.code} for $url"
                )
            }

            val body = response.body

            val contentLength = if (expectedSize > 0) expectedSize
                                 else body.contentLength()

            val digest = MessageDigest.getInstance("SHA-256")
            var downloaded = 0L

            withContext(Dispatchers.IO) {
                FileOutputStream(dest).use { out ->
                    body.byteStream().use { input ->
                        val buffer = ByteArray(65_536) // 64KB chunks
                        var read: Int
                        while (input.read(buffer).also { read = it } != -1) {
                            out.write(buffer, 0, read)
                            digest.update(buffer, 0, read)
                            downloaded += read

                            val progress = if (contentLength > 0) {
                                startPercent + (downloaded.toFloat() / contentLength * range).toInt()
                            } else startPercent + range / 2

                            val mb = downloaded / (1024 * 1024)
                            val totalMb =
                                if (contentLength > 0) contentLength / (1024 * 1024) else 0

                            onProgress(
                                InstallProgress(
                                    stage = stageLabel,
                                    percent = progress.coerceIn(startPercent, endPercent),
                                    detail = if (totalMb > 0) "${mb}MB / ${totalMb}MB" else "${mb}MB",
                                    bytesDownloaded = downloaded,
                                    bytesTotal = contentLength,
                                )
                            )
                        }
                    }
                }
            }

            // Verify checksum
            val actualSha256 = digest.digest().joinToString("") { "%02x".format(it) }
            if (actualSha256 != expectedSha256) {
                dest.delete()
                return InstallResult.Error(
                    "Download corrupted — checksum mismatch.\n" +
                    "Expected: $expectedSha256\n" +
                    "Got:      $actualSha256\n" +
                    "Please try again."
                )
            }

            Log.i(TAG, "Downloaded ${dest.name}: ${downloaded / (1024 * 1024)}MB, checksum OK")
            return null // success

        } catch (e: Exception) {
            dest.delete()
            return InstallResult.Error("Download error: ${e.message}", e)
        }
    }

    private fun extractTarGz(archive: File, destDir: File): InstallResult? {
        return try {
            destDir.mkdirs()
            val process = ProcessBuilder(
                "tar", "xzf", archive.absolutePath,
                "-C", destDir.absolutePath,
                "--no-same-owner",
                "--overwrite",
            ).redirectErrorStream(true).start()

            // Drain output to prevent blocking
            val output = process.inputStream.bufferedReader().readText()
            val exit = process.waitFor()

            // Do NOT fail based on tar's exit code.
            // Android's toybox tar is strict about absolute symlinks and hard links
            // that exist in Linux rootfs archives but are not needed by Manim.
            // Instead, verify only the files Manim actually requires.
            Log.d(TAG, "tar exited $exit. Validating essential files...")

            val essential = mapOf(
                "python3" to "usr/bin/python3",
                "pip3"    to "usr/bin/pip3",
                "ffmpeg"  to "usr/bin/ffmpeg",
                "latex"   to "usr/bin/latex",
                "dvisvgm" to "usr/bin/dvisvgm",
            )

            val missing = essential.filterValues { rel ->
                !File(destDir, rel).exists()
            }

            if (missing.isNotEmpty()) {
                return InstallResult.Error(
                    "Extraction incomplete — these files are missing: " +
                    "${missing.keys.joinToString(", ")}\n\n" +
                    "Last tar output:\n${output.takeLast(600)}"
                )
            }

            Log.i(TAG, "Extraction validated. All essential files present. " +
                       "(tar exit=$exit, warnings above are non-fatal)")
            null // success

        } catch (e: Exception) {
            InstallResult.Error("Extraction exception: ${e.message}", e)
        }
    }

    private fun injectDns() {
        try {
            val resolv = File(engine.usrDir, "etc/resolv.conf")
            resolv.parentFile?.mkdirs()
            resolv.writeText("nameserver 8.8.8.8\nnameserver 1.1.1.1\n")
            Log.i(TAG, "DNS injected")
        } catch (e: Exception) {
            Log.w(TAG, "DNS injection failed (non-fatal): ${e.message}")
        }
    }

    private suspend fun verify(): InstallResult? {
        val (exitCode, output) = engine.exec(
            listOf("/usr/bin/python3", "-c",
                "import manim; import cairo; print(manim.__version__)")
        )
        return if (exitCode != 0) {
            InstallResult.Error("Manim verification failed:\n$output")
        } else {
            Log.i(TAG, "Verification OK: $output")
            null
        }
    }

    private fun isNewerVersion(remote: String, local: String): Boolean {
        return try {
            val r = remote.split(".").map { it.toInt() }
            val l = local.split(".").map { it.toInt() }
            val maxLen = maxOf(r.size, l.size)
            for (i in 0 until maxLen) {
                val rv = r.getOrElse(i) { 0 }
                val lv = l.getOrElse(i) { 0 }
                if (rv > lv) return true
                if (rv < lv) return false
            }
            false
        } catch (_: Exception) {
            false
        }
    }
}
