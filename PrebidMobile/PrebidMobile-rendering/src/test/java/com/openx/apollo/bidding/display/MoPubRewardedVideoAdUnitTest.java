package com.openx.apollo.bidding.display;

import android.app.Activity;
import android.content.Context;

import com.apollo.test.utils.ResourceUtils;
import com.apollo.test.utils.WhiteBox;
import com.mopub.mobileads.MoPubView;
import com.openx.apollo.bidding.data.FetchDemandResult;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.listeners.OnFetchCompleteListener;
import com.openx.apollo.bidding.loader.BidLoader;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.sdk.ApolloSettings;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MoPubRewardedVideoAdUnitTest {

    private Context mContext;
    private MoPubRewardedVideoAdUnit mMopubRewardedAdUnit;

    @Before
    public void setUp() throws Exception {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        ApolloSettings.setAccountId("id");
        mMopubRewardedAdUnit = new MoPubRewardedVideoAdUnit(mContext, "mopub", "config");
        WhiteBox.setInternalState(mMopubRewardedAdUnit, "mBidLoader", mock(BidLoader.class));
    }

    @After
    public void cleanup() {
        ApolloSettings.setAccountId(null);
    }

    @Test
    public void whenIsMopubViewAndHashMapPassed_ReturnTrue() {
        assertTrue(mMopubRewardedAdUnit.isAdObjectSupported(new HashMap<String, String>()));
    }

    @Test
    public void whenIsMopubViewAndAnyObjectPassed_ReturnFalse() {
        MoPubView moPubView = new MoPubView(mContext);
        assertFalse(mMopubRewardedAdUnit.isAdObjectSupported(moPubView));
    }

    @Test
    public void whenInitAdConfig_PrepareAdConfigForInterstitial() {
        mMopubRewardedAdUnit.initAdConfig("config", null);
        AdConfiguration adConfiguration = mMopubRewardedAdUnit.mAdUnitConfig;
        assertEquals("config", adConfiguration.getConfigId());
        assertEquals(AdConfiguration.AdUnitIdentifierType.VAST, adConfiguration.getAdUnitIdentifierType());
        assertTrue(adConfiguration.isRewarded());
    }

    @Test
    public void whenOnResponseReceived_UpdateHashMapAndBidCache() throws IOException {
        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json");
        BidResponse bidResponse = new BidResponse(responseString);
        OnFetchCompleteListener mockListener = mock(OnFetchCompleteListener.class);
        HashMap<String, String> keywords = new HashMap<>();

        mMopubRewardedAdUnit.fetchDemand(keywords, mockListener);
        mMopubRewardedAdUnit.onResponseReceived(bidResponse);
        assertNotNull(BidResponseCache.getInstance().popBidResponse("mopub"));
        assertFalse(keywords.isEmpty());
        verify(mockListener).onComplete(FetchDemandResult.SUCCESS);
    }
}