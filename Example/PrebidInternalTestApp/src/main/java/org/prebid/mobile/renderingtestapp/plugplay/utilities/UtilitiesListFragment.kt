/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.renderingtestapp.plugplay.utilities

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.data.SimpleListItem
import org.prebid.mobile.renderingtestapp.databinding.FragmentUtilitiesListBinding
import org.prebid.mobile.renderingtestapp.utils.BaseFragment

class UtilitiesListFragment : BaseFragment() {

    override val layoutRes = R.layout.fragment_utilities_list
    private val binding: FragmentUtilitiesListBinding
        get() = getBinding()

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val utilitiesListAdapter = UtilitiesListAdapter { findNavController().navigate(it) }
        utilitiesListAdapter.setUtilitiesList(createUtilitiesList())

        val recyclerView = binding.rvUtilities
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = utilitiesListAdapter
    }

    private fun createUtilitiesList(): List<SimpleListItem> {
        val utilitiesList = mutableListOf<SimpleListItem>()
        utilitiesList.add(SimpleListItem("IAB Consent Settings", R.id.action_utilitiesListFragment_to_consentSettingsFragment))
        utilitiesList.add(SimpleListItem("App settings", R.id.action_utilitiesListFragment_to_appSettingsFragment))
        utilitiesList.add(SimpleListItem("Versions", R.id.action_utilitiesListFragment_to_versionInfoFragment))
        return utilitiesList
    }
}