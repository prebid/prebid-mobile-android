package com.openx.internal_test_app.plugplay.bidding.gam

import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.google.android.gms.ads.formats.UnifiedNativeAd
import com.google.android.gms.ads.formats.UnifiedNativeAdView
import com.openx.apollo.bidding.data.FetchDemandResult
import com.openx.apollo.eventhandlers.utils.GamUtils
import com.openx.internal_test_app.R
import com.openx.internal_test_app.plugplay.bidding.ppm.PpmNativeFragment
import com.openx.internal_test_app.plugplay.config.AdConfiguratorDialogFragment
import com.openx.internal_test_app.utils.SourcePicker
import com.openx.internal_test_app.utils.loadImage
import kotlinx.android.synthetic.main.lyt_native_ad.*
import kotlinx.android.synthetic.main.lyt_native_gam_events.*

class GamNativeFragment(override val layoutRes: Int = R.layout.fragment_native) : PpmNativeFragment() {
    companion object {
        const val ARG_CUSTOM_TEMPLATE_ID = "ARG_CUSTOM_TEMPLATE_ID"
    }

    private var gamAdLoader: AdLoader? = null
    private var customTemplateId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            customTemplateId = it.getString(ARG_CUSTOM_TEMPLATE_ID, "")
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
        val builder = PublisherAdRequest.Builder()
        val publisherAdRequest = builder.build()

        nativeAdUnit?.fetchDemand { result ->
            val fetchDemandResult = result.fetchDemandResult

            if (fetchDemandResult != FetchDemandResult.SUCCESS) {
                btnFetchDemandResultFailure.isEnabled = true
                loadGam(publisherAdRequest)
                return@fetchDemand
            }

            btnFetchDemandResultSuccess?.isEnabled = true
            GamUtils.prepare(builder, result)
            loadGam(publisherAdRequest)
        }
    }

    override fun getEventButtonViewId(): Int = R.layout.lyt_native_gam_events

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

    private fun loadGam(publisherAdRequest: PublisherAdRequest) {
        gamAdLoader = if (isCustomTemplateExample()) createCustomTemplateAdLoader() else createUnifiedAdLoader()

        gamAdLoader?.loadAd(publisherAdRequest)
    }

    private fun createUnifiedAdLoader(): AdLoader = AdLoader.Builder(requireContext(), adUnitId)
            .forUnifiedNativeAd { unifiedAd ->
                btnUnifiedRequestSuccess?.isEnabled = true
                handleUnifiedAd(unifiedAd)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                    btnPrimaryAdRequestFailure?.isEnabled = true
                }

                override fun onAdClicked() {
                    btnAdClicked?.isEnabled = true
                }
            })
            .build()

    private fun createCustomTemplateAdLoader() = AdLoader.Builder(requireContext(), adUnitId)
            .forCustomTemplateAd(customTemplateId, { customTemplate ->
                btnCustomAdRequestSuccess?.isEnabled = true
                handleCustomTemplateAd(customTemplate)
            }, null)
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                    btnPrimaryAdRequestFailure?.isEnabled = true
                }
            })
            .build()

    private fun handleUnifiedAd(unifiedAd: UnifiedNativeAd?) {
        unifiedAd ?: return

        if (GamUtils.didApolloWin(unifiedAd)) {
            GamUtils.findNativeAd(unifiedAd) {
                btnNativeAdLoaded?.isEnabled = true
                inflateViewContent(it)
            }
        }
        else {
            btnPrimaryAdWinUnified?.isEnabled = true
            inflateUnifiedNativeAd(unifiedAd)
        }
    }

    private fun handleCustomTemplateAd(customTemplate: NativeCustomTemplateAd?) {
        customTemplate ?: return

        if (GamUtils.didApolloWin(customTemplate)) {
            GamUtils.findNativeAd(customTemplate) {
                btnNativeAdLoaded?.isEnabled = true
                inflateViewContent(it)
            }
        }
        else {
            btnPrimaryAdWinCustom?.isEnabled = true
            inflateGamCustomTemplate(customTemplate)
        }
    }


    private fun inflateGamCustomTemplate(customTemplate: NativeCustomTemplateAd) {

        val title = customTemplate.getText("title")
        val text = customTemplate.getText("text")
        val cta = customTemplate.getText("cta")
        val sponsoredBy = customTemplate.getText("sponsoredBy")
        val iconUrl = customTemplate.getText("iconUrl")?.toString() ?: ""
        val imageUrl = customTemplate.getText("imgUrl")?.toString() ?: ""

        tvNativeTitle?.text = title
        tvNativeBody?.text = text
        tvNativeBrand?.text = sponsoredBy
        btnNativeAction?.text = cta

        loadImage(ivNativeMain, imageUrl)
        loadImage(ivNativeIcon, iconUrl)

        btnNativeAction?.setOnClickListener {
            customTemplate.performClick("cta")
            btnAdClicked?.isEnabled = true
        }
    }

    private fun inflateUnifiedNativeAd(nativeAd: UnifiedNativeAd?) {
        nativeAd ?: return

        val adView = LayoutInflater.from(requireContext()).inflate(R.layout.lyt_unified_native_ad, null) as UnifiedNativeAdView

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
        adView.mediaView.setMediaContent(nativeAd.mediaContent)

        // These assets aren't guaranteed to be in every UnifiedNativeAd, so it's important to
        // check before trying to display them.
        if (nativeAd.body == null) {
            adView.bodyView.visibility = View.INVISIBLE
        }
        else {
            adView.bodyView.visibility = View.VISIBLE
            (adView.bodyView as TextView).text = nativeAd.body
        }

        if (nativeAd.callToAction == null) {
            adView.callToActionView.visibility = View.INVISIBLE
        }
        else {
            adView.callToActionView.visibility = View.VISIBLE
            (adView.callToActionView as Button).text = nativeAd.callToAction
        }

        if (nativeAd.icon == null) {
            adView.iconView.visibility = View.GONE
        }
        else {
            (adView.iconView as ImageView).setImageDrawable(nativeAd.icon.drawable)
            adView.iconView.visibility = View.VISIBLE
        }

        if (nativeAd.advertiser == null) {
            adView.advertiserView.visibility = View.INVISIBLE
        }
        else {
            (adView.advertiserView as TextView).text = nativeAd.advertiser
            adView.advertiserView.visibility = View.VISIBLE
        }

        // This method tells the Google Mobile Ads SDK that you have finished populating your
        // native ad view with this native ad.
        adView.setNativeAd(nativeAd)

        adContainer?.removeAllViews()
        adContainer?.addView(adView)
    }

    private fun isCustomTemplateExample() = !TextUtils.isEmpty(customTemplateId)
}