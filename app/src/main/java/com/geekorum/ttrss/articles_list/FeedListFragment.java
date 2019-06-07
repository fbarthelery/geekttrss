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

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.ActivityOptionsCompat;
import androidx.fragment.app.FragmentFactory;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProviders;
import com.geekorum.geekdroid.dagger.DaggerDelegateFragmentFactory;
import com.geekorum.geekdroid.dagger.DaggerDelegateViewModelsFactory;
import com.geekorum.ttrss.BaseFragment;
import com.geekorum.ttrss.Features;
import com.geekorum.ttrss.R;
import com.geekorum.ttrss.data.Category;
import com.geekorum.ttrss.data.Feed;
import com.geekorum.ttrss.databinding.FragmentFeedsBinding;
import com.geekorum.ttrss.databinding.MenuFeedActionViewBinding;
import com.geekorum.ttrss.on_demand_modules.OnDemandModuleManager;
import com.geekorum.ttrss.settings.SettingsActivity;
import com.geekorum.ttrss.settings.manage_features.InstallFeatureActivity;
import com.google.android.material.navigation.NavigationView;
import timber.log.Timber;

import java.util.List;

import javax.inject.Inject;

/**
 * Display the list of feeds.
 */
public class FeedListFragment extends BaseFragment implements NavigationView.OnNavigationItemSelectedListener {

    private static final int MENU_GROUP_ID_SPECIAL = 1;
    private static final int CODE_INSTALL_MANAGE_FEED = 1;

    private FragmentFeedsBinding binding;
    private final SharedPreferences preferences;
    private List<Feed> currentFeeds;
    private boolean categoriesDisplayed;
    private FeedsViewModel feedsViewModel;
    private ActivityViewModel activityViewModel;
    private LiveData<List<Feed>> feedsForCategory;
    private TtrssAccountViewModel accountViewModel;
    private final OnDemandModuleManager moduleManager;

    @Inject
    public FeedListFragment(@NonNull DaggerDelegateViewModelsFactory viewModelsFactory, DaggerDelegateFragmentFactory fragmentFactory, OnDemandModuleManager moduleManager, SharedPreferences preferences) {
        super(viewModelsFactory, fragmentFactory);
        this.moduleManager = moduleManager;
        this.preferences = preferences;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFeedsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        NavigationView designNavigationView = binding.navigationView;
        designNavigationView.setNavigationItemSelectedListener(this);
        setupHeader();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        boolean showUnreadOnly = preferences.getBoolean("show_unread_only", true);
        feedsViewModel = ViewModelProviders.of(this, getViewModelsFactory()).get(FeedsViewModel.class);
        feedsViewModel.setOnlyUnread(showUnreadOnly);

        categoriesDisplayed = preferences.getBoolean("enable_cats", false);
        //        categoriesDisplayed = true;
        if (categoriesDisplayed) {
            feedsViewModel.getCategories().observe(this, categories -> {
                transformCategoriesInMenuEntry(binding.navigationView.getMenu(), categories);
                binding.navigationView.inflateMenu(R.menu.fragment_feed_list);
            });
        } else {
            feedsViewModel.getAllFeeds().observe(this, feeds -> {
                currentFeeds = feeds;
                transformFeedsInMenuEntry(binding.navigationView.getMenu(), feeds);
                binding.navigationView.inflateMenu(R.menu.fragment_feed_list);
            });
        }
        activityViewModel = ViewModelProviders.of(requireActivity(), getViewModelsFactory()).get(ActivityViewModel.class);
        activityViewModel.getSelectedFeed().observe(this, feed -> {
            Menu menu = binding.navigationView.getMenu();
            MenuItem item = menu.findItem((int) feed.getId());
            if (item != null) {
                item.setChecked(true);
            }
        });

        accountViewModel = ViewModelProviders.of(requireActivity(), getViewModelsFactory()).get(TtrssAccountViewModel.class);
        accountViewModel.getSelectedAccount().observe(this, account -> {
            if (account != null) {
                View headerView = binding.navigationView.getHeaderView(0);
                TextView login = headerView.findViewById(R.id.drawer_header_login);
                login.setText(account.name);
            }
        });

        accountViewModel.getSelectedAccountHost().observe(this, host -> {
            View headerView = binding.navigationView.getHeaderView(0);
            TextView server = headerView.findViewById(R.id.drawer_header_server);
            server.setText(host);
        });
    }

    private void setupHeader() {
        View header = binding.navigationView.getHeaderView(0);
        View settings = header.findViewById(R.id.drawer_settings_btn);

        settings.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), SettingsActivity.class);
            ActivityOptionsCompat options = ActivityOptionsCompat.makeScaleUpAnimation(v, 0, 0, v.getWidth(), v.getHeight());
            ActivityCompat.startActivity(requireActivity(), intent, options.toBundle());
        });
    }

    private void transformFeedsInMenuEntry(Menu menu, List<Feed> feeds) {
        menu.clear();
        Feed currentFeed = activityViewModel.getSelectedFeed().getValue();
        for (Feed feed : feeds) {
            String title = TextUtils.isEmpty(feed.getDisplayTitle()) ? feed.getTitle() : feed.getDisplayTitle();
            MenuItem menuItem;
            if (feed.getId() < 0) {
                menuItem = menu.add(Menu.NONE, (int) feed.getId(), 0, title);
            } else {
                menuItem = menu.add(MENU_GROUP_ID_SPECIAL, (int) feed.getId(), 0, title);
            }
            setMenuItemIcon(feed, menuItem);
            setMenuItemUnreadCount(feed, menuItem);
            menuItem.setCheckable(true);
            if (currentFeed != null) {
                menuItem.setChecked(currentFeed.getId() == feed.getId());
            }
        }
        categoriesDisplayed = false;
    }

    private void transformCategoriesInMenuEntry(Menu menu, List<Category> categories1) {
        menu.clear();
        for (Category category : categories1) {
            String title = category.getTitle();
            MenuItem menuItem = menu.add(Menu.NONE, (int) category.getId(), Menu.NONE, title);
            setMenuItemIcon(category, menuItem);
            setMenuItemUnreadCount(category, menuItem);
            menuItem.setCheckable(true);
        }
        categoriesDisplayed = true;
    }

    private void setMenuItemUnreadCount(Feed feed, MenuItem menuItem) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        MenuFeedActionViewBinding menuView = MenuFeedActionViewBinding.inflate(layoutInflater, null, false);
        menuView.unreadCounter.setText(String.valueOf(feed.getUnreadCount()));
        menuView.unreadCounter.setVisibility((feed.getUnreadCount() > 0) ? View.VISIBLE : View.INVISIBLE);
        menuItem.setActionView(menuView.getRoot());
    }

    private void setMenuItemUnreadCount(Category category, MenuItem menuItem) {
        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        MenuFeedActionViewBinding menuView = MenuFeedActionViewBinding.inflate(layoutInflater, null, false);
        menuView.unreadCounter.setText(String.valueOf(category.getUnreadCount()));
        menuView.unreadCounter.setVisibility((category.getUnreadCount() > 0) ? View.VISIBLE : View.INVISIBLE);
        menuItem.setActionView(menuView.getRoot());
    }

    private void setMenuItemIcon(Feed feed, MenuItem menuItem) {
        if (feed.isArchivedFeed()) {
            menuItem.setIcon(R.drawable.ic_archive);
        } else if (feed.isStarredFeed()) {
            menuItem.setIcon(R.drawable.ic_star);
        } else if (feed.isPublishedFeed()) {
            menuItem.setIcon(R.drawable.ic_checkbox_marked);
        } else if (feed.isFreshFeed()) {
            menuItem.setIcon(R.drawable.ic_coffee);
        } else if (feed.isAllArticlesFeed()) {
            menuItem.setIcon(R.drawable.ic_folder_outline);
        } else {
            menuItem.setIcon(R.drawable.ic_rss_box);
        }
    }

    private void setMenuItemIcon(Category category, MenuItem menuItem) {
        menuItem.setIcon(R.drawable.ic_folder_outline);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.manage_feeds) {
            installOrStartManageFeed();
            return true;
        } else if (categoriesDisplayed) {
            return onCategoriesSelected(item);
        } else {
            return onFeedSelected(item);
        }
    }

    private boolean onFeedSelected(MenuItem item) {
        boolean browseCatsLikeFeeds = preferences.getBoolean("browse_cats_like_feeds", false);

        // find feed
        for (Feed feed : currentFeeds) {
            long feedId = feed.getId();
            if (feedId == item.getItemId()) {
                activityViewModel.setSelectedFeed(feedId);
                return true;
            }
        }
        return false;
    }

    private void displayFeedCategory(Category category) {
        categoriesDisplayed = false;

        feedsViewModel.setSelectedCategory(category.getId());
        if (feedsForCategory == null) {
            feedsForCategory = feedsViewModel.getFeedsForCategory();
            feedsForCategory.observe(this, feeds -> {
                currentFeeds = feeds;
                transformFeedsInMenuEntry(binding.navigationView.getMenu(), feeds);
            });
        }
    }

    private boolean onCategoriesSelected(MenuItem item) {
        boolean browseCatsLikeFeeds = preferences.getBoolean("browse_cats_like_feeds", false);

        // find categories
        for (Category category : feedsViewModel.getCategories().getValue()) {
            if (category.getId() == item.getItemId()) {
                if (browseCatsLikeFeeds && false) { //TODO make this work but for now just disable this option
                    Feed feed = new Feed();
                    feed.setId(category.getId());
                    feed.setTitle(category.getTitle());
                    // TODO is cat = true);
                    // activityViewModel.setSelectedFeed(feed);
                } else {
                    displayFeedCategory(category);
                }
                return true;
            }
        }
        return false;
    }

    private void installOrStartManageFeed() {
        Context context = requireContext();
        if (isManageFeedInstalled()) {
            try {
                Context freshContext = context.createPackageContext(context.getPackageName(), 0);
                Intent intent = new Intent();
                intent.setComponent(ComponentName.createRelative(freshContext, "com.geekorum.ttrss.manage_feeds.ManageFeedsActivity"));
                startActivity(intent);
            } catch (PackageManager.NameNotFoundException e) {
                Timber.wtf(e, "Unable to create our package context");
            }
        } else {
            Intent intent = new Intent(context, InstallFeatureActivity.class);
            intent.putExtra(InstallFeatureActivity.EXTRA_FEATURES_LIST, new String[]{Features.MANAGE_FEEDS});
            startActivityForResult(intent, CODE_INSTALL_MANAGE_FEED);
        }
    }

    private boolean isManageFeedInstalled() {
        return moduleManager.getInstalledModules().contains(Features.MANAGE_FEEDS);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == CODE_INSTALL_MANAGE_FEED) {
            if (resultCode == Activity.RESULT_OK) {
                installOrStartManageFeed();
            }
        }
    }

    public static FeedListFragment newInstance(FragmentFactory factory) {
        FeedListFragment fragment = (FeedListFragment) factory.instantiate(FeedListFragment.class.getClassLoader(),
                FeedListFragment.class.getName());
        return fragment;
    }
}
