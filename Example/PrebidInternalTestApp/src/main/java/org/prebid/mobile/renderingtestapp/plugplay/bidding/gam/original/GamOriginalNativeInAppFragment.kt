package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.original

import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import kotlinx.android.synthetic.main.lyt_native_ad.*
import kotlinx.android.synthetic.main.lyt_native_gam_events.*
import kotlinx.android.synthetic.main.lyt_native_gam_events.btnAdClicked
import kotlinx.android.synthetic.main.lyt_native_gam_events.btnAdImpression
import kotlinx.android.synthetic.main.lyt_native_gam_events.btnFetchDemandResultFailure
import kotlinx.android.synthetic.main.lyt_native_gam_events.btnFetchDemandResultSuccess
import kotlinx.android.synthetic.main.lyt_native_in_app_events.*
import kotlinx.android.synthetic.main.view_native_ad.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.*
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm.PpmNativeFragment
import org.prebid.mobile.renderingtestapp.utils.loadImage

class GamOriginalNativeInAppFragment : PpmNativeFragment() {
    companion object {
        private const val TAG = "GamOriginalNativeInApp"
        private const val CUSTOM_FORMAT_ID = "11934135"
    }

    private var adView: AdManagerAdView? = null
    private var unifiedNativeAd: NativeAd? = null
    private var adUnit: NativeAdUnit? = null
    private var adLoader: AdLoader? = null
    private val onMainThread: (task: () -> Unit) -> Unit = { task ->
        CoroutineScope(Dispatchers.Main.immediate).launch {
            task.invoke()
        }
    }

    override fun loadAd() {
        createAd()
    }

    private fun createAd() {
        adUnit = configureNativeAdUnit()

        val adRequest = AdManagerAdRequest.Builder().build()
        adLoader = createAdLoader(adContainer)
        adUnit?.fetchDemand(adRequest) { resultCode ->
            if (resultCode != ResultCode.SUCCESS) {
                btnFetchDemandResultFailure.isEnabled = true
                adLoader!!.loadAd(
                    adRequest
                )
                return@fetchDemand
            }
            btnFetchDemandResultSuccess?.isEnabled = true
            adLoader!!.loadAd(
                adRequest
            )
        }
    }

    private fun inflatePrebidNativeAd(
        ad: PrebidNativeAd,
        wrapper: ViewGroup
    ) {
        val nativeContainer = View.inflate(wrapper.context, R.layout.lyt_native_ad, null)
        ad.registerView(nativeContainer, object : PrebidNativeAdEventListener {
            override fun onAdClicked() {
                Log.d(TAG, "onAdClicked called: ")
                onMainThread {
                    btnAdClicked?.isEnabled = true
                }
            }

            override fun onAdImpression() {
                Log.d(TAG, "onAdImpression called: ")
                onMainThread {
                    btnAdImpression?.isEnabled = true
                }
            }

            override fun onAdExpired() {
                Log.d(TAG, "onAdExpired called: ")
                onMainThread {
                    btnAdExpired?.isEnabled = true
                }
            }
        })

        loadImage(ivNativeIcon, ad.iconUrl)
        tvNativeTitle.text = ad.title
        loadImage(ivNativeMain, ad.imageUrl)
        tvNativeBody.text = ad.description
        btnNativeAction.text = ad.callToAction
        btnNativeAction.isEnabled = true
        adContainer.addView(nativeContainer)
    }

    private fun createAdLoader(
        wrapper: ViewGroup
    ): AdLoader? {
        val onGamAdLoaded = OnAdManagerAdViewLoadedListener { adManagerAdView: AdManagerAdView ->
            Log.d(TAG, "Gam loaded")
            adView = adManagerAdView
            wrapper.addView(adManagerAdView)
        }
        val onUnifiedAdLoaded = NativeAd.OnNativeAdLoadedListener { unifiedNativeAd: NativeAd? ->
            Log.d(TAG, "Unified native loaded")
            this.unifiedNativeAd = unifiedNativeAd
        }
        val onCustomAdLoaded =
            NativeCustomFormatAd.OnCustomFormatAdLoadedListener { nativeCustomTemplateAd: NativeCustomFormatAd? ->
                Log.d(TAG, "Custom ad loaded")
                AdViewUtils.findNative(nativeCustomTemplateAd!!, object : PrebidNativeAdListener {
                    override fun onPrebidNativeLoaded(ad: PrebidNativeAd) {
                        inflatePrebidNativeAd(ad, wrapper)
                        Log.d(TAG, "onPrebidNativeLoaded: ")
                        btnGetNativeAdResultSuccess?.isEnabled = true
                    }

                    override fun onPrebidNativeNotFound() {
                        Log.e(TAG, "onPrebidNativeNotFound")
                    }

                    override fun onPrebidNativeNotValid() {
                        Log.e(TAG, "onPrebidNativeNotValid")
                    }
                })
            }
        return AdLoader.Builder(wrapper.context, adUnitId)
            .forAdManagerAdView(onGamAdLoaded, AdSize.BANNER)
            .forNativeAd(onUnifiedAdLoaded)
            .forCustomFormatAd(
                CUSTOM_FORMAT_ID, onCustomAdLoaded
            ) { customAd: NativeCustomFormatAd?, s: String? ->
                btnCustomAdRequestSuccess?.isEnabled = true
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.e(TAG, "DFP onAdFailedToLoad")
                }
            })
            .build()
    }

    private fun configureNativeAdUnit(): NativeAdUnit {
        val adUnit = NativeAdUnit(configId);
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)
        val methods = ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD>()
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE)
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS)
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
        return adUnit;
    }

    override fun onDestroy() {
        super.onDestroy()
        adView?.destroy()
        adUnit?.stopAutoRefresh()
        unifiedNativeAd?.destroy()
    }
}