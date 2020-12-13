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

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.core.app.ShareCompat
import androidx.core.view.doOnPreDraw
import androidx.paging.PagedListAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.geekorum.geekdroid.views.recyclerview.SpacingItemDecoration
import com.geekorum.ttrss.BR
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.databinding.HeadlinesRowBinding
import kotlin.math.roundToInt


internal fun RecyclerView.setupCardSpacing() {
    val spacing = resources.getDimensionPixelSize(R.dimen.article_list_spacing)
    val spaceItemDecoration = SpacingItemDecoration(spacing, spacing)
    addItemDecoration(spaceItemDecoration)
}


internal open class ArticlesListAdapter(
    private val layoutInflater: LayoutInflater,
    private val eventHandler: CardEventHandler
) : PagingDataAdapter<ArticleWithFeed, HeadlinesBindingViewHolder>(ARTICLE_DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadlinesBindingViewHolder {
        val binding = HeadlinesRowBinding.inflate(layoutInflater, parent, false)
        return HeadlinesBindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeadlinesBindingViewHolder, position: Int) {
        with(holder) {
            val articleWithFeed = getItem(position)
            setArticle(articleWithFeed?.article)
            setHandler(eventHandler)
            setPosition(position)
        }
        holder.binding.executePendingBindings()
    }
}

internal class HeadlinesBindingViewHolder(val binding: HeadlinesRowBinding) :
    RecyclerView.ViewHolder(binding.root) {

    fun setArticle(article: Article?) {
        binding.setVariable(BR.article, article)
        setArticleFlavorImage(article?.flavorImageUri)
    }

    fun setHandler(cardEventHandler: CardEventHandler) {
        binding.setVariable(BR.handler, cardEventHandler)
    }

    fun setPosition(position: Int) {
        binding.setVariable(BR.position, position)
    }

    private fun setArticleFlavorImage(url: String?) {
        val view = binding.flavorImage
        val parent = view.parent as View
        val finalUrl = url.takeUnless { it.isNullOrEmpty() }
        // just to be sure that we will load an image. run it without size info
        view.load(finalUrl)

        // reload with size info to resize bitmap
        // this is important to not load big bitmap into memory
        parent.doOnPreDraw {
            val width = parent.width.takeIf { it != 0 } ?: return@doOnPreDraw
            val height = (width.toFloat() * 9 / 16).roundToInt()
            view.maxHeight = height
            view.load(finalUrl) {
                size(width, height)
            }
        }
    }
}


abstract class CardEventHandler(
    val context: Context
) {

    abstract fun onCardClicked(card: View, article: Article, position: Int)

    abstract fun onStarChanged(article: Article, newValue: Boolean)

    abstract fun onShareClicked(article: Article)

    abstract fun onMenuToggleReadSelected(article: Article)

    abstract fun onOpenButtonClicked(button: View, article: Article)

    fun onMenuButtonClicked(button: View, article: Article, position: Int) {
        PopupMenu(context, button).run {
            menuInflater.inflate(R.menu.item_article, menu)
            setOnMenuItemClickListener { item -> onArticleMenuItemSelected(item, article, position) }
            show()
        }
    }

    private fun onArticleMenuItemSelected(item: MenuItem, article: Article, position: Int): Boolean {
        return when (item.itemId) {
            R.id.headlines_article_unread -> {
                onMenuToggleReadSelected(article)
                true
            }
            else -> false
        }
    }

    protected fun createShareIntent(activity: Activity, article: Article): Intent {
        val shareIntent = ShareCompat.IntentBuilder.from(activity)
        shareIntent.setSubject(article.title)
            .setHtmlText(article.content)
            .setText(article.link)
            .setType("text/plain")
        return shareIntent.createChooserIntent()
    }
}

private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<ArticleWithFeed>() {
    override fun areItemsTheSame(oldItem: ArticleWithFeed, newItem: ArticleWithFeed): Boolean {
        return oldItem.article.id == newItem.article.id
    }

    override fun areContentsTheSame(oldItem: ArticleWithFeed, newItem: ArticleWithFeed): Boolean {
        return oldItem == newItem
    }
}
