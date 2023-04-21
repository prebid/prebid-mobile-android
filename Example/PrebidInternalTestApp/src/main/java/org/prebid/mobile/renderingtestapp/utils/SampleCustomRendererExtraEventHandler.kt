package org.prebid.mobile.renderingtestapp.utils

import org.prebid.mobile.api.rendering.pluginrenderer.PluginExtraEventHandler
import org.prebid.mobile.configuration.AdUnitConfiguration
import org.prebid.mobile.rendering.video.VideoAdEvent

// These implementation contains fake events that could be trigger by the
// custom renderer from models/views that are not part of the prebid project
interface SampleCustomRendererExtraEventHandler : PluginExtraEventHandler {
    fun onVideoEvent(videoAdEvent: VideoAdEvent.Event, adUnitConfiguration: AdUnitConfiguration)
}