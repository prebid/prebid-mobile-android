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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.AdSize
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.data.VideoPlacementType
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.renderingtestapp.R
import java.util.EnumSet

/**
 * Requests banner and outstream video demand on a single impression. The config id is picked at
 * random between a display and a video stored request, so either creative can win.
 */
open class PpmMultiformatBannerFragment : PpmVideoFragment() {

    override fun initAd(): Any? {
        val context = requireContext()
        bannerView = BannerView(
            context,
            listOf(
                context.getString(R.string.imp_prebid_id_banner_300x250),
                context.getString(R.string.imp_prebid_id_video_outstream)
            ).shuffled().first(),
            AdSize(width, height)
        )
        bannerView?.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO))
        bannerView?.videoPlacementType = VideoPlacementType.IN_BANNER
        bannerView?.setAutoRefreshDelay(refreshDelay)
        bannerView?.setBannerListener(this)
        bannerView?.setBannerVideoListener(this)
        binding.viewContainer.addView(bannerView)
        return bannerView
    }

}
