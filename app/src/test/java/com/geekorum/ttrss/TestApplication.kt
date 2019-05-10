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

import com.geekorum.geekdroid.dagger.AndroidFrameworkModule
import com.geekorum.geekdroid.dagger.AppInitializersModule
import com.geekorum.ttrss.di.AndroidBindingsModule
import com.geekorum.ttrss.di.ApplicationComponent
import com.geekorum.ttrss.di.ViewModelsModule
import dagger.Component
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
    ViewModelsModule::class,
    BatteryFriendlyActivityTestModule::class])
interface TestApplicationComponent : ApplicationComponent {
    @Component.Builder
    interface Builder : ApplicationComponent.Builder
}
