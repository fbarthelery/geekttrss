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
@file:JvmName("Di")

package com.geekorum.ttrss.article_details

import androidx.fragment.app.Fragment
import com.geekorum.geekdroid.dagger.FragmentKey
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.migration.DisableInstallInCheck
import dagger.multibindings.IntoMap


/**
 * Dependency injection pieces for the article_details functionality
 */


/**
 * Provides the Fragments injectors.
 */
@Module
@InstallIn(ActivityComponent::class)
abstract class FragmentsInjectorModule {

    @Binds
    @IntoMap
    @FragmentKey(ArticleDetailFragment::class)
    internal abstract fun providesArticleDetailsFragment(fragment: ArticleDetailFragment): Fragment

}

@Module
@DisableInstallInCheck
abstract class ResourcesWebFontProviderModule {

    @Binds
    internal abstract fun bindsWebFontProvider(webFontProvider: ResourcesWebFontProvider): WebFontProvider
}
