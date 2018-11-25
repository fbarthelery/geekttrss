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
package com.geekorum.ttrss.articles_list;

import android.accounts.Account;
import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import androidx.paging.DataSource;
import androidx.paging.LivePagedListBuilder;
import androidx.paging.PagedList;
import com.geekorum.ttrss.BackgroundJobManager;
import com.geekorum.ttrss.data.Article;
import com.geekorum.ttrss.data.Feed;
import com.geekorum.ttrss.session.Action;
import com.geekorum.ttrss.session.UndoManager;

import javax.inject.Inject;

/**
 * {@link ViewModel} for the {@link ArticlesListFragment}.
 */
public class FragmentViewModel extends ViewModel {

    public static final String PREF_VIEW_MODE = "view_mode";

    private final SharedPreferences prefs;
    private final SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener = (sharedPreferences, key) -> {
        if (PREF_VIEW_MODE.equals(key)) {
            FragmentViewModel.this.updateNeedUnread();
        }
    };

    private final ArticlesRepository articlesRepository;
    private final MutableLiveData<Long> feedId = new MutableLiveData<>();
    private final MutableLiveData<Integer> pendingArticlesSetUnread = new MutableLiveData<>();
    private LiveData<Feed> feed;
    private LiveData<PagedList<Article>> articles = Transformations.switchMap(getFeed(), this::getArticlesForFeed);
    private final MutableLiveData<Boolean> needUnreadLiveData = new MutableLiveData<>();
    private final FeedsRepository feedsRepository;
    private final UndoManager<Action> unreadActionUndoManager = new UndoManager<>();
    private final BackgroundJobManager backgroundJobManager;
    private final Account account;

    // default value in databinding is False for boolean and 0 for int
    // we can't test size() == 0 in layout file because the default value will make the test true
    // and will briefly show the empty view
    public final LiveData<Boolean> haveZeroArticles = Transformations.map(articles, articles -> articles.size() == 0);

    @Inject
    public FragmentViewModel(Application application, ArticlesRepository articlesRepository, FeedsRepository feedsRepository, BackgroundJobManager backgroundJobManager, Account account) {
        this.articlesRepository = articlesRepository;
        this.feedsRepository = feedsRepository;
        this.backgroundJobManager = backgroundJobManager;
        this.account = account;
        prefs = PreferenceManager
                .getDefaultSharedPreferences(application.getApplicationContext());
        prefs.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
        updateNeedUnread();
    }

    public void init(long feedId) {
        this.feedId.setValue(feedId);
    }

    public LiveData<PagedList<Article>> getArticles() {
        return articles;
    }

    public LiveData<Feed> getFeed() {
        if (feed == null) {
            feed = Transformations.switchMap(feedId, id -> {
                        if (Feed.isVirtualFeed(id)) {
                            return createFeedLivedata(Feed.createVirtualFeedForId(id));
                        }
                        return feedsRepository.getFeedById(id);
                    }
            );
        }
        return feed;
    }

    private LiveData<PagedList<Article>> getArticlesForFeed(Feed feed) {
        return Transformations.switchMap(needUnreadLiveData, needUnread -> {
            DataSource.Factory<Integer, Article> factory;
            if (feed.isStarredFeed()) {
                factory = needUnread ? articlesRepository.getAllUnreadStarredArticles()
                        : articlesRepository.getAllStarredArticles();
            } else if (feed.isPublishedFeed()) {
                factory = needUnread ? articlesRepository.getAllUnreadPublishedArticles()
                        : articlesRepository.getAllPublishedArticles();
            } else if (feed.isFreshFeed()) {
                long freshTimeSec = System.currentTimeMillis() / 1000 - 3600 * 36;
                factory = needUnread ? articlesRepository.getAllUnreadArticlesUpdatedAfterTime(freshTimeSec)
                        : articlesRepository.getAllArticlesUpdatedAfterTime(freshTimeSec);
            } else if (feed.isAllArticlesFeed()) {
                factory = needUnread ? articlesRepository.getAllUnreadArticles()
                        : articlesRepository.getAllArticles();
            } else {
                factory = needUnread ? articlesRepository.getAllUnreadArticlesForFeed(feed.getId())
                        : articlesRepository.getAllArticlesForFeed(feed.getId());
            }
            return new LivePagedListBuilder<>(factory, 50)
                    .setBoundaryCallback(new PageBoundaryCallback<>()).build();
        });
    }

    private void updateNeedUnread() {
        String viewMode = prefs.getString(PREF_VIEW_MODE, "adaptive");
        switch (viewMode) {
            case "unread":
            case "adaptive":
                needUnreadLiveData.setValue(true);
                break;
            default:
                needUnreadLiveData.setValue(false);
                break;
        }
    }

    public void refresh() {
        backgroundJobManager.refreshFeed(account, feedId.getValue());
    }

    public void setArticleUnread(long articleId, boolean newValue) {
        Action unreadAction = articlesRepository.setArticleUnread(articleId, newValue);
        unreadActionUndoManager.recordAction(unreadAction);
        pendingArticlesSetUnread.setValue(unreadActionUndoManager.getNbActions());
    }

    public LiveData<Integer> getPendingArticlesSetUnread() {
        return pendingArticlesSetUnread;
    }

    public void setArticleStarred(long articleId, boolean newValue) {
        articlesRepository.setArticleStarred(articleId, newValue);
    }

    public void commitSetUnreadActions() {
        unreadActionUndoManager.clear();
        pendingArticlesSetUnread.setValue(unreadActionUndoManager.getNbActions());
    }

    public void undoSetUnreadActions() {
        unreadActionUndoManager.undoAll();
        pendingArticlesSetUnread.setValue(unreadActionUndoManager.getNbActions());
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        prefs.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    private LiveData<Feed> createFeedLivedata(Feed feed) {
        MutableLiveData<Feed> mutableLiveData = new MutableLiveData<>();
        mutableLiveData.setValue(feed);
        return mutableLiveData;
    }

    private class PageBoundaryCallback<T> extends PagedList.BoundaryCallback<T> {
        @Override
        public void onZeroItemsLoaded() {
            // we should use PagingRequestHelper to prevent calling refresh many times
            // but as the SyncManager ensure the unicity of the synchronisation
            // there is no need to.
            refresh();
        }

        @Override
        public void onItemAtFrontLoaded(@NonNull T itemAtFront) {
            // nothing to do
        }

        @Override
        public void onItemAtEndLoaded(@NonNull T itemAtEnd) {
            // nothing to do
        }
    }

}
