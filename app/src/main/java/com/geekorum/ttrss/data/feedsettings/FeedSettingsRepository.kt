/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.data.feedsettings

import android.content.Context
import androidx.datastore.core.CorruptionException
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import com.google.protobuf.InvalidProtocolBufferException
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream
import javax.inject.Inject
import javax.inject.Singleton

class FeedSettingsRepository @Inject constructor(
    private val datastore: DataStore<FeedsSettings>
) {

    fun getFeedSettings(feedId: Long) = datastore.data.map {
        it.settingsMap[feedId]
    }

    suspend fun updateFeedSettings(feedId: Long, feedSettings: FeedSettings) {
        datastore.updateData {
            it.copy {
                settings[feedId] = feedSettings
            }
        }
    }
}

private object FeedsSettingsSerializer : Serializer<FeedsSettings> {
    override val defaultValue: FeedsSettings = FeedsSettings.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): FeedsSettings {
        try {
            return FeedsSettings.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Can't read proto file", e)
        }    }

    override suspend fun writeTo(t: FeedsSettings, output: OutputStream) {
        t.writeTo(output)
    }
}


val Context.feedsSettingsDatastore by dataStore(
    fileName = "feeds_settings.pb",
    serializer = FeedsSettingsSerializer
)

@Module
@InstallIn(SingletonComponent::class)
object FeedsSettingsModule {

    @Provides
    @Singleton
    fun providesFeedsSettingsDatastore(@ApplicationContext context: Context): DataStore<FeedsSettings> {
        return context.feedsSettingsDatastore
    }
}