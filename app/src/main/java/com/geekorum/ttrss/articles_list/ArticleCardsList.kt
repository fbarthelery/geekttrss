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
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geekorum.geekdroid.views.recyclerview.SpacingItemDecoration
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.ui.AppTheme


internal fun RecyclerView.setupCardSpacing() {
    val spacing = resources.getDimensionPixelSize(R.dimen.article_list_spacing)
    val spaceItemDecoration = SpacingItemDecoration(spacing, spacing)
    addItemDecoration(spaceItemDecoration)
}


internal open class ArticlesListAdapter(
    private val layoutInflater: LayoutInflater,
    private val eventHandler: CardEventHandler
) : PagingDataAdapter<ArticleWithFeed, HeadlinesComposeViewHolder>(ARTICLE_DIFF_CALLBACK) {

    var displayFeedName = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HeadlinesComposeViewHolder {
        return HeadlinesComposeViewHolder(ComposeView(parent.context))
    }

    override fun onBindViewHolder(holder: HeadlinesComposeViewHolder, position: Int) {
        with(holder) {
            val articleWithFeed = getItem(position)
            setArticle(articleWithFeed?.article)
            setFeedIcon(articleWithFeed?.feed?.feedIconUrl)
            if (displayFeedName) {
                setFeedName(articleWithFeed?.feed)
            } else {
                setAuthor(articleWithFeed?.article?.author)
            }
            setHandler(eventHandler)
            setPosition(position)
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
        val shareIntent = ShareCompat.IntentBuilder(activity)
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

internal class HeadlinesComposeViewHolder(
    private val composeView: ComposeView
) :
    RecyclerView.ViewHolder(composeView) {

    private var title by mutableStateOf("")
    private var flavorImageUrl by mutableStateOf("")
    private var excerpt by mutableStateOf("")
    private var feedNameOrAuthor by mutableStateOf("")
    private var feedIconUrl by mutableStateOf("")
    private var isUnread by mutableStateOf(true)
    private var isStarred by mutableStateOf(false)
    private var itemPosition = 0
    private var cardEventHandler: CardEventHandler? = null
    private var article: Article? = null

    init {
        composeView.setContent {
            AppTheme {
                BoxWithConstraints(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.TopCenter
                ) {
                    var paddingStart = 0.dp
                    var paddingEnd = 0.dp
                    if (maxWidth > 496.dp) { // 384 (max card width) + 24 + 88
                        paddingStart = 24.dp
                        paddingEnd = 88.dp
                    }
                    ArticleCard(title = title,
                        flavorImageUrl = flavorImageUrl,
                        excerpt = excerpt,
                        feedNameOrAuthor = feedNameOrAuthor,
                        feedIconUrl = feedIconUrl,
                        isUnread = isUnread,
                        isStarred = isStarred,
                        onCardClick = {
                            cardEventHandler?.onCardClicked(composeView, article!!, itemPosition)
                        },
                        onOpenInBrowserClick = {
                            cardEventHandler?.onOpenButtonClicked(composeView, article!!)
                        },
                        onStarChanged = {
                            cardEventHandler?.onStarChanged(article!!, it)
                        },
                        onShareClick = {
                            cardEventHandler?.onShareClicked(article!!)
                        },
                        onToggleUnreadClick = {
                            cardEventHandler?.onMenuToggleReadSelected(article!!)
                        },
                        modifier = Modifier.padding(1.dp)
                            .padding(start = paddingStart, end = paddingEnd)
                    )
                }
            }
        }
    }

    fun setArticle(article: Article?) {
        this.article = article
        title = article?.title ?: ""
        excerpt = article?.contentExcerpt ?: ""
        isUnread = article?.isUnread ?: false
        isStarred = article?.isStarred ?: false
        setArticleFlavorImage(article?.flavorImageUri)
    }

    fun setFeedIcon(feedIconUrl: String?) {
        this.feedIconUrl = feedIconUrl ?: ""
    }

    fun setAuthor(author: String?) {
        if (!author.isNullOrBlank()) {
            feedNameOrAuthor = composeView.resources.getString(R.string.author_formatted, author)
        } else {
            feedNameOrAuthor = ""
        }
    }

    fun setFeedName(feed: Feed?) {
        val feedName = if (feed?.displayTitle?.isBlank() == true) {
            feed.title
        } else {
            feed?.displayTitle
        }
        if (!feedName.isNullOrBlank()) {
            feedNameOrAuthor = feedName
        } else {
            feedNameOrAuthor = ""
        }
    }

    fun setHandler(cardEventHandler: CardEventHandler) {
        this.cardEventHandler = cardEventHandler
    }

    fun setPosition(position: Int) {
        this.itemPosition = position
    }

    private fun setArticleFlavorImage(url: String?) {
        flavorImageUrl = url ?: ""
    }
}
