/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2025 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.share

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.os.Build
import android.service.chooser.ChooserAction
import androidx.core.app.ShareCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.net.toUri
import com.geekorum.ttrss.R
import com.geekorum.ttrss.data.Article
import com.geekorum.ttrss.publish_article.ShareToPublishArticleActivity

fun createShareArticleIntent(context: Context, article: Article): Intent {
    val shareIntent = ShareCompat.IntentBuilder(context)
    shareIntent.setSubject(article.title)
        .setHtmlText(article.content)
        .setText(article.link)
        .setType("text/plain")
    val chooserIntent = shareIntent.createChooserIntent().apply {
        val shareToPublishActivityIntent = Intent(context, ShareToPublishArticleActivity::class.java).apply {
            data = context.getString(R.string.article_details_data_pattern)
                .replace("{article_id}", article.id.toString())
                .toUri()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            val chooserAction = ChooserAction.Builder(
                IconCompat.createWithResource(context, R.drawable.ic_rss_box).toIcon(context),
                context.getString(R.string.activity_share_to_publish_article_title),
                PendingIntent.getActivity(
                    context,
                    0,
                    shareToPublishActivityIntent,
                    PendingIntent.FLAG_IMMUTABLE
                )
            ).build()
            putExtra(Intent.EXTRA_CHOOSER_CUSTOM_ACTIONS, arrayOf(chooserAction))
        } else {
            val initialIntents = arrayOf(
                LabeledIntent(shareToPublishActivityIntent, context.packageName,  R.string.activity_share_to_publish_article_title, R.drawable.ic_rss_box)
            )
            putExtra(
                Intent.EXTRA_INITIAL_INTENTS, initialIntents)
        }
    }

    return chooserIntent
}
