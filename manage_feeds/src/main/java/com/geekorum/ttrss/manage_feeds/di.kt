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
package com.geekorum.ttrss.manage_feeds

import android.app.Activity
import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.HasDefaultViewModelProviderFactory
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.CreationExtras
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavBackStackEntry
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import com.geekorum.ttrss.ForceNightModeViewModel_HiltModules
import com.geekorum.ttrss.articles_list.TtrssAccountViewModel_HiltModules
import com.geekorum.ttrss.core.CoreFactoriesModule
import com.geekorum.ttrss.di.FeatureScope
import com.geekorum.ttrss.features_api.DefaultViewModelFactories
import com.geekorum.ttrss.features_api.DynamicFeatureViewModelModule
import com.geekorum.ttrss.features_api.ManageFeedsDependencies
import com.geekorum.ttrss.manage_feeds.add_feed.AddFeedActivity
import com.geekorum.ttrss.manage_feeds.add_feed.AddFeedModule
import com.geekorum.ttrss.manage_feeds.workers.WorkerComponent
import com.geekorum.ttrss.manage_feeds.workers.WorkersModule
import com.geekorum.ttrss.session.SessionAccountModule
import dagger.*
import dagger.hilt.android.lifecycle.withCreationCallback
import dagger.hilt.migration.DisableInstallInCheck

@Component(dependencies = [ManageFeedsDependencies::class],
    modules = [CoreFactoriesModule::class,
        WorkManagerModule::class,
        ManageFeedActivityModule::class,
        AddFeedModule::class,
        WorkersModule::class])
@FeatureScope
interface ManageFeedComponent {

    fun createActivityComponent(): ActivityComponent.Factory

    fun getWorkerFactory(): WorkerFactory

    fun createWorkerComponent(): WorkerComponent.Builder

}

@Module(subcomponents = [ActivityComponent::class])
@DisableInstallInCheck
interface ManageFeedActivityModule

@Subcomponent(modules = [
    SessionAccountModule::class,
    DynamicFeatureViewModelModule::class,
    ForceNightModeViewModel_HiltModules.BindsModule::class,
    ForceNightModeViewModel_HiltModules.KeyModule::class,
    TtrssAccountViewModel_HiltModules.BindsModule::class,
    TtrssAccountViewModel_HiltModules.KeyModule::class,
    ManageFeedModule::class,
])
interface ActivityComponent {

    @Subcomponent.Factory
    interface Factory {
        fun newComponent(@BindsInstance activity: Activity): ActivityComponent
    }

    val hiltViewModelFactoryFactory: DefaultViewModelFactories.InternalFactoryFactory


    // inject required to provide daggerDelegateFragmentFactory for fragment constructor injection.
    fun inject(baseSessionActivity: BaseSessionActivity)
    fun inject(addFeedActivity: AddFeedActivity)
}

@Module(includes = [
    ManageFeedViewModel_HiltModules.BindsModule::class,
    ManageFeedViewModel_HiltModules.KeyModule::class,
    EditFeedViewModel_HiltModules.BindsModule::class,
    EditFeedViewModel_HiltModules.KeyModule::class,
    EditSpecialFeedViewModel_HiltModules.BindsModule::class,
    EditSpecialFeedViewModel_HiltModules.KeyModule::class,
])
@DisableInstallInCheck
internal class ManageFeedModule

@Module
@DisableInstallInCheck
object WorkManagerModule {
    @Provides
    fun provideWorkManager(application: Application): WorkManager = WorkManager.getInstance(application)
}




/* inject hilt view model from manage_feed module */

@Composable
internal inline fun <reified VM : ViewModel> dfmHiltViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    factory: ViewModelProvider.Factory? = null,
    extras: CreationExtras = if (viewModelStoreOwner is HasDefaultViewModelProviderFactory) {
        viewModelStoreOwner.defaultViewModelCreationExtras
    } else {
        CreationExtras.Empty
    }
): VM {
    val actualFactory = factory ?: createHiltViewModelFactory(viewModelStoreOwner)
    return viewModel(viewModelStoreOwner, key = key, factory = actualFactory, extras = extras)
}

@Composable
inline fun <reified VM : ViewModel, reified VMF> dfmHiltViewModel(
    viewModelStoreOwner: ViewModelStoreOwner = checkNotNull(LocalViewModelStoreOwner.current) {
        "No ViewModelStoreOwner was provided via LocalViewModelStoreOwner"
    },
    key: String? = null,
    noinline creationCallback: (VMF) -> VM
): VM {
    val factory = createHiltViewModelFactory(viewModelStoreOwner)
    return viewModel(
        viewModelStoreOwner = viewModelStoreOwner,
        key = key,
        factory = factory,
        extras = viewModelStoreOwner.run {
            if (this is HasDefaultViewModelProviderFactory) {
                this.defaultViewModelCreationExtras.withCreationCallback(creationCallback)
            } else {
                CreationExtras.Empty.withCreationCallback(creationCallback)
            }
        }
    )
}


@Composable
@PublishedApi
internal fun createHiltViewModelFactory(
    viewModelStoreOwner: ViewModelStoreOwner
): ViewModelProvider.Factory? = if (viewModelStoreOwner is NavBackStackEntry) {
    HiltViewModelFactory(
        context = LocalContext.current,
        navBackStackEntry = viewModelStoreOwner
    )
} else {
    // Use the default factory provided by the ViewModelStoreOwner
    // and assume it is an @AndroidEntryPoint annotated fragment or activity
    null
}

@JvmName("create")
private fun HiltViewModelFactory(
    context: Context,
    navBackStackEntry: NavBackStackEntry
): ViewModelProvider.Factory {
    val activity = context.let {
        var ctx = it
        while (ctx is ContextWrapper) {
            // Hilt can only be used with ComponentActivity
            if (ctx is ComponentActivity) {
                return@let ctx
            }
            ctx = ctx.baseContext
        }
        throw IllegalStateException(
            "Expected an activity context for creating a HiltViewModelFactory " +
                    "but instead found: $ctx"
        )
    }
    if (activity is BaseSessionActivity) {
        return activity.activityComponent.hiltViewModelFactoryFactory.fromNavBackStackEntry(
            navBackStackEntry, navBackStackEntry.defaultViewModelProviderFactory
        )
    }
    return dagger.hilt.android.internal.lifecycle.HiltViewModelFactory.createInternal(
        activity,
        navBackStackEntry.defaultViewModelProviderFactory,
    )
}
