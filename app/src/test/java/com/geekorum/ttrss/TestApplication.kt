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
package com.geekorum.ttrss

import androidx.room.Room
import androidx.work.Configuration
import coil.ImageLoader
import com.geekorum.geekdroid.dagger.AndroidFrameworkModule
import com.geekorum.geekdroid.dagger.AppInitializersModule
import com.geekorum.geekdroid.dagger.FragmentFactoriesModule
import com.geekorum.geekdroid.dagger.WorkerInjectionModule
import com.geekorum.ttrss.accounts.LoginActivityTestModule
import com.geekorum.ttrss.core.CoreFactoriesModule
import com.geekorum.ttrss.core.CoroutineDispatchersProvider
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.data.migrations.ALL_MIGRATIONS
import com.geekorum.ttrss.di.AndroidBindingsModule
import com.geekorum.ttrss.di.ApplicationComponent
import com.geekorum.ttrss.di.ViewModelsModule
import com.geekorum.ttrss.settings.SettingsModule
import dagger.Component
import dagger.Module
import dagger.Provides
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineDispatcher
import okhttp3.OkHttpClient
import javax.inject.Singleton

/**
 * Application used by Robolectric when running tests with AndroidJUnit4
 */
open class TestApplication : Application() {
    override fun applicationInjector(): ApplicationComponent {
        return DaggerTestApplicationComponent.builder().bindApplication(this).build()
    }
}


@Singleton
@Component(modules = [AppInitializersModule::class,
    TestCoroutineDispatchersProviderModule::class,
    AndroidFrameworkModule::class,
    AndroidBindingsModule::class,
    CoreFactoriesModule::class,
    FragmentFactoriesModule::class,
    ViewModelsModule::class,
    SettingsModule::class,
    AllFeaturesInstalledModule::class,
    BatteryFriendlyActivityTestModule::class,
    LoginActivityTestModule::class,
    WorkerInjectionModule::class,
    WorkManagerConfigurationModule::class,
    MockDatabaseModule::class,
    MockNetworkModule::class
])
interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder
}


@Module
private object MockDatabaseModule {
    @Provides
    @Singleton
    internal fun providesAppDatabase(application: android.app.Application): ArticlesDatabase {
        return Room.inMemoryDatabaseBuilder(application, ArticlesDatabase::class.java)
            .addMigrations(*ALL_MIGRATIONS.toTypedArray())
            .build()
    }
}

@Module
private object MockNetworkModule {
    @Provides
    fun providesOkHttpClient(): OkHttpClient = mockk()

    @Provides
    fun providesImageLoader(): ImageLoader = mockk()
}

@Module
private object WorkManagerConfigurationModule {
    @Provides
    fun provideWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().build()
    }

}

@Module
@OptIn(ExperimentalCoroutinesApi::class)
private object TestCoroutineDispatchersProviderModule {
    @Provides
    fun provideCoroutineDispatchersProvider(): CoroutineDispatchersProvider {
        val testDispatcher = TestCoroutineDispatcher()
        return CoroutineDispatchersProvider(testDispatcher, testDispatcher, testDispatcher)
    }

}
