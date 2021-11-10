/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2021 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.features_api

import android.accounts.AccountManager
import android.os.PowerManager
import com.geekorum.ttrss.accounts.AndroidTinyrssAccountManager
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.ManageFeedsDao
import okhttp3.OkHttpClient

/**
 * Provides dependencies for the ManageFeeds feature.
 * All classes returned by this interface must be part of the public api of the application.
 * If from an external lib, the lib must be in the api gradle configuration
 */
interface ManageFeedsDependencies {

    fun getApplication(): android.app.Application

    fun getAccountManager(): AccountManager

    fun getPowerManager(): PowerManager

    fun getManageFeedsDao(): ManageFeedsDao

    fun getOkHttpClient(): OkHttpClient

    fun getCoroutineDispatchersProvider(): CoroutineDispatchersProvider

    fun getAndroidTinyrssAccountManager(): AndroidTinyrssAccountManager
}
