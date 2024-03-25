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
package com.geekorum.ttrss.data

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

class ArticlesSearchHistoryRepository @Inject constructor(
    private val dataStore: ArticlesSearchHistoryDataStore
) {

    val searchHistory = dataStore.data.map {
        it.queriesList
    }

    suspend fun recordSearchQuery(query: String) {
        dataStore.updateData { data ->
            val newQueries = data.queriesList.toMutableList()
                .apply {
                    remove(query)
                    add(0, query)
                }
                .filter { it.isNotBlank() }
                .take(10)

            data.toBuilder()
                .clearQueries()
                .addAllQueries(newQueries)
                .build()
        }
    }
}

typealias ArticlesSearchHistoryDataStore = DataStore<ArticlesSearchHistory>

val Context.articlesSearchHistoryDatastore by dataStore(
    fileName = "articles_search_history.pb",
    serializer = ArticlesSearchHistorySerializer
)

private object ArticlesSearchHistorySerializer : Serializer<ArticlesSearchHistory> {
    override val defaultValue: ArticlesSearchHistory = ArticlesSearchHistory.getDefaultInstance()

    override suspend fun readFrom(input: InputStream): ArticlesSearchHistory {
        try {
            return ArticlesSearchHistory.parseFrom(input)
        } catch (e: InvalidProtocolBufferException) {
            throw CorruptionException("Can't read proto file", e)
        }
    }

    override suspend fun writeTo(t: ArticlesSearchHistory, output: OutputStream) {
        t.writeTo(output)
    }
}


@Module
@InstallIn(SingletonComponent::class)
object ArticlesSearchHistoryModule {

    @Provides
    @Singleton
    fun providesArticlesSearchHistoryDatastore(@ApplicationContext context: Context): ArticlesSearchHistoryDataStore {
        return context.articlesSearchHistoryDatastore
    }
}