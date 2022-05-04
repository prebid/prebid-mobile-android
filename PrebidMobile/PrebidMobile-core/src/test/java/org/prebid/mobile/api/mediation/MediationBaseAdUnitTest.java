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

package org.prebid.mobile.api.mediation;

import android.app.Activity;
import android.content.Context;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.BannerAdPosition;
import org.prebid.mobile.api.data.FetchDemandResult;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.mediation.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.config.MockMediationUtils;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MediationBaseAdUnitTest {

    private MediationBaseAdUnit baseAdUnit;
    private Context context;
    private Object mopubView = new Object();
    @Mock
    private AdSize mockAdSize;
    @Mock
    private BidLoader mockBidLoader;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        context = Robolectric.buildActivity(Activity.class).create().get();
        baseAdUnit = createAdUnit("config");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);

        assertEquals(BannerAdPosition.UNDEFINED.getValue(), baseAdUnit.adUnitConfig.getAdPositionValue());
    }

    @After
    public void cleanup() {
        PrebidMobile.setPrebidServerAccountId(null);
    }

    @Test
    public void whenFetchDemandAndNotMoPubViewPassed_InvalidAdObjectResult() {
        PrebidMobile.setPrebidServerAccountId("testAccountId");
        new MediationBaseAdUnit(context, "123", mockAdSize, new MockMediationUtils()) {
            @Override
            protected void initAdConfig(
                String configId,
                AdSize adSize
            ) {
                adUnitConfig.setConfigId(configId);
            }
        }.fetchDemand(result -> {
            assertEquals(FetchDemandResult.INVALID_AD_OBJECT, result);
        });
    }

    @Test
    public void whenFetchDemandAndNoAccountId_InvalidAccountIdResult() {
        baseAdUnit.fetchDemand(result -> {
            assertEquals(FetchDemandResult.INVALID_ACCOUNT_ID, result);
        });
    }

    @Test
    public void whenFetchDemandAndNoConfigId_InvalidConfigIdResult() {
        PrebidMobile.setPrebidServerAccountId("id");
        baseAdUnit = createAdUnit("");
        baseAdUnit.fetchDemand(result -> {
            assertEquals(FetchDemandResult.INVALID_CONFIG_ID, result);
        });
    }

    @Test
    public void whenFetchDemandAndNoUrlForCustomHost_InvalidHostUrl() {
        PrebidMobile.setPrebidServerAccountId("id");
        final Host custom = Host.CUSTOM;
        custom.setHostUrl("");
        PrebidMobile.setPrebidServerHost(custom);
        baseAdUnit = createAdUnit("123");
        baseAdUnit.fetchDemand(result -> {
            assertEquals(FetchDemandResult.INVALID_HOST_URL, result);
        });
    }

    @Test
    public void whenFetchDemandAndEverythingOK_BidLoaderLoadCalled() {
        PrebidMobile.setPrebidServerAccountId("id");
        baseAdUnit.fetchDemand(result -> {
        });
        verify(mockBidLoader).load();
    }

    @Test
    public void whenDestroy_DestroyBidLoader() {
        baseAdUnit.destroy();
        verify(mockBidLoader).destroy();
    }

    @Test
    public void whenOnErrorReceived_PassErrorMessage() {
        PrebidMobile.setPrebidServerAccountId("id");
        OnFetchCompleteListener mockListener = mock(OnFetchCompleteListener.class);
        AdException adException = new AdException(AdException.INTERNAL_ERROR, "");

        baseAdUnit.fetchDemand(mockListener);
        baseAdUnit.onErrorReceived(adException);
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
        baseAdUnit.addContextData("key1", "value1");
        baseAdUnit.addContextData("key2", "value2");

        assertEquals(expectedMap, baseAdUnit.getContextDataDictionary());

        // update
        HashSet<String> updateSet = new HashSet<>();
        updateSet.add("value3");
        baseAdUnit.updateContextData("key1", updateSet);
        expectedMap.replace("key1", updateSet);

        assertEquals(expectedMap, baseAdUnit.getContextDataDictionary());

        // remove
        baseAdUnit.removeContextData("key1");
        expectedMap.remove("key1");
        assertEquals(expectedMap, baseAdUnit.getContextDataDictionary());

        // clear
        baseAdUnit.clearContextData();
        assertTrue(baseAdUnit.getContextDataDictionary().isEmpty());
    }

    @Test
    public void addRemoveContextKeywords_EqualsGetContextKeyWordsSet() {
        HashSet<String> expectedSet = new HashSet<>();
        expectedSet.add("key1");
        expectedSet.add("key2");

        // add
        baseAdUnit.addContextKeyword("key1");
        baseAdUnit.addContextKeyword("key2");

        assertEquals(expectedSet, baseAdUnit.getContextKeywordsSet());

        // remove
        baseAdUnit.removeContextKeyword("key2");
        expectedSet.remove("key2");
        assertEquals(expectedSet, baseAdUnit.getContextKeywordsSet());

        // clear
        baseAdUnit.clearContextKeywords();
        assertTrue(baseAdUnit.getContextKeywordsSet().isEmpty());

        // add all
        baseAdUnit.addContextKeywords(expectedSet);
        assertEquals(expectedSet, baseAdUnit.getContextKeywordsSet());
    }

    @Test
    public void setPbAdSlot_EqualsGetPbAdSlot() {
        final String expected = "12345";
        baseAdUnit.setPbAdSlot(expected);
        assertEquals(expected, baseAdUnit.getPbAdSlot());
    }

    private MediationBaseAdUnit createAdUnit(String configId) {
        MediationBaseAdUnit baseAdUnit = new MediationBaseAdUnit(context, configId, mockAdSize, new MockMediationUtils()) {
            @Override
            protected void initAdConfig(
                String configId,
                AdSize adSize
            ) {
                adUnitConfig.setConfigId(configId);
            }
        };
        WhiteBox.setInternalState(baseAdUnit, "bidLoader", mockBidLoader);
        return baseAdUnit;
    }

    public static class MediationViewMock {

        private String keywords;

        public void setKeywords(String keywords) {
            this.keywords = keywords;
        }

        public String getKeywords() {
            return keywords;
        }

    }

}