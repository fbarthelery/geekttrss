/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
 *
 * This file is part of Geekttrss.
 *
 * Geekttrss is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Geekttrss is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Geekttrss.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.geekorum.favikonsnoop.snoopers

import com.geekorum.favikonsnoop.FaviconInfo
import com.geekorum.favikonsnoop.Snooper
import kotlinx.serialization.ImplicitReflectionSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.parse
import okhttp3.HttpUrl
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Request
import okio.BufferedSource
import org.jsoup.Jsoup

/**
 * https://www.w3.org/TR/appmanifest/
 */
class AppManifestSnooper internal constructor(
    private val webAppManifestParser: WebAppManifestParser
) : Snooper() {

    constructor() : this(WebAppManifestParser())

    override fun snoop(baseUrl: HttpUrl, content: BufferedSource): Collection<FaviconInfo> {
        val document = runCatching { Jsoup.parse(content.inputStream() , null, baseUrl.toString()) }

        val manifestUrl = document.getOrNull()?.head()?.let { head ->
            val manifestLinkElem = head.getElementsByTag("link").firstOrNull {
                "manifest" in it.attr("rel").split("\\s".toRegex())
            }
            manifestLinkElem?.attr("abs:href")?.toHttpUrl()
        }

        val appManifest = manifestUrl?.let {
            getAppManifest(it)
        }

        return appManifest?.icons?.flatMap {
            val url = manifestUrl.resolve(it.src) ?: return@flatMap emptyList<FaviconInfo>()
            val sizes = parseSizes(it.sizes ?: "")
            if (sizes.isEmpty()) {
                listOf(FaviconInfo(url.toString(),
                    mimeType = it.type)
                )
            } else {
                sizes.map { dimension ->
                    FaviconInfo(url.toString(),
                        mimeType = it.type,
                        dimension = dimension
                    )
                }
            }
        } ?: emptyList()
    }

    private fun getAppManifest(url: HttpUrl): WebAppManifest? {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()
        val response = okHttpClient.newCall(request).execute()
        return response.use { response ->
            if (response.isSuccessful) {
                response.body?.string()?.let {
                    webAppManifestParser.parseManifest(it)
                }
            } else null
        }
    }
}

/**
 * Only encode the fields that we care about
 */
@Serializable
data class WebAppManifest(
    val dir: JsonElement? = null,
    val lang: JsonElement? = null,
    // name is required
    val name: JsonElement,
    val short_name: JsonElement? = null,
    val description: JsonElement? = null,
    val scope: JsonElement? = null,
    // icons is required
    val icons: Collection<ImageResource>,
    val display: JsonElement? = null,
    val orientation: JsonElement? = null,
    val start_url: JsonElement? = null,
    val serviceworker: JsonElement? = null,
    val theme_color: JsonElement? = null,
    val related_applications: JsonElement? = null,
    val prefer_related_applications: JsonElement? = null,
    val background_color: JsonElement? = null,
    val categories: JsonElement? = null,
    val screenshots: JsonElement? = null,
    val iarc_rating_id: JsonElement? = null
)

@Serializable
data class ImageResource(
    val src: String,
    val sizes: String? = null,
    val type: String? = null,
    val purpose: String? = null,
    val platform: String? = null
)

internal class WebAppManifestParser(
    private val  json: Json = Json(JsonConfiguration.Stable)
) {
    @OptIn(ImplicitReflectionSerializer::class)
    fun parseManifest(content: String): WebAppManifest? {
        return try {
            json.parse(content)
        } catch (e: SerializationException) {
            null
        }
    }
}
