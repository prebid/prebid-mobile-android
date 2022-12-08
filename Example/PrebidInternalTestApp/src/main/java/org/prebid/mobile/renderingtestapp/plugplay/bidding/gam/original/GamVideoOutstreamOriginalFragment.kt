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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.original

import android.util.Log
import android.widget.RelativeLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.Signals
import org.prebid.mobile.VideoAdUnit
import org.prebid.mobile.VideoBaseAdUnit
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

class GamVideoOutstreamOriginalFragment : AdFragment() {
    private var adUnit: VideoAdUnit? = null
    private var gamView: AdManagerAdView? = null
    private val TAG = GamVideoOutstreamOriginalFragment::class.simpleName
    override fun initAd(): Any? {
        val parameters = VideoBaseAdUnit.Parameters()
        parameters.mimes = listOf("video/mp4")
        parameters.protocols = listOf(Signals.Protocols.VAST_2_0)
        parameters.playbackMethod = listOf(Signals.PlaybackMethod.AutoPlaySoundOff)
        parameters.placement = Signals.Placement.InBanner

        adUnit = VideoAdUnit(
            configId,
            width,
            height
        )
        adUnit?.parameters = parameters

        gamView = AdManagerAdView(requireContext())
        gamView?.adUnitId = adUnitId
        gamView?.setAdSizes(AdSize(width, height))
        gamView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                AdViewUtils.findPrebidCreativeSize(gamView, object :
                    AdViewUtils.PbFindSizeListener {
                    override fun success(
                        width: Int,
                        height: Int
                    ) {
                        gamView?.setAdSizes(AdSize(width, height))
                    }

                    override fun failure(error: PbFindSizeError) {}
                })
                Log.d(TAG, "onAdLoaded() called")
                resetEventButtons()
                btnAdLoaded?.isEnabled = true
                btnLoad?.isEnabled = true
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "onAdClicked() called")
                btnAdClicked?.isEnabled = true
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d(TAG, "onAdFailed() called with throwable = [${p0.message}]")
                resetEventButtons()
                btnAdFailed?.isEnabled = true
                btnLoad?.isEnabled = true
            }
        }

        view?.findViewById<RelativeLayout>(R.id.viewContainer)?.addView(gamView)
        adUnit?.setAutoRefreshInterval(refreshDelay)
        return gamView
    }

    override fun loadAd() {
        val builder = AdManagerAdRequest.Builder()
        adUnit?.fetchDemand(builder) {
            val request = builder.build()
            gamView?.loadAd(request)
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
    }

    override val layoutRes: Int = R.layout.fragment_bidding_banner_video
}