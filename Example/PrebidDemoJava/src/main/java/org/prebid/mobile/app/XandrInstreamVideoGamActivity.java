package org.prebid.mobile.app;

import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Pair;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.ima.ImaAdsLoader;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.ads.AdsMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.AdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.OnCompleteListener2;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Signals;
import org.prebid.mobile.Util;
import org.prebid.mobile.VideoAdUnit;
import org.prebid.mobile.VideoBaseAdUnit;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;

public class XandrInstreamVideoGamActivity extends AppCompatActivity {
    AdUnit adUnit;
    private PlayerView playerView;
    private SimpleExoPlayer player;
    private ImaAdsLoader adsLoader;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
        adsLoader.release();
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_instream_video);
        playerView = findViewById(R.id.player_view);

        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId("aecd6ef7-b992-4e99-9bb8-65e2d984e1dd");
        VideoAdUnit.Parameters parameters = new VideoAdUnit.Parameters();
        parameters.setMimes(Arrays.asList("video/mp4"));

        parameters.setProtocols(Arrays.asList(Signals.Protocols.VAST_2_0));
        // parameters.setProtocols(Arrays.asList(new Signals.Protocols(2)));

        parameters.setPlaybackMethod(Arrays.asList(Signals.PlaybackMethod.AutoPlaySoundOff));
        // parameters.setPlaybackMethod(Arrays.asList(new Signals.PlaybackMethod(2)));

        parameters.setPlacement(Signals.Placement.InStream);
        // parameters.setPlacement(new Signals.Placement(2));

        VideoAdUnit adUnit = new VideoAdUnit("2c0af852-a55d-49dc-a5ca-ef7e141f73cc", 300, 250);
        adUnit.setParameters(parameters);
        this.adUnit = adUnit;
        // Create an AdsLoader with the ad tag url.
        adUnit.fetchDemand(new OnCompleteListener2() {
            @Override
            public void onComplete(ResultCode resultCode, Map<String, String> unmodifiableMap) {
                HashSet<AdSize> sizes = new HashSet<>();
                sizes.add(new AdSize(640, 480));
                String uri = Util.generateInstreamUriForGam("/19968336/Punnaghai_Instream_Video1", sizes, unmodifiableMap);
                adsLoader = new ImaAdsLoader(XandrInstreamVideoGamActivity.this, Uri.parse(uri));
                initializePlayer();
            }
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
        // Create a SimpleExoPlayer and set is as the player for content and ads.
        player = new SimpleExoPlayer.Builder(this).build();
        playerView.setPlayer(player);
        adsLoader.setPlayer(player);

        DataSource.Factory dataSourceFactory =
                new DefaultDataSourceFactory(this, getString(R.string.app_name));

        ProgressiveMediaSource.Factory mediaSourceFactory =
                new ProgressiveMediaSource.Factory(dataSourceFactory);

        // Create the MediaSource for the content you wish to play.
        MediaSource mediaSource =
                mediaSourceFactory.createMediaSource(Uri.parse(getString(R.string.content_url)));

        // Create the AdsMediaSource using the AdsLoader and the MediaSource.
        AdsMediaSource adsMediaSource =
                new AdsMediaSource(mediaSource, dataSourceFactory, adsLoader, playerView);

        // Prepare the content and ad to be played with the SimpleExoPlayer.
        player.prepare(adsMediaSource);

        // Set PlayWhenReady. If true, content and ads autoplay.
        player.setPlayWhenReady(false);
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
