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
import android.app.Activity
import com.geekorum.ttrss.accounts.NetworkLoginModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.di.AssistedFactoriesModule
import com.geekorum.ttrss.network.ApiService
import com.geekorum.ttrss.network.TinyrssApiModule
import com.geekorum.ttrss.session.SessionActivity
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dagger.hilt.migration.DisableInstallInCheck


/**
 * Dependency injection pieces for the article_list functionality.
 *
 * ArticleListActivity has a SubComponent of the ActivityComponent.
 * This component is bount to the lifecycle of the activity
 *
 * ArticleListActivity's component provides the Account selected
 */

@Module(subcomponents = [ArticleListActivityComponent::class])
@InstallIn(ActivityComponent::class)
class ArticleListActivityModule


@Subcomponent(modules = [
    AssistedFactoriesModule::class,
    TinyrssApiModule::class,
    NetworkLoginModule::class,
    AccountModule::class
])
@PerAccount
interface ArticleListActivityComponent {

    val account: Account
    val apiService: ApiService
    val articleRepository: ArticlesRepository

    @Subcomponent.Factory
    interface Factory {
        fun newComponent(): ArticleListActivityComponent
    }
}

@Module
@DisableInstallInCheck
internal class AccountModule {
    @Provides
    fun providesAccount(activity: Activity) : Account {
        return (activity as SessionActivity).account!!
    }
}

