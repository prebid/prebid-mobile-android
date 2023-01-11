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
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

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
        findView<RelativeLayout>(R.id.viewContainer)?.addView(adView)

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
        findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdLoadFailed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdDisplayFailed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdExpanded)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdCollapsed)?.isEnabled = false
    }

    protected fun createListener(): MaxAdViewAdListener {
        return object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {

                findView<Button>(R.id.btnLoad)?.isEnabled = true
                findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = true
            }

            override fun onAdClicked(ad: MaxAd?) {
                findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = true
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                findView<EventCounterView>(R.id.btnAdLoadFailed)?.isEnabled = true

                findView<Button>(R.id.btnLoad)?.isEnabled = true
                Log.d(TAG, "onAdLoadFailed(): ${error?.message}")
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                findView<EventCounterView>(R.id.btnAdDisplayFailed)?.isEnabled = true

                Log.d(TAG, "onAdDisplayFailed(): ${error?.message}")
            }

            override fun onAdExpanded(ad: MaxAd?) {
                findView<EventCounterView>(R.id.btnAdExpanded)?.isEnabled = true
            }

            override fun onAdCollapsed(ad: MaxAd?) {
                findView<EventCounterView>(R.id.btnAdCollapsed)?.isEnabled = true
            }

            // Deprecated according to documentation
            override fun onAdDisplayed(ad: MaxAd?) {}
            override fun onAdHidden(ad: MaxAd?) {}
        }
    }

}