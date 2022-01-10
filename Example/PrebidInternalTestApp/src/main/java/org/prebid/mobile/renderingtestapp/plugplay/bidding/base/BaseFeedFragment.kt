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