/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2022 by Frederic-Charles Barthelery.
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
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleContentIndexed
import com.geekorum.ttrss.ui.AppTheme
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

@Composable
fun ArticleDetailsScreen(
    articleDetailsViewModel: ArticleDetailsViewModel,
    widthSizeClass: WindowWidthSizeClass,
    heightSizeClass: WindowHeightSizeClass,
    onNavigateUpClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    webViewClient: WebViewClient? = null,
) {
    if (widthSizeClass == WindowWidthSizeClass.Compact || heightSizeClass == WindowHeightSizeClass.Compact) {
        ArticleDetailsScreen(
            articleDetailsViewModel = articleDetailsViewModel,
            onNavigateUpClick = onNavigateUpClick,
            onArticleClick = onArticleClick,
            webViewClient = webViewClient
        )
    } else {
        ArticleDetailsScreenHero(
            articleDetailsViewModel = articleDetailsViewModel,
            onNavigateUpClick = onNavigateUpClick,
            onArticleClick = onArticleClick,
            webViewClient = webViewClient
        )
    }
}

@Composable
fun ArticleDetailsScreenHero(
    articleDetailsViewModel: ArticleDetailsViewModel,
    onNavigateUpClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    webViewClient: WebViewClient? = null) {
    val articleDetailsScreenState = rememberArticleDetailsScreenState()

    val article by articleDetailsViewModel.article.observeAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            val backgroundColorId = if (article?.isTransientUnread == true) {
                R.color.article_detail_bottom_appbar_unread
            } else {
                R.color.article_detail_bottom_appbar_read
            }
            ArticleTopActionsBar(
                title = { Text(article?.title ?: "") },
                isStarred = article?.isStarred ?: false,
                background = getColorStateList(backgroundColorId),
                elevation = articleDetailsScreenState.appBarElevation,
                onNavigateUpClick = onNavigateUpClick,
                onToggleUnreadClick = { articleDetailsViewModel.toggleArticleRead() },
                onStarredChange = { articleDetailsViewModel.onStarChanged(it) },
                onShareClick = { articleDetailsViewModel.shareArticle(context) }
            )
        },
        floatingActionButton = {
            OpenInBrowserExtendedFab(
                modifier = Modifier.padding(8.dp),
                onClick = {
                    article?.let { articleDetailsViewModel.openArticleInBrowser(context, it) }
                }
            )
        }
    ) { padding ->
        article?.let { article ->
            val readMoreArticles by articleDetailsViewModel.additionalArticles.collectAsState()

            ArticleDetailsHeroContent(
                article,
                readMoreArticles = readMoreArticles,
                onArticleClick = onArticleClick,
                modifier = Modifier.padding(padding),
                scrollState = articleDetailsScreenState.scrollState,
                webViewClient = webViewClient
            )

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
private fun ArticleDetailsHeroContent(
    article: Article,
    readMoreArticles: List<ArticleWithTag>,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    webViewClient: WebViewClient? = null,
    scrollState: ScrollState = rememberScrollState(),
) {
    ArticleDetailsHeroContent(
        article,
        readMoreArticles,
        onArticleClick,
        modifier = modifier,
        scrollState = scrollState,
    ) {
        val context = LocalContext.current
        val baseUrl = article.link.toUri().let { "${it.scheme}://${it.host}" }
        val content = remember(context, article) {
            val cssOverride = createCssOverride(context)
            prepareArticleContent(article.content, cssOverride)
        }
        ArticleContentWebView(baseUrl = baseUrl, content = content,
            webViewClient = webViewClient,
        )
    }
}

@Composable
private fun ArticleDetailsHeroContent(
    article: Article,
    readMoreArticles: List<ArticleWithTag>,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    articleContent: @Composable () -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArticleHeader(
            title = article.title,
            date = article.getDateString(),
            flavorImageUri = article.flavorImageUri,
            author = article.author,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        Column(Modifier
            .width(450.dp)
            .padding(bottom = 64.dp)) {
            articleContent()

            if (readMoreArticles.isNotEmpty()) {
                Divider(Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp))
                ReadMoreSection(articles = readMoreArticles,
                    onArticleClick = onArticleClick)
            }
        }
    }
}



@Composable
fun ArticleDetailsScreen(
    articleDetailsViewModel: ArticleDetailsViewModel,
    onNavigateUpClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
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

            ArticleBottomActionsBar(
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
            val readMoreArticles by articleDetailsViewModel.additionalArticles.collectAsState()
            ArticleDetailsContent(it,
                readMoreArticles = readMoreArticles,
                onArticleClick = onArticleClick,
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
        TopAppBar(
            modifier = Modifier.statusBarsPadding(),
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
private fun OpenInBrowserExtendedFab(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text = stringResource(R.string.open_article_in_browser)
    ExtendedFloatingActionButton(
        text = { Text(text) },
        icon = { Icon(AppTheme.Icons.OpenInBrowser, contentDescription = text) },
        modifier = modifier,
        onClick = onClick)
}

@Composable
private fun ArticleDetailsContent(
    article: Article,
    readMoreArticles: List<ArticleWithTag>,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    webViewClient: WebViewClient? = null,
    scrollState: ScrollState = rememberScrollState()
) {
    ArticleDetailsContent(article, readMoreArticles, onArticleClick , modifier, scrollState) {
        val baseUrl = article.link.toUri().let { "${it.scheme}://${it.host}" }
        val context = LocalContext.current
        val content = remember(context, article) {
            val cssOverride = createCssOverride(context)
            prepareArticleContent(article.content, cssOverride)
        }
        val verticalPadding = if (article.content.isNotBlank()) 16.dp else 0.dp
        ArticleContentWebView(baseUrl = baseUrl, content = content,
            webViewClient = webViewClient,
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 450.dp)
                .padding(vertical = verticalPadding)
        )
    }
}

@Composable
private fun ArticleDetailsContent(
    article: Article,
    readMoreArticles: List<ArticleWithTag>,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    articleContent: @Composable () -> Unit) {
    Column(modifier
        .padding(horizontal = 16.dp)
        .verticalScroll(scrollState)
    ) {
        ArticleHeaderWithoutImage(
            title = article.title,
            date = article.getDateString()
        )
        Divider(Modifier
            .fillMaxWidth())
        articleContent()

        if (readMoreArticles.isNotEmpty()) {
            Divider(Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp))
            ReadMoreSection(articles = readMoreArticles,
                onArticleClick = onArticleClick,
                Modifier.padding(bottom = 80.dp))
        } else {
            Spacer(Modifier.height(72.dp))
        }
    }
}

@Composable
private fun ArticleHeader(
    title: String,
    flavorImageUri: String,
    author: String,
    date: String,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(modifier = modifier) {
        if (maxWidth < 600.dp) {
            ArticleHeaderWithoutImage(title = title, date = date)
        } else {
            ArticleHeaderWithFlavorImage(
                title = title,
                flavorImageUri = flavorImageUri,
                author = author,
                date = date
            )
        }
    }
}

@Composable
private fun ArticleHeaderWithoutImage(
    title: String,
    date: String,
) {
    Column(Modifier
        .fillMaxWidth()
        .padding(top = 36.dp)
    ) {
        Text(title, style = MaterialTheme.typography.h4, color = MaterialTheme.colors.primary)
        Text(date,
            style = MaterialTheme.typography.caption,
            modifier = Modifier
                .padding(top = 16.dp)
                .align(Alignment.End)
        )
    }
}

@Composable
private fun ArticleHeaderWithFlavorImage(
    title: String,
    flavorImageUri: String,
    author: String,
    date: String,
) {
    Column(Modifier.fillMaxWidth()) {
        val finalImageUrl = flavorImageUri.takeUnless { it.isEmpty() }
        var hasImage by remember { mutableStateOf(finalImageUrl != null) }
        if (hasImage) {
            val imageReq = ImageRequest.Builder(LocalContext.current)
                .data(finalImageUrl)
                .placeholder(R.drawable.drawer_header_dark)
                .error(R.drawable.drawer_header_dark)
                .listener(onError = { _, _ -> hasImage = false})
                .build()
            AsyncImage(model = imageReq,
                contentDescription = null,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 300.dp)
            )
        }

        Column(Modifier
            .widthIn(500.dp, 800.dp)
            .padding(top = 36.dp)
            .align(Alignment.CenterHorizontally)
        ) {
            Text(title,
                style = MaterialTheme.typography.h3,
                color = MaterialTheme.colors.primary,
                modifier = Modifier
                    .padding(top = 16.dp)
            )

            val authorDateText = when {
                author.isNotBlank() && date.isNotBlank() -> "$author, $date"
                author.isNotBlank() -> author
                date.isNotBlank() -> date
                else -> ""
            }
            Text(authorDateText,
                style = MaterialTheme.typography.h6,
                modifier = Modifier
                    .padding(top = 16.dp)
                    .align(Alignment.End)
            )
        }
    }
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
        Surface {
            ArticleDetailsContent(article = article, readMoreArticles = emptyList(),
                onArticleClick = {}) {
                Box(Modifier
                    .size(width = 400.dp, height = 900.dp)
                    .background(androidx.compose.ui.graphics.Color.Gray))
            }
        }
    }
}

@Preview(device = "spec:shape=Normal,width=1920,height=1080,unit=dp,dpi=480")
@Composable
fun PreviewArticleDetailsHeroContent() {
    MaterialTheme {
        val article = Article(contentData = ArticleContentIndexed(
            title = "My simple but hilariously and excessively long headline",
            content = "<b>Hello world</b>"
        ))
        Surface {
            ArticleDetailsHeroContent(article = article,
                readMoreArticles = emptyList(),
                onArticleClick = {}) {
                Box(Modifier
                    .fillMaxWidth()
                    .height(height = 1500.dp)
                    .background(androidx.compose.ui.graphics.Color.Gray))
            }
        }
    }
}


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ReadMoreSection(
    articles: List<ArticleWithTag>,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(stringResource(R.string.lbl_read_more),
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 16.dp)
        )
        for ((article, tag) in articles) {
            Card(onClick = { onArticleClick(article) },
            modifier = Modifier
                .padding(vertical = 8.dp)
                .widthIn(max = 400.dp)
                .fillMaxWidth()
            ) {
                Row(Modifier.padding(vertical = 8.dp)) {
                    AsyncImage(
                        model = article.flavorImageUri,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.size(88.dp),
                        contentDescription = null
                    )
                    Column(Modifier.padding(horizontal = 16.dp)) {
                        Text(article.title, style = MaterialTheme.typography.subtitle1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.heightIn(max = 56.dp)
                        )
                        Text("#$tag", style = MaterialTheme.typography.caption)
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun PreviewReadMoreSection() {
    AppTheme {
        ReadMoreSection(
            listOf(
                ArticleWithTag(
                    article = Article(id = 42, contentData = ArticleContentIndexed(title = "New quantum computer")),
                    tag = "Quantum"
                )
            ),
            onArticleClick = {}
        )
    }
}
