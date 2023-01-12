package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.RelativeLayout
import android.widget.TextView
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.adapters.prebid.utils.MaxMediationBannerUtils
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.mediation.MediationBannerAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingBannerApplovinMaxBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

open class MaxBannerFragment : AdFragment() {

    companion object {
        private const val TAG = "MaxBannerFragment"
    }

    protected var adView: MaxAdView? = null
    protected var adUnit: MediationBannerAdUnit? = null

    override val layoutRes = R.layout.fragment_bidding_banner_applovin_max

    protected val binding: FragmentBiddingBannerApplovinMaxBinding
        get() = getBinding()
    protected lateinit var events: Events

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER

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
        adView = if (width == 300) {
            MaxAdView(adUnitId, MaxAdFormat.MREC, requireContext())
        } else {
            MaxAdView(adUnitId, requireContext())
        }
        adView?.setListener(createListener())
        adView?.layoutParams = FrameLayout.LayoutParams(
            AppLovinSdkUtils.dpToPx(requireContext(), width),
            AppLovinSdkUtils.dpToPx(requireContext(), height)
        )
        binding.viewContainer.addView(adView)

        val mediationUtils =
            MaxMediationBannerUtils(adView)
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
        adUnit?.fetchDemand {
            adView?.loadAd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        adView?.destroy()
        adUnit?.destroy()
    }

    private fun resetAdEvents() {
        events.loaded(false)
        events.clicked(false)
        events.loadFailed(false)
        events.displayFailed(false)
        events.expanded(false)
        events.collapsed(false)
    }

    protected fun createListener(): MaxAdViewAdListener {
        return object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {

                binding.btnLoad.isEnabled = true
                events.loaded(true)
            }

            override fun onAdClicked(ad: MaxAd?) {
                events.clicked(true)
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                events.loadFailed(true)

                binding.btnLoad.isEnabled = true
                Log.d(TAG, "onAdLoadFailed(): ${error?.message}")
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                events.displayFailed(true)

                Log.d(TAG, "onAdDisplayFailed(): ${error?.message}")
            }

            override fun onAdExpanded(ad: MaxAd?) {
                events.expanded(true)
            }

            override fun onAdCollapsed(ad: MaxAd?) {
                events.collapsed(true)
            }

            // Deprecated according to documentation
            override fun onAdDisplayed(ad: MaxAd?) {}
            override fun onAdHidden(ad: MaxAd?) {}
        }
    }


    protected class Events(parentView: View) : BaseEvents(parentView) {

        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun impression(b: Boolean) = enable(R.id.btnAdImpression, b)
        fun clicked(b: Boolean) = enable(R.id.btnAdClicked, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)

        fun displayed(b: Boolean) = enable(R.id.btnAdDisplayed, b)
        fun expanded(b: Boolean) = enable(R.id.btnAdExpanded, b)
        fun collapsed(b: Boolean) = enable(R.id.btnAdCollapsed, b)
        fun loadFailed(b: Boolean) = enable(R.id.btnAdLoadFailed, b)
        fun displayFailed(b: Boolean) = enable(R.id.btnAdDisplayFailed, b)

    }

}