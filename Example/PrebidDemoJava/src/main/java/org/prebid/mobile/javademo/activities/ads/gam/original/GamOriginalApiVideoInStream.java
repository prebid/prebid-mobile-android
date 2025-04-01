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

import org.prebid.mobile.InStreamVideoAdUnit;
import org.prebid.mobile.Signals;
import org.prebid.mobile.Util;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.javademo.R;
import org.prebid.mobile.javademo.activities.BaseAdActivity;

import java.util.Collections;
import java.util.HashSet;

public class GamOriginalApiVideoInStream extends BaseAdActivity {

    private static final String AD_UNIT_ID = "/21808260008/prebid_demo_app_instream";
    private static final String CONFIG_ID = "prebid-demo-video-interstitial-320-480-original-api";

    private static final int WIDTH = 640;
    private static final int HEIGHT = 480;

    private static final String VIDEO_URL = "https://storage.googleapis.com/gvabox/media/samples/stock.mp4";

    private InStreamVideoAdUnit adUnit;
    private SimpleExoPlayer player;
    private Uri adsUri;
    private ImaAdsLoader adsLoader;
    private PlayerView playerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        createAd();
    }

    private void createAd() {
        playerView = new PlayerView(this);
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600);
        getAdWrapperView().addView(playerView, params);

        adUnit = new InStreamVideoAdUnit(CONFIG_ID, WIDTH, HEIGHT);

        VideoParameters parameters = new VideoParameters(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));
        parameters.setPlacement(Signals.Placement.InStream);
        parameters.setPlcmt(Signals.Plcmt.InStream);
        adUnit.setVideoParameters(parameters);

        adUnit.fetchDemand((bidInfo) -> {
            HashSet<org.prebid.mobile.AdSize> sizes = new HashSet<>();
            sizes.add(new org.prebid.mobile.AdSize(WIDTH, HEIGHT));
            adsUri = Uri.parse(Util.generateInstreamUriForGam(AD_UNIT_ID, sizes, bidInfo.getTargetingKeywords()));

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

        Uri uri = Uri.parse(VIDEO_URL);
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
            adUnit.destroy();
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
    }
}
