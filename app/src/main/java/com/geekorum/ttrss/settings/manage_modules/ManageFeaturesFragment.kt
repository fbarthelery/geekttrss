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

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.observe
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.geekorum.geekdroid.app.lifecycle.EventObserver
import com.geekorum.geekdroid.dagger.DaggerDelegateFragmentFactory
import com.geekorum.geekdroid.dagger.DaggerDelegateViewModelsFactory
import com.geekorum.ttrss.BaseFragment
import com.geekorum.ttrss.databinding.FragmentManageFeaturesBinding
import com.geekorum.ttrss.databinding.ItemFeatureBinding
import com.geekorum.ttrss.viewModels
import javax.inject.Inject

class ManageFeaturesFragment @Inject constructor(
    viewModelsFactory: DaggerDelegateViewModelsFactory,
    fragmentFactory: DaggerDelegateFragmentFactory
) : BaseFragment(viewModelsFactory, fragmentFactory) {

    lateinit var binding: FragmentManageFeaturesBinding
    private val viewModel: ManageFeaturesViewModel by viewModels()
    private lateinit var adapter: FeaturesAdapter


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = FragmentManageFeaturesBinding.inflate(inflater, container, false)
        setupRecyclerView(binding.moduleList)
        viewModel.features.observe(viewLifecycleOwner) {
            adapter.items = it
        }
        viewModel.startInstallModuleEvent.observe(this, EventObserver {
            val intent = Intent(requireContext(), InstallFeatureActivity::class.java).apply {
                putExtra(InstallFeatureActivity.EXTRA_FEATURES_LIST, arrayOf(it))
            }
            startActivity(intent)
        })

        return binding.root
    }

    private fun setupRecyclerView(recyclerView: RecyclerView) {
        adapter = FeaturesAdapter(layoutInflater, viewModel)
        recyclerView.adapter = adapter
        recyclerView.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
    }

}

class FeatureHolder(val binding: ItemFeatureBinding) : RecyclerView.ViewHolder(binding.root) {
    fun setFeature(feature: FeatureStatus) {
        binding.feature = feature
    }

    fun setViewModel(viewModel: ManageFeaturesViewModel) {
        binding.viewModel = viewModel
    }

}

class FeaturesAdapter(
    private val layoutInflater: LayoutInflater,
    private val viewModel: ManageFeaturesViewModel
) : RecyclerView.Adapter<FeatureHolder>() {

    var items = listOf<FeatureStatus>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeatureHolder {
        val binding = ItemFeatureBinding.inflate(layoutInflater, parent, false)
        return FeatureHolder(binding)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: FeatureHolder, position: Int) {
        val feature = items[position]
        holder.setFeature(feature)
        holder.setViewModel(viewModel)
        holder.binding.executePendingBindings()
    }

}
