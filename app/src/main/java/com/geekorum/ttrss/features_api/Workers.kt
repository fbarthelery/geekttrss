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

import android.content.Context
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.geekorum.ttrss.Application
import com.geekorum.ttrss.di.ApplicationComponentEntryPoint
import java.util.ServiceLoader
import javax.inject.Inject

/**
 * Each feature module can implement this interface to provide a list of [WorkerFactory] to the application.
 */
interface WorkerFactoryProvider {
    fun getWorkerFactories(appComponent: ApplicationComponentEntryPoint): List<WorkerFactory>
}


/**
 * A [WorkerFactory] that creates factories from the result of [WorkerFactoryProvider]
 *
 * This class uses [ServiceLoader] to locate the WorkerFactoryProvider implementation.
 */
class FeaturesWorkerFactory @Inject constructor() : WorkerFactory() {

    private val cache = mutableListOf<WorkerFactory>()

    override fun createWorker(
        appContext: Context, workerClassName: String, workerParameters: WorkerParameters
    ): ListenableWorker? {
        var worker = cache.asSequence()
            .mapNotNull {
                it.createWorker(appContext, workerClassName, workerParameters)
            }.firstOrNull()

        val applicationComponent = (appContext as Application).applicationComponent
        if (worker == null) {
            val loader = ServiceLoader.load(WorkerFactoryProvider::class.java,
                WorkerFactoryProvider::class.java.classLoader)
            val iterator = loader.iterator()
            iterator.forEach {
                val factories = it.getWorkerFactories(applicationComponent)
                cache += factories
                worker = factories.asSequence().mapNotNull {
                    it.createWorker(appContext, workerClassName, workerParameters)
                }.firstOrNull()
            }
        }
        return worker
    }

}
