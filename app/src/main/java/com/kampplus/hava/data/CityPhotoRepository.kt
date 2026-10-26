package com.kampplus.hava.data

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.text.Html
import android.util.LruCache
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import java.io.File
import java.util.concurrent.TimeUnit
import kotlin.math.round
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import okhttp3.Cache
import okhttp3.HttpUrl
import okhttp3.OkHttpClient
import okhttp3.Request

data class RemoteCityPhoto(val bitmap: Bitmap, val place: String, val author: String, val license: String, val source: String)

/** Only coarse coordinates reach Wikimedia. No API key, no precise device location. */
interface CityPhotoSource {
    suspend fun load(name: String, latitude: Double, longitude: Double): RemoteCityPhoto?
}

class WikimediaCityPhotoRepository(private val context: Context) : CityPhotoSource {
    private val lock = Mutex()
    private val memory = LruCache<String, RemoteCityPhoto>(12)
    private val misses = mutableMapOf<String, Long>()
    private var client: OkHttpClient? = null

    override suspend fun load(name: String, latitude: Double, longitude: Double): RemoteCityPhoto? = withContext(Dispatchers.IO) {
        val lat = round(latitude * 100) / 100
        val lon = round(longitude * 100) / 100
        val key = "$name:$lat:$lon"
        lock.withLock {
            memory.get(key)?.let { return@withLock it }
            if (System.currentTimeMillis() - (misses[key] ?: 0) < 300_000) return@withLock null
            val http = client ?: OkHttpClient.Builder()
                .cache(Cache(File(context.applicationContext.cacheDir, "city-photos"), 32L * 1024 * 1024))
                .callTimeout(18, TimeUnit.SECONDS).build().also { client = it }
            try {
                catalogPhoto(http, name, latitude, longitude)?.let {
                    memory.put(key, it)
                    return@withLock it
                }
                fun pages(result: JsonObject) = result.getAsJsonObject("query")?.getAsJsonObject("pages")?.entrySet()
                    ?.map { it.value.asJsonObject }.orEmpty()
                fun nearby(page: JsonObject): Boolean {
                    val coordinate = page.getAsJsonArray("coordinates")?.firstOrNull()?.asJsonObject ?: return false
                    val city = com.kampplus.hava.model.GeoPoint(latitude, longitude)
                    val photo = com.kampplus.hava.model.GeoPoint(coordinate.get("lat").asDouble, coordinate.get("lon").asDouble)
                    return city.distanceKmTo(photo) < 60 && page.has("thumbnail") && page.has("pageimage")
                }
                val candidates = mutableListOf<JsonObject>()
                var lookupName = name.substringBefore(" / ")
                for (language in listOf("tr", "en")) {
                    val direct = pages(
                        json(
                            http,
                            "https://$language.wikipedia.org/w/api.php",
                            mapOf(
                                "action" to "query",
                                "titles" to lookupName,
                                "redirects" to "1",
                                "prop" to "pageimages|coordinates|langlinks",
                                "lllang" to "en",
                                "piprop" to "name|thumbnail",
                                "pithumbsize" to "960",
                                "pilicense" to "free"
                            )
                        )
                    )
                    direct.firstOrNull()?.getAsJsonArray("langlinks")?.firstOrNull()?.asJsonObject?.get("*")?.asString?.let {
                        lookupName = it
                    }
                    candidates.addAll(direct.filter { nearby(it) })
                    if (candidates.isNotEmpty()) break
                }
                if (candidates.isEmpty()) {
                    val surrounding = pages(
                        json(
                            http,
                            "https://en.wikipedia.org/w/api.php",
                            mapOf(
                                "action" to "query", "generator" to "geosearch", "ggscoord" to "$lat|$lon", "ggsradius" to "10000",
                                "ggslimit" to "30", "colimit" to "max", "pilimit" to "max", "prop" to "pageimages|coordinates|langlinks",
                                "lllang" to "en", "piprop" to "name|thumbnail", "pithumbsize" to "960", "pilicense" to "free"
                            )
                        )
                    )
                    candidates.addAll(
                        surrounding.filter {
                            val title = it.get("title")?.asString.orEmpty()
                            nearby(it) && (title.equals(name, true) || title.startsWith("$name,", true) || title.equals("$name City", true))
                        }
                    )
                }
                for (page in candidates.take(3)) {
                    val title = page.get("pageimage").asString
                    val infoPages = json(
                        http,
                        "https://commons.wikimedia.org/w/api.php",
                        mapOf(
                            "action" to "query",
                            "titles" to "File:$title",
                            "redirects" to "1",
                            "prop" to "imageinfo",
                            "iiprop" to "url|extmetadata"
                        )
                    ).getAsJsonObject("query")?.getAsJsonObject("pages") ?: continue
                    val info =
                        infoPages.entrySet().firstOrNull()?.value?.asJsonObject?.getAsJsonArray("imageinfo")?.firstOrNull()?.asJsonObject
                            ?: continue
                    val meta = info.getAsJsonObject("extmetadata") ?: continue
                    fun field(key: String) =
                        Html.fromHtml(meta.getAsJsonObject(key)?.get("value")?.asString.orEmpty(), Html.FROM_HTML_MODE_LEGACY).toString()
                    val license = field("LicenseShortName")
                    if (!(license.startsWith("CC BY") || license.startsWith("CC0") || license.contains("Public domain", true))) continue
                    val url = HttpUrl.parse(page.getAsJsonObject("thumbnail").get("source").asString) ?: continue
                    if (url.host() !in setOf("upload.wikimedia.org", "thumb.wikimedia.org") || url.scheme() != "https") continue
                    val bitmap = download(http, url.toString()) ?: continue
                    val photo =
                        RemoteCityPhoto(bitmap, page.get("title").asString, field("Artist"), license, info.get("descriptionurl").asString)
                    memory.put(key, photo)
                    return@withLock photo
                }
                if (misses.size > 100) misses.clear()
                misses[key] = System.currentTimeMillis()
                null
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                misses[key] = System.currentTimeMillis()
                null
            }
        }
    }

    private val catalog by lazy {
        context.assets.open("online-city-photos.json").bufferedReader().use { JsonParser.parseReader(it).asJsonArray.toList() }
    }

    private fun catalogPhoto(http: OkHttpClient, name: String, latitude: Double, longitude: Double): RemoteCityPhoto? {
        val point = com.kampplus.hava.model.GeoPoint(latitude, longitude)
        val match = catalog.map { it.asJsonObject }.firstOrNull { entry ->
            entry.getAsJsonArray("names").any { it.asString.equals(name.substringBefore(" / "), ignoreCase = true) } &&
                point.distanceKmTo(com.kampplus.hava.model.GeoPoint(entry["latitude"].asDouble, entry["longitude"].asDouble)) < 60
        } ?: return null
        val bitmap = download(http, match["image"].asString) ?: return null
        return RemoteCityPhoto(
            bitmap,
            match["place"].asString,
            match["author"].asString,
            match["license"].asString,
            match["source"].asString
        )
    }

    private fun download(http: OkHttpClient, source: String): Bitmap? {
        val url = HttpUrl.parse(source) ?: return null
        if (url.host() !in setOf("upload.wikimedia.org", "thumb.wikimedia.org") || url.scheme() != "https") return null
        return http.newCall(request(url.toString())).execute().use { response ->
            if (!response.isSuccessful) null else response.body()?.byteStream()?.use { BitmapFactory.decodeStream(it) }
        }
    }

    private fun request(url: String) = Request.Builder().url(url).header("User-Agent", "Climora/1.0 (Android city weather app)").build()
    private fun json(http: OkHttpClient, base: String, params: Map<String, String>): JsonObject {
        val url = requireNotNull(HttpUrl.parse(base)).newBuilder().addQueryParameter("format", "json")
        params.forEach { (key, value) -> url.addQueryParameter(key, value) }
        return http.newCall(request(url.build().toString())).execute().use {
            check(it.isSuccessful) { "Photo source unavailable" }
            JsonParser.parseString(it.body()?.string().orEmpty()).asJsonObject
        }
    }
}
