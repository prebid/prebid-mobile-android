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

package org.prebid.mobile.renderingtestapp.utils.adapters

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.widget.FrameLayout
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.enums.VideoPlacementType
import org.prebid.mobile.rendering.bidding.parallel.BannerView

private const val TAG = "FeedAdapter"

open class OXBFeedAdapter(context: Context,
                          val width: Int,
                          val height: Int,
                          val configId: String) : BaseFeedAdapter(context) {

    protected var videoView: BannerView? = null

    override fun destroy() {
        Log.d(TAG, "Destroying adapter")
        videoView?.destroy()
    }

    override fun initAndLoadAdView(parent: ViewGroup?, container: FrameLayout): View? {
        if (videoView == null) {
            videoView = BannerView(container.context, configId, AdSize(width, height))
            videoView?.videoPlacementType = VideoPlacementType.IN_FEED
            val layoutParams = FrameLayout.LayoutParams(WRAP_CONTENT, WRAP_CONTENT)
            layoutParams.gravity = Gravity.CENTER
            videoView?.layoutParams = layoutParams
        }
        videoView?.loadAd()
        return videoView
    }
}