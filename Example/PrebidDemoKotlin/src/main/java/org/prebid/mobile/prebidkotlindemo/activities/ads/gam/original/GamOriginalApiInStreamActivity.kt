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
import org.prebid.mobile.Signals.PlaybackMethod
import org.prebid.mobile.Signals.Protocols
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

class GamOriginalApiInStreamActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/5300653/test_adunit_vast_pavliuchyk"
        const val CONFIG_ID = "1001-1"
        const val STORED_RESPONSE = "sample_video_response"
        const val WIDTH = 640
        const val HEIGHT = 480
    }

    private var adUnit: VideoAdUnit? = null
    private var player: SimpleExoPlayer? = null
    private var adsUri: Uri? = null
    private var adsLoader: ImaAdsLoader? = null
    private var playerView: PlayerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The ID of Mocked Bid Response on PBS. Only for test cases.
        PrebidMobile.setStoredAuctionResponse(STORED_RESPONSE)

        // This example uses Rubicon Server TODO: Rewrite to AWS Server
        PrebidMobile.setPrebidServerAccountId("1001")
        PrebidMobile.setPrebidServerHost(
            Host.createCustomHost("https://prebid-server.rubiconproject.com/openrtb2/auction")
        )

        createAd()
    }

    private fun createAd() {
        playerView = PlayerView(this)
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
        adWrapperView.addView(playerView, params)

        val parameters = VideoBaseAdUnit.Parameters()
        parameters.mimes = listOf("video/mp4")
        parameters.protocols = listOf(Protocols.VAST_2_0)
        parameters.playbackMethod = listOf(PlaybackMethod.AutoPlaySoundOff)
        parameters.placement = Signals.Placement.InStream

        adUnit = VideoAdUnit(CONFIG_ID, 640, 480)
        adUnit?.parameters = parameters
        adUnit?.fetchDemand { _: ResultCode?, keysMap: Map<String?, String?>? ->
            val sizes = HashSet<AdSize>()
            sizes.add(AdSize(640, 480))
            adsUri = Uri.parse(
                Util.generateInstreamUriForGam(
                    AD_UNIT_ID,
                    sizes,
                    keysMap
                )
            )
            val imaBuilder = ImaAdsLoader.Builder(this)
            adsLoader = imaBuilder.build()
            initializePlayer()
        }
    }

    private fun initializePlayer() {
        val playerBuilder = SimpleExoPlayer.Builder(this)
        player = playerBuilder.build()
        playerView!!.player = player
        adsLoader!!.setPlayer(player)

        val uri = Uri.parse("https://storage.googleapis.com/gvabox/media/samples/stock.mp4")
        // Uri uri = Uri.parse("<![CDATA[https://storage.googleapis.com/gvabox/media/samples/stock.mp4]]>");

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

        adUnit?.stopAutoRefresh()
        adsLoader?.setPlayer(null)
        adsLoader?.release()
        player?.release()

        // TODO: Return to AWS Server
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")
        PrebidMobile.setPrebidServerHost(
            Host.createCustomHost(
                "https://prebid-server-test-j.prebid.org/openrtb2/auction"
            )
        )
    }

}
