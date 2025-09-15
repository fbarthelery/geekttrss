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
package com.geekorum.ttrss.ui.components

import android.graphics.drawable.AdaptiveIconDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.geekorum.ttrss.ui.AppTheme3.Icons
import com.google.accompanist.drawablepainter.rememberDrawablePainter

@Composable
fun OpenInBrowserIcon(browserApplicationIcon: Drawable?,
                      contentDescription: String,
                      fallbackIcon: ImageVector = Icons.OpenInBrowser
) {
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                (browserApplicationIcon as? AdaptiveIconDrawable)?.monochrome != null -> {
            val monochromePainter =
                rememberDrawablePainter(browserApplicationIcon.monochrome!!)
            Icon(
                monochromePainter, contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
                    .scale(2f)

            )
        }

        browserApplicationIcon != null -> {
            Image(
                painter = rememberDrawablePainter(browserApplicationIcon),
                contentDescription = contentDescription,
                modifier = Modifier.size(24.dp)
            )
        }

        else -> {
            Icon(fallbackIcon, contentDescription = contentDescription)
        }
    }
}
