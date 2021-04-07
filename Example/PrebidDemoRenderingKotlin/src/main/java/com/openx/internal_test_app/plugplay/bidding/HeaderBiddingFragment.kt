package com.openx.internal_test_app.plugplay.bidding

import android.os.Bundle
import android.view.View
import android.widget.SearchView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.openx.internal_test_app.R
import com.openx.internal_test_app.data.DemoItem
import com.openx.internal_test_app.utils.BaseFragment
import com.openx.internal_test_app.utils.SegmentAdapterReImpl
import com.openx.internal_test_app.utils.adapters.DemoItemClickListener
import com.openx.internal_test_app.utils.adapters.DemoListAdapter
import kotlinx.android.synthetic.main.fragment_main.*
import segmented_control.widget.custom.android.com.segmentedcontrol.SegmentedControl
import segmented_control.widget.custom.android.com.segmentedcontrol.item_row_column.SegmentViewHolder
import segmented_control.widget.custom.android.com.segmentedcontrol.listeners.OnSegmentClickListener


class HeaderBiddingFragment : BaseFragment() {

    override val layoutRes = R.layout.fragment_main

    private lateinit var viewModel: HeaderBiddingViewModel

    private val integrationCategoriesArrayId = R.array.segments_integration
    private val adCategoriesArrayId = R.array.segments_ad_categories

    private var integrationCategoriesControl: SegmentedControl<String>? = null
    private var adCategoriesControl: SegmentedControl<String>? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        initViewModel()
        initMockSwitch()
        initIntegrationsSegmentControl(view)
        initAdCategoriesSegmentControl(view)
        initListView()
        initSearchView()
        initConfigurationToggleButton()
    }

    private fun initViewModel() {
        val viewModelFactory = HeaderBiddingViewModelFactory(resources.getStringArray(integrationCategoriesArrayId),
                resources.getStringArray(adCategoriesArrayId))
        viewModel = ViewModelProviders.of(this, viewModelFactory)[HeaderBiddingViewModel::class.java]
        viewModel.navigateToDemoExample.observe(this, Observer {
            it?.let {
                searchDemos.clearFocus()
                it.bundle?.putString(getString(R.string.key_title), it.label)
                findNavController().navigate(it.action, it.bundle)
                viewModel.onDemoItemNavigated()
            }
        })
    }

    private fun initIntegrationsSegmentControl(rootView: View) {
        val array = resources.getStringArray(integrationCategoriesArrayId)
        integrationCategoriesControl = initSegmentControlBase(rootView, R.id.integrationsSegmentedControl, array)
        val position = viewModel.getIntegrationCategoriesPosition()
        integrationCategoriesControl?.setSelectedSegment(position)
        integrationCategoriesControl?.addOnSegmentClickListener(object : OnSegmentClickListener<String> {
            var currentPosition: Int = position
            override fun onSegmentClick(segmentViewHolder: SegmentViewHolder<String>?) {
                if (segmentViewHolder != null && currentPosition != segmentViewHolder.absolutePosition) {
                    currentPosition = segmentViewHolder.absolutePosition
                    viewModel.onIntegrationControlChanged(currentPosition)
                }
            }
        })
    }

    private fun initAdCategoriesSegmentControl(rootView: View) {
        val array = resources.getStringArray(adCategoriesArrayId)
        adCategoriesControl = initSegmentControlBase(rootView, R.id.adCategoriesSegmentedControl, array)
        val position = viewModel.getAdCategoriesPosition()
        adCategoriesControl?.setSelectedSegment(position)
        adCategoriesControl?.addOnSegmentClickListener(object : OnSegmentClickListener<String> {
            var currentPosition: Int = position
            override fun onSegmentClick(segmentViewHolder: SegmentViewHolder<String>?) {
                if (segmentViewHolder != null && currentPosition != segmentViewHolder.absolutePosition) {
                    currentPosition = segmentViewHolder.absolutePosition
                    viewModel.onAdCategoryControlChanged(currentPosition)
                }
            }
        })
    }

    private fun initSegmentControlBase(rootView: View, viewId: Int, categories: Array<String>): SegmentedControl<String> {
        val segmentedControl = rootView.findViewById<SegmentedControl<String>>(viewId)
        segmentedControl?.setAdapter(SegmentAdapterReImpl())
        segmentedControl?.addSegments(categories)
        segmentedControl?.setColumnCount(categories.size)
        segmentedControl?.notifyConfigIsChanged()

        return segmentedControl
    }

    private fun initListView() {
        listDemos.adapter = DemoListAdapter(object : DemoItemClickListener {
            override fun onClick(item: DemoItem) {
                viewModel.onDemoItemClicked(item)
            }
        })
        viewModel.demoItems.observe(this, Observer {
            if (it != null) {
                (listDemos.adapter as DemoListAdapter).submitList(it)
            }
        })
    }

    private fun initSearchView() {
        searchDemos.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.onSearchTextChanged(newText)
                return true
            }
        })
    }

    private fun initMockSwitch() {
        switchUseMock.isChecked = viewModel.isMockServer()
        switchUseMock.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onMockSwitchStateChanged(isChecked)
        }
    }

    private fun initConfigurationToggleButton() {
        viewModel.configurationState.observe(this) { isChecked ->
            toggleConfigurationButton?.isChecked = isChecked
        }
        toggleConfigurationButton?.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onConfigurationToggleChanged(isChecked)
        }
    }
}