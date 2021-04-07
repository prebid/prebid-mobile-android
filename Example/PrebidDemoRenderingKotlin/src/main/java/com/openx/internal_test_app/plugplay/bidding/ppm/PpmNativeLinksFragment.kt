package com.openx.internal_test_app.plugplay.bidding.ppm

import android.widget.Button
import com.openx.apollo.bidding.data.ntv.NativeAd
import com.openx.apollo.models.openrtb.bidRequests.assets.NativeAssetData
import com.openx.internal_test_app.R
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.lyt_native_ad_links.*

class PpmNativeLinksFragment : PpmNativeFragment() {
    override val layoutRes: Int = R.layout.fragment_native_links

    override fun inflateViewContent(nativeAd: NativeAd?) {
        if (nativeAd == null) {
            btnAdFailed?.isEnabled = true
            return
        }

        btnAdDisplayed?.isEnabled = true
        this.nativeAd = nativeAd

        nativeAd.setNativeAdListener(this)

        nativeAd.registerView(adContainer, btnNativeLinkRoot)

        btnNativeLinkRoot.text = nativeAd.callToAction
        setupButton(nativeAd, btnNativeDeeplinkFallback, NativeAssetData.DataType.SPONSORED)
        setupButton(nativeAd, btnNativeDeeplinkOk, NativeAssetData.DataType.DESC)
        setupButton(nativeAd, btnNativeLinkUrl, NativeAssetData.DataType.RATING)
    }

    private fun setupButton(nativeAd: NativeAd?, button: Button, dataType: NativeAssetData.DataType) {
        val nativeData = nativeAd?.getNativeAdDataList(dataType)?.firstOrNull()
        if (nativeData != null) {
            button.text = nativeData.value
            nativeAd.registerClickView(button, nativeData)
        }
    }
}