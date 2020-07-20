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
package com.geekorum.ttrss.logging

import android.app.Application
import com.geekorum.geekdroid.dagger.AppInitializer
import com.geekorum.geekdroid.dagger.AppInitializersModule
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.multibindings.IntoSet
import dagger.multibindings.Multibinds
import timber.log.Timber
import javax.inject.Inject

/**
 * Declare multibindings for [Timber.Tree]
 */
@Module(includes = [AppInitializersModule::class])
@InstallIn(ApplicationComponent::class)
abstract class TimberModule {

    @Multibinds
    abstract fun providesTimberTrees(): Set<Timber.Tree>

    @Binds
    @IntoSet
    abstract fun providesTimberInitializer(timberInitializer: TimberInitializer): AppInitializer

}

class TimberInitializer @Inject constructor(
    // dagger provides java.util.set which is mutable
    private val timberTrees: MutableSet<Timber.Tree>
) : AppInitializer {
    override fun initialize(app: Application) {
        Timber.plant(*timberTrees.toTypedArray())
    }
}

