package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import android.util.Log
import android.view.View
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
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingBannerAdmobBinding
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

    private var binding = getBinding<FragmentBiddingBannerAdmobBinding>()
    private lateinit var events: Events

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        events = Events(view)

        binding.adIdLabel.text = getString(R.string.label_auid, configId)
        binding.btnLoad.setOnClickListener {
            resetAdEvents()
            it.isEnabled = false
            loadAd()
        }

        binding.btnStopRefresh.setOnClickListener {
            adUnit?.stopRefresh()
            resetEventButtons()
            binding.btnLoad.isEnabled = true
        }
    }

    override fun initAd(): Any? {
        bannerView = AdView(requireActivity())
        bannerView?.setAdSize(com.google.android.gms.ads.AdSize(width, height))
        bannerView?.adUnitId = adUnitId
        bannerView?.adListener = getListener()
        binding.viewContainer.addView(bannerView)

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
        events.failed.isEnabled = false
        events.clicked.isEnabled = false
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
            binding.btnLoad.isEnabled = true
            events.loaded.isEnabled = true
        }

        override fun onAdClicked() {
            Log.d(TAG, "onAdClicked")
            events.clicked.isEnabled = true
        }

        override fun onAdOpened() {
            Log.d(TAG, "onAdOpened")
            events.opened.isEnabled = true
        }

        override fun onAdImpression() {
            Log.d(TAG, "onAdImpression")
            events.impression.isEnabled = true
        }

        override fun onAdClosed() {
            Log.d(TAG, "onAdClosed")
            events.closed.isEnabled = true
        }

        override fun onAdFailedToLoad(p0: LoadAdError) {
            Log.d(TAG, "onAdFailedToLoad - ${p0.message}")
            resetAdEvents()
            binding.btnLoad.isEnabled = true
            events.failed.isEnabled = true
        }

    }

    private class Events(parent: View) {

        val loaded: EventCounterView = parent.findViewById(R.id.btnAdLoaded)
        val impression: EventCounterView = parent.findViewById(R.id.btnAdImpression)
        val opened: EventCounterView = parent.findViewById(R.id.btnAdOpened)
        val clicked: EventCounterView = parent.findViewById(R.id.btnAdClicked)
        val closed: EventCounterView = parent.findViewById(R.id.btnAdClosed)
        val failed: EventCounterView = parent.findViewById(R.id.btnAdFailed)

    }

}