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
package com.geekorum.ttrss.manage_feeds.add_feed

import android.os.Bundle
import androidx.lifecycle.ViewModel
import androidx.savedstate.SavedStateRegistryOwner
import com.geekorum.geekdroid.dagger.DaggerDelegateSavedStateVMFactory
import javax.inject.Provider

fun createDaggerDelegateSavedStateVMFactoryCreator(
    viewModel: ViewModel,
    key: Class<out ViewModel> = viewModel.javaClass
): DaggerDelegateSavedStateVMFactory.Creator {
    val viewModelProvider = Provider { viewModel }
    val simpleProvidersMap: Map<Class<out ViewModel>, @JvmSuppressWildcards Provider<ViewModel>> = mapOf(
        key to viewModelProvider
    )
    return object: DaggerDelegateSavedStateVMFactory.Creator {
        override fun create(
            owner: SavedStateRegistryOwner, defaultArgs: Bundle?
        ): DaggerDelegateSavedStateVMFactory {
            return DaggerDelegateSavedStateVMFactory(simpleProvidersMap, emptyMap(), owner, defaultArgs)
        }
    }

}
