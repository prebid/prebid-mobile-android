package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import android.widget.Button
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.lyt_native_ad_links.*
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData
import org.prebid.mobile.renderingtestapp.R

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