package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.content.Context;

import com.apollo.test.utils.WhiteBox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class InterstitialViewTest {
    private InterstitialView mSpyBidInterstitialView;
    @Mock
    private AdViewManager mMockAdViewManager;

    @Before
    public void setup() throws AdException, IllegalAccessException {
        MockitoAnnotations.initMocks(this);

        Context context = Robolectric.buildActivity(Activity.class).create().get();

        mSpyBidInterstitialView = spy(new InterstitialView(context));

        when(mMockAdViewManager.getAdConfiguration()).thenReturn(mock(AdConfiguration.class));
        WhiteBox.field(InterstitialView.class, "mAdViewManager").set(mSpyBidInterstitialView, mMockAdViewManager);
    }

    @Test
    public void loadAd_ExecuteBidTransactionLoad() {
        AdConfiguration mockAdUnitConfiguration = mock(AdConfiguration.class);
        BidResponse mockBidResponse = mock(BidResponse.class);

        mSpyBidInterstitialView.loadAd(mockAdUnitConfiguration, mockBidResponse);

        verify(mMockAdViewManager, times(1)).loadBidTransaction(eq(mockAdUnitConfiguration), eq(mockBidResponse));
    }

    @Test
    public void setInterstitialViewListener_ExecuteAddEventListener() {
        final InterstitialViewListener mockInterstitialViewListener = mock(InterstitialViewListener.class);

        mSpyBidInterstitialView.setInterstitialViewListener(mockInterstitialViewListener);

        verify(mSpyBidInterstitialView, times(1)).setInterstitialViewListener(eq(mockInterstitialViewListener));
    }
}