package org.prebid.mobile.app;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.mopub.common.MediationSettings;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.mopub.mobileads.MoPubRewardedVideos;
import org.prebid.mobile.Util;
import org.prebid.mobile.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class RubiconRewardedVideoMoPubDemoActivity extends AppCompatActivity implements MoPubRewardedVideoListener {
    AdUnit adUnit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_RUBICON);
        PrebidMobile.setStoredAuctionResponse(Constants.PBS_STORED_RESPONSE_VAST_RUBICON);
        VideoBaseAdUnit.Parameters parameters = new VideoBaseAdUnit.Parameters();
        parameters.setMimes(Arrays.asList("video/mp4"));

        parameters.setProtocols(Arrays.asList(Signals.Protocols.VAST_2_0));
        // parameters.setProtocols(Arrays.asList(new Signals.Protocols(2)));

        parameters.setPlaybackMethod(Arrays.asList(Signals.PlaybackMethod.AutoPlaySoundOff));
        // parameters.setPlaybackMethod(Arrays.asList(new Signals.PlaybackMethod(2)));

        RewardedVideoAdUnit adUnit = new RewardedVideoAdUnit("1001-1");
        adUnit.setParameters(parameters);

        this.adUnit = adUnit;
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(Constants.MP_ADUNITID_REWARDED)
                .build();
        MoPub.initializeSdk(this, sdkConfiguration, null);

        MoPubRewardedVideos.setRewardedVideoListener(this);
        final Map<String, String> keywordsMap = new HashMap<>();
        adUnit.fetchDemand(keywordsMap, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {

                MoPubRewardedVideoManager.RequestParameters parameters = new MoPubRewardedVideoManager.RequestParameters(Util.convertMapToMoPubKeywords(keywordsMap));
                MoPubRewardedVideos.loadRewardedVideo(Constants.MP_ADUNITID_REWARDED, parameters, (MediationSettings) null);
            }
        });

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
    }

    @Override
    public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
        if (MoPubRewardedVideos.hasRewardedVideo(Constants.MP_ADUNITID_REWARDED)) {
            MoPubRewardedVideos.showRewardedVideo(Constants.MP_ADUNITID_REWARDED);
        }
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
        LogUtil.debug("onRewardedVideoLoadFailure:" + errorCode);
    }

    @Override
    public void onRewardedVideoStarted(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {

    }

    @Override
    public void onRewardedVideoClicked(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoClosed(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {

    }
}
