package com.mopub.mediation;

import android.app.Activity;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.mopub.mock.MockAdView;
import org.prebid.mobile.mopub.mock.OpenMediationBaseAdUnit;
import org.prebid.mobile.mopub.mock.OpenMediationRewardedVideoAdUnit;
import org.prebid.mobile.mopub.mock.TestResponse;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.MediationRewardedVideoAdUnit;
import org.prebid.mobile.rendering.bidding.enums.Host;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MoPubMediationUtilsTest {

    @Mock
    private AdSize mockAdSize;
    @Mock
    private BidLoader mMockBidLoader;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        PrebidRenderingSettings.setAccountId("id");
        PrebidRenderingSettings.setBidServerHost(Host.APPNEXUS);
    }

    @Test
    public void whenOnResponseReceived_UpdateMoPubAndSuccessResult() {
        String responseString = TestResponse.get();
        BidResponse bidResponse = new BidResponse(responseString);
        String keywordsString = "hb_pb:value1,hb_bidder:value2,hb_cache_id:value3,";
        OnFetchCompleteListener mockListener = mock(OnFetchCompleteListener.class);

        MockAdView mediationViewMock = new MockAdView();

        OpenMediationBaseAdUnit adUnit = createAdUnit();
        adUnit.fetchDemand(mediationViewMock, mockListener);
        adUnit.onResponseReceived(bidResponse);

        verify(mockListener).onComplete(FetchDemandResult.SUCCESS);
        assertEquals(keywordsString, mediationViewMock.getKeywords());
        assertNotNull(BidResponseCache.getInstance().popBidResponse(bidResponse.getId()));
    }

    @Test
    public void whenOnResponseReceived_UpdateHashMapAndBidCache() {
        String responseString = TestResponse.get();
        BidResponse bidResponse = new BidResponse(responseString);
        OnFetchCompleteListener mockListener = mock(OnFetchCompleteListener.class);
        HashMap<String, String> keywords = new HashMap<>();

        OpenMediationRewardedVideoAdUnit adUnit = createRewardedAdUnit();
        adUnit.fetchDemand(keywords, mockListener);
        adUnit.onResponse(bidResponse);
        assertNotNull(BidResponseCache.getInstance().popBidResponse("mopub"));
        assertFalse(keywords.isEmpty());
        verify(mockListener).onComplete(FetchDemandResult.SUCCESS);
    }

    private OpenMediationBaseAdUnit createAdUnit() {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        OpenMediationBaseAdUnit adUnit = new OpenMediationBaseAdUnit(context, "configId", mockAdSize, new MoPubMediationUtils());
        WhiteBox.setInternalState(adUnit, "mBidLoader", mMockBidLoader);
        return adUnit;
    }

    private OpenMediationRewardedVideoAdUnit createRewardedAdUnit() {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        OpenMediationRewardedVideoAdUnit adUnit = new OpenMediationRewardedVideoAdUnit(
                context,
                "mopub",
                "config",
                new MoPubMediationUtils()
        );
        WhiteBox.setInternalState(adUnit, "mBidLoader", mMockBidLoader);
        return adUnit;
    }

}