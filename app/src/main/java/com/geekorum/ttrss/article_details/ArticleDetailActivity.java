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
package com.geekorum.ttrss.article_details;

import android.content.ContentUris;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import androidx.appcompat.app.ActionBar;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import com.geekorum.ttrss.R;
import com.geekorum.ttrss.articles_list.ArticleListActivity;
import com.geekorum.ttrss.databinding.ActivityArticleDetailBinding;
import com.geekorum.ttrss.databinding.ToolbarArticleDetailsBinding;
import com.geekorum.ttrss.di.ViewModelsFactory;
import com.geekorum.ttrss.session.SessionActivity;
import com.google.android.material.bottomappbar.BottomAppBar;

import javax.inject.Inject;

/**
 * An activity representing a single Article detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link ArticleListActivity}.
 */
public class ArticleDetailActivity extends SessionActivity {

    private ActivityArticleDetailBinding binding;

    private ArticleDetailsViewModel articleDetailsViewModel;

    @Inject ViewModelsFactory viewModelFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_article_detail);
        binding.setLifecycleOwner(this);
        articleDetailsViewModel = ViewModelProviders.of(this, viewModelFactory).get(ArticleDetailsViewModel.class);
        binding.setViewModel(articleDetailsViewModel);

        Resources.Theme theme = getTheme();
        TypedValue tv = new TypedValue();
        theme.resolveAttribute(R.attr.colorPrimary, tv, true);
        int colorPrimary = tv.data;
        binding.setColorPrimary(colorPrimary);
        setSupportActionBar(binding.detailToolbar);
        setUpBottomAppBar();

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        assert actionBar != null;
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Uri articleUri = intent.getData();

        if (savedInstanceState == null) {
            ArticleDetailFragment articleDetailFragment = ArticleDetailFragment.newInstance(articleUri);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.content, articleDetailFragment)
                    .commit();
        }

        articleDetailsViewModel.init(ContentUris.parseId(articleUri));
    }

    private void setUpBottomAppBar() {
        BottomAppBar bottomAppBar = binding.bottomAppBar;
        ToolbarArticleDetailsBinding toolbarBinding = ToolbarArticleDetailsBinding.inflate(getLayoutInflater());
        toolbarBinding.setLifecycleOwner(this);
        toolbarBinding.setViewModel(articleDetailsViewModel);
        bottomAppBar.addView(toolbarBinding.getRoot());
    }

}
