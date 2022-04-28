package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.adapters.prebid.utils.MaxMediationBannerUtils
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils
import kotlinx.android.synthetic.main.events_max_banner.*
import kotlinx.android.synthetic.main.fragment_bidding_banner_applovin_max.*
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.mediation.MediationBannerAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

open class MaxBannerFragment : AdFragment() {

    companion object {
        private const val TAG = "MaxBannerFragment"
    }

    protected var adView: MaxAdView? = null
    protected var adUnit: MediationBannerAdUnit? = null

    override val layoutRes = R.layout.fragment_bidding_banner_applovin_max

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER

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
        viewContainer.addView(adView)

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
        btnAdLoaded?.isEnabled = false
        btnAdClicked?.isEnabled = false
        btnAdLoadFailed?.isEnabled = false
        btnAdDisplayFailed?.isEnabled = false
        btnAdExpanded?.isEnabled = false
        btnAdCollapsed?.isEnabled = false
    }

    protected fun createListener(): MaxAdViewAdListener {
        return object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {

                btnLoad?.isEnabled = true
                btnAdLoaded?.isEnabled = true
            }

            override fun onAdClicked(ad: MaxAd?) {
                btnAdClicked?.isEnabled = true
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                btnAdLoadFailed?.isEnabled = true

                btnLoad?.isEnabled = true
                Log.d(TAG, "onAdLoadFailed(): ${error?.message}")
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                btnAdDisplayFailed?.isEnabled = true

                Log.d(TAG, "onAdDisplayFailed(): ${error?.message}")
            }

            override fun onAdExpanded(ad: MaxAd?) {
                btnAdExpanded?.isEnabled = true
            }

            override fun onAdCollapsed(ad: MaxAd?) {
                btnAdCollapsed?.isEnabled = true
            }

            // Deprecated according to documentation
            override fun onAdDisplayed(ad: MaxAd?) {}
            override fun onAdHidden(ad: MaxAd?) {}
        }
    }

}