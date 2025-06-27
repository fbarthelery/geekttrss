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
import androidx.activity.compose.LocalActivity
import androidx.activity.enableEdgeToEdge
import androidx.annotation.ColorRes
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.BottomAppBarDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.data.ArticleContentIndexed
import com.geekorum.ttrss.ui.AppTheme3
import com.geekorum.ttrss.ui.components.OpenInBrowserIcon
import com.geekorum.ttrss.ui.components.web.AccompanistWebViewClient
import dagger.hilt.android.EntryPointAccessors
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
    webViewClient: AccompanistWebViewClient
) {
    val articleDetailsScreenState = rememberArticleDetailsScreenState()

    val article by articleDetailsViewModel.article.collectAsStateWithLifecycle()
    val browserIcon by articleDetailsViewModel.browserIcon.collectAsStateWithLifecycle()
    val activity = LocalActivity.current as ComponentActivity
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
    val article by articleDetailsViewModel.article.collectAsStateWithLifecycle()
    val browserIcon by articleDetailsViewModel.browserIcon.collectAsStateWithLifecycle()

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val bottomAppBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()
    val context = LocalContext.current
    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection)
            .nestedScroll(bottomAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            ArticleFloatingTopAppBar(scrollBehavior = scrollBehavior, onNavigateUpClick = onNavigateUpClick)
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
                contentPadding = padding,
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
    scrollState: ScrollState = rememberScrollState(),
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    ArticleDetailsContent(article, readMoreArticles, onArticleClick , modifier, scrollState, contentPadding) {
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
    contentPadding: PaddingValues = PaddingValues(0.dp),
    articleContent: @Composable () -> Unit) {
    Column(modifier
        .padding(horizontal = 16.dp)
        .verticalScroll(scrollState)
    ) {
        Spacer(Modifier.height(contentPadding.calculateTopPadding()))

        ArticleHeaderWithoutImage(
            title = article.title,
            date = article.getDateString()
        )
        HorizontalDivider(Modifier
            .fillMaxWidth())
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
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

        Spacer(Modifier.height(contentPadding.calculateBottomPadding()))
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
        if (this.maxWidth < 600.dp) {
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
    AppTheme3 {
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
    AppTheme3 {
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


@Composable
fun ArticleDetailsPane(
    articleId: Long,
    isSinglePane: Boolean,
    onNavigateUpClick: () -> Unit,
    modifier: Modifier = Modifier,
    articleDetailsViewModel: ArticleDetailsViewModel = hiltViewModel(
        creationCallback = {factory: ArticleDetailsViewModel.Factory ->
            factory.create(articleId)
        }
    ),
    dualPanePadding: PaddingValues = PaddingValues(0.dp)
) {
    ArticleDetailsPaneLayout(
        isSinglePane = isSinglePane,
        dualPanePadding = dualPanePadding,
        modifier = modifier
    ) {
        val activity = LocalActivity.current!!
        val webViewClientFactory =  remember(activity) {
            EntryPointAccessors.fromActivity<ArticleDetailsEntryPoint>(activity)
                .articleDetailsWebViewClientFactory
        }
        val webViewClient = remember(articleDetailsViewModel) {
            webViewClientFactory.create(openUrlInBrowser = articleDetailsViewModel::openUrlInBrowser,
                onPageFinishedCallback = { _, _ ->})
        }

        MaterialTheme(colorScheme = MaterialTheme.colorScheme.copy(
            surface = if (isSinglePane) MaterialTheme.colorScheme.surface else
                MaterialTheme.colorScheme.surfaceContainerHigh
        )) {
            if (isSinglePane) {
                ArticleDetailsForSinglePane(
                    articleDetailsViewModel = articleDetailsViewModel,
                    onNavigateUpClick = onNavigateUpClick,
                    onArticleClick = {},
                    webViewClient = webViewClient
                )
            } else {
                ArticleDetailsForDualPanes(
                    articleDetailsViewModel = articleDetailsViewModel,
                    onArticleClick = {},
                    webViewClient = webViewClient
                )
            }
        }
    }
}

@Composable
fun ArticleDetailsPaneLayout(
    isSinglePane: Boolean,
    modifier: Modifier = Modifier,
    dualPanePadding: PaddingValues = PaddingValues(0.dp),
    content: @Composable () -> Unit
) {
    val cardShape = if (isSinglePane) RectangleShape else MaterialTheme.shapes.large
    val cardColors = if (isSinglePane) CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    ) else CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        contentColor = MaterialTheme.colorScheme.onSurface,
    )
    val paddingModifier = if (!isSinglePane) Modifier
        .padding(dualPanePadding)
        .consumeWindowInsets(dualPanePadding)
        .safeContentPadding()
    else Modifier
    Card(
        shape = cardShape,
        colors = cardColors,
        modifier = modifier.then(paddingModifier)
    ) {
       content()
    }
}

@Composable
fun ArticleDetailsForSinglePane(
    articleDetailsViewModel: ArticleDetailsViewModel,
    onNavigateUpClick: () -> Unit,
    onArticleClick: (Article) -> Unit,
    webViewClient: AccompanistWebViewClient
) {
    val article by articleDetailsViewModel.article.collectAsStateWithLifecycle()
    val readMoreArticles by articleDetailsViewModel.additionalArticles.collectAsState()
    val browserIcon by articleDetailsViewModel.browserIcon.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val articleDetailsScreenState = rememberArticleDetailsScreenState()

    ArticleDetailsForSinglePane(
        article = article,
        browserIcon = browserIcon,
        onNavigateUpClick = onNavigateUpClick,
        onEndOfArticleReached = { articleDetailsViewModel.setArticleUnread(false) },
        onToggleUnreadClick = { articleDetailsViewModel.toggleArticleRead() },
        onStarredChange = { articleDetailsViewModel.onStarChanged(it) },
        onShareClick = { articleDetailsViewModel.shareArticle(context) },
        onOpenInBrowserClick = {
            article?.let { articleDetailsViewModel.openArticleInBrowser(context, it) }
        },
        articleDetailsScreenState = articleDetailsScreenState,
    ) { article, contentPadding ->
        ArticleDetailsContent(article,
            readMoreArticles = readMoreArticles,
            onArticleClick = onArticleClick,
            webViewClient = webViewClient,
            contentPadding = contentPadding,
            scrollState = articleDetailsScreenState.scrollState)

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ArticleDetailsForSinglePane(
    article: Article?,
    browserIcon: Drawable?,
    onNavigateUpClick: () -> Unit,
    onEndOfArticleReached: () -> Unit,
    onToggleUnreadClick: () -> Unit,
    onStarredChange: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    modifier: Modifier = Modifier,
    articleDetailsScreenState: ArticleDetailsScreenState = rememberArticleDetailsScreenState(),
    articleContent: @Composable (article: Article, contentPadding: PaddingValues) -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val bottomAppBarScrollBehavior = BottomAppBarDefaults.exitAlwaysScrollBehavior()

    Scaffold(
        modifier = modifier
            .nestedScroll(scrollBehavior.nestedScrollConnection)
//         TODO cause issues in pane or in articles list activity
            .nestedScroll(bottomAppBarScrollBehavior.nestedScrollConnection),
        topBar = {
            ArticleFloatingTopAppBar(scrollBehavior = scrollBehavior,
                onNavigateUpClick = onNavigateUpClick)
        },
        bottomBar = {
            ArticleBottomAppBar(isUnread = article?.isTransientUnread == true,
                isStarred = article?.isStarred == true,
                floatingActionButton = null,
                scrollBehavior = bottomAppBarScrollBehavior,
                onToggleUnreadClick = onToggleUnreadClick,
                onStarredChange = onStarredChange,
                onShareClick = onShareClick )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onOpenInBrowserClick) {
                OpenInBrowserIcon(browserIcon, contentDescription = stringResource(R.string.open_article_in_browser))
            }
        },
        floatingActionButtonPosition = FabPosition.EndOverlay
    ) { contentPadding ->
        article?.let { article ->
            articleContent(article, contentPadding)

            LaunchedEffect(articleDetailsScreenState.isAtEndOfArticle) {
                if (articleDetailsScreenState.isAtEndOfArticle) {
                    if (articleDetailsScreenState.scrollState.value == 0) {
                        delay(2000)
                    }
                    onEndOfArticleReached()
                }
            }
        }
    }
}



@Composable
fun ArticleDetailsForDualPanes(
    articleDetailsViewModel: ArticleDetailsViewModel,
    onArticleClick: (Article) -> Unit,
    webViewClient: AccompanistWebViewClient
) {
    val articleDetailsScreenState = rememberArticleDetailsScreenState()

    val article by articleDetailsViewModel.article.collectAsStateWithLifecycle()
    val readMoreArticles by articleDetailsViewModel.additionalArticles.collectAsState()
    val browserIcon by articleDetailsViewModel.browserIcon.collectAsStateWithLifecycle()

    val context = LocalContext.current

    ArticleDetailsForDualPanes(
        article = article,
        browserIcon = browserIcon,
        onEndOfArticleReached = {
            articleDetailsViewModel.setArticleUnread(false)
        },
        onToggleUnreadClick = { articleDetailsViewModel.toggleArticleRead() },
        onStarredChange = { articleDetailsViewModel.onStarChanged(it) },
        onShareClick = { articleDetailsViewModel.shareArticle(context) },
        onOpenInBrowserClick = {
            article?.let { articleDetailsViewModel.openArticleInBrowser(context, it) }
        },
        articleDetailsScreenState = articleDetailsScreenState,
    ) { article ->
        ArticleDetailsHeroContent(
            article,
            readMoreArticles = readMoreArticles,
            onArticleClick = onArticleClick,
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(end = 56.dp),
            scrollState = articleDetailsScreenState.scrollState,
            webViewClient = webViewClient
        )
    }
}

@Composable
fun ArticleDetailsForDualPanes(
    article: Article?,
    browserIcon: Drawable?,
    onEndOfArticleReached: () -> Unit,
    onToggleUnreadClick: () -> Unit,
    onStarredChange: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onOpenInBrowserClick: () -> Unit,
    modifier: Modifier = Modifier,
    articleDetailsScreenState: ArticleDetailsScreenState = rememberArticleDetailsScreenState(),
    articleContent: @Composable (article: Article) -> Unit
) {
    Box(modifier.fillMaxSize()) {
        article?.let {
            articleContent(article)

            LaunchedEffect(articleDetailsScreenState.isAtEndOfArticle) {
                if (articleDetailsScreenState.isAtEndOfArticle) {
                    if (articleDetailsScreenState.scrollState.value == 0) {
                        delay(2000)
                    }
                    onEndOfArticleReached()
                }
            }
        }

        FloatingActionsBar(
            browserApplicationIcon = browserIcon,
            isUnread = article?.isTransientUnread == true,
            isStarred = article?.isStarred ?: false,
            onToggleUnreadClick = onToggleUnreadClick,
            onStarredChange = onStarredChange,
            onShareClick = onShareClick,
            onOpenInBrowserClick = onOpenInBrowserClick,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(16.dp)
                .padding(top = 24.dp)
//                .offset(x = 32.dp)
        )
    }
}


@Preview
@Composable
private fun PreviewArticleDetailForSinglePane() {
    AppTheme3 {
        val article = Article(contentData = ArticleContentIndexed(
            title = "My simple but hilariously and excessively long headline",
            content = "<b>Hello world</b>"
        ), isTransientUnread = true)
        ArticleDetailsPaneLayout(
            isSinglePane = true
        ) {
            ArticleDetailsForSinglePane(
                article = article,
                onNavigateUpClick = {},
                onOpenInBrowserClick = {},
                onEndOfArticleReached = {},
                onStarredChange = {},
                onShareClick = {},
                onToggleUnreadClick = {},
                browserIcon = null,
            ) { article, contentPadding ->
                ArticleDetailsContent(article = article,
                    readMoreArticles = emptyList(),
                    contentPadding = contentPadding,
                    onArticleClick = {}) {
                    Box(
                        Modifier
                            .size(width = 400.dp, height = 900.dp)
                            .background(androidx.compose.ui.graphics.Color.Gray))
                }
            }
        }
    }
}

@Preview(device = "spec:id=reference_tablet,shape=Normal,width=1280,height=800,unit=dp,dpi=240")
@Composable
private fun PreviewArticleDetailsForDualPanes() {
    AppTheme3 {
        Row(Modifier.background(MaterialTheme.colorScheme.surface)) {
            Spacer(Modifier.width(600.dp))

            ArticleDetailsPaneLayout(
                isSinglePane = false,
                dualPanePadding = PaddingValues(16.dp)
            ) {
                val article = Article(contentData = ArticleContentIndexed(
                    title = "My simple but hilariously and excessively long headline",
                    content = "<b>Hello world</b>",
                ), isTransientUnread = true)

                ArticleDetailsForDualPanes(
                    article = article,
                    onOpenInBrowserClick = {},
                    onEndOfArticleReached = {},
                    onStarredChange = {},
                    onShareClick = {},
                    onToggleUnreadClick = {},
                    browserIcon = null,
                ) {
                    ArticleDetailsHeroContent(
                        article,
                        readMoreArticles = emptyList(),
                        onArticleClick = {},
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .padding(end = 56.dp),
                    ) {
                        Box(
                            Modifier
                                .fillMaxWidth()
                                .height(height = 900.dp)
                                .background(androidx.compose.ui.graphics.Color.Gray))
                    }
                }
            }
        }
    }
}
