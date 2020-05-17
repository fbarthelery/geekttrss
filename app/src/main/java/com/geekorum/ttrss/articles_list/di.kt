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

package com.geekorum.ttrss.articles_list

import android.accounts.Account
import android.app.Application
import android.content.SharedPreferences
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.preference.PreferenceManager
import com.geekorum.geekdroid.dagger.FragmentKey
import com.geekorum.geekdroid.dagger.ViewModelAssistedFactory
import com.geekorum.geekdroid.dagger.ViewModelKey
import com.geekorum.ttrss.accounts.NetworkLoginModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.di.AssistedFactoriesModule
import com.geekorum.ttrss.in_app_update.InAppUpdateViewModel
import com.geekorum.ttrss.network.TinyrssApiModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.IntoMap


/**
 * Dependency injection pieces for the article_list functionality.
 *
 * ArticleListActivity has a SubComponent of the ApplicationComponent.
 * Each Fragment contained by ArticleListActivity has a SubComponent of ArticleListActivitySubComponent
 *
 * ArticleListActivity's component provides the Account selected
 */


/**
 * Provides the Activities injectors subcomponents.
 */
@Module
abstract class ActivitiesInjectorModule {

    @ContributesAndroidInjector(modules = [
        AssistedFactoriesModule::class,
        ArticlesListModule::class,
        com.geekorum.ttrss.article_details.FragmentsInjectorModule::class,
        com.geekorum.ttrss.articles_list.search.FragmentsInjectorModule::class,
        TinyrssApiModule::class,
        NetworkLoginModule::class])
    @PerAccount
    internal abstract fun contributesArticleListActivityInjector(): ArticleListActivity

}

/**
 * Provides the dependencies for article lists
 */
@Module
private abstract class ArticlesListModule {

    @Binds
    @IntoMap
    @FragmentKey(ArticlesListFragment::class)
    abstract fun bindArticlesListFragment(articlesListFragment: ArticlesListFragment): Fragment

    @Binds
    @IntoMap
    @ViewModelKey(ArticlesViewModel::class)
    abstract fun getArticlesFragmentViewModel(articlesViewModel: ArticlesViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    @IntoMap
    @ViewModelKey(FeedsViewModel::class)
    abstract fun getFeedsViewModel(feedsViewModel: FeedsViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    @IntoMap
    @ViewModelKey(ActivityViewModel::class)
    abstract fun getActivityViewModel(activityViewModel: ActivityViewModel.Factory): ViewModelAssistedFactory<out ViewModel>

    @Binds
    @IntoMap
    @ViewModelKey(InAppUpdateViewModel::class)
    abstract fun getInAppUpdateViewModel(fragmentViewModel: InAppUpdateViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(TagsViewModel::class)
    abstract fun getTagsViewModel(tagsViewModel: TagsViewModel.Factory): ViewModelAssistedFactory<out ViewModel>


    @Module
    companion object {

        @JvmStatic
        @Provides
        fun providesAccount(articleListActivity: ArticleListActivity): Account = articleListActivity.account!!

        @JvmStatic
        @Provides
        fun providesApplicationPreferences(application: Application): SharedPreferences =
            PreferenceManager.getDefaultSharedPreferences(application)

    }

}
