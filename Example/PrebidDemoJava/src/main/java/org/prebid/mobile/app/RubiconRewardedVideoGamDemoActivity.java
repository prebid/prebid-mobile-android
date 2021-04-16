package org.prebid.mobile.app;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.RewardedVideoAdUnit;
import org.prebid.mobile.Signals;
import org.prebid.mobile.VideoBaseAdUnit;

import java.util.Arrays;

public class RubiconRewardedVideoGamDemoActivity extends AppCompatActivity {
    AdUnit adUnit;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
    }

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
        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        adUnit.fetchDemand(builder, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {

                AdManagerAdRequest request = builder.build();

                RewardedAd.load(
                        RubiconRewardedVideoGamDemoActivity.this,
                        Constants.DFP_REWARDED_ADUNIT_ID_RUBICON,
                        request,
                        new RewardedAdLoadCallback() {
                            @Override
                            public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                                super.onAdLoaded(rewardedAd);
                                rewardedAd.show(RubiconRewardedVideoGamDemoActivity.this, new OnUserEarnedRewardListener() {
                                    @Override
                                    public void onUserEarnedReward(@NonNull RewardItem rewardItem) {

                                    }
                                });
                            }

                            @Override
                            public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                                super.onAdFailedToLoad(loadAdError);
                            }
                        });
            }
        });
    }

    /*
    private void showAdPreGAMv20() {
        final RewardedAd amRewardedAd = new RewardedAd(this, Constants.DFP_REWARDED_ADUNIT_ID_RUBICON);
        final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        adUnit.fetchDemand(builder, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {

                PublisherAdRequest request = builder.build();
                amRewardedAd.loadAd(request, new RewardedAdLoadCallback() {
                    @Override
                    public void onRewardedAdLoaded() {
                        // Ad successfully loaded.

                        if (amRewardedAd.isLoaded()) {
                            amRewardedAd.show(RubiconRewardedVideoGamDemoActivity.this, new RewardedAdCallback() {
                                @Override
                                public void onRewardedAdOpened() {
                                    // Ad opened.
                                }

                                @Override
                                public void onRewardedAdClosed() {
                                    // Ad closed.
                                }

                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem reward) {
                                    // User earned reward.
                                }

                                @Override
                                public void onRewardedAdFailedToShow(int errorCode) {
                                    // Ad failed to display.
                                }
                            });
                        }
                    }

                    @Override
                    public void onRewardedAdFailedToLoad(int errorCode) {
                        // Ad failed to load.
                    }
                });
            }
        });
    }
     */
}
