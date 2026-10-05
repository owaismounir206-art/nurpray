package com.nurpray.app.core.updater

import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.nurpray.app.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.net.HttpURLConnection
import java.net.URL

sealed class UpdateCheckResult {
    data class UpdateAvailable(
        val latestVersion: String,
        val currentVersion: String,
        val releaseNotes: String,
        val downloadUrl: String,
        val apkSizeMb: Double
    ) : UpdateCheckResult()

    data object UpToDate : UpdateCheckResult()
    data class Error(val message: String) : UpdateCheckResult()
}

class AppUpdateManager {

    companion object {
        private const val GITHUB_REPO_OWNER = "owaismounir206-art"
        private const val GITHUB_REPO_NAME = "nurpray"
        private const val GITHUB_API_URL = "https://api.github.com/repos/$GITHUB_REPO_OWNER/$GITHUB_REPO_NAME/releases/latest"
    }

    suspend fun checkForUpdates(currentVersion: String = BuildConfig.VERSION_NAME): UpdateCheckResult {
        return withContext(Dispatchers.IO) {
            try {
                val url = URL(GITHUB_API_URL)
                val connection = (url.openConnection() as HttpURLConnection).apply {
                    requestMethod = "GET"
                    setRequestProperty("Accept", "application/vnd.github.v3+json")
                    setRequestProperty("User-Agent", "NurPray-Android-Updater")
                    connectTimeout = 8000
                    readTimeout = 8000
                }

                if (connection.responseCode != 200) {
                    return@withContext UpdateCheckResult.Error("Risposta server GitHub: ${connection.responseCode}")
                }

                val responseJson = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(responseJson)

                val tagName = json.optString("tag_name", "").removePrefix("v").trim()
                val body = json.optString("body", "Nuova versione disponibile con miglioramenti e correzioni.")

                // Look for .apk asset
                val assets = json.optJSONArray("assets")
                var downloadUrl: String? = null
                var apkSizeBytes = 0L

                if (assets != null) {
                    for (i in 0 until assets.length()) {
                        val asset = assets.getJSONObject(i)
                        val name = asset.optString("name", "")
                        if (name.endsWith(".apk", ignoreCase = true)) {
                            downloadUrl = asset.optString("browser_download_url")
                            apkSizeBytes = asset.optLong("size", 0L)
                            break
                        }
                    }
                }

                if (downloadUrl == null) {
                    return@withContext UpdateCheckResult.Error("Nessun pacchetto APK trovato nella release")
                }

                if (isNewerVersion(tagName, currentVersion)) {
                    val sizeMb = apkSizeBytes / (1024.0 * 1024.0)
                    UpdateCheckResult.UpdateAvailable(
                        latestVersion = tagName,
                        currentVersion = currentVersion,
                        releaseNotes = body,
                        downloadUrl = downloadUrl,
                        apkSizeMb = sizeMb
                    )
                } else {
                    UpdateCheckResult.UpToDate
                }
            } catch (e: Exception) {
                UpdateCheckResult.Error("Errore controllo aggiornamenti: ${e.localizedMessage ?: "Errore sconosciuto"}")
            }
        }
    }

    suspend fun downloadAndInstallApk(
        context: Context,
        downloadUrl: String,
        onProgress: (Float) -> Unit
    ): Result<File> {
        return withContext(Dispatchers.IO) {
            try {
                // Ensure output directory exists
                val apksDir = File(context.cacheDir, "apks").apply { mkdirs() }
                val outputFile = File(apksDir, "nurpray-update.apk")
                if (outputFile.exists()) {
                    outputFile.delete()
                }

                // Handle HTTP redirects (GitHub releases assets redirect to AWS S3/objects.githubusercontent.com)
                var currentUrl = downloadUrl
                var connection: HttpURLConnection
                var redirects = 0

                while (true) {
                    val url = URL(currentUrl)
                    connection = (url.openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = true
                        setRequestProperty("User-Agent", "NurPray-Android-Updater")
                        connectTimeout = 15000
                        readTimeout = 30000
                    }

                    val status = connection.responseCode
                    if (status == HttpURLConnection.HTTP_MOVED_TEMP ||
                        status == HttpURLConnection.HTTP_MOVED_PERM ||
                        status == 307 || status == 308) {
                        currentUrl = connection.getHeaderField("Location")
                        redirects++
                        if (redirects > 5) throw IllegalStateException("Troppi redirect HTTP")
                        continue
                    }
                    break
                }

                val totalLength = connection.contentLengthLong

                connection.inputStream.use { input ->
                    FileOutputStream(outputFile).use { output ->
                        val buffer = ByteArray(8 * 1024)
                        var bytesRead: Int
                        var downloadedBytes = 0L

                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            output.write(buffer, 0, bytesRead)
                            downloadedBytes += bytesRead
                            if (totalLength > 0) {
                                val progress = downloadedBytes.toFloat() / totalLength.toFloat()
                                withContext(Dispatchers.Main) {
                                    onProgress(progress)
                                }
                            }
                        }
                        output.flush()
                    }
                }

                // Trigger package installation intent
                withContext(Dispatchers.Main) {
                    val uri = FileProvider.getUriForFile(
                        context,
                        "${context.packageName}.provider",
                        outputFile
                    )

                    val intent = Intent(Intent.ACTION_VIEW).apply {
                        setDataAndType(uri, "application/vnd.android.package-archive")
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    context.startActivity(intent)
                }

                Result.success(outputFile)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    private fun isNewerVersion(latest: String, current: String): Boolean {
        val latestParts = latest.split(".").mapNotNull { it.filter { c -> c.isDigit() }.toIntOrNull() }
        val currentParts = current.split(".").mapNotNull { it.filter { c -> c.isDigit() }.toIntOrNull() }

        val maxLen = maxOf(latestParts.size, currentParts.size)
        for (i in 0 until maxLen) {
            val l = latestParts.getOrElse(i) { 0 }
            val c = currentParts.getOrElse(i) { 0 }
            if (l > c) return true
            if (l < c) return false
        }
        return false
    }
}
