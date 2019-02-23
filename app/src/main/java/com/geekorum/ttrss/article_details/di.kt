/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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

import android.accounts.Account
import androidx.lifecycle.ViewModel
import com.geekorum.ttrss.accounts.NetworkLoginModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.di.AssistedFactoriesModule
import com.geekorum.ttrss.network.TinyrssApiModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap


/**
 * Dependency injection pieces for the article_details functionality
 */

/**
 * Provides the ViewModels of the article_details components.
 */
@Module
abstract class ViewModelsModule {
    @Binds
    @IntoMap
    @ClassKey(ArticleDetailsViewModel::class)
    abstract fun getArticleDetailsViewModel(articleDetailsViewModel: ArticleDetailsViewModel): ViewModel

}

/**
 * Provides the Activities injectors subcomponents.
 */
@Module
abstract class ActivitiesInjectorModule {
    @ContributesAndroidInjector(modules = [
        AssistedFactoriesModule::class,
        ViewModelsModule::class,
        NetworkLoginModule::class,
        TinyrssApiModule::class,
        SelectedAccountModule::class,
        FragmentsInjectorModule::class])
    @PerAccount
    internal abstract fun contributesArticleDetailsActivityInjector(): ArticleDetailActivity

}

/**
 * Provides the Fragments injectors subcomponents.
 */
@Module
abstract class FragmentsInjectorModule {

    @ContributesAndroidInjector(modules = [
        ViewModelsModule::class,
        TinyrssApiModule::class])
    internal abstract fun contributesArticleDetailsFragmentInjector(): ArticleDetailFragment

}

@Module
class SelectedAccountModule {

    @Provides
    fun providesAccount(articleDetailActivity: ArticleDetailActivity) : Account = articleDetailActivity.account!!
}
