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
package com.geekorum.ttrss.htmlparsers

import com.google.common.truth.Truth.assertThat
import org.jsoup.Jsoup
import kotlin.test.BeforeTest
import kotlin.test.Test

private const val emptyHtmlDoc = ""
private const val htmlDocWithoutImgs = "<html></html>"


private val htmlDocWithOneImg = """
    <html>
    <body>
    <div class="article-container ajax-container" id="container-post526156"  data-conf='{"prev":440463,"action":"singleVideo"}'>
    <div class="post-cover cover-pop-culture">
    <div class="post-cover__opacity-filter"></div>
    <img class="post-cover__background-image" src="https://www.numerama.com/content/uploads/2019/06/warhammer-astartes.jpg" />
    </div>
    </body>
    </html>
""".trimIndent()
private const val htmlDocOneImgUrl = "https://www.numerama.com/content/uploads/2019/06/warhammer-astartes.jpg"

private val htmlDocWithManyImg = """
    <html>
    <body>
    <div class="article-container ajax-container" id="container-post526156"  data-conf='{"prev":440463,"action":"singleVideo"}'>
    <div class="post-cover cover-pop-culture">
    <div class="post-cover__opacity-filter"></div>
    <img class="post-cover__background-image" src="https://www.numerama.com/content/uploads/2019/06/warhammer-astartes.jpg" />
    </div>
    <div class="img-container">
        <a href="https://www.numerama.com/startup/youtube">
            <img src="https://c0.lestechnophiles.com/www.numerama.com/content/uploads/2016/01/youtube.png?resize=230,230">
        </a>
    </div>
    </body>
    </html>
""".trimIndent()
private val htmlDocManyImgUrls = listOf(
    "https://www.numerama.com/content/uploads/2019/06/warhammer-astartes.jpg",
    "https://c0.lestechnophiles.com/www.numerama.com/content/uploads/2016/01/youtube.png?resize=230,230"
    )


class ImageUrlExtractorTest {

    lateinit var subject: ImageUrlExtractor

    @BeforeTest
    fun setUp() {
        subject = ImageUrlExtractor()
    }

    @Test
    fun testThatWhenEmptyHtmlDocReturnsNoUrl() {
        val doc = Jsoup.parse(emptyHtmlDoc)
        val result = subject.extract(doc)
        assertThat(result).isEmpty()
    }

    @Test
    fun testThatWhenHtmlDocWithoutImgsReturnsNoUrl() {
        val doc = Jsoup.parse(htmlDocWithoutImgs)
        val result = subject.extract(doc)
        assertThat(result).isEmpty()
    }

    @Test
    fun testThatWhenHtmlDocWithOneImgReturnsCorrectUrl() {
        val doc = Jsoup.parse(htmlDocWithOneImg)
        val result = subject.extract(doc)
        assertThat(result).containsExactly(htmlDocOneImgUrl)
    }

    @Test
    fun testThatWhenHtmlDocWithManysReturnCorrectUrls() {
        val doc = Jsoup.parse(htmlDocWithManyImg)
        val result = subject.extract(doc)
        assertThat(result).containsExactlyElementsIn(htmlDocManyImgUrls)
    }

}
