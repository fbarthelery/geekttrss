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
package com.geekorum.ttrss.articles_list

import android.content.Context
import android.graphics.Rect
import android.view.LayoutInflater
import android.view.View
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import com.geekorum.geekdroid.views.recyclerview.SingleItemSwipedCallback.OnSwipingItemListener
import com.geekorum.geekdroid.views.recyclerview.ViewItemDecoration
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.databinding.ViewChangeReadDecorationBinding
import com.google.android.material.card.MaterialCardView

/**
 * Recycler view item decoration to show the read/unread label when swiping.
 */
class ChangeReadDecoration(
    context: Context,
    private val articleProvider: ArticleProvider
) : ViewItemDecoration(), OnSwipingItemListener {

    private var isSwipingToRight = false
    private var swipingItem: RecyclerView.ViewHolder? = null

    init {
        setDrawPainter(Painter(context))
    }

    override fun onSwipingItem(item: RecyclerView.ViewHolder?) {
        swipingItem = item
    }

    private inner class Painter(context: Context) : ViewPainter() {
        private val binding: ViewChangeReadDecorationBinding

        init {
            val inflater = LayoutInflater.from(context)
            binding = ViewChangeReadDecorationBinding.inflate(inflater)
        }

        override fun computeDrawingBounds(parent: RecyclerView, state: RecyclerView.State, outBounds: Rect) {
            val childItemView = swipingItem?.let { swipingItem ->
                parent.children.find { parent.getChildViewHolder(it) == swipingItem }
            }
            childItemView?.let {
                isSwipingToRight = it.translationX >= 0
                parent.getDecoratedBoundsWithMargins(it, outBounds)
                val card = (it as? ConstraintLayout)?.findViewById<MaterialCardView>(R.id.headlines_row)
                if (card != null) { // we are in a w800dp layout
                    outBounds.left = card.left
                    outBounds.right = card.right
                }
            }
        }

        override fun getView(parent: RecyclerView, state: RecyclerView.State): View? {
            val swipingArticle = swipingItem?.let { articleProvider.getArticle(it) } ?: return null
            with(binding) {
                article = swipingArticle
                isSwipeToRight = isSwipingToRight
                executePendingBindings()
            }
            return binding.root
        }
    }

    /**
     * Interface to provide the article for the swiped item.
     */
    interface ArticleProvider {
        fun getArticle(item: RecyclerView.ViewHolder): Article?
    }
}