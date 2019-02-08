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
package com.geekorum.ttrss.article_details;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import androidx.core.app.ShareCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;
import com.geekorum.ttrss.articles_list.ArticlesRepository;
import com.geekorum.ttrss.data.Article;
import com.geekorum.ttrss.network.TtRssBrowserLauncher;

import javax.inject.Inject;

/**
 * {@link ViewModel} for {@link ArticleDetailActivity} and {@link ArticleDetailFragment}.
 */
public class ArticleDetailsViewModel extends ViewModel {
    private final ArticlesRepository articlesRepository;
    private final TtRssBrowserLauncher browserLauncher;
    private final MutableLiveData<Long> articleId = new MutableLiveData<>();
    private LiveData<Article> article;
    private LiveData<String> articleContent;

    @Inject
    public ArticleDetailsViewModel(ArticlesRepository articlesRepository, TtRssBrowserLauncher browserLauncher) {
        this.articlesRepository = articlesRepository;
        this.browserLauncher = browserLauncher;
        browserLauncher.warmUp();
    }

    public void init(long articleId) {
        if (article == null) {
            LiveData<Article> tempArticle = Transformations.switchMap(this.articleId, articlesRepository::getArticleById);
            article = Transformations.map(tempArticle, a -> {
                browserLauncher.mayLaunchUrl(Uri.parse(a.getLink()));
                return a;
            });
            articleContent = Transformations.distinctUntilChanged(Transformations.map(article, Article::getContent));
        }
        this.articleId.setValue(articleId);
    }

    public LiveData<Article> getArticle() {
        return article;
    }

    public LiveData<String> getArticleContent() {
        return articleContent;
    }

    @Override
    protected void onCleared() {
        browserLauncher.shutdown();
    }

    public void openArticleInBrowser(Context context, Article article) {
        Uri uri = Uri.parse(article.getLink());
        openUrlInBrowser(context, uri);
    }

    public void openUrlInBrowser(Context context, Uri uri) {
        browserLauncher.launchUrl(context, uri);
    }

    public void shareArticle(Activity activity) {
        ShareCompat.IntentBuilder shareIntent = ShareCompat.IntentBuilder.from(activity);
        Article articleInfo = article.getValue();
        shareIntent.setSubject(articleInfo.getTitle())
                .setHtmlText(articleInfo.getContent())
                .setText(articleInfo.getLink())
                .setType("text/plain");
        activity.startActivity(shareIntent.createChooserIntent());
    }

    // for use from databinding layout
    public void shareArticle(Context context) {
        while (context != null) {
            if (context instanceof Activity) {
                break;
            }
            if (context instanceof ContextWrapper) {
                context = ((ContextWrapper) context).getBaseContext();
            }
        }
        if (context == null) {
            // should never happen
            throw new IllegalArgumentException("Context is not an activity");
        }
        shareArticle((Activity) context);
    }

    public void onStarChanged(boolean newValue) {
        Article articleInfo = article.getValue();
        articlesRepository.setArticleStarred(articleInfo.getId(), newValue);
    }

    public void toggleArticleRead() {
        Article articleInfo = article.getValue();
        setArticleUnread(!articleInfo.isUnread());
    }

    public void setArticleUnread(boolean unread) {
        Article articleInfo = article.getValue();
        articlesRepository.setArticleUnread(articleInfo.getId(), unread);
    }
}
