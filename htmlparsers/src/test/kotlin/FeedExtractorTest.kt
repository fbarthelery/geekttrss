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
package com.geekorum.ttrss.htmlparsers

import com.google.common.truth.Truth.assertThat
import org.jsoup.Jsoup
import kotlin.test.BeforeTest
import kotlin.test.Test

private const val emptyHtmlDoc = ""
private const val htmlDocWithoutFeeds = "<html></html>"

private val htmlDocWithOneRssFeed = """
    <html>
        <head>
            <link rel="alternate" type="application/rss+xml" title="RSS Advisory Board" href="http://feeds.rssboard.org/rssboard" />
        </head>
    </html>
    """.trimIndent()

private val rssAdvisoryBoardFeedInfo = FeedInformation(
    href = "http://feeds.rssboard.org/rssboard",
    type = "application/rss+xml", title = "RSS Advisory Board")

private val htmlDocWithOneRelativeRssFeed = """
    <html>
        <head>
            <title>RSS Advisory Board</title>
            <base href="http://feeds.rssboard.org/">
            <link rel="alternate" type="application/rss+xml" href="rssboard">
        </head>
    </html>
    """.trimIndent()

private val rssAdvisoryBoardFeedInfoNoTitle = FeedInformation(
    href = "http://feeds.rssboard.org/rssboard",
    type = "application/rss+xml")


private val htmlDocWithOneAtomFeed = """
      <html>
        <head>
            <link rel="dns-prefetch" href="https://user-images.githubusercontent.com/">
            <meta name="viewport" content="width=device-width">
            <link href="https://github.com/codepath/android_guides/commits/master.atom" rel="alternate" title="Recent Commits to android_guides:master" type="application/atom+xml">
        </head>
    </html>
""".trimIndent()

private val githubRecentCommitsFeedInfo =
    FeedInformation(
        href = "https://github.com/codepath/android_guides/commits/master.atom",
        type = "application/atom+xml", title = "Recent Commits to android_guides:master")

private val htmlDocWithCombinedFeeds = """
      <html>
        <head>
            <title>RSS Advisory Board</title>
            <link href="https://github.com/codepath/android_guides/commits/master.atom" rel="alternate" title="Recent Commits to android_guides:master" type="application/atom+xml">
            <link rel="dns-prefetch" href="https://user-images.githubusercontent.com/">
            <link rel="alternate" type="application/rss+xml" href="rssboard">
            <base href="http://feeds.rssboard.org/">
            <link rel="alternate" type="application/rss+xml" title="RSS Advisory Board" href="http://feeds.rssboard.org/rssboard" />
            <meta name="viewport" content="width=device-width">
        </head>
    </html>
""".trimIndent()


class FeedExtractorTest {

    lateinit var feedExtractor: FeedExtractor

    @BeforeTest
    fun setUp() {
        feedExtractor = FeedExtractor()
    }

    @Test
    fun testThatWhenEmptyHtmlDocReturnNoFeedInformation() {
        val doc = Jsoup.parse(emptyHtmlDoc)
        val result = feedExtractor.extract(doc)
        assertThat(result).isEmpty()
    }

    @Test
    fun testThatWhenHtmlDocWithoutFeedsReturnNoFeedInformation() {
        val doc = Jsoup.parse(htmlDocWithoutFeeds)
        val result = feedExtractor.extract(doc)
        assertThat(result).isEmpty()
    }

    @Test
    fun testThatWhenHtmlDocWithRssFeedsReturnCorrectFeedInfo() {
        val doc = Jsoup.parse(htmlDocWithOneRssFeed)
        val result = feedExtractor.extract(doc)
        assertThat(result).containsExactly(rssAdvisoryBoardFeedInfo)
    }

    @Test
    fun testThatWhenHtmlDocWithAtomFeedsReturnCorrectFeedInfo() {
        val doc = Jsoup.parse(htmlDocWithOneAtomFeed)
        val result = feedExtractor.extract(doc)
        assertThat(result).containsExactly(githubRecentCommitsFeedInfo)
    }

    @Test
    fun testThatWhenHtmlDocWithRelativeRssFeedsReturnCorrectFeedInfo() {
        val doc = Jsoup.parse(htmlDocWithOneRelativeRssFeed)
        val result = feedExtractor.extract(doc)
        assertThat(result).containsExactly(rssAdvisoryBoardFeedInfoNoTitle)
    }

    @Test
    fun testThatWhenHtmlDocWithCombinedFeedsReturnCorrectFeedInfo() {
        val result = feedExtractor.extract(htmlDocWithCombinedFeeds)
        assertThat(result).containsExactly(rssAdvisoryBoardFeedInfoNoTitle,
            rssAdvisoryBoardFeedInfo,
            githubRecentCommitsFeedInfo)
    }

}
