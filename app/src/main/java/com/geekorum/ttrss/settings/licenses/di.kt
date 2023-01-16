/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.settings.licenses

import android.content.Context
import com.geekorum.geekdroid.osslicenses.LicenseInfoRepository
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
object LicenceInfoModule {
    @Provides
    fun providesLicenseInfoRepository(
        coroutineDispatchersProvider: CoroutineDispatchersProvider,
        @ApplicationContext appContext: Context
    ): LicenseInfoRepository {
        return LicenseInfoRepository(
            appContext,
            mainCoroutineDispatcher = coroutineDispatchersProvider.main,
            ioCoroutineDispatcher = coroutineDispatchersProvider.io
        )
    }
}


