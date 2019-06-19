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
package com.geekorum.ttrss

import androidx.room.Room
import androidx.work.Configuration
import com.geekorum.geekdroid.dagger.AndroidFrameworkModule
import com.geekorum.geekdroid.dagger.AppInitializersModule
import com.geekorum.geekdroid.dagger.FragmentFactoriesModule
import com.geekorum.geekdroid.dagger.WorkerInjectionModule
import com.geekorum.ttrss.accounts.LoginActivityTestModule
import com.geekorum.ttrss.data.ArticlesDatabase
import com.geekorum.ttrss.data.migrations.MigrationFrom1To2
import com.geekorum.ttrss.data.migrations.MigrationFrom2To3
import com.geekorum.ttrss.data.migrations.MigrationFrom3To4
import com.geekorum.ttrss.data.migrations.MigrationFrom4To5
import com.geekorum.ttrss.data.migrations.MigrationFrom5To6
import com.geekorum.ttrss.data.migrations.MigrationFrom6To7
import com.geekorum.ttrss.data.migrations.MigrationFrom7To8
import com.geekorum.ttrss.di.AndroidBindingsModule
import com.geekorum.ttrss.di.ApplicationComponent
import com.geekorum.ttrss.di.ViewModelsModule
import com.geekorum.ttrss.settings.SettingsModule
import dagger.Component
import dagger.Module
import dagger.Provides
import io.mockk.mockk
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

/**
 *  Application used by Robolectric when running tests with AndroidJUnit4 but for the google flavor
 */
class TestGoogleFlavorApplication : TestApplication()

@Singleton
@Component(modules = [AppInitializersModule::class,
    AndroidFrameworkModule::class,
    AndroidBindingsModule::class,
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
private class MockDatabaseModule {
    @Provides
    @Singleton
    internal fun providesAppDatabase(application: android.app.Application): ArticlesDatabase {
        return Room.inMemoryDatabaseBuilder(application, ArticlesDatabase::class.java)
            .addMigrations(MigrationFrom1To2,
                MigrationFrom2To3,
                MigrationFrom3To4,
                MigrationFrom4To5,
                MigrationFrom5To6,
                MigrationFrom6To7,
                MigrationFrom7To8)
            .build()
    }
}

@Module
private class MockNetworkModule {
    @Provides
    fun providesOkHttpClient(): OkHttpClient = mockk()
}

@Module
private class WorkManagerConfigurationModule {
    @Provides
    fun provideWorkManagerConfiguration(): Configuration {
        return Configuration.Builder().build()
    }

}
