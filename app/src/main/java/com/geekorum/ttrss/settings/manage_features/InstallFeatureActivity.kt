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
package com.geekorum.ttrss.settings.manage_features

import android.app.Activity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.geekorum.ttrss.core.BaseActivity
import com.geekorum.ttrss.R
import com.geekorum.ttrss.databinding.ActivityInstallFeatureBinding
import com.geekorum.ttrss.on_demand_modules.InstallModuleViewModel
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.CANCELED
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.FAILED
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.INSTALLED
import com.geekorum.ttrss.on_demand_modules.InstallSession.State.Status.REQUIRES_USER_CONFIRMATION
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Quick and dirty InstallFeatureActivity
 */
class InstallFeatureActivity : BaseActivity() {

    companion object {
        const val EXTRA_FEATURES_LIST = "features"
        const val CODE_REQUEST_USER_CONFIRMATION = 1
    }

    lateinit var binding: ActivityInstallFeatureBinding

    val viewModel: InstallModuleViewModel by viewModels()

    private var animationIsRunning = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_install_feature)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel

        viewModel.sessionState.observe(this) {
            when (it.status) {
                INSTALLED -> {
                    setResult(Activity.RESULT_OK)
                    lifecycleScope.launch {
                        delay(1000)
                        finish()
                    }
                }
                FAILED,
                CANCELED -> stopAnimation()
                REQUIRES_USER_CONFIRMATION -> {
                    viewModel.startUserConfirmationDialog(this@InstallFeatureActivity,
                        CODE_REQUEST_USER_CONFIRMATION)
                }
                else -> startAnimation()
            }
        }
        val features = intent.getStringArrayExtra(EXTRA_FEATURES_LIST) ?: emptyArray()
        viewModel.startInstallModules(*features)
    }

    private fun startAnimation() {
        if (animationIsRunning) {
            return
        }
        binding.moduleLogo.animate()
            .rotationBy(360f)
            .withEndAction {
                animationIsRunning = false
                startAnimation()
            }
            .setInterpolator(null)
            .setDuration(2000L)
            .start()
        animationIsRunning = true
    }

    private fun stopAnimation() {
        binding.moduleLogo.animate().cancel()
        animationIsRunning = false
    }

    override fun onPause() {
        super.onPause()
        stopAnimation()
    }
}


