package com.openx.internal_test_app.plugplay.utilities

import android.os.Bundle
import android.view.View
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.openx.internal_test_app.R
import com.openx.internal_test_app.data.SimpleListItem
import com.openx.internal_test_app.utils.BaseFragment
import kotlinx.android.synthetic.main.fragment_utilities_list.*

class UtilitiesListFragment : BaseFragment() {
    override val layoutRes = R.layout.fragment_utilities_list

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        initRecyclerView()
    }

    private fun initRecyclerView() {
        val utilitiesListAdapter = UtilitiesListAdapter { findNavController().navigate(it) }
        utilitiesListAdapter.setUtilitiesList(createUtilitiesList())

        rvUtilities.layoutManager = LinearLayoutManager(context)
        rvUtilities.adapter = utilitiesListAdapter
    }

    private fun createUtilitiesList(): List<SimpleListItem> {
        val utilitiesList = mutableListOf<SimpleListItem>()
        utilitiesList.add(SimpleListItem("IAB Consent Settings", R.id.action_utilitiesListFragment_to_consentSettingsFragment))
        utilitiesList.add(SimpleListItem("App settings", R.id.action_utilitiesListFragment_to_appSettingsFragment))
        utilitiesList.add(SimpleListItem("Versions", R.id.action_utilitiesListFragment_to_versionInfoFragment))
        return utilitiesList
    }
}