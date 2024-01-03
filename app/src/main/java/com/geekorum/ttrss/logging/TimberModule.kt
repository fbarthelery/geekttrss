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
package com.geekorum.ttrss.logging

import android.content.Context
import androidx.annotation.Keep
import androidx.startup.Initializer
import com.geekorum.geekdroid.dagger.AppInitializersModule
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.EarlyEntryPoint
import dagger.hilt.android.EarlyEntryPoints
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.Multibinds
import timber.log.Timber

/**
 * Declare multibindings for [Timber.Tree]
 */
@Module(includes = [AppInitializersModule::class])
@InstallIn(SingletonComponent::class)
abstract class TimberModule {

    @Multibinds
    abstract fun providesTimberTrees(): Set<Timber.Tree>

}

@Keep
class TimberInitializer : Initializer<Unit> {

    @EarlyEntryPoint
    @InstallIn(SingletonComponent::class)
    interface TimberEntryPoint {
        // dagger provides java.util.set which is mutable
        val timberTrees: MutableSet<Timber.Tree>
    }

    override fun create(context: Context) {
        val entryPoint =  EarlyEntryPoints.get(context, TimberEntryPoint::class.java)
        val timberTrees = entryPoint.timberTrees
        Timber.plant(*timberTrees.toTypedArray())
    }

    override fun dependencies(): List<Class<out Initializer<*>>> = emptyList()
}

