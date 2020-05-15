/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2020 by Frederic-Charles Barthelery.
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
import android.graphics.Rect;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.geekorum.geekdroid.views.recyclerview.SingleItemSwipedCallback;
import com.geekorum.geekdroid.views.recyclerview.ViewItemDecoration;
import com.geekorum.ttrss.R;
import com.geekorum.ttrss.data.Article;
import com.geekorum.ttrss.databinding.ViewChangeReadDecorationBinding;
import com.google.android.material.card.MaterialCardView;

/**
 * Recycler view item decoration to show the read/unread label when swiping.
 */
public class ChangeReadDecoration extends ViewItemDecoration implements SingleItemSwipedCallback.OnSwipingItemListener {
    private final ArticleProvider articleProvider;
    private boolean isSwipingToRight;
    private RecyclerView.ViewHolder swipingItem;

    public ChangeReadDecoration(Context context, ArticleProvider articleProvider) {
        this.articleProvider = articleProvider;
        setDrawPainter(new Painter(context));
    }

    @Override
    public void onSwipingItem(@Nullable RecyclerView.ViewHolder item) {
        swipingItem = item;
    }

    private class Painter extends ViewPainter {
        private final ViewChangeReadDecorationBinding binding;

        Painter(Context context) {
            LayoutInflater inflater = LayoutInflater.from(context);
            binding = ViewChangeReadDecorationBinding.inflate(inflater);
        }

        @Override
        protected void computeDrawingBounds(RecyclerView parent, RecyclerView.State state, Rect outBounds) {
            for (int i = 0; i < parent.getChildCount(); i++) {
                final View child = parent.getChildAt(i);
                if (swipingItem != null && parent.getChildViewHolder(child) == swipingItem) {
                    isSwipingToRight = child.getTranslationX() >= 0;
                    parent.getDecoratedBoundsWithMargins(child, outBounds);
                    if (swipingItem.itemView instanceof ConstraintLayout) { // we are in a w800dp layout
                        MaterialCardView card = swipingItem.itemView.findViewById(R.id.headlines_row);
                        outBounds.left = card.getLeft();
                        outBounds.right = card.getRight();
                    }
                    return;
                }
            }
        }

        @Override
        protected View getView(RecyclerView parent, RecyclerView.State state) {
            if (swipingItem == null) {
                return null;
            }
            Article article = articleProvider.getArticle(swipingItem);
            if (article != null) {
                binding.setArticle(article);
            }
            binding.setIsSwipeToRight(isSwipingToRight);
            binding.executePendingBindings();
            return binding.getRoot();
        }
    }

    /**
     * Interface to provide the article for the swiped item.
     */
    public interface ArticleProvider {
        Article getArticle(RecyclerView.ViewHolder item);
    }
}
