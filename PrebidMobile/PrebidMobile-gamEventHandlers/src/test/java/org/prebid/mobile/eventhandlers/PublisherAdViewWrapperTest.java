package org.prebid.mobile.eventhandlers;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.apollo.test.utils.WhiteBox;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.eventhandlers.global.Constants;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

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
            mPublisherAdViewWrapper.onAdFailedToLoad(i);
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
        PublisherAdView publisherAdView = new PublisherAdView(activity);

        WhiteBox.field(PublisherAdViewWrapper.class, "mPublisherAdView").set(mPublisherAdViewWrapper, publisherAdView);

        final View view = mPublisherAdViewWrapper.getView();

        assertEquals(publisherAdView, view);
    }
}