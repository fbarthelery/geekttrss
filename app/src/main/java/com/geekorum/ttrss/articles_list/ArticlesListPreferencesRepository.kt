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
package com.geekorum.ttrss.articles_list

import android.content.SharedPreferences
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.channels.trySendBlocking
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject

private const val PREF_VIEW_MODE = "view_mode"
private const val PREF_SORT_ORDER = "sort_order"
private const val PREF_ARTICLES_COMPACT_LIST_ITEMS = "articles_compact_list_item"

//TODO migrate to datastore
class ArticlesListPreferencesRepository @Inject constructor(
    private val prefs: SharedPreferences
) {

    fun getSortOrder(): Flow<String> {
        return callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == PREF_SORT_ORDER) {
                    trySendBlocking(prefs.getString(PREF_SORT_ORDER, "most_recent_first")!!)
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            val initial = prefs.getString(PREF_SORT_ORDER, "most_recent_first")!!
            send(initial)
            awaitClose {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }

    fun getViewMode(): Flow<String> {
        return callbackFlow {
            val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
                if (key == PREF_SORT_ORDER) {
                    trySendBlocking(prefs.getString(PREF_VIEW_MODE, "adaptive")!!)
                }
            }
            prefs.registerOnSharedPreferenceChangeListener(listener)
            val initial = prefs.getString(PREF_VIEW_MODE, "adaptive")!!
            send(initial)
            awaitClose {
                prefs.unregisterOnSharedPreferenceChangeListener(listener)
            }
        }
    }

    fun getDisplayCompactArticles() = callbackFlow {
        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == PREF_ARTICLES_COMPACT_LIST_ITEMS) {
                trySendBlocking(prefs.getBoolean(PREF_ARTICLES_COMPACT_LIST_ITEMS, false))
            }
        }
        prefs.registerOnSharedPreferenceChangeListener(listener)
        val initial = prefs.getBoolean(PREF_ARTICLES_COMPACT_LIST_ITEMS, false)
        send(initial)
        awaitClose {
            prefs.unregisterOnSharedPreferenceChangeListener(listener)
        }
    }


    fun setSortByMostRecentFirst(mostRecentFirst: Boolean) {
        if (mostRecentFirst) {
            prefs.edit().putString(PREF_SORT_ORDER, "most_recent_first").apply()
        } else {
            prefs.edit().putString(PREF_SORT_ORDER, "oldest_first").apply()
        }
    }

    fun setViewMode(viewMode: String) {
        prefs.edit().putString(PREF_VIEW_MODE, viewMode).apply()
    }
}