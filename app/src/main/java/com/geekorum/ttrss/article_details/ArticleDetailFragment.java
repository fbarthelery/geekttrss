/**
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2018 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.article_details;

import android.content.ContentUris;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import com.geekorum.geekdroid.network.OkHttpWebViewClient;
import com.geekorum.ttrss.R;
import com.geekorum.ttrss.articles_list.ArticleListActivity;
import com.geekorum.ttrss.data.Article;
import com.geekorum.ttrss.databinding.FragmentArticleDetailBinding;
import com.geekorum.ttrss.di.ViewModelsFactory;
import dagger.android.support.AndroidSupportInjection;
import okhttp3.OkHttpClient;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Locale;

import javax.inject.Inject;

/**
 * A fragment representing a single Article detail screen.
 * This fragment is either contained in a {@link ArticleListActivity}
 * in two-pane mode (on tablets) or a {@link com.geekorum.ttrss.article_details.ArticleDetailActivity}
 * on handsets.
 */
public class ArticleDetailFragment extends Fragment {
    public static final String ARG_ARTICLE_URI = "article_uri";
    public static final String TAG = ArticleDetailFragment.class.getSimpleName();

    private FragmentArticleDetailBinding binding;
    private Article article;
    private Uri articleUri;
    private View customView;
    private FSVideoChromeClient chromeClient;
    private ArticleDetailsViewModel articleDetailsViewModel;
    @Inject
    ViewModelsFactory viewModelFactory;
    @Inject
    OkHttpClient okHttpClient;

    public static ArticleDetailFragment newInstance(Uri articleUri) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_ARTICLE_URI, articleUri);
        ArticleDetailFragment fragment = new ArticleDetailFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        AndroidSupportInjection.inject(this);
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments().containsKey(ARG_ARTICLE_URI)) {
            articleUri = getArguments().getParcelable(ARG_ARTICLE_URI);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentArticleDetailBinding.inflate(inflater, container, false);
        binding.articleContent.setWebViewClient(new MyWebViewClient());
        binding.setLifecycleOwner(this);
        configureWebview();
        return binding.getRoot();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        articleDetailsViewModel = ViewModelProviders.of(requireActivity(), viewModelFactory).get(ArticleDetailsViewModel.class);
        articleDetailsViewModel.init(ContentUris.parseId(articleUri));
        articleDetailsViewModel.getArticle().observe(this, article -> {
            this.article = article;
        });
        articleDetailsViewModel.getArticleContent().observe(this, this::renderContent);
        binding.setViewModel(articleDetailsViewModel);
    }

    private void renderContent(String articleContent) {
      /*  if (m_prefs.getBoolean("justify_article_text", true)) {
            cssOverride += "body { text-align : justify; } ";
        }

        ws.setDefaultFontSize(m_articleFontSize);
*/

        String cssOverride = getCssOverride();
        StringBuilder content = new StringBuilder("<html>" +
                "<head>" +
                "<meta content=\"text/html; charset=utf-8\" http-equiv=\"content-type\">" +
                "<meta name=\"viewport\" content=\"width=device-width, user-scalable=no\" />" +
                "<style type=\"text/css\">" +
                "body { padding : 0px; margin : 0px; line-height : 130%; }" +
                "img, video, iframe { max-width : 100%; width : auto; height : auto; }" +
                " table { width : 100%; }" +
                cssOverride +
                "</style>" +
                "</head>" +
                "<body>");

        content.append(articleContent);

       /* TODO maybe reimplement this one day
       if (article.attachments != null && article.attachments.size() != 0) {
            String flatContent = articleContent.replaceAll("[\r\n]", "");
            boolean hasImages = flatContent.matches(".*?<img[^>+].*?");

            for (Attachment a : article.attachments) {
                if (a.content_type != null && a.content_url != null) {
                    try {
                        if (a.content_type.contains("image") &&
                                (!hasImages || article.always_display_attachments)) {

                            URL url = new URL(a.content_url.trim());
                            String strUrl = url.toString().trim();

                            content.append("<p><img src=\"" + strUrl.replace("\"", "\\\"") + "\"></p>");
                        }

                    } catch (MalformedURLException e) {
                        //
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }*/

        content.append("</body></html>");

        try {
            String baseUrl = null;

            try {
                URL url = new URL(article.getLink());
                baseUrl = url.getProtocol() + "://" + url.getHost();
            } catch (MalformedURLException e) {
                //
            }

            binding.articleContent.loadDataWithBaseURL(baseUrl, content.toString(), "text/html", "utf-8", null);

        } catch (RuntimeException e) {
            e.printStackTrace();
        }

    }

    private void configureWebview() {
        WebSettings ws = binding.articleContent.getSettings();
        ws.setSupportZoom(false);
        ws.setJavaScriptEnabled(true);
        ws.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        ws.setMediaPlaybackRequiresUserGesture(false);
        chromeClient = new FSVideoChromeClient();
        binding.articleContent.setWebChromeClient(chromeClient);
    }

    private String getCssOverride() {
        Resources.Theme theme = requireActivity().getTheme();
        WebviewColors colors = WebviewColors.fromTheme(theme);
        String backgroundHexColor = convertToRgbaCall(colors.backgroundColor);
        String textColor = convertToRgbaCall(colors.textColor);
        String linkHexColor = convertToRgbaCall(colors.linkColor);
        String cssOverride = "body { background : " + backgroundHexColor + "; "
                + "color : " + textColor + "; }";
        cssOverride += " a:link {color: " + linkHexColor + ";} a:visited { color: " + linkHexColor + ";}";
        return cssOverride;
    }

    private static class WebviewColors {
        @ColorInt
        final int backgroundColor;

        @ColorInt
        final int textColor;

        @ColorInt
        final int linkColor;

        private WebviewColors(int backgroundColor, int textColor, int linkColor) {
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            this.linkColor = linkColor;
        }

        private static WebviewColors fromTheme(Resources.Theme theme) {
            TypedValue typedValue = new TypedValue();
            theme.resolveAttribute(R.attr.articleBackground, typedValue, true);
            int backgroundColor = typedValue.data;
            theme.resolveAttribute(R.attr.articleTextColor, typedValue, true);
            int textColor = typedValue.data;
            theme.resolveAttribute(R.attr.linkColor, typedValue, true);
            int linkColor = typedValue.data;
            return new WebviewColors(backgroundColor, textColor, linkColor);
        }
    }

    private String convertToRgbaCall(int color) {
        return String.format(Locale.ENGLISH, "rgba(%d, %d, %d, %.2f)",
                Color.red(color), Color.green(color), Color.blue(color), Color.alpha(color) / 255f);
    }

    @Override
    public void onPause() {
        super.onPause();
        binding.articleContent.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.articleContent.onResume();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        binding.articleContent.saveState(outState);
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        binding.articleContent.restoreState(savedInstanceState);
    }

    private boolean inCustomView() {
        return (customView != null);
    }

    @Override
    public void onStop() {
        super.onStop();

        if (inCustomView()) {
            hideCustomView();
        }
    }

    private void hideCustomView() {
        if (chromeClient != null) {
            chromeClient.onHideCustomView();
        }
    }

    private class FSVideoChromeClient extends WebChromeClient {
        //protected View m_videoChildView;

        private CustomViewCallback m_callback;

        @Override
        public void onShowCustomView(View view, CustomViewCallback callback) {
            AppCompatActivity m_activity = (AppCompatActivity) getActivity();
            m_activity.getSupportActionBar().hide();

            // if a view already exists then immediately terminate the new one
            if (customView != null) {
                callback.onCustomViewHidden();
                return;
            }
            customView = view;

            binding.articleFullscreenVideo.setVisibility(View.VISIBLE);
            binding.articleFullscreenVideo.addView(view);

            View fab = requireActivity().findViewById(R.id.fab);
            if (fab != null) {
                fab.setVisibility(View.GONE);
            }

            //            m_activity.showSidebar(false);

            m_callback = callback;
        }

        @Override
        public void onHideCustomView() {
            super.onHideCustomView();
            AppCompatActivity m_activity = (AppCompatActivity) getActivity();

            m_activity.getSupportActionBar().show();

            if (customView == null) {
                return;
            }

            binding.articleFullscreenVideo.setVisibility(View.GONE);

            // Remove the custom view from its container.
            binding.articleFullscreenVideo.removeView(customView);
            m_callback.onCustomViewHidden();

            View fab = requireActivity().findViewById(R.id.fab);
            if (fab != null) {
                fab.setVisibility(View.VISIBLE);
            }

            customView = null;

            //            m_activity.showSidebar(true);
        }
    }

    private class MyWebViewClient extends OkHttpWebViewClient {
        MyWebViewClient() {
            super(okHttpClient);
        }

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
            articleDetailsViewModel.openUrlInBrowser(view.getContext(), request.getUrl());
            return true;
        }
    }
}
