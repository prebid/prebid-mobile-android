package org.prebid.mobile.javademo.activities.ads.gam.original;

import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DataSpec;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.Signals;
import org.prebid.mobile.Util;
import org.prebid.mobile.VideoAdUnit;
import org.prebid.mobile.javademo.R;
import org.prebid.mobile.javademo.activities.BaseAdActivity;

import java.util.Collections;
import java.util.HashSet;

public class GamOriginalApiVideoInStream extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/5300653/test_adunit_vast_pavliuchyk";
    private static final String CONFIG_ID = "1001-1";
    private static final String STORED_RESPONSE = "sample_video_response";

    private VideoAdUnit adUnit;
    private SimpleExoPlayer player;
    private Uri adsUri;
    private ImaAdsLoader adsLoader;
    private PlayerView playerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // The ID of Mocked Bid Response on PBS. Only for test cases.
        PrebidMobile.setStoredAuctionResponse(STORED_RESPONSE);

        // This example uses Rubicon Server TODO: Rewrite to AWS Server
        PrebidMobile.setPrebidServerAccountId("1001");
        PrebidMobile.setPrebidServerHost(
            Host.createCustomHost("https://prebid-server.rubiconproject.com/openrtb2/auction")
        );

        createAd();
    }

    private void createAd() {
        playerView = new PlayerView(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600);
        getAdWrapperView().addView(playerView, params);

        adUnit = new VideoAdUnit(CONFIG_ID, 640, 480);

        VideoAdUnit.Parameters parameters = new VideoAdUnit.Parameters();
        parameters.setMimes(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));
        parameters.setPlacement(Signals.Placement.InStream);
        adUnit.setParameters(parameters);

        adUnit.fetchDemand((resultCode, keysMap) -> {
            HashSet<org.prebid.mobile.AdSize> sizes = new HashSet<>();
            sizes.add(new org.prebid.mobile.AdSize(640, 480));
            adsUri = Uri.parse(Util.generateInstreamUriForGam(AD_UNIT_ID, sizes, keysMap));

            ImaAdsLoader.Builder imaBuilder = new ImaAdsLoader.Builder(this);
            adsLoader = imaBuilder.build();

            initializePlayer();
        });
    }

    private void initializePlayer() {
        SimpleExoPlayer.Builder playerBuilder = new SimpleExoPlayer.Builder(this);
        player = playerBuilder.build();
        playerView.setPlayer(player);
        adsLoader.setPlayer(player);

        Uri uri = Uri.parse("https://storage.googleapis.com/gvabox/media/samples/stock.mp4");
//        Uri uri = Uri.parse("<![CDATA[https://storage.googleapis.com/gvabox/media/samples/stock.mp4]]>");
        MediaItem mediaItem = MediaItem.fromUri(uri);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, getString(R.string.app_name));
        ProgressiveMediaSource.Factory mediaSourceFactory = new ProgressiveMediaSource.Factory(dataSourceFactory);
        MediaSource mediaSource = mediaSourceFactory.createMediaSource(mediaItem);

        DataSpec dataSpec = new DataSpec(adsUri);
        AdsMediaSource adsMediaSource = new AdsMediaSource(mediaSource, dataSpec, "ad", mediaSourceFactory, adsLoader, playerView);

        player.setMediaSource(adsMediaSource);
        player.setPlayWhenReady(true);
        player.prepare();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (adUnit != null) {
            adUnit.stopAutoRefresh();
        }
        if (adsLoader != null) {
            adsLoader.setPlayer(null);
            adsLoader.release();
        }
        if (playerView != null) {
            playerView.setPlayer(null);
        }
        if (player != null) {
            player.release();
        }

        // TODO: Return to AWS Server
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d");
        PrebidMobile.setPrebidServerHost(
            Host.createCustomHost(
                "https://prebid-server-test-j.prebid.org/openrtb2/auction"
            )
        );
        PrebidMobile.setStoredAuctionResponse(null);
    }
}
