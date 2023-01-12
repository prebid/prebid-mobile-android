package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.original

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.*
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingBannerBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

class GamOriginalNativeBannerFragment : AdFragment() {

    companion object {
        private const val TAG = "GamOriginalNativeBanner"
    }

    private var nativeAdUnit: NativeAdUnit? = null
    override val layoutRes: Int = R.layout.fragment_bidding_banner

    private val binding: FragmentBiddingBannerBinding
        get() = getBinding()
    private lateinit var events: Events

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        events = Events(view)
        binding.btnLoad.setOnClickListener {
            resetEventButtons()
            loadAd()
        }
    }

    override fun initAd(): Any? {
        nativeAdUnit = configureNativeAdUnit()
        return nativeAdUnit
    }

    override fun loadAd() {
        createAd()
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
    }

    private fun createAd() {
        val gamView = AdManagerAdView(requireContext())
        gamView.adUnitId = adUnitId
        gamView.setAdSizes(AdSize.FLUID)
        gamView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                AdViewUtils.findPrebidCreativeSize(gamView, object : AdViewUtils.PbFindSizeListener {
                    override fun success(width: Int, height: Int) {
                        gamView.setAdSizes(AdSize(width, height))
                    }

                    override fun failure(error: PbFindSizeError) {}
                })
                Log.d(TAG, "onAdLoaded() called")
                resetEventButtons()
                events.loaded(true)
                binding.btnLoad.isEnabled = true
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                super.onAdFailedToLoad(p0)
                Log.d(TAG, "onAdFailed() called with throwable = [${p0.message}]")
                resetEventButtons()
                events.failed(true)
                binding.btnLoad.isEnabled = true
            }

            override fun onAdClicked() {
                super.onAdClicked()
                Log.d(TAG, "onAdClicked() called")
                events.clicked(true)
            }

        }
        binding.viewContainer.addView(gamView)

        val builder = AdManagerAdRequest.Builder()
        nativeAdUnit?.setAutoRefreshInterval(refreshDelay)
        nativeAdUnit?.fetchDemand(builder) {
            val request = builder.build()
            gamView.loadAd(request)
        }
    }

    private fun configureNativeAdUnit(): NativeAdUnit {
        val adUnit = NativeAdUnit(configId)

        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)
        val methods = ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD>()
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE)
        try {
            val tracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            adUnit.addEventTracker(tracker)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        adUnit.addAsset(title)
        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        adUnit.addAsset(icon)
        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        adUnit.addAsset(image)
        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        adUnit.addAsset(data)
        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        adUnit.addAsset(body)
        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        adUnit.addAsset(cta)
        return adUnit
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAdUnit?.stopAutoRefresh()
    }

    private class Events(parentView: View) : BaseEvents(parentView) {

        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun clicked(b: Boolean) = enable(R.id.btnAdClicked, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)

    }

}