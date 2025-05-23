/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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
package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.original

import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.SimpleExoPlayer
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader
import com.google.android.exoplayer2.source.MediaSource
import com.google.android.exoplayer2.source.ProgressiveMediaSource
import com.google.android.exoplayer2.source.ads.AdsMediaSource
import com.google.android.exoplayer2.ui.PlayerView
import com.google.android.exoplayer2.upstream.DataSource
import com.google.android.exoplayer2.upstream.DataSpec
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory
import org.prebid.mobile.*
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

class GamOriginalApiInStreamActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid_demo_app_instream"
        const val CONFIG_ID = "prebid-demo-video-interstitial-320-480-original-api"

        const val WIDTH = 640
        const val HEIGHT = 480

        const val VIDEO_URL = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4"
    }

    private var adUnit: InStreamVideoAdUnit? = null
    private var player: SimpleExoPlayer? = null
    private var adsUri: Uri? = null
    private var adsLoader: ImaAdsLoader? = null
    private var playerView: PlayerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        // 1. Create InStreamVideoAdUnit
        adUnit = InStreamVideoAdUnit(CONFIG_ID, WIDTH, HEIGHT)

        // 2. Configure video parameters
        adUnit?.videoParameters = configureVideoParameters()

        // 3. Init player view
        playerView = PlayerView(this)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
        adWrapperView.addView(playerView, params)

        // 4. Make a bid request to Prebid Server
        adUnit?.fetchDemand {
            // 5. Prepare the creative URI
            val sizes = HashSet<AdSize>()
            sizes.add(AdSize(WIDTH, HEIGHT))
            val prebidURL =  Util.generateInstreamUriForGam(
                AD_UNIT_ID,
                sizes,
                it.targetingKeywords
            )

            adsUri = Uri.parse(prebidURL)

            // 6. Init player
            initializePlayer()
        }
    }

    private fun configureVideoParameters(): VideoParameters {
        return VideoParameters(listOf("video/x-flv", "video/mp4")).apply {
            placement = Signals.Placement.InStream
            plcmt = Signals.Plcmt.InStream

            api = listOf(
                Signals.Api.VPAID_1,
                Signals.Api.VPAID_2
            )

            maxBitrate = 1500
            minBitrate = 300
            maxDuration = 30
            minDuration = 5
            playbackMethod = listOf(Signals.PlaybackMethod.AutoPlaySoundOn)
            protocols = listOf(
                Signals.Protocols.VAST_2_0
            )
        }
    }

    private fun initializePlayer() {
        adsLoader = ImaAdsLoader.Builder(this).build()

        val playerBuilder = SimpleExoPlayer.Builder(this)
        player = playerBuilder.build()
        playerView!!.player = player
        adsLoader!!.setPlayer(player)

        val uri = Uri.parse(VIDEO_URL)

        val mediaItem = MediaItem.fromUri(uri)
        val dataSourceFactory: DataSource.Factory = DefaultDataSourceFactory(this, getString(R.string.app_name))
        val mediaSourceFactory = ProgressiveMediaSource.Factory(dataSourceFactory)
        val mediaSource: MediaSource = mediaSourceFactory.createMediaSource(mediaItem)
        val dataSpec = DataSpec(adsUri!!)
        val adsMediaSource = AdsMediaSource(
            mediaSource, dataSpec, "ad", mediaSourceFactory,
            adsLoader!!, playerView!!
        )
        player?.setMediaSource(adsMediaSource)
        player?.playWhenReady = true
        player?.prepare()
    }


    override fun onDestroy() {
        super.onDestroy()

        adUnit?.destroy()
        adsLoader?.setPlayer(null)
        adsLoader?.release()
        player?.release()
    }

}
