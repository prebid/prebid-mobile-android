package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import kotlinx.android.synthetic.main.events_admob.*

import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.AdSize
import org.prebid.mobile.admob.AdMobMediationBannerUtils
import org.prebid.mobile.admob.PrebidBannerAdapter
import org.prebid.mobile.api.mediation.MediationBannerAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

open class AdMobBannerFragment : AdFragment() {

    companion object {
        private const val TAG = "AdMobBannerFragment"
    }

    protected var adRequest: AdRequest? = null
    protected var adUnit: MediationBannerAdUnit? = null
    protected var bannerView: AdView? = null
    protected var adRequestExtras: Bundle? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        adIdLabel.text = getString(R.string.label_auid, configId)
        btnLoad?.setOnClickListener {
            resetAdEvents()
            it.isEnabled = false
            loadAd()
        }

        btnStopRefresh?.setOnClickListener {
            adUnit?.stopRefresh()
            resetEventButtons()
            btnLoad?.isEnabled = true
        }
    }

    override fun initAd(): Any? {
        bannerView = AdView(requireActivity())
        bannerView?.adSize = com.google.android.gms.ads.AdSize(width, height)
        bannerView?.adUnitId = adUnitId
        bannerView?.adListener = getListener()
        viewContainer.addView(bannerView)

        adRequestExtras = Bundle()
        adRequest = AdRequest
            .Builder()
            .addCustomEventExtrasBundle(PrebidBannerAdapter::class.java, adRequestExtras!!)
            .build()
        val mediationUtils =
            AdMobMediationBannerUtils(adRequestExtras, bannerView)


        adUnit = MediationBannerAdUnit(
            requireContext(),
            configId,
            AdSize(width, height),
            mediationUtils
        )
        adUnit?.setRefreshInterval(refreshDelay)
        return adUnit
    }

    override fun loadAd() {
        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")
            bannerView?.loadAd(adRequest!!)
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
    }

    override val layoutRes = R.layout.fragment_bidding_banner_admob

    private fun resetAdEvents() {
        btnAdFailed?.isEnabled = false
        btnAdClicked?.isEnabled = false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        adUnit?.destroy()
        bannerView?.destroy()
    }

    protected fun getListener() = object : AdListener() {
        override fun onAdLoaded() {
            Log.d(TAG, "onAdLoaded")
            resetAdEvents()
            btnLoad?.isEnabled = true
            btnAdLoaded?.isEnabled = true
        }

        override fun onAdClicked() {
            Log.d(TAG, "onAdClicked")
            btnAdClicked?.isEnabled = true
        }

        override fun onAdOpened() {
            Log.d(TAG, "onAdOpened")
            btnAdOpened?.isEnabled = true
        }

        override fun onAdImpression() {
            Log.d(TAG, "onAdImpression")
            btnAdImpression?.isEnabled = true
        }

        override fun onAdClosed() {
            Log.d(TAG, "onAdClosed")
            btnAdClosed?.isEnabled = true
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            Log.d(TAG, "onAdFailedToLoad - ${p0.message}")
            resetAdEvents()
            btnLoad?.isEnabled = true
            btnAdFailed?.isEnabled = true
        }

    }

}