package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.original

import android.net.Uri
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
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingBannerVideoBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

class GamOriginalInstreamNewApiFragment : AdFragment() {

    private var adUnit: InStreamVideoAdUnit? = null
    private var player: SimpleExoPlayer? = null
    private var adsUri: Uri? = null
    private var adsLoader: ImaAdsLoader? = null
    private var playerView: PlayerView? = null

    private val binding: FragmentBiddingBannerVideoBinding
        get() = getBinding()

    override val layoutRes: Int = R.layout.fragment_bidding_banner_video

    override fun initAd(): Any? {
        PrebidMobile.setPrebidServerAccountId("1001")
        PrebidMobile.initializeSdk(context, "https://prebid-server.rubiconproject.com/openrtb2/auction", null);
        createAd()
        return null
    }

    override fun loadAd() {
        adUnit?.fetchDemand {
            val sizes = HashSet<AdSize>()
            sizes.add(AdSize(width, height))
            adsUri = Uri.parse(
                Util.generateInstreamUriForGam(
                    adUnitId,
                    sizes,
                    it.targetingKeywords
                )
            )
            val imaBuilder = ImaAdsLoader.Builder(requireActivity())
            adsLoader = imaBuilder.build()
            initializePlayer()
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
    }

    private fun createAd() {
        playerView = PlayerView(requireContext())
        val params = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600)
        binding.viewContainer.addView(playerView, params)

        val parameters = VideoParameters(listOf("video/mp4"))
        parameters.protocols = listOf(Signals.Protocols.VAST_2_0)
        parameters.playbackMethod = listOf(Signals.PlaybackMethod.AutoPlaySoundOff)
        parameters.placement = Signals.Placement.InStream
        parameters.plcmt = Signals.Plcmt.InStream

        adUnit = InStreamVideoAdUnit(configId, width, height)
        adUnit?.videoParameters = parameters
    }

    private fun initializePlayer() {
        val playerBuilder = SimpleExoPlayer.Builder(requireContext())
        player = playerBuilder.build()
        playerView!!.player = player
        adsLoader!!.setPlayer(player)

        val uri = Uri.parse("https://storage.googleapis.com/gvabox/media/samples/stock.mp4")
        // Uri uri = Uri.parse("<![CDATA[https://storage.googleapis.com/gvabox/media/samples/stock.mp4]]>");

        val mediaItem = MediaItem.fromUri(uri)
        val dataSourceFactory: DataSource.Factory =
            DefaultDataSourceFactory(requireContext(), getString(R.string.app_name))
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

    override fun onDestroyView() {
        super.onDestroyView()
        adUnit?.destroy()
        adsLoader?.setPlayer(null)
        adsLoader?.release()
        player?.release()
        PrebidMobile.initializeSdk(context, "https://prebid-server-test-j.prebid.org/openrtb2/auction", null);
        PrebidMobile.setPrebidServerAccountId(getString(R.string.prebid_account_id_prod))
    }

}