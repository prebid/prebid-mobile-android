package org.prebid.mobile.app;

import android.net.Uri;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
import org.prebid.mobile.Util;
import org.prebid.mobile.*;

import java.util.Collections;
import java.util.HashSet;

public class RubiconInstreamVideoIMADemoActivity extends AppCompatActivity {

    VideoAdUnit adUnit;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private ImaAdsLoader adsLoader;
    private Uri adsUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instream_video);
        playerView = findViewById(R.id.player_view);

        initPrebid();
        initVideoAdUnit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
        adsLoader.release();
    }

    private void initPrebid() {
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_RUBICON);
        PrebidMobile.setStoredAuctionResponse(Constants.PBS_STORED_RESPONSE_VAST_RUBICON);
    }

    private void initVideoAdUnit() {
        VideoAdUnit.Parameters parameters = new VideoAdUnit.Parameters();
        parameters.setMimes(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));
        parameters.setPlacement(Signals.Placement.InStream);

        adUnit = new VideoAdUnit("1001-1", 640, 480);
        adUnit.setParameters(parameters);
        adUnit.fetchDemand((resultCode, keysMap) -> {
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(640, 480));
            adsUri = Uri.parse(Util.generateInstreamUriForGam(Constants.DFP_VAST_ADUNIT_ID_RUBICON, sizes, keysMap));

            ImaAdsLoader.Builder imaBuilder = new ImaAdsLoader.Builder(RubiconInstreamVideoIMADemoActivity.this);
            adsLoader = imaBuilder.build();

            initializePlayer();
        });
    }

    private void releasePlayer() {
        if (adsLoader != null) {
            adsLoader.setPlayer(null);
        }
        if (playerView != null) {
            playerView.setPlayer(null);
        }
        if (player != null) {
            player.release();
            player = null;
        }
    }

    private void initializePlayer() {
        SimpleExoPlayer.Builder playerBuilder = new SimpleExoPlayer.Builder(this);
        player = playerBuilder.build();
        playerView.setPlayer(player);
        adsLoader.setPlayer(player);

        Uri uri = Uri.parse(getString(R.string.content_url));
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
    public void onStart() {
        super.onStart();
        if (android.os.Build.VERSION.SDK_INT > 23) {
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (android.os.Build.VERSION.SDK_INT <= 23 || player == null) {
            if (playerView != null) {
                playerView.onResume();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (android.os.Build.VERSION.SDK_INT <= 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (android.os.Build.VERSION.SDK_INT > 23) {
            if (playerView != null) {
                playerView.onPause();
            }
            releasePlayer();
        }
    }
}
