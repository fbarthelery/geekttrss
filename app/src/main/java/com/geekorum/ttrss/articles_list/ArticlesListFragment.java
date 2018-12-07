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

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.databinding.ViewDataBinding;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.paging.PagedListAdapter;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.geekorum.geekdroid.views.recyclerview.ItemSwiper;
import com.geekorum.geekdroid.views.recyclerview.ScrollFromBottomAppearanceItemAnimator;
import com.geekorum.geekdroid.views.recyclerview.SpacingItemDecoration;
import com.geekorum.ttrss.BR;
import com.geekorum.ttrss.R;
import com.geekorum.ttrss.data.Article;
import com.geekorum.ttrss.databinding.FragmentArticleListBinding;
import com.geekorum.ttrss.databinding.HeadlinesRowBinding;
import com.geekorum.ttrss.di.ViewModelsFactory;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;
import dagger.android.support.AndroidSupportInjection;

import javax.inject.Inject;

/**
 * Display all the articles in a list.
 */
public class ArticlesListFragment extends Fragment {
    private static final String ARG_FEED_ID = "feed_id";

    private long feedId;

    private ArticlesListAdapter adapter;
    private FragmentArticleListBinding binding;

    @Inject
    ViewModelsFactory viewModelFactory;
    private FragmentViewModel fragmentViewModel;
    private ActivityViewModel activityViewModel;
    private Snackbar setUnreadSnackbar = null;

    public ArticlesListFragment() {
        // Required public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param feedId the id of the feed displayed by this fragment.
     *
     * @return A new instance of fragment ArticlesListFragment.
     */
    public static ArticlesListFragment newInstance(long feedId) {
        ArticlesListFragment fragment = new ArticlesListFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_FEED_ID, feedId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        feedId = getArguments().getLong(ARG_FEED_ID);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentArticleListBinding.inflate(inflater, container, false);
        binding.setLifecycleOwner(this);
        setupRecyclerView(binding.articleList, binding.swipeRefreshContainer);
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        fragmentViewModel = ViewModelProviders.of(this, viewModelFactory).get(FragmentViewModel.class);
        fragmentViewModel.init(feedId);
        fragmentViewModel.getArticles().observe(this, articles -> adapter.submitList(articles));

        fragmentViewModel.getPendingArticlesSetUnread().observe(this, nbArticles -> {
            if (nbArticles > 0) {
                updateSetUnreadSnackbar(nbArticles);
            }
        });

        activityViewModel = ViewModelProviders.of(requireActivity()).get(ActivityViewModel.class);
        binding.setActivityViewModel(activityViewModel);
        binding.setFragmentViewModel(fragmentViewModel);
        activityViewModel.getRefreshEvent().observe(this, event -> {
            if (event.getContentIfNotHandled() != null) {
                fragmentViewModel.refresh();
            }
        });
    }

    private void updateSetUnreadSnackbar(Integer nbArticles) {
        String text = getResources().getQuantityString(R.plurals.undo_set_articles_read_text, nbArticles, nbArticles);
        if (setUnreadSnackbar == null) {
            setUnreadSnackbar = Snackbar.make(binding.getRoot(), text, Snackbar.LENGTH_LONG);
            setUnreadSnackbar.addCallback(new BaseTransientBottomBar.BaseCallback<Snackbar>() {
                @Override
                public void onDismissed(Snackbar transientBottomBar, int event) {
                    fragmentViewModel.commitSetUnreadActions();
                }
            });
            setUnreadSnackbar.setAction(R.string.undo_set_articles_read_btn, view -> fragmentViewModel.undoSetUnreadActions());
        }
        setUnreadSnackbar.setText(text);
        setUnreadSnackbar.show();
    }

    private void setupRecyclerView(@NonNull RecyclerView recyclerView, SwipeRefreshLayout swipeRefresh) {
        adapter = new ArticlesListAdapter();
        recyclerView.setAdapter(adapter);
        int spacing = getResources().getDimensionPixelSize(R.dimen.article_list_spacing);
        recyclerView.addItemDecoration(new SpacingItemDecoration(
                spacing, spacing));

        swipeRefresh.setOnRefreshListener(() -> {
            activityViewModel.refresh();
            // leave the progress at least 1s, then refresh its value
            // So if the user trigger a refresh but no sync operation is launch (eg: because of no connectivity)
            // the SwipeRefreshLayout will come back to original status
            swipeRefresh.postDelayed(
                    () -> binding.swipeRefreshContainer.setRefreshing(activityViewModel.isRefreshing().getValue()),
                    1000);
        });
        recyclerView.setItemAnimator(new ScrollFromBottomAppearanceItemAnimator(recyclerView, new DefaultItemAnimator()));
        ChangeReadSwiper changeReadSwiper = new ChangeReadSwiper(getContext());
        changeReadSwiper.attachToRecyclerView(recyclerView);
    }

    public class ArticlesListAdapter
            extends PagedListAdapter<Article, ArticlesListAdapter.BindingViewHolder>
            implements ChangeReadDecoration.ArticleProvider {

        private final ArticleEventHandler handler = new ArticleEventHandler();
        private LayoutInflater layoutInflater;

        ArticlesListAdapter() {
            super(ARTICLE_DIFF_CALLBACK);
            setHasStableIds(true);
            layoutInflater = LayoutInflater.from(getActivity());
        }

        @Override
        public long getItemId(int position) {
            Article item = getItem(position);
            if (item == null) {
                return RecyclerView.NO_ID;
            }
            return item.getId();
        }

        @NonNull
        @Override
        public BindingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ViewDataBinding binding = HeadlinesRowBinding.inflate(layoutInflater, parent, false);
            return new BindingViewHolder(binding);
        }

        @Nullable
        @Override
        public Article getArticle(RecyclerView.ViewHolder item) {
            int position = item.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION) {
                return getItem(position);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(@NonNull final BindingViewHolder holder, int position) {
            final Article article = getItem(position);
            ViewDataBinding binding = holder.binding;
            holder.setArticle(article);
            holder.setPosition(position);
            holder.setHandler(handler);
            binding.executePendingBindings();
        }

        public class BindingViewHolder extends RecyclerView.ViewHolder {
            private ViewDataBinding binding;

            BindingViewHolder(ViewDataBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }

            public void setArticle(Article article) {
                binding.setVariable(BR.article, article);
            }

            public void setHandler(ArticleEventHandler articleEventHandler) {
                binding.setVariable(BR.handler, articleEventHandler);
            }

            public void setPosition(int position) {
                binding.setVariable(BR.position, position);
            }
        }
    }

    public class ArticleEventHandler {

        public void onCardClicked(View card, Article article, int position) {
            activityViewModel.displayArticle(position, article);
        }

        public void onStarChanged(Article article, boolean newValue) {
            fragmentViewModel.setArticleStarred(article.getId(), newValue);
        }

        public void onShareClicked(Article article) {
            ShareCompat.IntentBuilder shareIntent = ShareCompat.IntentBuilder.from(requireActivity());
            shareIntent.setSubject(article.getTitle())
                    .setHtmlText(article.getContent())
                    .setText(article.getLink())
                    .setType("text/plain");
            startActivity(shareIntent.createChooserIntent());
        }

        public void onMenuButtonClicked(View button, final Article article, final int position) {
            PopupMenu popup = new PopupMenu(getActivity(), button);
            MenuInflater inflater = popup.getMenuInflater();
            inflater.inflate(R.menu.item_article, popup.getMenu());

            popup.setOnMenuItemClickListener(item -> onArticleMenuItemSelected(item, article, position));

            popup.show();

        }

        private boolean onArticleMenuItemSelected(MenuItem item, Article article, int position) {
            if (article == null) {
                return false;
            }

            switch (item.getItemId()) {
                case R.id.headlines_article_unread:
                    fragmentViewModel.setArticleUnread(article.getId(), !article.isTransientUnread());
                    return true;
                default:
                    return false;
            }
        }
    }

    private static final DiffUtil.ItemCallback<Article> ARTICLE_DIFF_CALLBACK = new DiffUtil.ItemCallback<Article>() {
        @Override
        public boolean areItemsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
            return oldItem.getId() == newItem.getId();
        }

        @Override
        public boolean areContentsTheSame(@NonNull Article oldItem, @NonNull Article newItem) {
            return oldItem.equals(newItem);
        }
    };

    class ChangeReadSwiper extends ItemSwiper {

        private final ChangeReadDecoration decoration;

        ChangeReadSwiper(Context context) {
            super(ItemTouchHelper.START | ItemTouchHelper.END);
            decoration = new ChangeReadDecoration(context, adapter);
            setOnSwipingListener(decoration);
        }

        @Override
        public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
            Article article = adapter.getArticle(viewHolder);
            if (article != null) {
                fragmentViewModel.setArticleUnread(article.getId(), !article.isTransientUnread());
            }
        }

        public void attachToRecyclerView(RecyclerView recyclerView) {
            super.attachToRecyclerView(recyclerView);
            recyclerView.addItemDecoration(decoration);
        }

        @Override
        public float getSwipeThreshold(RecyclerView.ViewHolder viewHolder) {
            return 0.3f;
        }
    }

}
