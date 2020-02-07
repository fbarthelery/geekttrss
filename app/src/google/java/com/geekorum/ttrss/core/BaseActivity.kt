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
package com.geekorum.ttrss.core

import android.annotation.SuppressLint
import android.content.Context
import android.os.StrictMode.allowThreadDiskReads
import com.geekorum.ttrss.debugtools.withStrictMode
import com.google.android.play.core.splitcompat.SplitCompat


/**
 * Common base activity for the application
 * For the Google flavor it's an [InjectableBaseActivity] and
 * allow to load code/resources from on demand modules immediately after install
*/
@SuppressLint("Registered")
open class BaseActivity : InjectableBaseActivity() {

    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(newBase)
        withStrictMode(allowThreadDiskReads()) {
            SplitCompat.installActivity(this)
        }
    }
}
