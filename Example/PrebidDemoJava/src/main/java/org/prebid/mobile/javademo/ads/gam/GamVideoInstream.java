package org.prebid.mobile.javademo.ads.gam;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.Uri;
import android.view.ViewGroup;
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
import org.prebid.mobile.AdSize;
import org.prebid.mobile.Signals;
import org.prebid.mobile.Util;
import org.prebid.mobile.VideoAdUnit;
import org.prebid.mobile.javademo.R;

import java.util.Collections;
import java.util.HashSet;

@SuppressLint("StaticFieldLeak")
public class GamVideoInstream {

    private static VideoAdUnit adUnit;
    private static SimpleExoPlayer player;
    private static Uri adsUri;
    private static ImaAdsLoader adsLoader;
    private static PlayerView playerView;

    public static void create(
        ViewGroup wrapper,
        String adUnitId,
        String configId
    ) {
        playerView = new PlayerView(wrapper.getContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 600);
        wrapper.addView(playerView, params);

        VideoAdUnit.Parameters parameters = new VideoAdUnit.Parameters();
        parameters.setMimes(Collections.singletonList("video/mp4"));
        parameters.setProtocols(Collections.singletonList(Signals.Protocols.VAST_2_0));
        parameters.setPlaybackMethod(Collections.singletonList(Signals.PlaybackMethod.AutoPlaySoundOff));
        parameters.setPlacement(Signals.Placement.InStream);

        adUnit = new VideoAdUnit(configId, 640, 480);
        adUnit.setParameters(parameters);
        adUnit.fetchDemand((resultCode, keysMap) -> {
            HashSet<AdSize> sizes = new HashSet<>();
            sizes.add(new AdSize(640, 480));
            adsUri = Uri.parse(Util.generateInstreamUriForGam(adUnitId, sizes, keysMap));

            ImaAdsLoader.Builder imaBuilder = new ImaAdsLoader.Builder(wrapper.getContext());
            adsLoader = imaBuilder.build();

            initializePlayer(wrapper.getContext());
        });
    }

    public static void destroy() {
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
        if (adsLoader != null) {
            adsLoader.setPlayer(null);
            adsLoader.release();
            adsLoader = null;
        }
        if (playerView != null) {
            playerView.setPlayer(null);
            playerView = null;
        }
        if (player != null) {
            player.release();
            player = null;
        }
        if (adsUri != null) {
            adsUri = null;
        }
    }


    private static void initializePlayer(Context context) {
        SimpleExoPlayer.Builder playerBuilder = new SimpleExoPlayer.Builder(context);
        player = playerBuilder.build();
        playerView.setPlayer(player);
        adsLoader.setPlayer(player);

        Uri uri = Uri.parse("https://storage.googleapis.com/gvabox/media/samples/stock.mp4");
//        Uri uri = Uri.parse("<![CDATA[https://storage.googleapis.com/gvabox/media/samples/stock.mp4]]>");
        MediaItem mediaItem = MediaItem.fromUri(uri);
        DataSource.Factory dataSourceFactory = new DefaultDataSourceFactory(context, context.getString(R.string.app_name));
        ProgressiveMediaSource.Factory mediaSourceFactory = new ProgressiveMediaSource.Factory(dataSourceFactory);
        MediaSource mediaSource = mediaSourceFactory.createMediaSource(mediaItem);

        DataSpec dataSpec = new DataSpec(adsUri);
        AdsMediaSource adsMediaSource = new AdsMediaSource(mediaSource, dataSpec, "ad", mediaSourceFactory, adsLoader, playerView);

        player.setMediaSource(adsMediaSource);
        player.setPlayWhenReady(true);
        player.prepare();
    }

}
