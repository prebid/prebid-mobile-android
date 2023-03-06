/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.renderingtestapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.FrameLayout
import android.widget.ProgressBar
import androidx.preference.PreferenceManager
import androidx.test.espresso.idling.CountingIdlingResource
import org.prebid.mobile.*
import org.prebid.mobile.api.mediation.MediationNativeAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.config.*
import org.prebid.mobile.renderingtestapp.utils.BaseFragment
import org.prebid.mobile.renderingtestapp.utils.ConfigurationViewSettings
import org.prebid.mobile.renderingtestapp.utils.OpenRtbConfigs
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

abstract class AdFragment : BaseFragment() {

    companion object {
        private val TAG = AdFragment::class.simpleName

        const val CONFIGURATOR_REQUEST_CODE = 0

        const val ARGUMENT_ACCOUNT_ID = "ARGUMENT_ACCOUNT_ID"
    }

    var idlingResource = CountingIdlingResource(AdFragment::class.java.simpleName)

    protected var mockAssetName: String? = null

    protected var configId: String = ""
    protected var adUnitId: String = ""
    protected var width = 0
    protected var height = 0
    protected var refreshDelay = PrebidMobile.AUTO_REFRESH_DELAY_MIN / 1000

    private var adView: Any? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        arguments?.let {
            configId = it.getString(getString(R.string.key_bid_config_id), "")
            adUnitId = it.getString(getString(R.string.key_ad_unit), "")
            width = it.getInt(getString(R.string.key_width))
            val storedAuctionResponse = it.getString(getString(R.string.stored_auction_response), "")
            PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
            height = it.getInt(getString(R.string.key_height))
            val title = it.getString(getString(R.string.key_title), getString(R.string.segment_title_in_app))
            setTitle(title)
            shouldSetNoBids()
            it.getString(ARGUMENT_ACCOUNT_ID)?.let { accountId ->
                Log.d(TAG, "Using custom account id: $accountId")
                PrebidMobile.setPrebidServerAccountId(accountId)
            }
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
        baseBinding.root.findViewById<EventCounterView>(R.id.btnAdFailed)?.isEnabled = false
        baseBinding.root.findViewById<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = false
        baseBinding.root.findViewById<EventCounterView>(R.id.btnAdClicked)?.isEnabled = false
        baseBinding.root.findViewById<EventCounterView>(R.id.btnAdDisplayed)?.isEnabled = false
        baseBinding.root.findViewById<EventCounterView>(R.id.btnAdClosed)?.isEnabled = false
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

    protected open fun configureNativeAdUnit(nativeAdUnit: MediationNativeAdUnit) {
        nativeAdUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        nativeAdUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        nativeAdUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)

        val methods: ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> = ArrayList()
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE)
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS)
        try {
            val tracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            nativeAdUnit.addEventTracker(tracker)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        nativeAdUnit.addAsset(title)

        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        nativeAdUnit.addAsset(icon)

        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        nativeAdUnit.addAsset(image)

        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        nativeAdUnit.addAsset(data)

        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        nativeAdUnit.addAsset(body)

        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        nativeAdUnit.addAsset(cta)
    }

    private fun startAd() {
        adView = initAd()
        setImpExtData()
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
            PrebidMobile.setPrebidServerAccountId(getString(R.string.prebid_account_id_prod_no_bids))
        } else {
            PrebidMobile.setPrebidServerAccountId(getString(R.string.prebid_account_id_prod))
        }
    }

    private fun isNoBids(): Boolean = configId == getString(R.string.prebid_config_id_no_bids)


    private fun setImpExtData() {
        OpenRtbConfigs.setImpExtDataTo(adView)
    }

    protected fun configureOriginalPrebid() {
        val hostUrl = PrebidMobile.getPrebidServerHost().hostUrl
        val host = Host.CUSTOM
        host.hostUrl = hostUrl
        PrebidMobile.setPrebidServerHost(host)
        PrebidMobile.setPrebidServerAccountId(PrebidMobile.getPrebidServerAccountId())
        PrebidMobile.initializeSdk(requireContext(), null)
    }

}