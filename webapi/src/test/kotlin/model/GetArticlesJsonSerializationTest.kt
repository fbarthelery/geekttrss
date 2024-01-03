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
package com.geekorum.ttrss.webapi.model

import com.geekorum.ttrss.webapi.Json
import com.geekorum.ttrss.webapi.error
import com.google.common.truth.Truth.assertThat
import kotlin.test.Test

class GetArticlesJsonSerializationTest {

    @Test
    fun testThatGetArticlesRequestPayloadDoCorrectJson() {
        val payload = GetArticlesRequestPayload(
            feedId = 256,
            viewMode = GetArticlesRequestPayload.ViewMode.ALL_ARTICLES,
            showContent = true,
            showExcerpt = false,
            includeAttachments = true,
            skip = 10,
            sinceId = 200,
            limit = 10,
            orderBy = GetArticlesRequestPayload.SortOrder.TITLE
        ).apply {
            sessionId = "SESSION_ID"
        }
        val serializer = getSerializer<GetArticlesRequestPayload>()
        val result = Json.encodeToString(serializer, payload)
        assertThat(result).isEqualTo("""
            {"sid":"SESSION_ID","feed_id":256,"view_mode":"all_articles","show_content":true,"show_excerpt":false,"include_attachments":true,"skip":10,"since_id":200,"limit":10,"order_by":"title","op":"getHeadlines"}
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
              "labels": [
                [
                  -1029,
                  "Beem",
                  "#7fff00",
                  "#000000"
                ],
                [
                  -1028,
                  "other",
                  "#7fff00",
                  "#0000ff"
                ]
              ],
              "feed_title": "Bondamanjak",
              "comments_count": 0,
              "comments_link": "http:\/\/www.bondamanjak.com\/images-jour-31-10-18-martinique-philippot\/#respond",
              "always_display_attachments": true,
              "author": "bondamanjak",
              "score": 0,
              "note": null,
              "lang": "fr",
              "site_url": "http:\/\/www.bondamanjak.com"
            },
            {
              "id": 560198,
              "unread": false,
              "marked": false,
              "published": false,
              "updated": 1541039212,
              "is_updated": false,
              "title": "Some article from apiLevel 14 and tt-rss version 19.8",
              "link": "https:\/\/discourse.tt-rss.org\/t\/blacklisted-by-feedburner-ever-happened-to-anyone\/2677\/9",
              "feed_id": 4,
              "tags": [""],
              "excerpt": "",
              "content": "<html><body><header><h1>Some article from apiLevel 14 and tt-rss version 19.8<\/h1><\/header><\/body><\/html>",
              "labels": [],
              "feed_title": "Tiny Tiny RSS: Forum",
              "comments_count": 0,
              "comments_link": "",
              "always_display_attachments": true,
              "author": "anonymous",
              "score": 0,
              "note": null,
              "lang": "",
              "flavor_image":"https:\/\/discourse.tt-rss.org\/user_avatar\/discourse.tt-rss.org\/fox\/40\/889_2.png",
              "flavor_stream":"",
              "flavor_kind": 1
            },
            {
              "always_display_attachments" : false,
              "attachments" : [
                {
                  "0" : 313085,
                  "1" : "https://traffic.libsyn.com/rands/theimportantthing0024.mp3",
                  "2" : "audio/mpeg",
                  "3" : "",
                  "4" : "0",
                  "5" : 694227,
                  "6" : 0,
                  "7" : 0,
                  "content_type" : "audio/mpeg",
                  "content_url" : "https://traffic.libsyn.com/rands/theimportantthing0024.mp3",
                  "duration" : "0",
                  "height" : 0,
                  "id" : 313085,
                  "post_id" : 694227,
                  "title" : "",
                  "width" : 0
                },
                {
                   "0" : 189026,
                   "1" : "http://0.gravatar.com/avatar/0c686ee27e835834c44ad17115185e12?s=96&d=identicon&r=PG",
                   "2" : "",
                   "3" : "",
                   "4" : "",
                   "5" : 499753,
                   "6" : 0,
                   "7" : 0,
                   "content_type" : "",
                   "content_url" : "http://0.gravatar.com/avatar/0c686ee27e835834c44ad17115185e12?s=96&d=identicon&r=PG",
                   "duration" : "",
                   "height" : 0,
                   "id" : 189026,
                   "post_id" : 499753,
                   "title" : "",
                   "width" : 0
                }
              ],
              "author" : "rands",
              "comments_count" : 0,
              "comments_link" : "https://randsinrepose.com/archives/the-one-about-disney/#respond",
              "content" : "",
              "feed_id" : 181,
              "feed_title" : "Rands In Repose",
              "flavor_image" : "https://i1.wp.com/randsinrepose.com/wp-content/uploads/2019/06/The-Important-Thing-Logo-250-1.png?resize=250%2C250&ssl=1",
              "flavor_stream" : "",
              "guid" : "SHA1:f92464de4b9031863776a256af9bf86c60eb4082",
              "id" : 694227,
              "is_updated" : false,
              "labels" : [],
              "lang" : "en",
              "link" : "https://randsinrepose.com/archives/the-one-about-disney/",
              "marked" : false,
              "note" : null,
              "published" : false,
              "score" : 0,
              "tags" : [
                "the important thing"
              ],
              "title" : "The One About Disney",
              "unread" : false,
              "updated" : 1574440244
            }
          ]
        }
""".trimIndent()
        val serializer = getSerializer<ListResponsePayload<Headline>>()
        val result = Json.decodeFromString(serializer, jsonString)
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
                    labels = listOf(LabelInfo(-1029, "Beem", "#7fff00", "#000000"),
                        LabelInfo(-1028, "other", "#7fff00", "#0000ff")),
                    feedTitle = "Bondamanjak",
                    nbComments = 0,
                    commentsLink = "http://www.bondamanjak.com/images-jour-31-10-18-martinique-philippot/#respond",
                    alwaysDisplayAttachment = true, author = "bondamanjak",
                    score = 0, note = null, lang = "fr",
                    siteUrl = "http://www.bondamanjak.com"),
                Headline(id = 560198, unread = false, marked = false, published = false, lastUpdatedTimestamp = 1541039212,
                    isUpdated = false, title = "Some article from apiLevel 14 and tt-rss version 19.8",
                    link = "https://discourse.tt-rss.org/t/blacklisted-by-feedburner-ever-happened-to-anyone/2677/9",
                    feedId = 4, tags = listOf(""),
                    excerpt = "",
                    content = "<html><body><header><h1>Some article from apiLevel 14 and tt-rss version 19.8</h1></header></body></html>",
                    labels = emptyList(),
                    feedTitle = "Tiny Tiny RSS: Forum",
                    nbComments = 0,
                    commentsLink = "",
                    alwaysDisplayAttachment = true,
                    author = "anonymous",
                    score = 0, note = null, lang = "",
                    flavorImage = "https://discourse.tt-rss.org/user_avatar/discourse.tt-rss.org/fox/40/889_2.png",
                    flavorStream = "",
                    flavorKind = FlavorKind.ALBUM,
                ),
                    Headline(id = 694227, unread = false, marked = false, published = false, lastUpdatedTimestamp = 1574440244,
                            isUpdated = false, title = "The One About Disney",
                            link = "https://randsinrepose.com/archives/the-one-about-disney/",
                            feedId = 181, tags = listOf("the important thing"),
                            excerpt = "",
                            content = "",
                            labels = emptyList(),
                            feedTitle = "Rands In Repose",
                            nbComments = 0,
                            commentsLink = "https://randsinrepose.com/archives/the-one-about-disney/#respond",
                            alwaysDisplayAttachment = false,
                            author = "rands",
                            score = 0, note = null, lang = "en",
                            flavorImage = "https://i1.wp.com/randsinrepose.com/wp-content/uploads/2019/06/The-Important-Thing-Logo-250-1.png?resize=250%2C250&ssl=1",
                            flavorStream = "",
                            guid = "SHA1:f92464de4b9031863776a256af9bf86c60eb4082",
                            attachments = listOf(
                                    Attachment(id = 313085,
                                            indexedId = 313085,
                                            postId = 694227,
                                            indexedPostId = 694227,
                                            contentUrl = "https://traffic.libsyn.com/rands/theimportantthing0024.mp3",
                                            indexedContentUrl = "https://traffic.libsyn.com/rands/theimportantthing0024.mp3",
                                            contentType = "audio/mpeg", indexedContentType = "audio/mpeg",
                                            title = "", indexedTitle = "",
                                            duration = 0, indexedDuration = 0,
                                            width = 0, indexedWidth = 0,
                                            height = 0, indexedHeight = 0
                                            ),
                                Attachment(id = 189026,
                                    indexedId = 189026,
                                    postId = 499753,
                                    indexedPostId = 499753,
                                    contentUrl = "http://0.gravatar.com/avatar/0c686ee27e835834c44ad17115185e12?s=96&d=identicon&r=PG",
                                    indexedContentUrl = "http://0.gravatar.com/avatar/0c686ee27e835834c44ad17115185e12?s=96&d=identicon&r=PG",
                                    contentType = "", indexedContentType = "",
                                    title = "", indexedTitle = "",
                                    duration = 0, indexedDuration = 0,
                                    width = 0, indexedWidth = 0,
                                    height = 0, indexedHeight = 0
                                            )

                            )
                    )
            ))
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.result).containsExactlyElementsIn(expected.result)
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
        val result = Json.decodeFromString(serializer, jsonString)
        val expected = ListResponsePayload<Headline>(
            sequence = 2,
            status = 1,
            content = ErrorContent(error = Error.NOT_LOGGED_IN)
        )
        assertThat(result.sequence).isEqualTo(expected.sequence)
        assertThat(result.status).isEqualTo(expected.status)
        assertThat(result.error).isEqualTo(expected.error)
    }
}
