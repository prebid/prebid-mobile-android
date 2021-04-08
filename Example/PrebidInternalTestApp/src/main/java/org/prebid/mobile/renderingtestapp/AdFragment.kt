package org.prebid.mobile.renderingtestapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.preference.PreferenceManager
import androidx.test.espresso.idling.CountingIdlingResource
import kotlinx.android.synthetic.main.events_bids.*
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetImage
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetTitle
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings
import org.prebid.mobile.renderingtestapp.plugplay.config.*
import org.prebid.mobile.renderingtestapp.utils.*

const val CONFIGURATOR_REQUEST_CODE = 0

abstract class AdFragment : BaseFragment() {
    var idlingResource = CountingIdlingResource(AdFragment::class.java.simpleName)

    protected var mockAssetName: String? = null

    protected var configId: String = ""
    protected var adUnitId: String = ""
    protected var width = 0
    protected var height = 0
    protected var refreshDelay = PrebidRenderingSettings.AUTO_REFRESH_DELAY_DEFAULT / 1000

    private var adView: Any? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            configId = it.getString(getString(R.string.key_bid_config_id), "")
            adUnitId = it.getString(getString(R.string.key_ad_unit), "")
            width = it.getInt(getString(R.string.key_width))
            height = it.getInt(getString(R.string.key_height))
            val title = it.getString(getString(R.string.key_title), getString(R.string.segment_title_in_app))
            setTitle(title)
            shouldSetNoBids()
        }
        if (ConfigurationViewSettings.isEnabled && configuratorMode() != null) {
            startAdConfigurator()
        }
        else {
            startAd()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        addProgressDialog(view)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        setNoBidsAccountId(false)
        adView = null
    }

    protected fun resetEventButtons() {
        btnAdFailed?.isEnabled = false
        btnAdLoaded?.isEnabled = false
        btnAdClicked?.isEnabled = false
        btnAdDisplayed?.isEnabled = false
        btnAdClosed?.isEnabled = false
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            configId = data.getStringExtra(EXTRA_CONFIG_ID).toString()
            width = data.getIntExtra(EXTRA_WIDTH, width)
            height = data.getIntExtra(EXTRA_HEIGHT, height)
            refreshDelay = data.getIntExtra(EXTRA_REFRESH_DELAY, refreshDelay)
        }
        startAd()
    }

    abstract fun initAd(): Any?

    abstract fun loadAd()

    abstract fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode?

    protected open fun getNativeAdConfig(): NativeAdConfiguration? {
        if (ConfigurationViewSettings.isEnabled && NativeConfigurationStore.getStoredNativeConfig() != null) {
            return NativeConfigurationStore.getStoredNativeConfig()
        }

        val nativeAdConfiguration = NativeAdConfiguration()
        nativeAdConfiguration.contextType = NativeAdConfiguration.ContextType.SOCIAL_CENTRIC
        nativeAdConfiguration.placementType = NativeAdConfiguration.PlacementType.CONTENT_FEED
        nativeAdConfiguration.contextSubType = NativeAdConfiguration.ContextSubType.GENERAL_SOCIAL

        val methods = ArrayList<NativeEventTracker.EventTrackingMethod>()
        methods.add(NativeEventTracker.EventTrackingMethod.IMAGE)
        methods.add(NativeEventTracker.EventTrackingMethod.JS)
        val eventTracker = NativeEventTracker(NativeEventTracker.EventType.IMPRESSION, methods)
        nativeAdConfiguration.addTracker(eventTracker)

        val assetTitle = NativeAssetTitle()
        assetTitle.len = 90
        assetTitle.isRequired = true
        nativeAdConfiguration.addAsset(assetTitle)

        val assetIcon = NativeAssetImage()
        assetIcon.type = NativeAssetImage.ImageType.ICON
        assetIcon.wMin = 20
        assetIcon.hMin = 20
        assetIcon.isRequired = true
        nativeAdConfiguration.addAsset(assetIcon)

        val assetImage = NativeAssetImage()
        assetImage.hMin = 20
        assetImage.wMin = 200
        assetImage.isRequired = true
        nativeAdConfiguration.addAsset(assetImage)

        val assetData = NativeAssetData()
        assetData.len = 90
        assetData.type = NativeAssetData.DataType.SPONSORED
        assetData.isRequired = true
        nativeAdConfiguration.addAsset(assetData)

        val assetBody = NativeAssetData()
        assetBody.isRequired = true
        assetBody.type = NativeAssetData.DataType.DESC
        nativeAdConfiguration.addAsset(assetBody)

        val assetCta = NativeAssetData()
        assetCta.isRequired = true
        assetCta.type = NativeAssetData.DataType.CTA_TEXT
        nativeAdConfiguration.addAsset(assetCta)

        return nativeAdConfiguration
    }

    private fun startAd() {
        adView = initAd()
        setImpContextData()
        loadAd()
    }

    private fun addProgressDialog(view: View) {
        val keyShowProgressDialog = getString(R.string.key_show_progress_dialog)
        val showProgressDialog = PreferenceManager.getDefaultSharedPreferences(context).getBoolean(keyShowProgressDialog, false)

        if (showProgressDialog && view is ViewGroup) {
            val progressBar = ProgressBar(context)
            val progressBarLayoutParams = FrameLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
            progressBarLayoutParams.gravity = Gravity.CENTER
            progressBar.layoutParams = progressBarLayoutParams

            val frameLayout = FrameLayout(requireContext())
            frameLayout.layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)

            frameLayout.addView(progressBar)

            view.addView(frameLayout)
        }
    }

    private fun startAdConfigurator() {
        configuratorMode()?.let {
            val ft = parentFragmentManager.beginTransaction()
            val prev = parentFragmentManager.findFragmentByTag("dialog")
            if (prev != null) {
                ft.remove(prev)
            }
            ft.addToBackStack(null)

            val dialogFragment = AdConfiguratorDialogFragment.newInstance(it, configId, width, height)

            dialogFragment.setTargetFragment(this, CONFIGURATOR_REQUEST_CODE)
            dialogFragment.show(ft, "dialog")
        }
    }

    private fun shouldSetNoBids() {
        if (isNoBids()) {
            setNoBidsAccountId(true)
        }
    }

    private fun setNoBidsAccountId(enable: Boolean) {
        if (enable) {
            PrebidRenderingSettings.setAccountId(getString(R.string.prebid_account_id_prod_no_bids))
        }
        else {
            PrebidRenderingSettings.setAccountId(getString(R.string.prebid_account_id_prod))
        }
    }

    private fun isNoBids(): Boolean = configId == getString(R.string.prebid_config_id_no_bids)

    private fun disableNoBids() {
        MockServerUtils.cancelRandomNoBids()
    }

    private fun setImpContextData() {
        OpenRtbConfigs.setImpContextDataTo(adView)
    }
}