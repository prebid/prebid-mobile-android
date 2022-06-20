/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.eventhandlers;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;
import static org.prebid.mobile.eventhandlers.global.Constants.APP_EVENT;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class RewardedAdWrapperTest {
    private RewardedAdWrapper rewardedAdWrapper;

    @Mock GamAdEventListener mockListener;

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();


    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Context context = Robolectric.buildActivity(Activity.class).create().get();

        rewardedAdWrapper = RewardedAdWrapper.newInstance(context, "123", mockListener);
    }

    @Test
    public void newInstance_WithNullContext_NullValueReturned() {
        RewardedAdWrapper rewardedAdWrapper = RewardedAdWrapper.newInstance(null, "124", mockListener);

        assertNull(rewardedAdWrapper);
    }

    @Test
    public void onAdMetadataChanged_WithMetadataContainsAdEvent_NotifyAppEventListener() {
        final RewardedAdLoadCallback rewardedAdLoadCallback = getRewardedAdLoadCallback();
        final RewardedAd mockRewardedAd = mock(RewardedAd.class);
        final Bundle bundle = new Bundle();
        bundle.putString(RewardedAdWrapper.KEY_METADATA, APP_EVENT);
        when(mockRewardedAd.getAdMetadata()).thenReturn(bundle);

        rewardedAdLoadCallback.onAdLoaded(mockRewardedAd);

        verify(mockListener, times(1)).onEvent(eq(AdEvent.APP_EVENT_RECEIVED));
    }

    @Test
    public void onAdMetadataChangedMetadata_ContainsNoAdEvent_DoNothing() {
        final RewardedAdLoadCallback rewardedAdLoadCallback = getRewardedAdLoadCallback();
        final RewardedAd mockRewardedAd = mock(RewardedAd.class);
        final Bundle bundle = new Bundle();
        when(mockRewardedAd.getAdMetadata()).thenReturn(bundle);

        rewardedAdLoadCallback.onAdLoaded(mockRewardedAd);

        verify(mockListener, times(0)).onEvent(eq(AdEvent.APP_EVENT_RECEIVED));
    }

    @Test
    public void onGamAdClosed_NotifyEventCloseListener() {
        rewardedAdWrapper.onAdDismissedFullScreenContent();

        verify(mockListener, times(1)).onEvent(AdEvent.CLOSED);
    }

    @Test
    public void onGamAdFailedToLoad_NotifyEventErrorListener() {
        final int wantedNumberOfInvocations = 10;

        for (int i = 0; i < wantedNumberOfInvocations; i++) {
            AdError adError = new AdError(i, "", "");
            rewardedAdWrapper.onAdFailedToShowFullScreenContent(adError);
        }
        verify(mockListener, times(wantedNumberOfInvocations)).onEvent(eq(AdEvent.FAILED));
    }

    @Test
    public void onGamAdOpened_NotifyBannerEventDisplayListener() {
        rewardedAdWrapper.onAdShowedFullScreenContent();

        verify(mockListener, times(1)).onEvent(AdEvent.DISPLAYED);
    }

    @Test
    public void onUserEarnedReward_NotifyClosedAndRewardEarnedListeners() {
        rewardedAdWrapper.onUserEarnedReward(null);

        verify(mockListener, times(1)).onEvent(eq(AdEvent.REWARD_EARNED));
        verify(mockListener, times(1)).onEvent(eq(AdEvent.CLOSED));
    }

    @Test
    public void onGamAdLoadedAppEventExpected_ScheduleAppEventHandler() {
        getRewardedAdLoadCallback().onAdLoaded(mock(RewardedAd.class));

        verify(mockListener, times(1)).onEvent(eq(AdEvent.LOADED));
    }

    @Test
    public void isLoaded_adIsNull_ReturnFalse() {
        assertFalse(rewardedAdWrapper.isLoaded());
    }

    @Test
    public void isLoaded_adIsNonNull_ReturnTrue() {
        getRewardedAdLoadCallback().onAdLoaded(mock(RewardedAd.class));

        assertTrue(rewardedAdWrapper.isLoaded());
    }

    private RewardedAdLoadCallback getRewardedAdLoadCallback() {
        return WhiteBox.getInternalState(rewardedAdWrapper, "rewardedAdLoadCallback");
    }
}