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
package com.geekorum.ttrss.features_api

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.*
import androidx.navigation.NavBackStackEntry
import androidx.savedstate.SavedStateRegistryOwner
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.migration.DisableInstallInCheck
import dagger.multibindings.Multibinds
import javax.inject.Inject
import javax.inject.Provider

/*
 * Simplified Fork of [dagger.hilt.android.internal.lifecycle.DefaultViewModelFactories] from
 * com.google.dagger:hilt-android:2.31.2 to not use hilt features (entrypoint)
 */

/**
 * Modules and entry points for the default view model factory used by activities and fragments
 * annotated with @AndroidEntryPoint.
 *
 *
 * Entry points are used to acquire the factory because injected fields in the generated
 * activities and fragments are ignored by Dagger when using the transform due to the generated
 * class not being part of the hierarchy during compile time.
 */
object DefaultViewModelFactories {

    /** Internal factory for the Hilt ViewModel Factory.  */
    class InternalFactoryFactory @Inject constructor(
        private val application: Application,
        @param:HiltViewModelMap.KeySet
        private val keySet: Set<String>,
        private val viewModelComponentBuilder: ViewModelComponent.Builder,
    ) {

        fun fromActivity(activity: ComponentActivity, delegateFactory: ViewModelProvider.Factory): ViewModelProvider.Factory {
            return getHiltViewModelFactory(activity,
                activity.intent?.extras,
                delegateFactory)
        }

        fun fromFragment(fragment: Fragment, delegateFactory: ViewModelProvider.Factory): ViewModelProvider.Factory {
            return getHiltViewModelFactory(fragment, fragment.arguments, delegateFactory)
        }

        /**
         * Added to get a factory from a [NavBackStackEntry]
         */
        fun fromNavBackStackEntry(navBackStackEntry: NavBackStackEntry, delegateFactory: ViewModelProvider.Factory): ViewModelProvider.Factory {
            return getHiltViewModelFactory(navBackStackEntry,
                navBackStackEntry.arguments,
                delegateFactory)
        }

        private fun getHiltViewModelFactory(
            owner: SavedStateRegistryOwner,
            defaultArgs: Bundle?,
            extensionDelegate: ViewModelProvider.Factory?
        ): ViewModelProvider.Factory {
            val delegate = extensionDelegate
                ?: SavedStateViewModelFactory(application, owner, defaultArgs)
            return HiltViewModelFactory(
                owner, defaultArgs, keySet, delegate, viewModelComponentBuilder)
        }

    }

    /** The activity module to declare the optional factories.  */
    @Module(includes = [DynamicFeatureViewModelModule::class])
    @DisableInstallInCheck
    interface ActivityModule {
        @Multibinds
        @HiltViewModelMap.KeySet
        fun viewModelKeys(): Set<String?>

    }

}

/**
 * View Model Provider Factory for the Hilt Extension.
 *
 *
 * A provider for this factory will be installed in the [ ] and [ ]. An instance of this factory will also be the
 * default factory by activities and fragments annotated with [ ].
 */
private class HiltViewModelFactory(
    owner: SavedStateRegistryOwner,
    defaultArgs: Bundle?,
    private val hiltViewModelKeys: Set<String>,
    private val delegateFactory: ViewModelProvider.Factory,
    viewModelComponentBuilder: ViewModelComponent.Builder
) : ViewModelProvider.Factory {

    private val hiltViewModelFactory: AbstractSavedStateViewModelFactory =
        object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
            override fun <T : ViewModel?> create(
                key: String, modelClass: Class<T>, handle: SavedStateHandle
            ): T {
                val component = viewModelComponentBuilder.savedStateHandle(handle).build()

                val provider = component.hiltViewModelMap[modelClass.name]
                    ?: throw IllegalStateException(
                        "Expected the @HiltViewModel-annotated class '${modelClass.name}' to be available in the multi-binding of @HiltViewModelMap but none was found.")
                @Suppress("UNCHECKED_CAST")
                return provider.get() as T
            }
        }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (hiltViewModelKeys.contains(modelClass.name)) {
            hiltViewModelFactory.create(modelClass)
        } else {
            delegateFactory.create(modelClass)
        }
    }
}

//TODO put this under an ActivityRetained component
// see hilt ActivityRetainedComponentManager
@Module(subcomponents = [ViewModelComponent::class])
@DisableInstallInCheck
object DynamicFeatureViewModelModule

@Subcomponent
@ViewModelScoped
interface ViewModelComponent {
    @get:HiltViewModelMap
    val hiltViewModelMap: Map<String, Provider<ViewModel>>

    @Subcomponent.Builder
    interface Builder {
        fun savedStateHandle(@BindsInstance savedStateHandle: SavedStateHandle): Builder
        fun build(): ViewModelComponent
    }
}
