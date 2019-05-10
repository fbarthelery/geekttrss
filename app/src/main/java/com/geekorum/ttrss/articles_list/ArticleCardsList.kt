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
package com.geekorum.ttrss.articles_list

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.PopupMenu
import androidx.core.app.ShareCompat
import androidx.paging.PagedListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geekorum.geekdroid.views.recyclerview.SpacingItemDecoration
import com.geekorum.ttrss.BR
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.databinding.HeadlinesRowBinding
import com.squareup.picasso.Picasso
import kotlin.math.roundToInt


internal fun RecyclerView.setupCardSpacing() {
    val spacing = resources.getDimensionPixelSize(R.dimen.article_list_spacing)
    val spaceItemDecoration = SpacingItemDecoration(spacing, spacing)
    addItemDecoration(spaceItemDecoration)
}


internal open class ArticlesListAdapter(
    private val layoutInflater: LayoutInflater,
    private val eventHandler: CardEventHandler
) : PagedListAdapter<Article, HeadlinesBindingViewHolder>(ARTICLE_DIFF_CALLBACK) {

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position)?.id ?: RecyclerView.NO_ID
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadlinesBindingViewHolder {
        val binding = HeadlinesRowBinding.inflate(layoutInflater, parent, false)
        return HeadlinesBindingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HeadlinesBindingViewHolder, position: Int) {
        with(holder) {
            val article = getItem(position)
            setArticle(article)
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
        parent.viewTreeObserver.addOnPreDrawListener(object: ViewTreeObserver.OnPreDrawListener {

            override fun onPreDraw(): Boolean {
                // wait for the parent to be laid out
                val width: Float = parent.width.takeIf { it != 0 }?.toFloat() ?: return true
                val height = width * 9 / 16
                val finalUrl = url.takeUnless { it.isNullOrEmpty() }
                Picasso.with(view.context)
                    .load(finalUrl)
                    .resize(0, height.roundToInt())
                    .onlyScaleDown()
                    .into(view)

                parent.viewTreeObserver.removeOnPreDrawListener(this)
                return true
            }
        })
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

private val ARTICLE_DIFF_CALLBACK = object : DiffUtil.ItemCallback<Article>() {
    override fun areItemsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Article, newItem: Article): Boolean {
        return oldItem == newItem
    }
}
