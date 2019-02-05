/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
import com.geekorum.geekdroid.dagger.AndroidComponentsModule
import com.geekorum.geekdroid.dagger.PerAndroidComponent
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
@Module(subcomponents = [PurgeArticleJobServiceComponent::class, ArticleProviderComponent::class])
abstract class AndroidInjectorsModule {

    @Binds
    @IntoMap
    @ClassKey(ArticlesProvider::class)
    abstract fun bindArticleProviderInjectorFactory(
        builder: ArticleProviderComponent.Builder
    ): AndroidInjector.Factory<*>

    @Binds
    @IntoMap
    @ClassKey(PurgeArticlesJobService::class)
    abstract fun bindPurgeArticlesJobServiceInjectorFactory(
        builder: PurgeArticleJobServiceComponent.Builder
    ): AndroidInjector.Factory<*>

}


interface BaseComponent<T> : AndroidInjector<T> {

    abstract class Builder<T> : AndroidInjector.Builder<T>() {

        @BindsInstance
        abstract fun bindAndroidComponent(context: Context?): Builder<T>
    }
}

@Subcomponent(modules = [AndroidComponentsModule::class])
@PerAndroidComponent
interface PurgeArticleJobServiceComponent : BaseComponent<PurgeArticlesJobService> {
    @Subcomponent.Builder
    abstract class Builder : BaseComponent.Builder<PurgeArticlesJobService>() {

        override fun seedInstance(instance: PurgeArticlesJobService) {
            bindAndroidComponent(instance)
        }
    }
}


@Subcomponent(modules = [AndroidComponentsModule::class])
@PerAndroidComponent
interface ArticleProviderComponent : BaseComponent<ArticlesProvider> {

    @Subcomponent.Builder
    abstract class Builder : BaseComponent.Builder<ArticlesProvider>() {

        override fun seedInstance(instance: ArticlesProvider) {
            bindAndroidComponent(instance.context)
        }

    }
}
