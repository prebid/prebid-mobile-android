package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.renderingtestapp.utils.SampleCustomRenderer

class AdMobInterstitialCustomRendererFragment : AdMobInterstitialFragment() {

    private val sampleCustomRenderer = SampleCustomRenderer()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        PrebidMobile.registerPluginRenderer(sampleCustomRenderer)
    }

    override fun onDestroy() {
        PrebidMobile.unregisterPluginRenderer(sampleCustomRenderer)
        super.onDestroy()
    }
}
