/**
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
package com.geekorum.ttrss.di;

import com.geekorum.geekdroid.dagger.AndroidFrameworkModule;
import com.geekorum.ttrss.Application;
import com.geekorum.ttrss.accounts.AndroidTinyrssAccountManagerModule;
import com.geekorum.ttrss.data.ArticlesDatabaseModule;
import com.geekorum.ttrss.logging.LoggingModule;
import com.geekorum.ttrss.providers.ArticleProviderComponent;
import dagger.BindsInstance;
import dagger.Component;
import dagger.android.AndroidInjectionModule;

import javax.inject.Singleton;

/**
 * Main component for the application
 */
@Component(modules = {
        AndroidInjectionModule.class,
        AndroidBindingsModule.class,
        ViewModelsModule.class,
        NetworkModule.class,
        AndroidFrameworkModule.class,
        ArticlesDatabaseModule.class,
        LoggingModule.class,
        com.geekorum.ttrss.article_details.ActivitiesInjectorModule.class,
        com.geekorum.ttrss.articles_list.ActivitiesInjectorModule.class,
        com.geekorum.ttrss.sync.ServiceInjectorModule.class,
        com.geekorum.ttrss.accounts.ServicesInjectorModule.class,
        com.geekorum.ttrss.add_feed.AndroidInjectorsModule.class,
        AndroidTinyrssAccountManagerModule.class
})
@Singleton
public interface ApplicationComponent {

    @Component.Builder
    interface Builder {
        ApplicationComponent build();

        @BindsInstance
        Builder bindApplication(android.app.Application application);
    }

    void inject(Application application);

    ArticleProviderComponent.Builder createArticleProviderComponent();

}
