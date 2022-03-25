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
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.eventhandlers.global.Constants;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.*;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PublisherInterstitialAdWrapperTest {
    private PublisherInterstitialAdWrapper mPublisherInterstitialAdWrapper;

    @Mock
    GamAdEventListener mMockListener;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Activity context = Robolectric.buildActivity(Activity.class).create().get();

        mPublisherInterstitialAdWrapper = PublisherInterstitialAdWrapper.newInstance(context, "123", mMockListener);
    }

    @Test
    public void newInstance_WithNullContext_NullValueReturned() {
        PublisherInterstitialAdWrapper publisherInterstitialAdWrapper = PublisherInterstitialAdWrapper
            .newInstance(null, "124", mMockListener);

        assertNull(publisherInterstitialAdWrapper);
    }

    @Test
    public void onAppEvent_WithValidNameAndExpectedAppEvent_NotifyAppEventListener() {
        mPublisherInterstitialAdWrapper.onAppEvent(Constants.APP_EVENT, "");

        verify(mMockListener, times(1)).onEvent(AdEvent.APP_EVENT_RECEIVED);
    }

    @Test
    public void onAppEvent_WithInvalidNameAndExpectedAppEvent_DoNothing() {
        mPublisherInterstitialAdWrapper.onAppEvent("test", "");

        verifyZeroInteractions(mMockListener);
    }

    @Test
    public void onGamAdClosed_NotifyEventCloseListener() {
        mPublisherInterstitialAdWrapper.onAdDismissedFullScreenContent();

        verify(mMockListener, times(1)).onEvent(eq(AdEvent.CLOSED));
    }

    @Test
    public void onGamAdFailedToLoad_NotifyEventErrorListener() {
        final int wantedNumberOfInvocations = 10;

        for (int i = 0; i < wantedNumberOfInvocations; i++) {
            AdError adError = new AdError(i, "", "");
            mPublisherInterstitialAdWrapper.onAdFailedToShowFullScreenContent(adError);
        }
        verify(mMockListener, times(wantedNumberOfInvocations)).onEvent(eq(AdEvent.FAILED));
    }

    @Test
    public void onGamAdOpened_NotifyEventDisplayedListener() {
        mPublisherInterstitialAdWrapper.onAdShowedFullScreenContent();

        verify(mMockListener, times(1)).onEvent(AdEvent.DISPLAYED);
    }

    @Test
    public void onGamAdLoadedAppEventExpected_NotifyLoadedListener() {
        final AdManagerInterstitialAdLoadCallback listener = getAdLoadCallback();

        final AdManagerInterstitialAd mock = mock(AdManagerInterstitialAd.class);
        listener.onAdLoaded(mock);

        verify(mMockListener, times(1)).onEvent(AdEvent.LOADED);
    }

    @Test
    public void isLoaded_adIsNull_ReturnFalse() {
        assertFalse(mPublisherInterstitialAdWrapper.isLoaded());
    }

    @Test
    public void isLoaded_adIsNonNull_ReturnTrue() {
        getAdLoadCallback().onAdLoaded(mock(AdManagerInterstitialAd.class));

        assertTrue(mPublisherInterstitialAdWrapper.isLoaded());
    }

    private AdManagerInterstitialAdLoadCallback getAdLoadCallback() {
        return WhiteBox.getInternalState(mPublisherInterstitialAdWrapper, "mAdLoadCallback");
    }
}