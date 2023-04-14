/*
 * Geekttrss is a RSS feed reader application on the Android Platform.
 *
 * Copyright (C) 2017-2023 by Frederic-Charles Barthelery.
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
package com.geekorum.ttrss.settings.licenses

import android.content.Context
import android.net.Uri
import androidx.core.net.toUri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geekorum.aboutoss.core.LicenseInfoRepository
import com.geekorum.ttrss.network.TtRssBrowserLauncher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class OpenSourceLicensesViewModel @Inject constructor(
    private val licenseInfoRepository: LicenseInfoRepository,
    private val browserLauncher: TtRssBrowserLauncher,
) : ViewModel() {
    init {
        browserLauncher.warmUp()
    }

    private val licensesInfo = flow {
        emit(licenseInfoRepository.getLicensesInfo())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val dependenciesList = licensesInfo.map { licensesInfo ->
        licensesInfo.keys.sortedBy { it.lowercase() }
    }

    fun getLicenseDependency(dependency: String) = flow {
        emit(licenseInfoRepository.getLicenseFor(dependency))
    }

    fun openLinkInBrowser(context: Context, link: String) {
        browserLauncher.launchUrl(context, link.toUri())
    }

    fun mayLaunchUrl(vararg uris: Uri) = browserLauncher.mayLaunchUrl(*uris)

    override fun onCleared() {
        browserLauncher.shutdown()
    }
}
