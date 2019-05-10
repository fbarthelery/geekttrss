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
package com.geekorum.ttrss.di;

import androidx.lifecycle.ViewModel;
import com.geekorum.ttrss.ForceNightModeViewModel;
import com.geekorum.ttrss.articles_list.TtrssAccountViewModel;
import dagger.Binds;
import dagger.Module;
import dagger.multibindings.ClassKey;
import dagger.multibindings.IntoMap;

/**
 * Provides the different {@link ViewModel} of the application.
 */
@Module
public abstract class ViewModelsModule {

    @Binds
    @IntoMap
    @ClassKey(TtrssAccountViewModel.class)
    public abstract ViewModel getTtrssAccountViewModel(TtrssAccountViewModel ttrssAccountViewModel);

    @Binds
    @IntoMap
    @ClassKey(ForceNightModeViewModel.class)
    public abstract ViewModel getForceNightViewModel(ForceNightModeViewModel ttrssAccountViewModel);


}
