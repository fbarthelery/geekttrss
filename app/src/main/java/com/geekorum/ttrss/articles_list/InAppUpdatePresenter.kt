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
package com.geekorum.ttrss.articles_list

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.doOnNextLayout
import androidx.core.view.updatePadding
import androidx.lifecycle.observe
import com.geekorum.geekdroid.views.banners.BannerContainer
import com.geekorum.geekdroid.views.banners.BannerSpec
import com.geekorum.geekdroid.views.banners.buildBanner
import com.geekorum.ttrss.R
import com.geekorum.ttrss.in_app_update.InAppUpdateViewModel
import com.google.android.material.bottomsheet.BottomSheetBehavior
import timber.log.Timber

/**
 * Presenter for InAppUpdates.
 * Really tied to ArticleListActivity
 */
class InAppUpdatePresenter(
    private val bannerContainer: BannerContainer,
    private val activity: ArticleListActivity,
    private val inAppUpdateViewModel: InAppUpdateViewModel
) {

    init {
        setUpViewModels()
    }

    private fun setUpViewModels() {
        inAppUpdateViewModel.isUpdateAvailable.observe(activity) {
            if (it) {
                Timber.d("Update available")
                val banner = buildBanner(activity) {
                    messageId = R.string.banner_update_msg
                    icon = IconCompat.createWithResource(activity,
                        R.mipmap.ic_launcher)
                    setPositiveButton(R.string.banner_update_btn) {
                        inAppUpdateViewModel.startUpdateFlow(activity,
                            ArticleListActivity.CODE_START_IN_APP_UPDATE)
                        hideBanner()
                    }
                    setNegativeButton(R.string.banner_dismiss_btn) {
                        hideBanner()
                    }
                }

                showBanner(banner)
            }
        }

        inAppUpdateViewModel.isUpdateReadyToInstall.observe(activity) {
            if (it) {
                Timber.d("Update ready to install")
                val banner = buildBanner(activity) {
                    message = "Update ready to install"
                    icon = IconCompat.createWithResource(activity,
                        R.mipmap.ic_launcher)
                    setPositiveButton("Restart") {
                        hideBanner()
                        inAppUpdateViewModel.completeUpdate()
                    }
                }

                showBanner(banner)
            }
        }
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ArticleListActivity.CODE_START_IN_APP_UPDATE && resultCode != Activity.RESULT_OK) {
            inAppUpdateViewModel.cancelUpdateFlow()
        }
    }

    private fun showBanner(bannerSpec: BannerSpec) {
        bannerContainer.show(bannerSpec)
        val behavior = BottomSheetBehavior.from(bannerContainer)
        behavior.state = BottomSheetBehavior.STATE_EXPANDED

        // wait for expanded state to set non hideable
        behavior.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                // nothing to do
            }

            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    behavior.isHideable = false
                    behavior.removeBottomSheetCallback(this)
                }
            }
        })

        (bannerContainer.parent as? View)?.doOnNextLayout {
            val fragmentContainerView = activity.findViewById<View>(R.id.middle_pane_layout)
            fragmentContainerView.updatePadding(bottom = bannerContainer.height)
        }
    }

    private fun hideBanner() {
        val behavior = BottomSheetBehavior.from(bannerContainer)
        behavior.isHideable = true
        behavior.state = BottomSheetBehavior.STATE_HIDDEN
        val fragmentContainerView = activity.findViewById<View>(R.id.middle_pane_layout)
        fragmentContainerView?.updatePadding(bottom = 0)
    }
}
