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

import android.accounts.AccountManager;
import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowInsets;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProviders;
import com.geekorum.ttrss.BackgroundJobManager;
import com.geekorum.ttrss.R;
import com.geekorum.ttrss.article_details.ArticleDetailActivity;
import com.geekorum.ttrss.article_details.ArticleDetailFragment;
import com.geekorum.ttrss.data.Article;
import com.geekorum.ttrss.data.Feed;
import com.geekorum.ttrss.databinding.ActivityArticleListBinding;
import com.geekorum.ttrss.di.ViewModelsFactory;
import com.geekorum.ttrss.providers.ArticlesContract;
import com.geekorum.ttrss.session.SessionActivity;

import javax.inject.Inject;

/**
 * An activity representing a list of Articles. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link ArticleDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class ArticleListActivity extends SessionActivity {

    private static final String FRAGMENT_ARTICLES_LIST = "articles_list";
    private static final String FRAGMENT_FEEDS_LIST = "feeds_list";

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean twoPane;
    private DrawerLayout drawerLayout;
    private ActionBarDrawerToggle actionBarDrawerToggle;

    private String m_searchQuery;

    private ActivityArticleListBinding binding;

    @Inject
    BackgroundJobManager backgroundJobManager;
    @Inject AccountManager accountManager;
    @Inject ViewModelsFactory viewModelFactory;
    private ActivityViewModel activityViewModel;
    private TtrssAccountViewModel accountViewModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupPeriodicJobs();
        PreferenceManager.setDefaultValues(this, R.xml.pref_general, false);

        activityViewModel = ViewModelProviders.of(this).get(ActivityViewModel.class);
        activityViewModel.getSelectedFeed().observe(this, this::onFeedSelected);

        activityViewModel.getArticleSelectedEvent().observe(this, event -> {
            ActivityViewModel.ArticleSelectedParameters parameters = event.getContentIfNotHandled();
            if (parameters != null) {
                onArticleSelected(parameters.getPosition(), parameters.getArticle());
            }
        });

        accountViewModel = ViewModelProviders.of(this, viewModelFactory).get(TtrssAccountViewModel.class);
        accountViewModel.getSelectedAccount().observe(this, account -> {
            if (account != null) {
                activityViewModel.setAccount(account);
            } else {
                accountViewModel.startSelectAccountActivity(this);
            }
        });

        accountViewModel.getNoAccountSelectedEvent().observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                finish();
            }
        });

        binding = DataBindingUtil.setContentView(this, R.layout.activity_article_list);
        binding.setLifecycleOwner(this);
        binding.setActivityViewModel(activityViewModel);
        if (binding.headlinesDrawer != null) {
            // dispatch window inset up to the navigation view (FeedsListFragment)
            binding.startPaneLayout.setOnApplyWindowInsetsListener((view, insets) -> {
                WindowInsets result = insets;
                int childCount = binding.startPaneLayout.getChildCount();
                for (int i = 0; i < childCount; i++) {
                    View child = binding.startPaneLayout.getChildAt(i);
                    result = child.dispatchApplyWindowInsets(result);
                }
                return result;
            });
        }

        if (findViewById(R.id.article_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w600dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            twoPane = true;
        }
        drawerLayout = binding.headlinesDrawer;

        if (savedInstanceState == null) {
            FeedListFragment feedListFragment = new FeedListFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.start_pane_layout, feedListFragment, FRAGMENT_FEEDS_LIST)
                    .commit();

            Feed feed = Feed.createVirtualFeedForId(Feed.FEED_ID_ALL_ARTICLES);
            activityViewModel.setSelectedFeed(feed);
        }
        setupActionBar();
    }

    private void setupActionBar() {
        Toolbar toolbar = binding.toolbar.toolbar;
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());
        if (!twoPane) {
            actionBarDrawerToggle = new ActionBarDrawerToggle(this, binding.headlinesDrawer, toolbar,
                    R.string.drawer_open, R.string.drawer_close);
            binding.headlinesDrawer.addDrawerListener(actionBarDrawerToggle);
        }
        //noinspection ConstantConditions we just set the actionBar
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupPeriodicJobs() {
        backgroundJobManager.setupPeriodicJobs();
    }

    @Override
    public void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (!twoPane) {
            actionBarDrawerToggle.syncState();
        }
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!twoPane) {
            actionBarDrawerToggle.onConfigurationChanged(newConfig);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!twoPane && actionBarDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        } else if (twoPane && item.getItemId() == android.R.id.home) {
            binding.middlePaneLayout.setVisibility(View.GONE);
            binding.startPaneLayout.setVisibility(View.VISIBLE);
        }
        return super.onOptionsItemSelected(item);
    }

    private void onArticleSelected(int position, Article item) {
        Uri articleUri = ContentUris.withAppendedId(ArticlesContract.Article.CONTENT_URI, item.getId());
        if (twoPane) {
            FragmentManager supportFragmentManager = getSupportFragmentManager();
            ArticleDetailFragment articleDetailFragment = ArticleDetailFragment.newInstance(articleUri);
            supportFragmentManager.beginTransaction()
                    .replace(R.id.article_detail_container, articleDetailFragment)
                    .commit();
            findViewById(R.id.start_pane_layout).setVisibility(View.GONE);
            // TODO: add some good animation
        } else {
            Intent intent = new Intent(this, ArticleDetailActivity.class);
            intent.setAction(Intent.ACTION_VIEW);
            intent.setData(articleUri);
            this.startActivity(intent);
        }
    }

    /*
     *  From MasterActivity
     */
    public void onFeedSelected(Feed feed) {
        FragmentTransaction ft = getSupportFragmentManager()
                .beginTransaction();

        ArticlesListFragment hf = ArticlesListFragment.newInstance(feed.getId());

        ft.replace(R.id.middle_pane_layout, hf, FRAGMENT_ARTICLES_LIST);

        ft.commit();
        if (twoPane) {
            binding.middlePaneLayout.setVisibility(View.VISIBLE);
            binding.startPaneLayout.setVisibility(View.GONE);
        } else {
            drawerLayout.closeDrawers();
        }

    }

/*    public void unsubscribeFeed(final Feed feed) {
        org.fox.ttrss.ApiRequest req = new org.fox.ttrss.ApiRequest(getApplicationContext()) {
            protected void onPostExecute(JsonElement result) {
                refresh();
            }
        };

        @SuppressWarnings("serial")
        HashMap<String, String> map = new HashMap<String, String>() {
            {
                put("sid", "sessionId");
                put("op", "unsubscribeFeed");
                put("feed_id", String.valueOf(feed.id));
            }
        };

        req.execute(map);

    }*/


    /*   public void createFeedShortcut(Feed feed) {
           final Intent shortcutIntent = new Intent(this, MasterActivity.class);
           shortcutIntent.putExtra("feed_id", feed.id);
           shortcutIntent.putExtra("feed_is_cat", feed.is_cat);
           shortcutIntent.putExtra("feed_title", feed.title);
           shortcutIntent.putExtra("shortcut_mode", true);

           Intent intent = new Intent("com.android.launcher.action.INSTALL_SHORTCUT");

           intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, feed.title);
           intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
           intent.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, Intent.ShortcutIconResource.fromContext(this, R.drawable.ic_launcher));
           intent.putExtra("duplicate", false);

           sendBroadcast(intent);

           toast(R.string.shortcut_has_been_placed_on_the_home_screen);
       }
   */
}
