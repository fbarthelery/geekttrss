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
package com.geekorum.ttrss.article_details

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.webkit.WebViewClient
import androidx.annotation.ColorRes
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.core.text.parseAsHtml
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleContentIndexed
import com.google.accompanist.insets.LocalWindowInsets
import com.google.accompanist.insets.rememberInsetsPaddingValues
import com.google.accompanist.insets.ui.Scaffold
import kotlinx.coroutines.delay
import java.util.*


@Composable
fun rememberArticleDetailsScreenState(): ArticleDetailsScreenState {
    val scrollState = rememberScrollState()
    return remember {
        ArticleDetailsScreenState(scrollState)
    }
}


class ArticleDetailsScreenState(
    val scrollState: ScrollState
) {
    private var _bottomAppBarIsVisible by mutableStateOf(true)
    val bottomAppBarIsVisible: Boolean
        @Composable get() {
            if (scrollState.isScrollingUp()) {
                _bottomAppBarIsVisible = true
            } else if (scrollState.isScrollingDown()) {
                _bottomAppBarIsVisible = false
            }
            return _bottomAppBarIsVisible
        }

    val appBarElevation: Dp
        @Composable get() {
            val result by animateDpAsState(if (scrollState.value > 0)
                AppBarDefaults.TopAppBarElevation
            else 0.dp)
            return result
        }

    val isAtEndOfArticle: Boolean by derivedStateOf {
        scrollState.value == scrollState.maxValue
    }

    @Composable
    private fun ScrollState.isScrollingUp(): Boolean {
        var previousScrollOffset by remember(this) { mutableStateOf(value) }
        return remember(this) {
            derivedStateOf {
                return@derivedStateOf (previousScrollOffset > value)
                    .also {
                        previousScrollOffset = value
                    }
            }
        }.value
    }

    @Composable
    private fun ScrollState.isScrollingDown(): Boolean {
        var previousScrollOffset by remember(this) { mutableStateOf(value) }
        return remember(this) {
            derivedStateOf {
                return@derivedStateOf (previousScrollOffset < value)
                    .also {
                        previousScrollOffset = value
                    }
            }
        }.value
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ArticleDetailsScreen(
    articleDetailsViewModel: ArticleDetailsViewModel,
    onNavigateUpClick: () -> Unit,
    webViewClient: WebViewClient? = null,
) {
    val articleDetailsScreenState = rememberArticleDetailsScreenState()

    val article by articleDetailsViewModel.article.observeAsState()

    Scaffold(
        topBar = {
            val appBarElevation = articleDetailsScreenState.appBarElevation
            ArticleTopAppBar(appBarElevation, onNavigateUpClick)
        },
        bottomBar = {
            val context = LocalContext.current
            val backgroundColorId = if (article?.isTransientUnread == true) {
                R.color.article_detail_bottom_appbar_unread
            } else {
                R.color.article_detail_bottom_appbar_read
            }

            ArticleBottomAppBar(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                articleDetailsViewModel = articleDetailsViewModel,
                bottomAppBarIsVisible = articleDetailsScreenState.bottomAppBarIsVisible,
                background = getColorStateList(backgroundColorId),
                onFabClicked = {
                    article?.let { articleDetailsViewModel.openArticleInBrowser(context, it) }
                },
            )
        }
    ) { padding ->
        article?.let {
            ArticleDetailsContent(it,
                webViewClient = webViewClient,
                modifier = Modifier.padding(top = padding.calculateTopPadding()),
                scrollState = articleDetailsScreenState.scrollState)

            LaunchedEffect(articleDetailsScreenState.isAtEndOfArticle) {
                if (articleDetailsScreenState.isAtEndOfArticle) {
                    if (articleDetailsScreenState.scrollState.value == 0) {
                        delay(2000)
                    }
                    articleDetailsViewModel.setArticleUnread(false)
                }
            }
        }
    }
}

@Composable
private fun ArticleTopAppBar(appBarElevation: Dp, onNavigateUpClick: () -> Unit) {
    Surface(elevation = appBarElevation) {
        val statusBarPadding = rememberInsetsPaddingValues(LocalWindowInsets.current.statusBars)
        TopAppBar(
            modifier = Modifier.padding(statusBarPadding),
            title = {},
            elevation = 0.dp,
            backgroundColor = MaterialTheme.colors.surface,
            navigationIcon = {
                IconButton(onClick = onNavigateUpClick) {
                    Icon(Icons.Default.ArrowBack, contentDescription = null)
                }
            },
        )
    }
}


@Composable
private fun ArticleDetailsContent(
    article: Article,
    modifier: Modifier = Modifier,
    webViewClient: WebViewClient? = null,
    scrollState: ScrollState = rememberScrollState()
) {
    ArticleDetailsContent(article, modifier, scrollState) {
        val baseUrl = article.link.toUri().let { "${it.scheme}://${it.host}" }
        val context = LocalContext.current
        val content = remember(context, article) {
            val cssOverride = createCssOverride(context)
            prepareArticleContent(article.content, cssOverride)
        }
        ArticleContentWebView(baseUrl = baseUrl, content = content,
            webViewClient = webViewClient,
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 64.dp)
        )
    }
}

@Composable
private fun ArticleDetailsContent(
    article: Article,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    articleContent: @Composable ColumnScope.() -> Unit) {
    Column(modifier
        .padding(horizontal = 16.dp)
        .verticalScroll(scrollState)
    ) {
        val title = remember(article) {
            article.title.parseAsHtml().toString()
        }
        ArticleHeader(title = title, date = article.getDateString())
        Divider(Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp))
        articleContent()
    }
}

@Composable
private fun ArticleHeader(
    title: String,
    date: String,
) {
    Column(Modifier
        .fillMaxWidth()
        .padding(top = 36.dp)
    ) {
        ArticleTitle(title)
        Text(date,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.End)
        )
    }
}

@Composable
private fun ArticleTitle(title: String) {
    Text(title, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.primary)
}

@Composable
private fun getColorStateList(@ColorRes colorRes: Int): ColorStateList? {
    val context = LocalContext.current
    val resources = context.resources
    return ResourcesCompat.getColorStateList(resources, colorRes, context.theme)
}


private fun createCssOverride(context: Context): String {
    val theme = context.theme
    val colors = WebViewColors.fromTheme(theme)
    val backgroundHexColor = colors.backgroundColor.toRgbaCall()
    val textColor = colors.textColor.toRgbaCall()
    val linkHexColor = colors.linkColor.toRgbaCall()
    return """
                @font-face {
                    font-family: "TextAppearance.AppTheme.Body1";
                    src: url("${WebFontProvider.WEB_FONT_ARTICLE_BODY_URL}");
                }
                body {
                    background : $backgroundHexColor;
                    color : $textColor;
                    font-family: "TextAppearance.AppTheme.Body1", serif;
                }
                a:link {
                    color: $linkHexColor;
                }
                a:visited {
                    color: $linkHexColor;
                }
                """.trimIndent()
}

private fun Int.toRgbaCall(): String {
    return "rgba(%d, %d, %d, %.2f)".format(Locale.ENGLISH,
        Color.red(this), Color.green(this), Color.blue(this), Color.alpha(this) / 255f)
}


private fun prepareArticleContent(
    articleContent: String,
    cssOverride: String,
): String {
    return """<html>
                |<head>
                |<meta content="text/html; charset=utf-8" http-equiv="content-type">
                |<meta name="viewport" content="width=device-width, user-scalable=no" />
                |<style type="text/css">body {
                |padding : 0px; margin : 0px; line-height : 130%;
                |}
                |img, video, iframe { max-width : 100%; width : auto; height : auto; }
                |table { width : 100%; }
                |$cssOverride
                |</style>
                |</head>
                |<body>
                |$articleContent
                |</body>
                |</html>""".trimMargin()
}



@Preview
@Composable
fun PreviewArticleDetailsContent() {
    MaterialTheme {
        val article = Article(contentData = ArticleContentIndexed(
            title = "My simple but hilariously and excessively long headline",
            content = "<b>Hello world</b>"
        ))
        ArticleDetailsContent(article = article) {
            Box(Modifier
                .size(width = 400.dp, height = 900.dp)
                .background(androidx.compose.ui.graphics.Color.Gray))
        }
    }

}
