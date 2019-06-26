/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.webapi.model

import com.google.common.truth.Truth.assertThat
import kotlinx.serialization.KSerializer
import kotlinx.serialization.UnstableDefault
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializerByTypeToken
import kotlinx.serialization.typeTokenOf
import kotlin.test.Test
import com.geekorum.ttrss.webapi.model.Error.NOT_LOGGED_IN

@UnstableDefault
class JsonSerializationTest {

    @Test
    fun testThatLoginRequestPayloadDoCorrectJson() {
        val payload = LoginRequestPayload("myuser", "mypassword")
        val serializer = getSerializer<LoginRequestPayload>()
        val result = Json.stringify(serializer, payload)
        assertThat(result).isEqualTo("""
            {"user":"myuser","password":"mypassword","op":"login"}
        """.trimIndent())
    }

    @Test
    fun testThatLoginResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": null,
              "status": 0,
              "content": {"session_id": "XXX", "api_level": 3}
            }
        """.trimIndent()
        val serializer = getSerializer<LoginResponsePayload>()
        val result = Json.parse(serializer, jsonString)
        val expected = LoginResponsePayload(
            status = 0,
            content = LoginResponsePayload.Content("XXX", 3)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }

    @Test
    fun testThatLoginResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": null,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<LoginResponsePayload>()
        val result = Json.parse(serializer, jsonString)
        val expected = LoginResponsePayload(
            status = 1,
            content = LoginResponsePayload.Content(error = NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }


    @Test
    fun testThatUpdateArticleRequestPayloadDoCorrectJson() {
        val payload = UpdateArticleRequestPayload(
            "23",
            1,
            2 // UNREAD
        ).apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<UpdateArticleRequestPayload>()
        val result = Json.stringify(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","article_ids":"23","mode":1,"field":2,"data":null,"op":"updateArticle"}
        """.trimIndent())
    }

    @Test
    fun testThatUpdateArticleResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 0,
              "status": 0,
              "content": {"status": "OK", "updated": 1}
            }
        """.trimIndent()
        val serializer = getSerializer<UpdateArticleResponsePayload>()
        val result = Json.parse(serializer, jsonString)
        val expected = UpdateArticleResponsePayload(
            sequence = 0,
            status = 0,
            content = UpdateArticleResponsePayload.Content("OK", 1)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }

    @Test
    fun testThatUpdateArticleResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 0,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<UpdateArticleResponsePayload>()
        val result = Json.parse(serializer, jsonString)
        val expected = UpdateArticleResponsePayload(
            sequence = 0,
            status = 1,
            content = UpdateArticleResponsePayload.Content(error = NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content).isEqualTo(expected.content)
    }


    @Test
    fun testThatGetFeedsRequestPayloadDoCorrectJson() {
        val payload = GetFeedsRequestPayload(true, false,
            GetFeedsRequestPayload.CATEGORY_ID_ALL_EXCLUDE_VIRTUALS).apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<GetFeedsRequestPayload>()
        val result = Json.stringify(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","include_nested":true,"unread_only":false,"cat_id":-3,"op":"getFeeds"}
        """.trimIndent())
    }

    @Test
    fun testThatGetFeedsResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": [
                {
                  "feed_url": "https:\/\/medium.com\/feed\/google-developers",
                  "title": "Google Developers \u2014 Medium",
                  "id": 256,
                  "unread": 0,
                  "has_icon": true,
                  "cat_id": 2,
                  "last_updated": 1541010158,
                  "order_id": 4
                },
                {
                  "feed_url": "https:\/\/blog.mozilla.org\/beyond-the-code\/feed\/",
                  "title": "Beyond the Code",
                  "id": 192,
                  "unread": 0,
                  "has_icon": true,
                  "cat_id": 3,
                  "last_updated": 1541010042,
                  "order_id": 2
                },
                {
                  "feed_url": "https:\/\/www.genymotion.com\/blog\/feed\/",
                  "title": "Blog \u2013 Genymotion \u2013 Android Emulator for app testing",
                  "id": 272,
                  "unread": 0,
                  "has_icon": true,
                  "cat_id": 26,
                  "last_updated": 1541010322,
                  "order_id": 0
                }
              ]
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<Feed>>()
        val result = Json.parse(serializer, jsonString)
        val expected = ListResponsePayload(
            sequence = 2,
            status = 1,
            content = ListContent(listOf(
                Feed(id = 256,
                    title = "Google Developers \u2014 Medium",
                    url = "https://medium.com/feed/google-developers",
                    nbUnreadArticles = 0,
                    hasIcon = true,
                    categoryId = 2,
                    lastUpdatedTimestamp = 1541010158,
                    orderId = 4),
                Feed(id = 192,
                    title = "Beyond the Code",
                    url = "https://blog.mozilla.org/beyond-the-code/feed/",
                    nbUnreadArticles = 0,
                    hasIcon = true,
                    categoryId = 3,
                    lastUpdatedTimestamp = 1541010042,
                    orderId = 2),
                Feed(id = 272,
                    title = "Blog \u2013 Genymotion \u2013 Android Emulator for app testing",
                    url = "https://www.genymotion.com/blog/feed/",
                    nbUnreadArticles = 0,
                    hasIcon = true,
                    categoryId = 26,
                    lastUpdatedTimestamp = 1541010322,
                    orderId = 0)
            ))
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.list).isEqualTo(expected.content.list)
    }

    @Test
    fun testThatGetFeedsResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<Feed>>()
        val result = Json.parse(serializer, jsonString)
        val expected = ListResponsePayload<Feed>(
            sequence = 2,
            status = 1,
            content = ListContent(error = NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.error).isEqualTo(expected.content.error)
    }


    @Test
    fun testThatGetCategoriesRequestPayloadDoCorrectJson() {
        val payload = GetCategoriesRequestPayload(
            includeNested = true,
            unreadOnly = false
        ).apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<GetCategoriesRequestPayload>()
        val result = Json.stringify(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","include_nested":true,"unread_only":false,"op":"getCategories"}
        """.trimIndent())
    }

    @Test
    fun testThatGetCategoriesResponsePayloadDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": [
                {
                  "id": "39",
                  "title": "microservices",
                  "unread": 0,
                  "order_id": 0
                },
                {
                  "id": "2",
                  "title": "Android",
                  "unread": 12,
                  "order_id": 1
                }
              ]
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<FeedCategory>>()
        val result = Json.parse(serializer, jsonString)
        val expected = ListResponsePayload(
            sequence = 2,
            status = 1,
            content = ListContent(listOf(
                FeedCategory(id = 39, title = "microservices", nbUnreadArticles = 0, orderId = 0),
                FeedCategory(id = 2, title = "Android", nbUnreadArticles = 12, orderId = 1)
            ))
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.list).isEqualTo(expected.content.list)
    }


    @Test
    fun testThatGetCategoriesResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<FeedCategory>>()
        val result = Json.parse(serializer, jsonString)
        val expected = ListResponsePayload<FeedCategory>(
            sequence = 2,
            status = 1,
            content = ListContent(error = NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.error).isEqualTo(expected.content.error)
    }


    @Test
    fun testThatGetArticlesRequestPayloadDoCorrectJson() {
        val payload = GetArticlesRequestPayload(
            feedId = 256,
            viewMode = GetArticlesRequestPayload.ViewMode.ALL_ARTICLES,
            showContent = true,
            showExcerpt = false,
            skip = 10,
            sinceId = 200,
            limit = 10,
            orderBy = GetArticlesRequestPayload.SortOrder.TITLE
        ).apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<GetArticlesRequestPayload>()
        val result = Json.stringify(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","feed_id":256,"view_mode":"ALL_ARTICLES","show_content":true,"show_excerpt":false,"skip":10,"since_id":200,"limit":10,"order_by":"TITLE","op":"getHeadlines"}
        """.trimIndent())
    }

    @Test
    fun testThatGetArticlesResponsePayloadDoLoadCorrectly() {
        val jsonString = """
        {
          "seq": 2,
          "status": 1,
          "content": [
            {
              "id": 560752,
              "unread": true,
              "marked": false,
              "published": false,
              "updated": 1541045992,
              "is_updated": false,
              "title": "TMZ on TV 2018 10 30 480p x264-mSD",
              "link": "https:\/\/eztv.re\/ep\/1284471\/tmz-on-tv-2018-10-30-480p-x264-msd\/",
              "feed_id": "82",
              "tags": [
                "tv"
              ],
              "excerpt": "",
              "content": "",
              "labels": [],
              "feed_title": "ezRSS - Latest torrent releases",
              "comments_count": 0,
              "comments_link": "",
              "always_display_attachments": false,
              "author": "",
              "score": 0,
              "note": null,
              "lang": ""
            },
            {
              "id": 560718,
              "unread": false,
              "marked": false,
              "published": false,
              "updated": 1541039212,
              "is_updated": false,
              "title": "Images du jour 31\/10\/18 \u2013 Martinique \u2013 Philippot-",
              "link": "http:\/\/www.bondamanjak.com\/images-jour-31-10-18-martinique-philippot\/",
              "feed_id": "191",
              "tags": [
                "a la une",
                "aujourdhui en martinique",
                "breaking news",
                "martinique",
                "b\u00e9atrice bellay",
                "florian philippot",
                "marine dewo",
                "socialiste"
              ],
              "excerpt": "&hellip;",
              "content": "<html><body><header><h1 itemprop=\"mainEntityOfPage\">Images du jour 31\/10\/18 &ndash; Martinique &ndash; Philippot-<\/h1>\r\n        <p>\r\n        <span itemprop=\"dateCreated\">octobre 31st, 2018<\/span>                                    <\/p>\r\n        <\/header><div><p><img src=\"https:\/\/tinyrss.elyzion.net\/public.php?op=cached_image&amp;hash=daff495cb40939033ed3cbd5c20d0a8c7d37fc07\" alt=\"\" data-recalc-dims=\"1\"><img src=\"https:\/\/tinyrss.elyzion.net\/public.php?op=cached_image&amp;hash=473855ee3a04935e8c50c7f7ada769c2b0521bbe\" alt=\"\" data-recalc-dims=\"1\"><br>\nCe jour en #Martinique, une pinc&eacute;e de militants-es de Marine D&egrave;wo ont emp&ecirc;ch&eacute; la venue du d&eacute;put&eacute; europ&eacute;en Florian Philippot dans les studios de &laquo;&nbsp;RFO&nbsp;&raquo; Martinique La 1&egrave;re.&nbsp; On note la pr&eacute;sence d&eacute;termin&eacute;e et clairement affich&eacute;e de B&eacute;atrice Bellay la premi&egrave;re secr&eacute;taire de la F&eacute;d&eacute;ration socialiste de l&rsquo;&icirc;le.&nbsp; Comme quoi le kumquat.<\/p>\n<div><div data-a2a-url=\"http:\/\/www.bondamanjak.com\/images-jour-31-10-18-martinique-philippot\/\" data-a2a-title=\"Images du jour 31\/10\/18 &ndash; Martinique &ndash; Philippot-\"><a href=\"https:\/\/www.addtoany.com\/add_to\/facebook?linkurl=http%3A%2F%2Fwww.bondamanjak.com%2Fimages-jour-31-10-18-martinique-philippot%2F&amp;linkname=Images%20du%20jour%2031%2F10%2F18%20%E2%80%93%20Martinique%20%E2%80%93%20Philippot-\" title=\"Facebook\" rel=\"noreferrer\" target=\"_blank\"><\/a><a href=\"https:\/\/www.addtoany.com\/add_to\/twitter?linkurl=http%3A%2F%2Fwww.bondamanjak.com%2Fimages-jour-31-10-18-martinique-philippot%2F&amp;linkname=Images%20du%20jour%2031%2F10%2F18%20%E2%80%93%20Martinique%20%E2%80%93%20Philippot-\" title=\"Twitter\" rel=\"noreferrer\" target=\"_blank\"><\/a><a href=\"https:\/\/www.addtoany.com\/add_to\/google_plus?linkurl=http%3A%2F%2Fwww.bondamanjak.com%2Fimages-jour-31-10-18-martinique-philippot%2F&amp;linkname=Images%20du%20jour%2031%2F10%2F18%20%E2%80%93%20Martinique%20%E2%80%93%20Philippot-\" title=\"Google+\" rel=\"noreferrer\" target=\"_blank\"><\/a><a href=\"https:\/\/www.addtoany.com\/add_to\/whatsapp?linkurl=http%3A%2F%2Fwww.bondamanjak.com%2Fimages-jour-31-10-18-martinique-philippot%2F&amp;linkname=Images%20du%20jour%2031%2F10%2F18%20%E2%80%93%20Martinique%20%E2%80%93%20Philippot-\" title=\"WhatsApp\" rel=\"noreferrer\" target=\"_blank\"><\/a><a href=\"https:\/\/www.addtoany.com\/share\" rel=\"noreferrer\" target=\"_blank\"><\/a><\/div><\/div><\/div>\r\n<!-- start:tags -->\r\n<!-- end:tags --><!-- start:article footer --><!-- end:article footer --><div itemprop=\"publisher\" itemscope=\"\" itemtype=\"https:\/\/schema.org\/Organization\">\r\n    <div itemprop=\"logo\" itemscope=\"\" itemtype=\"https:\/\/schema.org\/ImageObject\">\r\n      <\/div>\r\n    <\/div>\r\n\r\n\r<\/body><\/html>\n",
              "labels": [],
              "feed_title": "Bondamanjak",
              "comments_count": 0,
              "comments_link": "http:\/\/www.bondamanjak.com\/images-jour-31-10-18-martinique-philippot\/#respond",
              "always_display_attachments": true,
              "author": "bondamanjak",
              "score": 0,
              "note": null,
              "lang": ""
            }]
        }
""".trimIndent()
        val serializer = getSerializer<ListResponsePayload<Headline>>()
        val result = Json.parse(serializer, jsonString)
        val expected = ListResponsePayload<Headline>(
            sequence = 2,
            status = 1,
            content = ListContent(listOf(
                Headline(id = 560752, unread = true, marked = false, published = false, lastUpdatedTimestamp = 1541045992,
                    isUpdated = false, title = "TMZ on TV 2018 10 30 480p x264-mSD",
                    link = "https://eztv.re/ep/1284471/tmz-on-tv-2018-10-30-480p-x264-msd/",
                    feedId = 82, tags = listOf("tv"), excerpt = "", content = "", labels = emptyList(),
                    feedTitle = "ezRSS - Latest torrent releases",
                    nbComments = 0, commentsLink = "", alwaysDisplayAttachment = false, author = "",
                    score = 0, note = null, lang = ""),
                Headline(id = 560718, unread = false, marked = false, published = false, lastUpdatedTimestamp = 1541039212,
                    isUpdated = false, title = "Images du jour 31/10/18 \u2013 Martinique \u2013 Philippot-",
                    link = "http://www.bondamanjak.com/images-jour-31-10-18-martinique-philippot/",
                    feedId = 191, tags = listOf(
                        "a la une",
                        "aujourdhui en martinique",
                        "breaking news",
                        "martinique",
                        "b\u00e9atrice bellay",
                        "florian philippot",
                        "marine dewo",
                        "socialiste"),
                    excerpt = "&hellip;",
                    content = "<html><body><header><h1 itemprop=\"mainEntityOfPage\">Images du jour 31/10/18 &ndash; Martinique &ndash; Philippot-</h1>\r\n        <p>\r\n        <span itemprop=\"dateCreated\">octobre 31st, 2018</span>                                    </p>\r\n        </header><div><p><img src=\"https://tinyrss.elyzion.net/public.php?op=cached_image&amp;hash=daff495cb40939033ed3cbd5c20d0a8c7d37fc07\" alt=\"\" data-recalc-dims=\"1\"><img src=\"https://tinyrss.elyzion.net/public.php?op=cached_image&amp;hash=473855ee3a04935e8c50c7f7ada769c2b0521bbe\" alt=\"\" data-recalc-dims=\"1\"><br>\nCe jour en #Martinique, une pinc&eacute;e de militants-es de Marine D&egrave;wo ont emp&ecirc;ch&eacute; la venue du d&eacute;put&eacute; europ&eacute;en Florian Philippot dans les studios de &laquo;&nbsp;RFO&nbsp;&raquo; Martinique La 1&egrave;re.&nbsp; On note la pr&eacute;sence d&eacute;termin&eacute;e et clairement affich&eacute;e de B&eacute;atrice Bellay la premi&egrave;re secr&eacute;taire de la F&eacute;d&eacute;ration socialiste de l&rsquo;&icirc;le.&nbsp; Comme quoi le kumquat.</p>\n<div><div data-a2a-url=\"http://www.bondamanjak.com/images-jour-31-10-18-martinique-philippot/\" data-a2a-title=\"Images du jour 31/10/18 &ndash; Martinique &ndash; Philippot-\"><a href=\"https://www.addtoany.com/add_to/facebook?linkurl=http%3A%2F%2Fwww.bondamanjak.com%2Fimages-jour-31-10-18-martinique-philippot%2F&amp;linkname=Images%20du%20jour%2031%2F10%2F18%20%E2%80%93%20Martinique%20%E2%80%93%20Philippot-\" title=\"Facebook\" rel=\"noreferrer\" target=\"_blank\"></a><a href=\"https://www.addtoany.com/add_to/twitter?linkurl=http%3A%2F%2Fwww.bondamanjak.com%2Fimages-jour-31-10-18-martinique-philippot%2F&amp;linkname=Images%20du%20jour%2031%2F10%2F18%20%E2%80%93%20Martinique%20%E2%80%93%20Philippot-\" title=\"Twitter\" rel=\"noreferrer\" target=\"_blank\"></a><a href=\"https://www.addtoany.com/add_to/google_plus?linkurl=http%3A%2F%2Fwww.bondamanjak.com%2Fimages-jour-31-10-18-martinique-philippot%2F&amp;linkname=Images%20du%20jour%2031%2F10%2F18%20%E2%80%93%20Martinique%20%E2%80%93%20Philippot-\" title=\"Google+\" rel=\"noreferrer\" target=\"_blank\"></a><a href=\"https://www.addtoany.com/add_to/whatsapp?linkurl=http%3A%2F%2Fwww.bondamanjak.com%2Fimages-jour-31-10-18-martinique-philippot%2F&amp;linkname=Images%20du%20jour%2031%2F10%2F18%20%E2%80%93%20Martinique%20%E2%80%93%20Philippot-\" title=\"WhatsApp\" rel=\"noreferrer\" target=\"_blank\"></a><a href=\"https://www.addtoany.com/share\" rel=\"noreferrer\" target=\"_blank\"></a></div></div></div>\r\n<!-- start:tags -->\r\n<!-- end:tags --><!-- start:article footer --><!-- end:article footer --><div itemprop=\"publisher\" itemscope=\"\" itemtype=\"https://schema.org/Organization\">\r\n    <div itemprop=\"logo\" itemscope=\"\" itemtype=\"https://schema.org/ImageObject\">\r\n      </div>\r\n    </div>\r\n\r\n\r</body></html>\n",
                    labels = emptyList(),
                    feedTitle = "Bondamanjak",
                    nbComments = 0,
                    commentsLink = "http://www.bondamanjak.com/images-jour-31-10-18-martinique-philippot/#respond",
                    alwaysDisplayAttachment = true, author = "bondamanjak",
                    score = 0, note = null, lang = "")
            ))
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.list).isEqualTo(expected.content.list)
    }

    @Test
    fun testThatGetArticlesResponsePayloadWithErrorDoLoadCorrectly() {
        val jsonString = """
            {
              "seq": 2,
              "status": 1,
              "content": {"error":"NOT_LOGGED_IN"}
            }
        """.trimIndent()
        val serializer = getSerializer<ListResponsePayload<Headline>>()
        val result = Json.parse(serializer, jsonString)
        val expected = ListResponsePayload<Headline>(
            sequence = 2,
            status = 1,
            content = ListContent(error = NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.content.error).isEqualTo(expected.content.error)
    }

}

internal inline fun <reified T> getSerializer(): KSerializer<T> {
    val typeToken = typeTokenOf<T>()
    // we use type token because that's what RetrofitConverter use to get the serializer
    @Suppress("UNCHECKED_CAST")
    return serializerByTypeToken(typeToken) as KSerializer<T>

}
