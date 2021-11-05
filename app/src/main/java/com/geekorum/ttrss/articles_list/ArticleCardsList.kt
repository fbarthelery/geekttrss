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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.app.ShareCompat
import androidx.paging.PagingDataAdapter
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.compose.itemsIndexed
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.geekorum.geekdroid.views.recyclerview.SpacingItemDecoration
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleContentIndexed
import com.geekorum.ttrss.data.ArticleWithFeed
import com.geekorum.ttrss.data.Feed
import com.geekorum.ttrss.ui.AppTheme
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState


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
                        modifier = Modifier
                            .padding(1.dp)
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
        isUnread = article?.isTransientUnread ?: false
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




@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ArticleCardList(
    viewModel: BaseArticlesViewModel,
    onCardClick: (Int, Article) -> Unit,
    onShareClick: (Article) -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    additionalContentPaddingBottom: Dp = 0.dp,
) {
    val isRefreshing by viewModel.isRefreshing.observeAsState(false)
    SwipeRefresh(rememberSwipeRefreshState(isRefreshing),
        onRefresh = {
            viewModel.refresh()
        },
        modifier = modifier
    ) {
        val pagingItems = viewModel.articles.collectAsLazyPagingItems()
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = rememberInsetsPaddingValues(
                insets = LocalWindowInsets.current.navigationBars,
                additionalBottom = additionalContentPaddingBottom,
                additionalStart = 8.dp,
                additionalTop = 8.dp,
                additionalEnd = 8.dp
            ),
            modifier = Modifier.fillMaxSize()
        ) {
            itemsIndexed(pagingItems,
                key = { _, articleWithFeed -> articleWithFeed.article.id }
            ) { index, articleWithFeed ->
                if (articleWithFeed != null) {
                    SwipeableArticleCard(
                        articleWithFeed = articleWithFeed,
                        viewModel = viewModel,
                        onCardClick = { onCardClick(index, articleWithFeed.article) },
                        onOpenInBrowserClick = onOpenInBrowserClick,
                        onShareClick = onShareClick)
                }
            }
        }
    }
}

@Composable
private fun SwipeableArticleCard(
    articleWithFeed: ArticleWithFeed,
    viewModel: BaseArticlesViewModel,
    onCardClick: () -> Unit,
    onOpenInBrowserClick: (Article) -> Unit,
    onShareClick: (Article) -> Unit
) {
    val (article, feed) = articleWithFeed
    val displayFeedName by viewModel.isMultiFeed.collectAsState()
    val feedNameOrAuthor = if (displayFeedName) {
        feed.displayTitle.takeIf { it.isNotBlank() } ?: feed.title
    } else {
        stringResource(R.string.author_formatted, article.author)
    }

    SwipeableArticleCard(
//                TODO add this on beta03
//                modifier = Modifier.animateItemPlacement(),
        title = article.title,
        flavorImageUrl = article.flavorImageUri,
        excerpt = article.contentExcerpt,
        feedNameOrAuthor = feedNameOrAuthor,
        feedIconUrl = feed.feedIconUrl,
        isUnread = article.isUnread,
        isStarred = article.isStarred,
        onCardClick = onCardClick,
        onOpenInBrowserClick = { onOpenInBrowserClick(article) },
        onStarChanged = { viewModel.setArticleStarred(article.id, it) },
        onShareClick = { onShareClick(article) },
        onToggleUnreadClick = {
            viewModel.setArticleUnread(article.id, !article.isTransientUnread)
        },
        behindCardContent = { direction ->
            if (direction != null) {
                ChangeReadBehindItem(direction)
            }
        },
        onSwiped = {
            viewModel.setArticleUnread(article.id, !article.isTransientUnread)
        }
    )
}


@Composable
private fun ChangeReadBehindItem(dismissDirection: DismissDirection) {
    val horizontalArrangement = when(dismissDirection) {
        DismissDirection.StartToEnd -> Arrangement.Start
        else -> Arrangement.End
    }
    Row(modifier = Modifier
        .fillMaxSize()
        .padding(horizontal = 16.dp),
        horizontalArrangement = horizontalArrangement,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val text = stringResource(id = R.string.mark_as_read)
        if (dismissDirection == DismissDirection.StartToEnd) {
            Icon(painter = painterResource(R.drawable.ic_archive), contentDescription = text,
                modifier = Modifier.padding(end = 8.dp),
                tint = MaterialTheme.colors.secondary
            )
        }
        Text(text,
            style = MaterialTheme.typography.caption)
        if (dismissDirection == DismissDirection.EndToStart) {
            Icon(painter = painterResource(R.drawable.ic_archive), contentDescription = text,
                modifier = Modifier.padding(start = 8.dp),
                tint = MaterialTheme.colors.secondary
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun ArticleCardList() {
    val articles = Array(25) {
        Article(id = it.toLong(),
            contentData = ArticleContentIndexed("article $it", author = "author $it"),
            contentExcerpt = "Excerpt $it"
        )
    }
    val articlesState = remember { mutableStateListOf(*articles)}

    val isRefreshing = false
    SwipeRefresh(rememberSwipeRefreshState(isRefreshing),
        onRefresh = { /*TODO*/ }
    ) {
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(articlesState, key = { it.id }) { article ->
                SwipeableArticleCard(
//                TODO add this on beta03
//                modifier = Modifier.animateItemPlacement(),
                    title = article.title,
                    flavorImageUrl = article.flavorImageUri,
                    excerpt = article.contentExcerpt,
                    feedNameOrAuthor = article.author,
                    feedIconUrl = "",
                    isUnread = article.isTransientUnread,
                    isStarred = article.isStarred,
                    onCardClick = {},
                    onOpenInBrowserClick = {},
                    onStarChanged = {  },
                    onShareClick = {},
                    onToggleUnreadClick = {  },
                    behindCardContent = { direction ->
                        if (direction != null) {
                            val color = if (direction == DismissDirection.StartToEnd)
                                Color.Blue
                            else Color.Red
                            Box(Modifier
                                .fillMaxSize()
                                .background(color)) {
                                ChangeReadBehindItem(direction)
                            }
                        }
                    },
                    onSwiped = {
                        articlesState.remove(article)
                    }
                )
            }
        }
    }
}


@Preview
@Composable
fun PreviewArticleCardList() {
    AppTheme {
        ArticleCardList()
    }
}
