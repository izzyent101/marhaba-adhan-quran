package com.mpls.salattv

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

/** Downloads are automatic; Android still controls approval of app installation. */
object UpdateChecker {
    private const val LATEST_RELEASE_API = "https://api.github.com/repos/izzyent101/marhaba-adhan-quran/releases/latest"
    var status by mutableStateOf("Updates check automatically every 15 minutes.")
        private set
    var busy by mutableStateOf(false)
        private set
    var needsPermission by mutableStateOf(false)
        private set
    private val lock = Mutex()
    private var pendingApk: File? = null
    private var pendingVersion = 0
    private var waitingForPermissionReturn = false

    internal fun releaseVersion(tag: String): Int? = tag.toIntOrNull()?.takeIf { it > 0 }

    private data class Update(val version: Int, val url: String, val size: Long)

    private fun request(url: String) = (URL(url).openConnection() as HttpURLConnection).apply {
        connectTimeout = 20_000
        readTimeout = 60_000
        useCaches = false
        setRequestProperty("User-Agent", "MarhabaTV/${BuildConfig.VERSION_CODE}")
        setRequestProperty("Accept", "application/vnd.github+json")
    }

    suspend fun checkAndInstall(context: Context) {
        if (!lock.tryLock()) return
        busy = true
        try {
            if (pendingApk?.isFile == true) {
                offerInstall(context)
                return
            }
            status = "Checking for an update…"
            val update = withContext(Dispatchers.IO) {
                val conn = request(LATEST_RELEASE_API)
                val json = try {
                    check(conn.responseCode in 200..299) { "Update server returned HTTP ${conn.responseCode}" }
                    JSONObject(conn.inputStream.bufferedReader().use { it.readText() })
                } finally { conn.disconnect() }
                val version = releaseVersion(json.optString("tag_name"))
                    ?: error("The latest release does not have a valid app version")
                if (version <= BuildConfig.VERSION_CODE) return@withContext null
                val assets = json.getJSONArray("assets")
                val apk = (0 until assets.length()).map { assets.getJSONObject(it) }
                    .firstOrNull { it.optString("name") == "app-debug.apk" }
                    ?: error("The release APK is not available yet")
                val url = apk.getString("browser_download_url")
                check(url.startsWith("https://github.com/izzyent101/marhaba-adhan-quran/releases/download/")) { "Unexpected update download address" }
                Update(version, url, apk.getLong("size"))
            }
            if (update == null) {
                status = "Version ${BuildConfig.VERSION_CODE} is up to date."
                needsPermission = false
                return
            }
            status = "Downloading version ${update.version}…"
            val apk = withContext(Dispatchers.IO) {
                val finalFile = File(context.cacheDir, "marhaba-update-${update.version}.apk")
                val partial = File(context.cacheDir, "marhaba-update.part")
                try {
                    val conn = request(update.url).apply { setRequestProperty("Accept", "application/octet-stream") }
                    try {
                        check(conn.responseCode in 200..299) { "Download returned HTTP ${conn.responseCode}" }
                        conn.inputStream.use { input -> partial.outputStream().use { input.copyTo(it) } }
                    } finally { conn.disconnect() }
                    check(partial.length() == update.size && update.size > 0) { "Incomplete download; please retry" }
                    val info = context.packageManager.getPackageArchiveInfo(partial.absolutePath, 0)
                    check(info?.packageName == context.packageName) { "Downloaded file is not the Marhaba app" }
                    val version = if (Build.VERSION.SDK_INT >= 28) info!!.longVersionCode else {
                        @Suppress("DEPRECATION")
                        info!!.versionCode.toLong()
                    }
                    check(version == update.version.toLong()) { "Downloaded app version does not match the release" }
                    if (finalFile.exists()) check(finalFile.delete()) { "Could not replace cached update" }
                    check(partial.renameTo(finalFile)) { "Could not save the update" }
                    finalFile
                } finally { partial.delete() }
            }
            pendingApk = apk
            pendingVersion = update.version
            offerInstall(context)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            status = "Update not completed: ${e.message ?: "check the internet connection"}. Use Check for update to retry."
        } finally {
            busy = false
            lock.unlock()
        }
    }

    private fun offerInstall(context: Context) {
        val apk = pendingApk ?: return
        if (Build.VERSION.SDK_INT >= 26 && !context.packageManager.canRequestPackageInstalls()) {
            needsPermission = true
            status = "Version $pendingVersion is downloaded. Select Allow app updates, enable permission, then return here."
            return
        }
        needsPermission = false
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        status = "Version $pendingVersion is ready. Select Install in Android's installer. If dismissed, use Check for update to reopen it."
    }

    fun openInstallPermission(context: Context) {
        waitingForPermissionReturn = true
        try {
            context.startActivity(Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                Uri.parse("package:${context.packageName}")).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        } catch (_: Exception) {
            status = "Open Android Settings → Apps → Special access → Install unknown apps, then allow Marhaba."
            waitingForPermissionReturn = false
        }
    }

    fun resumePendingInstall(context: Context) {
        if (!waitingForPermissionReturn) return
        waitingForPermissionReturn = false
        try { offerInstall(context) } catch (e: Exception) {
            status = "Could not open the installer: ${e.message}. Use Check for update to retry."
        }
    }
}
