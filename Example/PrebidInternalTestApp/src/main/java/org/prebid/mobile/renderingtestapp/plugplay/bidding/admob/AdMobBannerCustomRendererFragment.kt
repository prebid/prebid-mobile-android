package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.plugin.PluginCapability
import org.prebid.mobile.renderingtestapp.utils.SampleCustomRenderer

class AdMobBannerCustomRendererFragment : AdMobBannerFragment() {

    private val sampleCustomRenderer = SampleCustomRenderer()

    override fun onCreate(savedInstanceState: android.os.Bundle?) {
        super.onCreate(savedInstanceState)
        PrebidMobile.registerPlugin(sampleCustomRenderer, PluginCapability.RENDERER)
    }

    override fun onDestroy() {
        PrebidMobile.unregisterPlugin(sampleCustomRenderer, PluginCapability.RENDERER)
        super.onDestroy()
    }
}
