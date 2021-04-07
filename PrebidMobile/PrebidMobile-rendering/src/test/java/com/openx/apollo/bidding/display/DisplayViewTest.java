package com.openx.apollo.bidding.display;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.listeners.DisplayViewListener;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.AdDetails;
import com.openx.apollo.views.AdViewManager;
import com.openx.apollo.views.AdViewManagerListener;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class DisplayViewTest {

    private DisplayView mDisplayView;
    private Context mContext;
    private AdConfiguration mAdUnitConfiguration;
    @Mock
    private BidResponse mBidResponse;
    @Mock
    private DisplayViewListener mMockDisplayViewListener;
    @Mock
    private AdViewManager mMockAdViewManager;

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(DisplayViewTest.this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();

        mAdUnitConfiguration = new AdConfiguration();
        mAdUnitConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);

        BidResponse mockResponse = mock(BidResponse.class);
        Bid mockBid = mock(Bid.class);
        when(mockBid.getAdm()).thenReturn("adm");
        when(mockResponse.getWinningBid()).thenReturn(mockBid);

        mDisplayView = new DisplayView(mContext, mMockDisplayViewListener, mAdUnitConfiguration, mockResponse);
        reset(mMockDisplayViewListener);
    }

    @Test
    public void whenDisplayAd_LoadBidTransaction() {
        Assert.assertNotNull(WhiteBox.getInternalState(mDisplayView, "mAdViewManager"));
    }

    @Test
    public void whenAdViewManagerListenerAdLoaded_NotifyListenerOnAdLoaded()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.adLoaded(mock(AdDetails.class));
        verify(mMockDisplayViewListener).onAdLoaded();
    }

    @Test
    public void whenAdViewManagerListenerViewReadyForImmediateDisplay_NotifyListenerOnAdDisplayed()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.viewReadyForImmediateDisplay(mock(View.class));
        verify(mMockDisplayViewListener).onAdDisplayed();
    }

    @Test
    public void whenAdViewManagerListenerFailedToLoad_NotifyListenerOnAdFailed()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.failedToLoad(new AdException(AdException.INTERNAL_ERROR, "Test"));
        verify(mMockDisplayViewListener).onAdFailed(any(AdException.class));
    }

    @Test
    public void whenAdViewManagerListenerCreativeWasClicked_NotifyListenerOnAdClicked()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.creativeClicked("");
        verify(mMockDisplayViewListener).onAdClicked();
    }

    @Test
    public void whenAdViewManagerListenerCreativeInterstitialDidClose_NotifyListenerOnAdClosed()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.creativeInterstitialClosed();
        verify(mMockDisplayViewListener).onAdClosed();
    }

    @Test
    public void whenAdViewManagerListenerCreativeDidCollapse_NotifyListenerOnAdClosed()
    throws IllegalAccessException {
        AdViewManagerListener adViewManagerListener = getAdViewManagerListener();
        adViewManagerListener.creativeCollapsed();
        verify(mMockDisplayViewListener).onAdClosed();
    }

    private AdViewManagerListener getAdViewManagerListener() throws IllegalAccessException {
        return (AdViewManagerListener) WhiteBox.field(DisplayView.class, "mAdViewManagerListener").get(mDisplayView);
    }
}