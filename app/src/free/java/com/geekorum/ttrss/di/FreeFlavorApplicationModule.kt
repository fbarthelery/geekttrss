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
package com.geekorum.ttrss.di

import com.geekorum.ttrss.AllFeaturesInstalledModule
import com.geekorum.ttrss.app_reviews.NoAppReviewModule
import com.geekorum.ttrss.article_details.ResourcesWebFontProviderModule
import com.geekorum.ttrss.in_app_update.NoInAppUpdateModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module(includes = [
    ResourcesWebFontProviderModule::class,
    AllFeaturesInstalledModule::class,
    NoInAppUpdateModule::class,
    NoAppReviewModule::class,
])
@InstallIn(SingletonComponent::class)
interface FreeFlavorApplicationModule
