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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import kotlinx.android.synthetic.main.lyt_native_ad.*
import kotlinx.android.synthetic.main.lyt_native_gam_events.*
import org.prebid.mobile.eventhandlers.utils.GamUtils
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm.PpmNativeFragment
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.SourcePicker
import org.prebid.mobile.renderingtestapp.utils.loadImage

class GamNativeFragment(override val layoutRes: Int = R.layout.fragment_native) : PpmNativeFragment() {
    companion object {
        const val ARG_CUSTOM_FORMAT_ID = "ARG_CUSTOM_FORMAT_ID"
    }

    private var gamAdLoader: AdLoader? = null
    private var customFormatId: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customFormatId = it.getString(ARG_CUSTOM_FORMAT_ID, "")
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!SourcePicker.useMockServer) {
            SourcePicker.enableQaEndpoint(true)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (!SourcePicker.useMockServer) {
            SourcePicker.enableQaEndpoint(false)
        }
    }

    override fun loadAd() {
        val builder = AdManagerAdRequest.Builder()
        val publisherAdRequest = builder.build()

        nativeAdUnit?.fetchDemand { result ->
            val fetchDemandResult = result.fetchDemandResult

            if (fetchDemandResult != FetchDemandResult.SUCCESS) {
                btnFetchDemandResultFailure.isEnabled = true
                loadGam(publisherAdRequest)
                return@fetchDemand
            }

            btnFetchDemandResultSuccess?.isEnabled = true
            GamUtils.prepare(publisherAdRequest, result)
            loadGam(publisherAdRequest)
        }
    }

    override fun getEventButtonViewId(): Int = R.layout.lyt_native_gam_events

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

    private fun loadGam(publisherAdRequest: AdManagerAdRequest) {
        gamAdLoader = if (isCustomFormatExample()) createCustomFormatAdLoader() else createNativeAdLoader()

        gamAdLoader?.loadAd(publisherAdRequest)
    }

    private fun createNativeAdLoader(): AdLoader = AdLoader.Builder(requireContext(), adUnitId)
            .forNativeAd { unifiedAd ->
                btnUnifiedRequestSuccess?.isEnabled = true
                handleNativeAd(unifiedAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    btnPrimaryAdRequestFailure?.isEnabled = true
                }

                override fun onAdClicked() {
                    btnAdClicked?.isEnabled = true
                }
            })
            .build()

    private fun createCustomFormatAdLoader() = AdLoader.Builder(requireContext(), adUnitId)
            .forCustomFormatAd(customFormatId, { formatAd ->
                btnCustomAdRequestSuccess?.isEnabled = true
                handleCustomFormatAd(formatAd)
            }, null)
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: LoadAdError) {
                    btnPrimaryAdRequestFailure?.isEnabled = true
                }
            })
            .build()

    private fun handleNativeAd(nativeAd: NativeAd?) {
        nativeAd ?: return

        if (GamUtils.didPrebidWin(nativeAd)) {
            GamUtils.findNativeAd(nativeAd) {
                btnNativeAdLoaded?.isEnabled = true
                inflateViewContent(it)
            }
        }
        else {
            btnPrimaryAdWinUnified?.isEnabled = true
            inflateNativeAd(nativeAd)
        }
    }

    private fun handleCustomFormatAd(customFormatAd: NativeCustomFormatAd?) {
        customFormatAd ?: return

        if (GamUtils.didPrebidWin(customFormatAd)) {
            GamUtils.findNativeAd(customFormatAd) {
                btnNativeAdLoaded?.isEnabled = true
                inflateViewContent(it)
            }
        }
        else {
            btnPrimaryAdWinCustom?.isEnabled = true
            inflateGamCustomFormat(customFormatAd)
        }
    }


    private fun inflateGamCustomFormat(customFormatAd: NativeCustomFormatAd) {

        val title = customFormatAd.getText("title")
        val text = customFormatAd.getText("text")
        val cta = customFormatAd.getText("cta")
        val sponsoredBy = customFormatAd.getText("sponsoredBy")
        val iconUrl = customFormatAd.getText("iconUrl")?.toString() ?: ""
        val imageUrl = customFormatAd.getText("imgUrl")?.toString() ?: ""

        tvNativeTitle?.text = title
        tvNativeBody?.text = text
        tvNativeBrand?.text = sponsoredBy
        btnNativeAction?.text = cta

        loadImage(ivNativeMain, imageUrl)
        loadImage(ivNativeIcon, iconUrl)

        btnNativeAction?.setOnClickListener {
            customFormatAd.performClick("cta")
            btnAdClicked?.isEnabled = true
        }
    }

    private fun inflateNativeAd(nativeAd: NativeAd?) {
        nativeAd ?: return

        val adView = LayoutInflater.from(requireContext()).inflate(R.layout.lyt_unified_native_ad, null) as NativeAdView

        adView.mediaView = adView.findViewById(R.id.ad_media)

        // Set other ad assets.
        adView.headlineView = adView.findViewById(R.id.ad_headline)
        adView.bodyView = adView.findViewById(R.id.ad_body)
        adView.callToActionView = adView.findViewById(R.id.ad_call_to_action)
        adView.iconView = adView.findViewById(R.id.ad_app_icon)
        adView.priceView = adView.findViewById(R.id.ad_price)
        adView.starRatingView = adView.findViewById(R.id.ad_stars)
        adView.storeView = adView.findViewById(R.id.ad_store)
        adView.advertiserView = adView.findViewById(R.id.ad_advertiser)

        // The headline and media content are guaranteed to be in every UnifiedNativeAd.
        (adView.headlineView as TextView).text = nativeAd.headline

        nativeAd.mediaContent?.let { mediaContent ->
            adView.mediaView?.setMediaContent(mediaContent)
        }

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView?.visibility = View.INVISIBLE
        }
        else {
            adView.bodyView?.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView?.visibility = View.INVISIBLE
        }
        else {
            adView.callToActionView?.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView?.visibility = View.GONE
        }
        else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon?.drawable)
            adView.iconView?.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView?.visibility = View.INVISIBLE
        }
        else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView?.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        adContainer?.removeAllViews()
        adContainer?.addView(adView)
    }

    private fun isCustomFormatExample() = !TextUtils.isEmpty(customFormatId)
}