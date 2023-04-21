package org.prebid.mobile.renderingtestapp.utils

import org.prebid.mobile.api.rendering.pluginrenderer.PluginExtraEventHandler
import org.prebid.mobile.configuration.AdUnitConfiguration

// These implementation contains fake events that could be trigger by the
// custom renderer from models/views that are not part of the prebid project
interface SampleCustomRendererExtraEventHandler : PluginExtraEventHandler {
    fun onGliding(adUnitConfiguration: AdUnitConfiguration)
    fun onZooming(adUnitConfiguration: AdUnitConfiguration)
    fun onSlinking(adUnitConfiguration: AdUnitConfiguration)
    fun onImpression(adUnitConfiguration: AdUnitConfiguration)
}