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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.accounts.Account
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import com.geekorum.geekdroid.dagger.FragmentFactoriesModule
import com.geekorum.geekdroid.dagger.FragmentKey
import com.geekorum.geekdroid.dagger.ViewModelKey
import com.geekorum.ttrss.di.ViewModelsModule
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.android.ContributesAndroidInjector
import dagger.android.support.AndroidSupportInjectionModule
import dagger.multibindings.IntoMap


@Module(includes = [AndroidSupportInjectionModule::class])
abstract class AddFeedModule {

    @ContributesAndroidInjector(modules = [
        FragmentFactoriesModule::class,
        ViewModelsModule::class])
    internal abstract fun contributesAddFeedActivityInjector(): AddFeedActivity

    @ContributesAndroidInjector(modules = [
        FragmentFactoriesModule::class,
        ViewModelsModule::class,
        SubscribeToFeedActivityModule::class])
    internal abstract fun contributesSubscribeToFeedActivityInjector(): SubscribeToFeedActivity

    @Binds
    @IntoMap
    @ViewModelKey(AddFeedViewModel::class)
    abstract fun bindAddFeedViewModel(vm: AddFeedViewModel): ViewModel

}

@Module
private abstract class SubscribeToFeedActivityModule {
    @Module
    companion object {
        @JvmStatic
        @Provides
        fun providesAccount(subscribeToFeedActivity: SubscribeToFeedActivity): Account = subscribeToFeedActivity.account!!
    }

    @Binds
    @IntoMap
    @ViewModelKey(SubscribeToFeedViewModel::class)
    abstract fun bindAddFeedDialogViewModel(vm: SubscribeToFeedViewModel): ViewModel

    @Binds
    @IntoMap
    @FragmentKey(EnterFeedUrlFragment::class)
    abstract fun bindEnterFeedUrlFragment(f: EnterFeedUrlFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(SelectFeedFragment::class)
    abstract fun bindSelectFeedFragment(f: SelectFeedFragment): Fragment

    @Binds
    @IntoMap
    @FragmentKey(DisplayErrorFragment::class)
    abstract fun bindDisplayErrorFragment(f: DisplayErrorFragment): Fragment

}

