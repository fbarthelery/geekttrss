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
package com.geekorum.ttrss.providers

import android.content.Context
import androidx.work.WorkerFactory
import com.geekorum.geekdroid.dagger.AndroidComponentsModule
import com.geekorum.geekdroid.dagger.PerAndroidComponent
import com.geekorum.geekdroid.dagger.WorkerKey
import dagger.Binds
import dagger.BindsInstance
import dagger.Module
import dagger.Subcomponent
import dagger.android.AndroidInjector
import dagger.multibindings.ClassKey
import dagger.multibindings.IntoMap

/**
 * Dagger module declaring the subcomponents of the providers package
 */
@Module(subcomponents = [ArticleProviderComponent::class])
abstract class AndroidInjectorsModule {

    @Binds
    @IntoMap
    @ClassKey(ArticlesProvider::class)
    abstract fun bindArticleProviderInjectorFactory(
        builder: ArticleProviderComponent.Builder
    ): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @WorkerKey(PurgeArticlesWorker::class)
    abstract fun providesPurgeArticlesWorkerFactory(workerFactory: PurgeArticlesWorker.Factory): WorkerFactory

}


@Subcomponent(modules = [AndroidComponentsModule::class])
@PerAndroidComponent
interface ArticleProviderComponent : AndroidInjector<ArticlesProvider> {

    @Subcomponent.Builder
    abstract class Builder : AndroidInjector.Builder<ArticlesProvider>() {

        override fun seedInstance(instance: ArticlesProvider) {
            bindAndroidComponent(instance.context)
        }

        @BindsInstance
        abstract fun bindAndroidComponent(context: Context?): Builder
    }
}
