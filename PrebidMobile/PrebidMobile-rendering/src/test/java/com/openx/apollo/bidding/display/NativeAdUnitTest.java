package com.openx.apollo.bidding.display;

import android.content.Context;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.bidding.data.AdSize;
import com.openx.apollo.bidding.data.FetchDemandResult;
import com.openx.apollo.bidding.data.NativeFetchDemandResult;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.listeners.OnNativeFetchCompleteListener;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.ntv.NativeAdConfiguration;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

public class NativeAdUnitTest {

    @Mock
    private Context mMockContext;
    @Mock
    private OnNativeFetchCompleteListener mMockListener;
    @Mock
    private BidResponse mMockBidResponse;

    private NativeAdUnit mSpyNativeAdUnit;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        mSpyNativeAdUnit = spy(new NativeAdUnit(mMockContext, "123", new NativeAdConfiguration()));
    }

    @Test
    public void initAdConfig_AdUnitIsFilledWithExpectedFields() {
        String expectedId = "1234";
        mSpyNativeAdUnit.initAdConfig(expectedId, new AdSize(10, 10));

        AdConfiguration adUnitConfig = mSpyNativeAdUnit.mAdUnitConfig;

        assertEquals(expectedId, adUnitConfig.getConfigId());
        assertTrue(adUnitConfig.getAdSizes().isEmpty());
        assertEquals(AdConfiguration.AdUnitIdentifierType.NATIVE, adUnitConfig.getAdUnitIdentifierType());
    }

    @Test
    public void isAdObjectSupported_anyObject_ReturnTrue() {
        assertTrue(mSpyNativeAdUnit.isAdObjectSupported(null));
        assertTrue(mSpyNativeAdUnit.isAdObjectSupported(new Object()));
    }

    @Test
    public void onResponseReceived_withNullListener_DoNothing() {
        mSpyNativeAdUnit.onResponseReceived(mMockBidResponse);

        verifyZeroInteractions(mMockListener);
    }

    @Test
    public void onResponseReceived_withValidListener_SaveBidResponseAndNotifyListener() {
        WhiteBox.setInternalState(mSpyNativeAdUnit, "mNativeFetchCompleteListener", mMockListener);
        mSpyNativeAdUnit.onResponseReceived(mMockBidResponse);

        verify(mMockListener, times(1)).onComplete(any(NativeFetchDemandResult.class));
    }

    @Test
    public void onErrorReceived_withNullListener_DoNothing() {
        mSpyNativeAdUnit.onErrorReceived(new AdException("err", "orr"));

        verifyZeroInteractions(mMockListener);
    }

    @Test
    public void onErrorReceived_withValidListener_SaveBidResponseAndNotifyListener() {
        WhiteBox.setInternalState(mSpyNativeAdUnit, "mNativeFetchCompleteListener", mMockListener);

        mSpyNativeAdUnit.onErrorReceived(new AdException("Timeout", "Timeout"));

        verify(mMockListener, times(1)).onComplete(eq(new NativeFetchDemandResult(FetchDemandResult.TIMEOUT)));
    }
}