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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.api.rendering.listeners.BannerViewListener
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

open class PpmBannerFragment : AdFragment(),
    BannerViewListener {
    private val TAG = PpmBannerFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_bidding_banner

    protected var bannerView: BannerView? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        findView<TextView>(R.id.adIdLabel)?.text = getString(R.string.label_auid, configId)
        findView<Button>(R.id.btnLoad)?.setOnClickListener {
            resetEventButtons()
            it.isEnabled = false
            loadAd()
        }

        findView<Button>(R.id.btnStopRefresh)?.setOnClickListener {
            bannerView?.stopRefresh()
            resetEventButtons()
            findView<Button>(R.id.btnLoad)?.isEnabled = true
        }
    }

    override fun initAd(): Any? {
        bannerView = BannerView(
            requireContext(),
            configId,
            AdSize(width, height)
        )
        bannerView?.setAutoRefreshDelay(refreshDelay)
        bannerView?.setBannerListener(this)
        findView<RelativeLayout>(R.id.viewContainer)?.addView(bannerView)
        return bannerView
    }

    override fun loadAd() {
        bannerView?.loadAd()
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
    }

    override fun onAdFailed(bannerView: BannerView?, exception: AdException?) {
        resetEventButtons()
        findView<EventCounterView>(R.id.btnAdFailed)?.isEnabled = true
        findView<Button>(R.id.btnLoad)?.isEnabled = true
    }

    override fun onAdLoaded(bannerView: BannerView?) {
        resetEventButtons()
        findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = true
        findView<Button>(R.id.btnLoad)?.isEnabled = true
    }

    override fun onAdClicked(bannerView: BannerView?) {
        findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = true
    }

    override fun onAdClosed(bannerView: BannerView?) {
        findView<EventCounterView>(R.id.btnAdClosed)?.isEnabled = true
    }

    override fun onAdDisplayed(bannerView: BannerView?) {
        findView<EventCounterView>(R.id.btnAdDisplayed)?.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerView?.destroy()
    }
}