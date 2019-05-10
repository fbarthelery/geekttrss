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
package com.geekorum.ttrss.articles_list;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.geekorum.ttrss.data.Category;
import com.geekorum.ttrss.data.Feed;

import java.util.List;

import javax.inject.Inject;

/**
 * {@link ViewModel} for {@link FeedListFragment}
 */
public class FeedsViewModel extends ViewModel {
    private final FeedsRepository feedsRepository;
    private LiveData<List<Category>> categoryLiveData;
    private LiveData<List<Feed>> feedLiveData;
    private final MutableLiveData<Long> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> onlyUnread = new MutableLiveData<>();

    @Inject
    public FeedsViewModel(FeedsRepository feedsRepository) {
        this.feedsRepository = feedsRepository;
        setOnlyUnread(true);
    }

    public void setOnlyUnread(boolean onlyUnread) {
        this.onlyUnread.setValue(onlyUnread);
    }

    public void setSelectedCategory(long selectedCategoryId) {
        selectedCategory.setValue(selectedCategoryId);
    }

    public LiveData<List<Feed>> getAllFeeds() {
        if (feedLiveData == null) {
            feedLiveData = Transformations.switchMap(onlyUnread, onlyUnread -> {
                        if (onlyUnread) {
                            return feedsRepository.getAllUnreadFeeds();
                        } else {
                            return feedsRepository.getAllFeeds();
                        }
                    }
            );
        }
        return feedLiveData;
    }

    private LiveData<List<Feed>> getFeedsForCategory(long catId) {
       return Transformations.switchMap(onlyUnread, onlyUnread -> {
                        if (onlyUnread) {
                            return feedsRepository.getUnreadFeedsForCategory(catId);
                        } else {
                            return feedsRepository.getFeedsForCategory(catId);
                        }
                    }
            );
    }

    public LiveData<List<Feed>> getFeedsForCategory() {
        return Transformations.switchMap(selectedCategory, this::getFeedsForCategory);
    }

    public LiveData<List<Category>> getCategories() {
        if (categoryLiveData == null) {
            categoryLiveData = Transformations.switchMap(onlyUnread, onlyUnread -> {
                        if (onlyUnread) {
                            return feedsRepository.getAllUnreadCategories();
                        } else {
                            return feedsRepository.getAllCategories();
                        }
                    }
            );
        }
        return categoryLiveData;
    }
}
