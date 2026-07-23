package org.prebid.mobile.admob;

import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;

import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.test.utils.WhiteBox;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class PrebidRewardedAdapterTest {

    private PrebidRewardedAdapter adapter;
    private MediationRewardedAdCallback rewardedAdCallback;
    private InterstitialController interstitialController;

    @Before
    public void setUp() {
        adapter = new PrebidRewardedAdapter();
        rewardedAdCallback = mock(MediationRewardedAdCallback.class);
        interstitialController = mock(InterstitialController.class);

        WhiteBox.setInternalState(adapter, "rewardedAdCallback", rewardedAdCallback);
        WhiteBox.setInternalState(adapter, "interstitialController", interstitialController);
    }

    @Test
    public void displayRewarded_DoesNotCallVideoLifecycleCallbacks() {
        when(interstitialController.getAdUnitIdentifierType()).thenReturn(AdFormat.INTERSTITIAL);

        adapter.notifyAdDisplayed();
        adapter.notifyAdClosed();

        verify(rewardedAdCallback).reportAdImpression();
        verify(rewardedAdCallback).onAdOpened();
        verify(rewardedAdCallback).onAdClosed();
        verify(rewardedAdCallback, never()).onVideoStart();
        verify(rewardedAdCallback, never()).onVideoComplete();
    }

    @Test
    public void videoRewarded_CallsVideoLifecycleCallbacks() {
        when(interstitialController.getAdUnitIdentifierType()).thenReturn(AdFormat.VAST);

        adapter.notifyAdDisplayed();
        adapter.notifyAdClosed();

        verify(rewardedAdCallback).reportAdImpression();
        verify(rewardedAdCallback).onAdOpened();
        verify(rewardedAdCallback).onVideoStart();
        verify(rewardedAdCallback).onVideoComplete();
        verify(rewardedAdCallback).onAdClosed();
    }

    @Test
    public void userEarnedReward_WhenInterstitialControllerIsNull_NotifiesRewardCallback() {
        WhiteBox.setInternalState(adapter, "interstitialController", null);

        adapter.notifyUserEarnedReward();

        verify(rewardedAdCallback).onUserEarnedReward();
    }

}
