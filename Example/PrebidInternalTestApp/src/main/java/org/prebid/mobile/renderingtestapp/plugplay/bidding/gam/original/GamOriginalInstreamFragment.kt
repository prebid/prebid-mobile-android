package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.original

import android.net.Uri
import android.view.ViewGroup
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.ima.ImaAdsLoader
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.ui.PlayerView
import org.prebid.mobile.*
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingBannerVideoBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import java.util.*

class GamOriginalInstreamFragment : AdFragment() {

    private var adUnit: BannerAdUnit? = null
    private var player: ExoPlayer? = null
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
            adsLoader = ImaAdsLoader.Builder(requireActivity()).build()
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

        adUnit = BannerAdUnit(configId, width, height, EnumSet.of(AdUnitFormat.VIDEO))
        adUnit?.videoParameters = parameters
    }

    private fun initializePlayer() {
        val dataSourceFactory = DefaultDataSource.Factory(requireContext())
        val mediaSourceFactory = DefaultMediaSourceFactory(dataSourceFactory)
            .setLocalAdInsertionComponents({ adsLoader!! }, playerView!!)

        player = ExoPlayer.Builder(requireContext())
            .setMediaSourceFactory(mediaSourceFactory)
            .build()
        playerView!!.player = player
        adsLoader!!.setPlayer(player)

        val mediaItem = MediaItem.Builder()
            .setUri(Uri.parse("https://storage.googleapis.com/gvabox/media/samples/stock.mp4"))
            .setAdsConfiguration(
                MediaItem.AdsConfiguration.Builder(adsUri!!).build()
            )
            .build()

        player?.setMediaItem(mediaItem)
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