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
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.enums.BannerAdPosition;
import org.prebid.mobile.rendering.bidding.enums.Host;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class BaseAdUnitTest {

    private BaseAdUnit mBaseAdUnit;
    private Context mContext;
    private Object mMopubView = new Object();
    @Mock
    private AdSize mMockAdSize;
    @Mock
    private BidLoader mMockBidLoader;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();
        mBaseAdUnit = createAdUnit("config");
        PrebidRenderingSettings.setBidServerHost(Host.APPNEXUS);

        assertEquals(BannerAdPosition.UNDEFINED.getValue(), mBaseAdUnit.mAdUnitConfig.getAdPositionValue());
    }

    @After
    public void cleanup() {
        PrebidRenderingSettings.setAccountId(null);
    }

    @Test
    public void whenFetchDemandAndNotMoPubViewPassed_InvalidAdObjectResult() {
        new BaseAdUnit(mContext, "123", mMockAdSize) {
            @Override
            protected void initAdConfig(String configId, AdSize adSize) {
                mAdUnitConfig.setConfigId(configId);
            }

            @Override
            protected boolean isAdObjectSupported(Object adObject) {
                return false;
            }
        }.fetchDemand(mock(View.class), result -> {
            assertEquals(FetchDemandResult.INVALID_AD_OBJECT, result);
        });
    }

    @Test
    public void whenFetchDemandAndNoAccountId_InvalidAccountIdResult() {
        mBaseAdUnit.fetchDemand(mMopubView, result -> {
            assertEquals(FetchDemandResult.INVALID_ACCOUNT_ID, result);
        });
    }

    @Test
    public void whenFetchDemandAndNoConfigId_InvalidConfigIdResult() {
        PrebidRenderingSettings.setAccountId("id");
        mBaseAdUnit = createAdUnit("");
        mBaseAdUnit.fetchDemand(mMopubView, result -> {
            assertEquals(FetchDemandResult.INVALID_CONFIG_ID, result);
        });
    }

    @Test
    public void whenFetchDemandAndNoUrlForCustomHost_InvalidHostUrl() {
        PrebidRenderingSettings.setAccountId("id");
        final Host custom = Host.CUSTOM;
        custom.setHostUrl("");
        PrebidRenderingSettings.setBidServerHost(custom);
        mBaseAdUnit = createAdUnit("123");
        mBaseAdUnit.fetchDemand(mMopubView, result -> {
            assertEquals(FetchDemandResult.INVALID_HOST_URL, result);
        });
    }

    @Test
    public void whenFetchDemandAndEverythingOK_BidLoaderLoadCalled() {
        PrebidRenderingSettings.setAccountId("id");
        mBaseAdUnit.fetchDemand(mMopubView, result -> {
        });
        verify(mMockBidLoader).load();
    }

    @Test
    public void whenDestroy_DestroyBidLoader() {
        mBaseAdUnit.destroy();
        verify(mMockBidLoader).destroy();
    }

    @Test
    public void whenOnResponseReceived_UpdateMoPubAndSuccessResult() throws IOException {
        PrebidRenderingSettings.setAccountId("id");
        String responseString = ResourceUtils.convertResourceToString("bidding_response_obj.json");
        String keywordsString = "hb_pb:value1,hb_bidder:value2,hb_cache_id:value3,";
        BidResponse bidResponse = new BidResponse(responseString);
        OnFetchCompleteListener mockListener = mock(OnFetchCompleteListener.class);

        MoPubViewMock moPubViewMock = new MoPubViewMock();

        mBaseAdUnit.fetchDemand(moPubViewMock, mockListener);
        mBaseAdUnit.onResponseReceived(bidResponse);

        verify(mockListener).onComplete(FetchDemandResult.SUCCESS);
        assertEquals(keywordsString, moPubViewMock.getKeywords());
        assertNotNull(BidResponseCache.getInstance().popBidResponse(bidResponse.getId()));
    }

    @Test
    public void whenOnErrorReceived_PassErrorMessage() {
        PrebidRenderingSettings.setAccountId("id");
        OnFetchCompleteListener mockListener = mock(OnFetchCompleteListener.class);
        AdException adException = new AdException(AdException.INTERNAL_ERROR, "");

        mBaseAdUnit.fetchDemand(mMopubView, mockListener);
        mBaseAdUnit.onErrorReceived(adException);
        verify(mockListener).onComplete(FetchDemandResult.SERVER_ERROR);
    }

    @Test
    public void addUpdateRemoveClearContextData_EqualsGetContextDataDictionary() {
        Map<String, Set<String>> expectedMap = new HashMap<>();
        HashSet<String> value1 = new HashSet<>();
        value1.add("value1");
        HashSet<String> value2 = new HashSet<>();
        value2.add("value2");
        expectedMap.put("key1", value1);
        expectedMap.put("key2", value2);

        // add
        mBaseAdUnit.addContextData("key1", "value1");
        mBaseAdUnit.addContextData("key2", "value2");

        assertEquals(expectedMap, mBaseAdUnit.getContextDataDictionary());

        // update
        HashSet<String> updateSet = new HashSet<>();
        updateSet.add("value3");
        mBaseAdUnit.updateContextData("key1", updateSet);
        expectedMap.replace("key1", updateSet);

        assertEquals(expectedMap, mBaseAdUnit.getContextDataDictionary());

        // remove
        mBaseAdUnit.removeContextData("key1");
        expectedMap.remove("key1");
        assertEquals(expectedMap, mBaseAdUnit.getContextDataDictionary());

        // clear
        mBaseAdUnit.clearContextData();
        assertTrue(mBaseAdUnit.getContextDataDictionary().isEmpty());
    }

    @Test
    public void addRemoveContextKeywords_EqualsGetContextKeyWordsSet() {
        HashSet<String> expectedSet = new HashSet<>();
        expectedSet.add("key1");
        expectedSet.add("key2");

        // add
        mBaseAdUnit.addContextKeyword("key1");
        mBaseAdUnit.addContextKeyword("key2");

        assertEquals(expectedSet, mBaseAdUnit.getContextKeywordsSet());

        // remove
        mBaseAdUnit.removeContextKeyword("key2");
        expectedSet.remove("key2");
        assertEquals(expectedSet, mBaseAdUnit.getContextKeywordsSet());

        // clear
        mBaseAdUnit.clearContextKeywords();
        assertTrue(mBaseAdUnit.getContextKeywordsSet().isEmpty());

        // add all
        mBaseAdUnit.addContextKeywords(expectedSet);
        assertEquals(expectedSet, mBaseAdUnit.getContextKeywordsSet());
    }

    @Test
    public void setPbAdSlot_EqualsGetPbAdSlot() {
        final String expected = "12345";
        mBaseAdUnit.setPbAdSlot(expected);
        assertEquals(expected, mBaseAdUnit.getPbAdSlot());
    }

    private BaseAdUnit createAdUnit(String configId) {
        BaseAdUnit baseAdUnit = new BaseAdUnit(mContext, configId, mMockAdSize) {
            @Override
            protected void initAdConfig(String configId, AdSize adSize) {
                mAdUnitConfig.setConfigId(configId);
            }

            @Override
            protected boolean isAdObjectSupported(Object adObject) {
                return true;
            }
        };
        WhiteBox.setInternalState(baseAdUnit, "mBidLoader", mMockBidLoader);
        return baseAdUnit;
    }

    static class MoPubViewMock {
        private String mKeywords;

        public void setKeywords(String keywords) {
            mKeywords = keywords;
        }

        public String getKeywords() {
            return mKeywords;
        }
    }
}