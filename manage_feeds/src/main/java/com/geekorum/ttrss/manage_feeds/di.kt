/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2019 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.manage_feeds

import androidx.lifecycle.ViewModel
import com.geekorum.geekdroid.dagger.FragmentFactoriesModule
import com.geekorum.geekdroid.dagger.ViewModelKey
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.di.FeatureScope
import com.geekorum.ttrss.di.ViewModelsModule
import com.geekorum.ttrss.features_api.ManageFeedsDependencies
import dagger.Binds
import dagger.Component
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap

@Component(dependencies = [ManageFeedsDependencies::class],
    modules = [FragmentFactoriesModule::class,
        ManageFeedModule::class,
        ViewModelsModule::class])
@FeatureScope
interface ManageFeedComponent {

    fun inject(activity: ManageFeedsActivity)
}

@Module
private abstract class ManageFeedModule {
    @Binds
    @IntoMap
    @ViewModelKey(ManageFeedViewModel::class)
    abstract fun bindManageFeedViewModel(vm: ManageFeedViewModel): ViewModel

    @Module
    companion object {

        @Provides
        @JvmStatic
        fun providesFeedsDao(articlesDatabase: ArticlesDatabase) = articlesDatabase.manageFeedsDao()
    }
}

