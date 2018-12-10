/**
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

package com.geekorum.ttrss.articles_list

import android.accounts.Account
import androidx.lifecycle.ViewModel
import com.geekorum.ttrss.accounts.NetworkLoginModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.network.TinyrssApiModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.multibindings.ClassKey
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
        com.geekorum.ttrss.articles_list.FragmentsInjectorModule::class,
        com.geekorum.ttrss.article_details.FragmentsInjectorModule::class,
        com.geekorum.ttrss.articles_list.search.FragmentsInjectorModule::class,
        ActivityViewModelModule::class,
        TinyrssApiModule::class,
        NetworkLoginModule::class,
        SelectedAccountModule::class])
    @PerAccount
    internal abstract fun contributesArticleListActivityInjector(): ArticleListActivity

}

/**
 * Provides the Fragments injectors subcomponents.
 */
@Module
abstract class FragmentsInjectorModule {

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    internal abstract fun contributesArticleListFragmentInjector(): ArticlesListFragment

    @ContributesAndroidInjector(modules = [ViewModelModule::class])
    internal abstract fun contributesFeedListFragmentInjector(): FeedListFragment

}


@Module
private abstract class ViewModelModule {
    @Binds
    @IntoMap
    @ClassKey(FragmentViewModel::class)
    abstract fun getArticlesFragmentViewModel(fragmentViewModel: FragmentViewModel): ViewModel

    @Binds
    @IntoMap
    @ClassKey(FeedsViewModel::class)
    abstract fun getFeedsViewModel(feedsViewModel: FeedsViewModel): ViewModel

}

@Module
private abstract class ActivityViewModelModule{
    @Binds
    @IntoMap
    @ClassKey(ActivityViewModel::class)
    abstract fun getActivityViewModel(activityViewModel: ActivityViewModel): ViewModel

}

@Module
internal class SelectedAccountModule {

    @Provides
    fun providesAccount(articleListActivity: ArticleListActivity): Account = articleListActivity.account!!
}
