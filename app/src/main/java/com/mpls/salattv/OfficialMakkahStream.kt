package com.mpls.salattv

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URI
import java.net.URL

/** Public guest playback endpoint used by aloula.sba.sa/en/live/quran.
 * Signed URLs are short-lived: request one per startup/recovery, never persist one.
 */
internal object OfficialMakkahStream {
    const val PAGE = "https://aloula.sba.sa/en/live/quran"
    const val USER_AGENT = "Mozilla/5.0 MarhabaTV/1.0"

    fun isOfficialUrl(value: String): Boolean = try {
        val uri = URI(value)
        uri.scheme == "https" && uri.host == "live.kwikmotion.com" &&
            uri.path.startsWith("/ksaquranlive/ksaquran.smil/") &&
            uri.path.endsWith(".m3u8") && uri.userInfo == null && uri.port == -1
    } catch (_: Exception) { false }

    suspend fun resolve(): String? = withContext(Dispatchers.IO) {
        var connection: HttpURLConnection? = null
        try {
            connection = (URL("https://aloula.faulio.com/api/v1.1/channels/7/player").openConnection() as HttpURLConnection).apply {
                connectTimeout = 5000
                readTimeout = 5000
                setRequestProperty("User-Agent", USER_AGENT)
                setRequestProperty("Accept", "application/json")
                setRequestProperty("Referer", PAGE)
                setRequestProperty("Cache-Control", "no-cache")
            }
            if (connection.responseCode != 200) return@withContext null
            val body = connection.inputStream.bufferedReader().use { it.readText() }
            val url = JSONObject(body).optJSONObject("streams")?.optString("hls").orEmpty()
            url.takeIf(::isOfficialUrl)
        } catch (_: Exception) { null }
        finally { connection?.disconnect() }
    }
}
