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
package com.geekorum.ttrss.settings.manage_modules

import android.os.Bundle
import androidx.databinding.DataBindingUtil
import com.geekorum.ttrss.BaseActivity
import com.geekorum.ttrss.R
import com.geekorum.ttrss.databinding.ActivityInstallFeatureBinding
import com.geekorum.ttrss.features_manager.InstallModuleViewModel
import com.geekorum.ttrss.viewModels

/**
 * Quick and dirty InstallFeatureActivity
 */
class InstallFeatureActivity : BaseActivity() {

    companion object {
        const val EXTRA_FEATURES_LIST = "features"
    }

    lateinit var binding: ActivityInstallFeatureBinding

    val viewModel: InstallModuleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_install_feature)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        val features = intent.getStringArrayExtra(EXTRA_FEATURES_LIST)
        viewModel.startInstallModules(*features)
    }
}


