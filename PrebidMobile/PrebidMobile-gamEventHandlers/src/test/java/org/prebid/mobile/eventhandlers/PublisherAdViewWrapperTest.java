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
import android.view.View;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.eventhandlers.global.Constants;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PublisherAdViewWrapperTest {
    private PublisherAdViewWrapper mPublisherAdViewWrapper;

    @Mock
    GamAdEventListener mMockListener;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        Context context = Robolectric.buildActivity(Activity.class).create().get();

        mPublisherAdViewWrapper = PublisherAdViewWrapper.newInstance(context, "124", mMockListener, new AdSize(300, 250));
    }

    @Test
    public void newInstance_WithNullContext_NullValueReturned() {
        PublisherAdViewWrapper publisherAdViewWrapper = PublisherAdViewWrapper
            .newInstance(null, "124", mMockListener, new AdSize(300, 250));

        assertNull(publisherAdViewWrapper);
    }

    @Test
    public void onAppEvent_WithValidNameAndExpectedAppEvent_NotifyAppEventListener() {
        mPublisherAdViewWrapper.onAppEvent(Constants.APP_EVENT, "");

        verify(mMockListener, times(1)).onEvent(AdEvent.APP_EVENT_RECEIVED);
    }

    @Test
    public void onAppEvent_WithInvalidNameAndExpectedAppEvent_DoNothing() {
        mPublisherAdViewWrapper.onAppEvent("test", "");

        verifyZeroInteractions(mMockListener);
    }

    @Test
    public void onGamAdClosed_NotifyBannerEventCloseListener() {
        mPublisherAdViewWrapper.onAdClosed();

        verify(mMockListener, times(1)).onEvent(eq(AdEvent.CLOSED));
    }

    @Test
    public void onGamAdFailedToLoad_NotifyBannerEventErrorListener() {
        final int wantedNumberOfInvocations = 10;

        for (int i = 0; i < wantedNumberOfInvocations; i++) {
            LoadAdError loadAdError = new LoadAdError(i, "", "", null, null);
            mPublisherAdViewWrapper.onAdFailedToLoad(loadAdError);
        }
        verify(mMockListener, times(wantedNumberOfInvocations)).onEvent(eq(AdEvent.FAILED));
    }

    @Test
    public void onGamAdOpened_NotifyBannerEventClickedListener() {
        mPublisherAdViewWrapper.onAdOpened();

        verify(mMockListener, times(1)).onEvent(AdEvent.CLICKED);
    }

    @Test
    public void onGamAdLoadedAppEventExpected_NotifyLoadedListener() {
        mPublisherAdViewWrapper.onAdLoaded();

        verify(mMockListener, times(1)).onEvent(AdEvent.LOADED);
    }

    @Test
    public void getView_ReturnGamView() throws IllegalAccessException {
        final Activity activity = Robolectric.buildActivity(Activity.class).create().get();
        AdManagerAdView publisherAdView = new AdManagerAdView(activity);

        WhiteBox.field(PublisherAdViewWrapper.class, "mAdView").set(mPublisherAdViewWrapper, publisherAdView);

        final View view = mPublisherAdViewWrapper.getView();

        assertEquals(publisherAdView, view);
    }
}