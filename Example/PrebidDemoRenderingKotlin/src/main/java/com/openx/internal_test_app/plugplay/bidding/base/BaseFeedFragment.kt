package com.openx.internal_test_app.plugplay.bidding.base

import android.view.ViewGroup
import android.widget.ListView
import com.openx.internal_test_app.AdFragment
import com.openx.internal_test_app.R
import com.openx.internal_test_app.plugplay.config.AdConfiguratorDialogFragment
import com.openx.internal_test_app.utils.adapters.BaseFeedAdapter

abstract class BaseFeedFragment : AdFragment() {
    override val layoutRes: Int = R.layout.fragment_feed

    private var adapter: BaseFeedAdapter? = null
    private lateinit var listView: ListView

    protected abstract fun initFeedAdapter(): BaseFeedAdapter

    override fun initAd() {
        listView = (view as ViewGroup).getChildAt(0) as ListView
        adapter = initFeedAdapter()
        listView.adapter = adapter
    }

    override fun loadAd() {}

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

    override fun onDestroyView() {
        super.onDestroyView()
        adapter?.destroy()
    }

    protected fun getAdapter() = adapter
}