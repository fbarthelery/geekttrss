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
package com.geekorum.ttrss;

import androidx.databinding.BindingAdapter;
import android.text.TextUtils;
import android.widget.ImageView;
import com.squareup.picasso.Picasso;

/**
 * Various adapters for the Android Data Binding library
 */
public final class BindingAdapters {

    private BindingAdapters() {
        throw new UnsupportedOperationException("You should not instanciate me");
    }

    @BindingAdapter("imageUrl")
    public static void setImageUrl(ImageView imageView, String url) {
        if (TextUtils.isEmpty(url)) {
            url = null;
        }
        //TODO look at HeadlinesFragment to adapt the behavior
        Picasso.with(imageView.getContext())
                .load(url).into(imageView);
    }
}
