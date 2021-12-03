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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import org.prebid.mobile.eventhandlers.GamBannerEventHandler
import org.prebid.mobile.rendering.bidding.enums.VideoPlacementType
import org.prebid.mobile.rendering.bidding.parallel.BannerView
import org.prebid.mobile.renderingtestapp.R

class GamOutstreamFragment : GamBannerFragment() {
    override val layoutRes: Int = R.layout.fragment_bidding_banner_video

    override fun initBanner(configId: String?, eventHandler: GamBannerEventHandler): BannerView {
        val bannerView = BannerView(requireContext(), configId, eventHandler)
        bannerView.videoPlacementType = VideoPlacementType.IN_BANNER
        return bannerView
    }
}