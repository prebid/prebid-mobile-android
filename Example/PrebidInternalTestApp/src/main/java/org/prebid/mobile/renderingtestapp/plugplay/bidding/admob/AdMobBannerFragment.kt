package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError
import org.prebid.mobile.AdSize
import org.prebid.mobile.admob.AdMobMediationBannerUtils
import org.prebid.mobile.admob.PrebidBannerAdapter
import org.prebid.mobile.api.mediation.MediationBannerAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

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

        findView<TextView>(R.id.adIdLabel)?.text = getString(R.string.label_auid, configId)
        findView<Button>(R.id.btnLoad)?.setOnClickListener {
            resetAdEvents()
            it.isEnabled = false
            loadAd()
        }

        findView<Button>(R.id.btnStopRefresh)?.setOnClickListener {
            adUnit?.stopRefresh()
            resetEventButtons()
            findView<Button>(R.id.btnLoad)?.isEnabled = true
        }
    }

    override fun initAd(): Any? {
        bannerView = AdView(requireActivity())
        bannerView?.setAdSize(com.google.android.gms.ads.AdSize(width, height))
        bannerView?.adUnitId = adUnitId
        bannerView?.adListener = getListener()
        findView<RelativeLayout>(R.id.viewContainer)?.addView(bannerView)

        adRequestExtras = Bundle()
        adRequest = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidBannerAdapter::class.java, adRequestExtras!!)
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
        findView<EventCounterView>(R.id.btnAdFailed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = false
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
            findView<Button>(R.id.btnLoad)?.isEnabled = true
            findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = true
        }

        override fun onAdClicked() {
            Log.d(TAG, "onAdClicked")
            findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = true
        }

        override fun onAdOpened() {
            Log.d(TAG, "onAdOpened")
            findView<EventCounterView>(R.id.btnAdOpened)?.isEnabled = true
        }

        override fun onAdImpression() {
            Log.d(TAG, "onAdImpression")
            findView<EventCounterView>(R.id.btnAdImpression)?.isEnabled = true
        }

        override fun onAdClosed() {
            Log.d(TAG, "onAdClosed")
            findView<EventCounterView>(R.id.btnAdClosed)?.isEnabled = true
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            Log.d(TAG, "onAdFailedToLoad - ${p0.message}")
            resetAdEvents()
            findView<Button>(R.id.btnLoad)?.isEnabled = true
            findView<EventCounterView>(R.id.btnAdFailed)?.isEnabled = true
        }

    }

}