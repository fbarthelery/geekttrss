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
package com.geekorum.ttrss.sync

import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.webapi.ApiCallException
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import okio.buffer
import okio.sink
import java.io.File
import kotlin.jvm.Throws

/**
 * Use TinyTinyRss api to download Feed icon.
 * Method is available since TinyTinyRss api level 19
 */
class FeedIconApiDownloader @AssistedInject constructor(
    private val apiService: ApiService,
    @Assisted private val downloadDir: File,
) {

    @AssistedFactory
    interface Factory {
        fun create(downloadDir: File): FeedIconApiDownloader
    }

    init {
        downloadDir.mkdirs()
    }

    @Throws(ApiCallException::class)
    suspend fun downloadFeedIcon(feed: Feed): File {
        val iconSource = apiService.getFeedIcon(feed.id)
        val downloadFile = downloadDir.resolve("${feed.id}.ico")
        iconSource.use { source ->
            downloadFile.sink().buffer().use { sink ->
                source.readAll(sink)
            }
        }
        return downloadFile
    }
 }