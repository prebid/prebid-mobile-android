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

package org.prebid.mobile.rendering.bidding.display;

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
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
        PrebidRenderingSettings.setAccountId("id");
        mMopubRewardedAdUnit = new MoPubRewardedVideoAdUnit(mContext, "mopub", "config");
        WhiteBox.setInternalState(mMopubRewardedAdUnit, "mBidLoader", mock(BidLoader.class));
    }

    @After
    public void cleanup() {
        PrebidRenderingSettings.setAccountId(null);
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