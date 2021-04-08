package org.prebid.mobile.renderingtestapp.plugplay.bidding.base

import android.view.ViewGroup
import android.widget.ListView
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.adapters.BaseFeedAdapter

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