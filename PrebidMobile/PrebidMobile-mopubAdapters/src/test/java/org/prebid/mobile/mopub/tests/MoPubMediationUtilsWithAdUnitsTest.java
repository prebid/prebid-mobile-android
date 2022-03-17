package org.prebid.mobile.mopub.tests;

import android.app.Activity;
import android.content.Context;
import com.mopub.mediation.MoPubBannerMediationUtils;
import com.mopub.mediation.MoPubRewardedVideoMediationUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.mopub.mock.OpenMediationBaseAdUnit;
import org.prebid.mobile.mopub.mock.OpenMediationRewardedVideoAdUnit;
import org.prebid.mobile.mopub.mock.TestResponse;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;

import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MoPubMediationUtilsWithAdUnitsTest {

    private Context context;

    @Mock
    private AdSize mockAdSize;
    @Mock
    private BidLoader mMockBidLoader;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        context = Robolectric.buildActivity(Activity.class).create().get();

        PrebidMobile.setPrebidServerAccountId("id");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
    }

    @Test
    public void whenOnResponseReceived_UpdateMoPubAndSuccessResult() {
        String testConfigId = "configId";
        String responseString = TestResponse.getResponse();
        HashMap<String, String> keywordsMap = TestResponse.getKeywordsMap();
        BidResponse bidResponse = new BidResponse(responseString);
        OnFetchCompleteListener mockListener = mock(OnFetchCompleteListener.class);

        MoPubBannerMediationUtils bannerUtils = mock(MoPubBannerMediationUtils.class);
        OpenMediationBaseAdUnit adUnit = new OpenMediationBaseAdUnit(context, testConfigId, mockAdSize, bannerUtils);
        WhiteBox.setInternalState(adUnit, "mBidLoader", mMockBidLoader);

        adUnit.fetchDemand(mockListener);
        adUnit.onResponseReceived(bidResponse);

        verify(mockListener).onComplete(FetchDemandResult.SUCCESS);
        verify(bannerUtils).setResponseToLocalExtras(bidResponse);
        verify(bannerUtils).handleKeywordsUpdate(keywordsMap);
        assertNotNull(BidResponseCache.getInstance().popBidResponse(bidResponse.getId()));
    }

    @Test
    public void whenOnResponseReceived_UpdateHashMapAndBidCache() {
        String responseString = TestResponse.getResponse();
        HashMap<String, String> keywordsMap = TestResponse.getKeywordsMap();
        BidResponse bidResponse = new BidResponse(responseString);
        OnFetchCompleteListener mockListener = mock(OnFetchCompleteListener.class);

        MoPubRewardedVideoMediationUtils mediationUtils = mock(MoPubRewardedVideoMediationUtils.class);
        OpenMediationRewardedVideoAdUnit adUnit = new OpenMediationRewardedVideoAdUnit(
                context,
                "mopub",
                mediationUtils
        );
        WhiteBox.setInternalState(adUnit, "mBidLoader", mMockBidLoader);
        adUnit.fetchDemand(mockListener);
        adUnit.onResponse(bidResponse);

        verify(mockListener).onComplete(FetchDemandResult.SUCCESS);
        verify(mediationUtils).handleKeywordsUpdate(keywordsMap);
    }

}