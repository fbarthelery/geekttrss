/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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
import androidx.activity.ComponentActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.navigation.NavBackStackEntry
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.hilt.android.ViewModelLifecycle
import dagger.hilt.android.internal.lifecycle.HiltViewModelAssistedMap
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory.CREATION_CALLBACK_KEY
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import dagger.hilt.android.internal.lifecycle.RetainedLifecycleImpl
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
        private val keySet: Map<Class<*>, Boolean>,
        private val viewModelComponentBuilder: ViewModelComponent.Builder,
    ) {

        fun fromActivity(activity: ComponentActivity, delegateFactory: ViewModelProvider.Factory?): ViewModelProvider.Factory {
            return getHiltViewModelFactory(delegateFactory)
        }

        fun fromFragment(fragment: Fragment, delegateFactory: ViewModelProvider.Factory?): ViewModelProvider.Factory {
            return getHiltViewModelFactory( delegateFactory)
        }

        /**
         * Added to get a factory from a [NavBackStackEntry]
         */
        fun fromNavBackStackEntry(navBackStackEntry: NavBackStackEntry, delegateFactory: ViewModelProvider.Factory): ViewModelProvider.Factory {
            return getHiltViewModelFactory(delegateFactory)
        }

        private fun getHiltViewModelFactory(
            extensionDelegate: ViewModelProvider.Factory?
        ): ViewModelProvider.Factory {
            val delegate = checkNotNull(extensionDelegate)
            return HiltViewModelFactory(keySet, delegate, viewModelComponentBuilder)
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
    private val hiltViewModelKeys: Map<Class<*>, Boolean>,
    private val delegateFactory: ViewModelProvider.Factory,
    viewModelComponentBuilder: ViewModelComponent.Builder
) : ViewModelProvider.Factory {

    private val hiltViewModelFactory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
            override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
                val lifecycle = RetainedLifecycleImpl()

                val handle = extras.createSavedStateHandle()
                val component = viewModelComponentBuilder
                    .savedStateHandle(handle)
                    .viewModelLifecycle(lifecycle)
                    .build()

                val viewModel = createViewModel(component, modelClass, extras)
                viewModel.addCloseable(lifecycle::dispatchOnCleared)
                return viewModel
            }
        }

    private fun <T : ViewModel?> createViewModel(
        component: ViewModelComponent,
        modelClass: Class<T>,
        extras: CreationExtras
    ): T {
        val provider = component
                .hiltViewModelMap[modelClass]
        val creationCallback =
            extras.get<Function1<Any, ViewModel>>(CREATION_CALLBACK_KEY)

        val assistedFactory = component
                .hiltViewModelAssistedMap[modelClass]

        if (assistedFactory == null) {
            if (creationCallback == null) {
                if (provider == null) {
                    throw java.lang.IllegalStateException(
                        "Expected the @HiltViewModel-annotated class "
                                + modelClass.name
                                + " to be available in the multi-binding of "
                                + "@HiltViewModelMap"
                                + " but none was found."
                    )
                } else {
                    @Suppress("UNCHECKED_CAST")
                    return provider.get() as T
                }
            } else {
                // Provider could be null or non-null.
                throw java.lang.IllegalStateException(
                    ("Found creation callback but class "
                            + modelClass.name
                            + " does not have an assisted factory specified in @HiltViewModel.")
                )
            }
        } else {
            if (provider == null) {
                if (creationCallback == null) {
                    throw java.lang.IllegalStateException(
                        ("Found @HiltViewModel-annotated class "
                                + modelClass.name
                                + " using @AssistedInject but no creation callback"
                                + " was provided in CreationExtras.")
                    )
                } else {
                    @Suppress("UNCHECKED_CAST")
                    return creationCallback.invoke(assistedFactory) as T
                }
            } else {
                // Creation callback could be null or non-null.
                throw AssertionError(
                    ("Found the @HiltViewModel-annotated class "
                            + modelClass.name
                            + " in both the multi-bindings of "
                            + "@HiltViewModelMap and @HiltViewModelAssistedMap.")
                )
            }
        }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (hiltViewModelKeys.containsKey(modelClass)) {
            hiltViewModelFactory.create(modelClass)
        } else {
            delegateFactory.create(modelClass)
        }
    }

    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return if (hiltViewModelKeys.containsKey(modelClass)) {
            hiltViewModelFactory.create(modelClass, extras)
        } else {
            delegateFactory.create(modelClass, extras)
        }
    }
}

//TODO put this under an ActivityRetained component
// see hilt ActivityRetainedComponentManager
@Module(subcomponents = [ViewModelComponent::class])
@DisableInstallInCheck
object DynamicFeatureViewModelModule

@Subcomponent(modules = [ViewModelModule::class])
@ViewModelScoped
interface ViewModelComponent {
    @get:HiltViewModelMap
    val hiltViewModelMap: Map<Class<*>, Provider<ViewModel>>

    @get:HiltViewModelAssistedMap
    val hiltViewModelAssistedMap: Map<Class<*>, Any>

    @Subcomponent.Builder
    interface Builder {
        fun savedStateHandle(@BindsInstance savedStateHandle: SavedStateHandle): Builder
        fun viewModelLifecycle(@BindsInstance viewModelLifecycle: ViewModelLifecycle): Builder
        fun build(): ViewModelComponent
    }

}


@Module
@DisableInstallInCheck
interface ViewModelModule{
    @Multibinds
    @HiltViewModelMap
    fun hiltViewModelMap(): Map<String?, ViewModel?>?

    @Multibinds
    @HiltViewModelAssistedMap
    fun hiltViewModelAssistedMap(): Map<Class<*>?, Any?>?
}

