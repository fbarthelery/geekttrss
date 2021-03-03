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
import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.hilt.EntryPoints
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.internal.builders.ViewModelComponentBuilder
import dagger.hilt.android.internal.lifecycle.DefaultActivityViewModelFactory
import dagger.hilt.android.internal.lifecycle.DefaultFragmentViewModelFactory
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory
import dagger.hilt.android.internal.lifecycle.HiltViewModelFactory.ViewModelFactoriesEntryPoint
import dagger.hilt.android.internal.lifecycle.HiltViewModelMap
import dagger.hilt.android.internal.lifecycle.HiltWrapper_HiltViewModelFactory_ViewModelModule
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.multibindings.Multibinds
import javax.inject.Inject

/**
 * Use [DynamicFeatureViewModelFactory.fromActivity] and [DynamicFeatureViewModelFactory.fromFragment]
 * to get the [ViewModelProvider.Factory] for your activity or fragment
 */
class DynamicFeatureViewModelFactory @Inject constructor(
    private val application: Application,
    @HiltViewModelMap.KeySet
    private val keySet: Set<String>,
    private val viewModelComponentBuilder: DynamicFeatureViewModelComponent.Builder,
    @DefaultActivityViewModelFactory
    private val defaultActivityFactorySet: MutableSet<ViewModelProvider.Factory?>,
    @DefaultFragmentViewModelFactory
    private val defaultFragmentFactorySet: MutableSet<ViewModelProvider.Factory?>
) {

    private var defaultActivityFactory = getFactoryFromSet(defaultActivityFactorySet)
    private var defaultFragmentFactory = getFactoryFromSet(defaultFragmentFactorySet)

    fun fromActivity(activity: ComponentActivity): ViewModelProvider.Factory {
        return getHiltViewModelFactory(activity,
            if (activity.intent != null) activity.intent.extras else null,
            defaultActivityFactory)
    }

    fun fromFragment(fragment: Fragment): ViewModelProvider.Factory {
        return getHiltViewModelFactory(fragment, fragment.arguments, defaultFragmentFactory)
    }

    private fun getHiltViewModelFactory(
        owner: SavedStateRegistryOwner,
        defaultArgs: Bundle?,
        extensionDelegate: ViewModelProvider.Factory?): ViewModelProvider.Factory {
        val delegate = extensionDelegate
            ?: SavedStateViewModelFactory(application, owner, defaultArgs)
        return HiltDynamicFeatureViewModelFactory(
            owner, defaultArgs, keySet, delegate, viewModelComponentBuilder)
    }

    private fun getFactoryFromSet(set: Set<ViewModelProvider.Factory?>): ViewModelProvider.Factory? {
        // A multibinding set is used instead of BindsOptionalOf because Optional is not available in
        // Android until API 24 and we don't want to have Guava as a transitive dependency.
        if (set.isEmpty()) {
            return null
        }
        check(set.size <= 1) { "At most one default view model factory is expected. Found $set" }
        return set.iterator().next()
            ?: throw IllegalStateException("Default view model factory must not be null.")
    }
}

/**
 * Fork of [HiltViewModelFactory] to not use [EntryPoints] and work with non Hilt generated component
 */
class HiltDynamicFeatureViewModelFactory(
    private val owner: SavedStateRegistryOwner,
    private val defaultArgs: Bundle?,
    private val hiltViewModelKeys: Set<String>,
    private val delegateFactory: ViewModelProvider.Factory,
    private val viewModelComponentBuilder: DynamicFeatureViewModelComponent.Builder
) : ViewModelProvider.Factory{

    private val hiltViewModelFactory = object : AbstractSavedStateViewModelFactory(owner, defaultArgs) {
        override fun <T : ViewModel?> create(
            key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            val component = viewModelComponentBuilder.savedStateHandle(handle).build()
            val provider = checkNotNull(component.hiltViewModelMap[modelClass.name]) {
                "Expected the @HiltViewModel-annotated class '${modelClass.name}' to be " +
                    "available in the multi-binding of @HiltViewModelMap but none was found."
            }
            return provider.get() as T
        }
    }

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return if (hiltViewModelKeys.contains(modelClass.name)) {
            hiltViewModelFactory.create(modelClass)
        } else {
            delegateFactory.create(modelClass)
        }
    }
}

/**
 * To add viewmodels, annotate them with @HiltViewModel and add the generated modules to your component
 * ex :
 * ```
 *
 *   @HiltViewModel
 *   class AddFeedViewModel @Inject constructor(
 *   ...
 *
 *   @Module(includes = [
 *       AddFeedViewModel_HiltModules.BindsModule::class,
 *      AddFeedViewModel_HiltModules.KeyModule::class
 *   ])
 * ```
 */
@Module(subcomponents = [DynamicFeatureViewModelComponent::class])
abstract class DynamicFeatureViewModelModule {

    @Multibinds
    @DefaultActivityViewModelFactory
    abstract fun defaultActivityViewModelFactory(): MutableSet<ViewModelProvider.Factory?>

    @Multibinds
    @DefaultFragmentViewModelFactory
    abstract fun defaultFragmentViewModelFactory(): MutableSet<ViewModelProvider.Factory?>
}

@Subcomponent(modules = [HiltWrapper_HiltViewModelFactory_ViewModelModule::class])
@ViewModelScoped
interface DynamicFeatureViewModelComponent : ViewModelComponent,
    ViewModelFactoriesEntryPoint {

    @Subcomponent.Builder
    interface Builder : ViewModelComponentBuilder {
        override fun savedStateHandle(@BindsInstance handle: SavedStateHandle): Builder
        override fun build(): DynamicFeatureViewModelComponent
    }
}
