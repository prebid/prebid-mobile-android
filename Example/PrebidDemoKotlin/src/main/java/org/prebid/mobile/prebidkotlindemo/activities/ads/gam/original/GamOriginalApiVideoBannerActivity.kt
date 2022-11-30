package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.original

import android.os.Bundle
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.Signals
import org.prebid.mobile.Signals.PlaybackMethod
import org.prebid.mobile.Signals.Protocols
import org.prebid.mobile.VideoAdUnit
import org.prebid.mobile.VideoBaseAdUnit
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.AdViewUtils.PbFindSizeListener
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

class GamOriginalApiVideoBannerActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid_oxb_outstream_video_reandom"
        const val CONFIG_ID = "imp-prebid-video-outstream"
        const val STORED_RESPONSE = "response-prebid-video-outstream"
        const val WIDTH = 300
        const val HEIGHT = 250
    }

    private var adUnit: VideoAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The ID of Mocked Bid Response on PBS. Only for test cases.
        PrebidMobile.setStoredAuctionResponse(STORED_RESPONSE)

        createAd()
    }

    private fun createAd() {
        val parameters = VideoBaseAdUnit.Parameters()
        parameters.mimes = listOf("video/mp4")
        parameters.protocols = listOf(Protocols.VAST_2_0)
        parameters.playbackMethod = listOf(PlaybackMethod.AutoPlaySoundOff)
        parameters.placement = Signals.Placement.InBanner

        adUnit = VideoAdUnit(
            CONFIG_ID,
            WIDTH,
            HEIGHT
        )
        adUnit?.parameters = parameters

        val gamView = AdManagerAdView(this)
        gamView.adUnitId = AD_UNIT_ID
        gamView.setAdSizes(AdSize(WIDTH, HEIGHT))
        gamView.adListener = createListener(gamView)

        adWrapperView.addView(gamView)

        val builder = AdManagerAdRequest.Builder()

        adUnit?.setAutoRefreshInterval(refreshTimeSeconds)
        adUnit?.fetchDemand(builder) {
            val request = builder.build()
            gamView.loadAd(request)
        }
    }

    private fun createListener(gamView: AdManagerAdView): AdListener {
        return object : AdListener() {
            override fun onAdLoaded() {
                AdViewUtils.findPrebidCreativeSize(gamView, object : PbFindSizeListener {
                    override fun success(
                        width: Int,
                        height: Int
                    ) {
                        gamView.setAdSizes(AdSize(width, height))
                    }

                    override fun failure(error: PbFindSizeError) {}
                })
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.stopAutoRefresh()
    }

}
