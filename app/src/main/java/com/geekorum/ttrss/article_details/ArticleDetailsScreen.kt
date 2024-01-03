/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2024 by Frederic-Charles Barthelery.
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

import android.content.res.ColorStateList
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.res.ResourcesCompat
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleContentIndexed
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.components.OpenInBrowserIcon
import com.google.accompanist.web.AccompanistWebViewClient
import kotlinx.coroutines.delay
import java.util.Locale


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
    val isAtEndOfArticle: Boolean by derivedStateOf {
        scrollState.value == scrollState.maxValue
    }
}

@Composable
fun ArticleDetailsScreen(
    articleDetailsViewModel: ArticleDetailsViewModel,
    widthSizeClass: WindowWidthSizeClass,
    heightSizeClass: WindowHeightSizeClass,
    onNavigateUpClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    webViewClient: AccompanistWebViewClient,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailsScreenHero(
    articleDetailsViewModel: ArticleDetailsViewModel,
    onNavigateUpClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    webViewClient: AccompanistWebViewClient) {
    val articleDetailsScreenState = rememberArticleDetailsScreenState()

    val article by articleDetailsViewModel.article.observeAsState()
    val browserIcon by articleDetailsViewModel.browserIcon.collectAsStateWithLifecycle()
    val activity = LocalContext.current as ComponentActivity
    val statusBarStyle = when {
        // inverse of dark mode when is unread
        article?.isTransientUnread == true -> SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { resources ->
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                    Configuration.UI_MODE_NIGHT_NO
        }
        else -> SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT)
    }
    DisposableEffect(activity, statusBarStyle) {
        activity.enableEdgeToEdge(statusBarStyle = statusBarStyle)
        onDispose {
            activity.enableEdgeToEdge()
        }
    }

    val context = LocalContext.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    Scaffold(
        modifier  = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            ArticleTopActionsBar(
                scrollBehavior = scrollBehavior,
                title = { Text(article?.title ?: "") },
                isUnread = article?.isTransientUnread == true,
                isStarred = article?.isStarred ?: false,
                onNavigateUpClick = onNavigateUpClick,
                onToggleUnreadClick = { articleDetailsViewModel.toggleArticleRead() },
                onStarredChange = { articleDetailsViewModel.onStarChanged(it) },
                onShareClick = { articleDetailsViewModel.shareArticle(context) }
            )
        },
        floatingActionButton = {
            OpenInBrowserExtendedFab(
                browserIcon,
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
    webViewClient: AccompanistWebViewClient,
    scrollState: ScrollState = rememberScrollState(),
) {
    ArticleDetailsHeroContent(
        article,
        readMoreArticles,
        onArticleClick,
        modifier = modifier,
        scrollState = scrollState,
    ) {
        val colorScheme = MaterialTheme.colorScheme
        val baseUrl = article.link.toUri().let { "${it.scheme}://${it.host}/" }
        val content = remember(colorScheme, article) {
            val cssOverride = createCssOverride(colorScheme)
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
                HorizontalDivider(Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp))
                ReadMoreSection(articles = readMoreArticles,
                    onArticleClick = onArticleClick)
            }
        }
    }
}



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailsScreen(
    articleDetailsViewModel: ArticleDetailsViewModel,
    onNavigateUpClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    webViewClient: AccompanistWebViewClient,
) {
    val articleDetailsScreenState = rememberArticleDetailsScreenState()
    val article by articleDetailsViewModel.article.observeAsState()
    val browserIcon by articleDetailsViewModel.browserIcon.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val bottomAppBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(bottomAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            ArticleTopAppBar(scrollBehavior = scrollBehavior, onNavigateUpClick = onNavigateUpClick)
        },
        bottomBar = {
            ArticleBottomAppBar(isUnread = article?.isTransientUnread == true,
                isStarred = article?.isStarred == true,
                floatingActionButton = null,
                scrollBehavior = bottomAppBarScrollBehavior,
                onToggleUnreadClick = { articleDetailsViewModel.toggleArticleRead() },
                onStarredChange = { articleDetailsViewModel.onStarChanged(it) },
                onShareClick = { articleDetailsViewModel.shareArticle(context) } )
        },
         floatingActionButton = {
             FloatingActionButton(onClick = {
                 article?.let { articleDetailsViewModel.openArticleInBrowser(context, it) }
             }) {
                 OpenInBrowserIcon(browserIcon, contentDescription = stringResource(R.string.open_article_in_browser))
             }
         },
        floatingActionButtonPosition = FabPosition.EndOverlay
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
private fun OpenInBrowserExtendedFab(
    browserIcon: Drawable?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val text = stringResource(R.string.open_article_in_browser)
    ExtendedFloatingActionButton(
        text = { Text(text) },
        icon = { OpenInBrowserIcon(browserIcon, contentDescription = text) },
        modifier = modifier,
        onClick = onClick)
}

@Composable
private fun ArticleDetailsContent(
    article: Article,
    readMoreArticles: List<ArticleWithTag>,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier,
    webViewClient: AccompanistWebViewClient,
    scrollState: ScrollState = rememberScrollState()
) {
    ArticleDetailsContent(article, readMoreArticles, onArticleClick , modifier, scrollState) {
        val baseUrl = article.link.toUri().let { "${it.scheme}://${it.host}/" }
        val colorScheme = MaterialTheme.colorScheme
        val content = remember(colorScheme, article) {
            val cssOverride = createCssOverride(colorScheme)
            prepareArticleContent(article.content, cssOverride)
        }
        val verticalPadding = if (article.content.isNotBlank()) 16.dp else 0.dp
        ArticleContentWebView(baseUrl = baseUrl, content = content,
            webViewClient = webViewClient,
            modifier = Modifier
                .widthIn(max = 450.dp)
                .wrapContentHeight()
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
        HorizontalDivider(Modifier
            .fillMaxWidth())
        Box(Modifier.fillMaxWidth()) {
            articleContent()
        }

        if (readMoreArticles.isNotEmpty()) {
            HorizontalDivider(Modifier
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
        Text(title, style = MaterialTheme.typography.headlineLarge, color = MaterialTheme.colorScheme.primary)
        Text(date,
            style = MaterialTheme.typography.bodySmall,
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
                style = MaterialTheme.typography.displaySmall,
                color = MaterialTheme.colorScheme.primary,
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
                style = MaterialTheme.typography.titleLarge,
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


private fun createCssOverride(colorScheme: ColorScheme): String {
    val backgroundHexColor = colorScheme.surface.toRgbaCall()
    val textColor = colorScheme.onSurface.toRgbaCall()
    val linkHexColor = colorScheme.primary.toRgbaCall()
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

private fun androidx.compose.ui.graphics.Color.toRgbaCall(): String {
    val argb = toArgb()
    return "rgba(%d, %d, %d, %.2f)".format(Locale.ENGLISH,
        Color.red(argb), Color.green(argb), Color.blue(argb), Color.alpha(argb) / 255f)
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


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReadMoreSection(
    articles: List<ArticleWithTag>,
    onArticleClick: (Article) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier) {
        Text(stringResource(R.string.lbl_read_more),
            style = MaterialTheme.typography.titleLarge,
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
                        Text(article.title, style = MaterialTheme.typography.titleMedium,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.heightIn(max = 56.dp)
                        )
                        Text("#$tag", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}


@Preview
@Composable
fun PreviewReadMoreSection() {
    AppTheme3 {
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
