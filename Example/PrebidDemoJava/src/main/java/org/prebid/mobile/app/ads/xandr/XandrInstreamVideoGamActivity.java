package org.prebid.mobile.app.ads.xandr;

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
import org.prebid.mobile.*;

import java.util.Collections;
import java.util.HashSet;

import static org.prebid.mobile.Util.generateInstreamUriForGam;

public class XandrInstreamVideoGamActivity extends AppCompatActivity {

    VideoAdUnit adUnit;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private ImaAdsLoader adsLoader;
    private Uri adsUri;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(org.prebid.mobile.app.R.layout.activity_instream_video);
        playerView = findViewById(org.prebid.mobile.app.R.id.player_view);

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
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId("aecd6ef7-b992-4e99-9bb8-65e2d984e1dd");
    }

    private void initVideoAdUnit() {
        VideoAdUnit.Parameters parameters = new VideoAdUnit.Parameters();
        parameters.setMimes(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));
        parameters.setPlacement(Signals.Placement.InStream);

        adUnit = new VideoAdUnit("2c0af852-a55d-49dc-a5ca-ef7e141f73cc", 300, 250);
        adUnit.setParameters(parameters);
        adUnit.fetchDemand((resultCode, unmodifiableMap) -> {
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(640, 480));
            adsUri = Uri.parse(generateInstreamUriForGam("/19968336/Punnaghai_Instream_Video1", sizes, unmodifiableMap));

            ImaAdsLoader.Builder imaBuilder = new ImaAdsLoader.Builder(XandrInstreamVideoGamActivity.this);
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

        Uri uri = Uri.parse("<![CDATA[https://storage.googleapis.com/gvabox/media/samples/stock.mp4]]>");
        MediaItem mediaItem = MediaItem.fromUri(uri);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(this, getString(org.prebid.mobile.app.R.string.app_name));
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