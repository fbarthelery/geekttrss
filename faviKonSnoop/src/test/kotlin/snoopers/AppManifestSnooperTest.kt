/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
import com.geekorum.favikonsnoop.await
import com.geekorum.favikonsnoop.source
import com.google.common.truth.Truth.assertThat
import io.mockk.*
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import okhttp3.HttpUrl.Companion.toHttpUrl
import okhttp3.Protocol.HTTP_1_1
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Test
import kotlin.test.BeforeTest

private val SIMPLE_MANIFEST = """
{
  "name": "Donate App",
  "description": "This app helps you donate to worthy causes.",
  "icons": [{
    "src": "images/icon.png"
  }]
}
""".trimIndent()

private val TYPICAL_MANIFEST = """
{
  "lang": "en",
  "dir": "ltr",
  "name": "Super Racer 3000",
  "description": "The ultimate futuristic racing game from the future!",
  "short_name": "Racer3K",
  "icons": [{
    "src": "icon/lowres.webp",
    "sizes": "64x64",
    "type": "image/webp"
  },{
    "src": "icon/lowres.png",
    "sizes": "64x64"
  }, {
    "src": "icon/hd_hi",
    "sizes": "128x128"
  }],
  "scope": "/racer/",
  "start_url": "/racer/start.html",
  "display": "fullscreen",
  "orientation": "landscape",
  "theme_color": "aliceblue",
  "background_color": "red",
  "serviceworker": {
    "src": "sw.js",
    "scope": "/racer/",
    "update_via_cache": "none"
  },
  "screenshots": [{
    "src": "screenshots/in-game-1x.jpg",
    "sizes": "640x480",
    "type": "image/jpeg"
  },{
    "src": "screenshots/in-game-2x.jpg",
    "sizes": "1280x920",
    "type": "image/jpeg"
  }]
}    
""".trimIndent()

private const val INVALID_MANIFEST = "{}"

private val INVALID_HTML =
    """fw""".trimIndent()

private val NO_MANIFEST_HTML = """
    <html lang="en">
     <head>
      <title>lsForums — Inbox</title>
      <link rel=icon href=favicon.png sizes="16x16" type="image/png">
      <script src=lsforums.js></script>
      <meta name=application-name content="lsForums">
     </head>
    </html>
""".trimIndent()

private val WITH_MANIFEST_HTML = """
    <html lang="en">
     <head>
      <title>lsForums — Inbox</title>
      <link rel="manifest" href=/static/manifest.json >
      <link rel=icon href=favicon.png sizes="16x16" type="image/png">
     </head>
    </html>
""".trimIndent()


class AppManifestSnooperTest {
    lateinit var subject: AppManifestSnooper
    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    @BeforeTest
    fun setUp() {
        subject = AppManifestSnooper(testDispatcher)
    }

    @Test
    fun testInvalidHtmlReturnsEmpty() = testScope.runTest {
        val result = INVALID_HTML.source().use {
            subject.snoop("http://exemple.com", it)
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun testHtmlWithoutManifestReturnsEmpty() = testScope.runTest {
        val result = NO_MANIFEST_HTML.source().use {
            subject.snoop("http://exemple.com", it)
        }

        assertThat(result).isEmpty()
    }

    @Test
    fun testHtmlWithInvalidManifestReturnsEmpty() = testScope.runTest {
        mockkStatic("com.geekorum.favikonsnoop.FaviKonSnoopKt")
        subject.okHttpClient = mockk()
        val requestSlot = slot<Request>()

        coEvery { subject.okHttpClient.newCall(capture(requestSlot)).await() } answers {
            Response.Builder()
                .code(200)
                .request(requestSlot.captured)
                .protocol(HTTP_1_1)
                .message("ok")
                .body(INVALID_MANIFEST.toResponseBody())
                .build()
        }

        val result = WITH_MANIFEST_HTML.source().use {
            subject.snoop("http://exemple.com", it)
        }
        assertThat(requestSlot.captured.url).isEqualTo("http://exemple.com/static/manifest.json".toHttpUrl())
        assertThat(result).isEmpty()
    }

    @Test
    fun testHtmlWithSimpleManifestReturnsSimpleResult() = testScope.runTest {
        mockkStatic("com.geekorum.favikonsnoop.FaviKonSnoopKt")
        subject.okHttpClient = mockk()
        val requestSlot = slot<Request>()
        coEvery { subject.okHttpClient.newCall(capture(requestSlot)).await() } answers {
            Response.Builder()
                .code(200)
                .request(requestSlot.captured)
                .protocol(HTTP_1_1)
                .message("ok")
                .body(SIMPLE_MANIFEST.toResponseBody())
                .build()
        }
        val result = WITH_MANIFEST_HTML.source().use {
            subject.snoop("http://exemple.com", it)
        }
        assertThat(requestSlot.captured.url).isEqualTo("http://exemple.com/static/manifest.json".toHttpUrl())
        assertThat(result).containsExactly(
            FaviconInfo("http://exemple.com/static/images/icon.png")
        )
    }

}

class WebAppManifestParserTest {
    private lateinit var subject: WebAppManifestParser

    @BeforeTest
    fun setUp() {
        subject = WebAppManifestParser()
    }

    @Test
    fun parseSimpleWebAppManifest() {
        val result = subject.parseManifest(SIMPLE_MANIFEST)
        assertThat(result!!.icons).containsExactly(
            ImageResource("images/icon.png")
        )
    }

    @Test
    fun parseTypicalWebAppManifest() {
        val result = subject.parseManifest(TYPICAL_MANIFEST)
        assertThat(result!!.icons).containsExactly(
            ImageResource("icon/lowres.webp",
                sizes = "64x64",
                type = "image/webp"),
            ImageResource("icon/lowres.png",
                sizes = "64x64"),
            ImageResource("icon/hd_hi",
                sizes = "128x128")
        )
    }

    @Test
    fun parseInvalidManifest() {
        val result = subject.parseManifest(INVALID_MANIFEST)
        assertThat(result).isNull()
    }

}
