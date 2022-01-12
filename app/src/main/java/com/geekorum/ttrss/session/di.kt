/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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

package com.geekorum.ttrss.session

import android.accounts.Account
import android.accounts.AccountManager
import android.app.Application
import com.geekorum.geekdroid.accounts.AccountSelector
import com.geekorum.ttrss.accounts.NetworkLoginModule
import com.geekorum.ttrss.accounts.PerAccount
import com.geekorum.ttrss.articles_list.ArticlesRepository
import com.geekorum.ttrss.articles_list.SetArticleFieldAction
import com.geekorum.ttrss.network.ApiService
import dagger.Module
import dagger.Provides
import dagger.Subcomponent
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent
import dagger.hilt.components.SingletonComponent
import dagger.hilt.migration.DisableInstallInCheck
import javax.inject.Singleton

/**
 * Dependency injection pieces for a SessionActivity
 *
 * SessionActivity has a SubComponent of the ActivityComponent.
 * This component is bound to the lifecycle of the activity
 *
 * SessionActivity's  provides the selected Account to the component
 */

@Module(subcomponents = [SessionActivityComponent::class])
@InstallIn(ActivityRetainedComponent::class)
class SessionActivityModule

@Module
@InstallIn(SingletonComponent::class)
object AccountSelectorModule {
    @Singleton
    @Provides
    fun bindsAccountSelector(application: Application, accountManager: AccountManager) =
        AccountSelector(application, accountManager)
}

@Subcomponent(modules = [
    NetworkLoginModule::class,
    SessionAccountModule::class
])
@PerAccount
interface SessionActivityComponent {

    val account: Account
    val apiService: ApiService
    val articleRepository: ArticlesRepository
    val setArticleFieldActionFactory: SetArticleFieldAction.Factory

    @Subcomponent.Factory
    interface Factory {
        fun newComponent(): SessionActivityComponent
    }
}

@Module
@DisableInstallInCheck
class SessionAccountModule {

    @Provides
    fun providesAccount(accountSelector: AccountSelector) : Account {
        return accountSelector.savedAccount!!
    }
}
