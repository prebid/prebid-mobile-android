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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.api.data.FetchDemandResult;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.mediation.listeners.OnFetchCompleteListener;
import org.prebid.mobile.api.original.OnFetchDemandResult;
import org.prebid.mobile.reflection.sdk.PrebidMobileReflection;
import org.prebid.mobile.rendering.bidding.config.MockMediationUtils;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.bidding.loader.BidLoader;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MediationBaseAdUnitTest {

    private MediationBaseAdUnit baseAdUnit;
    private Context context;
    @Mock
    private AdSize mockAdSize;
    @Mock
    private BidLoader mockBidLoader;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        context = Robolectric.buildActivity(Activity.class).create().get();
        baseAdUnit = createAdUnit("config");
        PrebidMobileReflection.setHost("https://test.com");

        assertEquals(AdPosition.UNDEFINED.getValue(), baseAdUnit.adUnitConfig.getAdPositionValue());
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
    public void fetchDemandBidInfo_ResponseReceived_ReturnsBidInfoWithWinningBid() throws IOException {
        PrebidMobile.setPrebidServerAccountId("id");
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        baseAdUnit.fetchDemandBidInfo(listener);
        bidRequesterListener(baseAdUnit).onFetchCompleted(createBidResponse(baseAdUnit));

        BidInfo bidInfo = captureBidInfo(listener);
        assertEquals(ResultCode.SUCCESS, bidInfo.getResultCode());
        assertEquals("value1", bidInfo.getTargetingKeywords().get("hb_pb"));
        assertEquals(Double.valueOf(0.15), bidInfo.getPrice());
        assertEquals("USD", bidInfo.getCurrency());
        assertEquals("crid", bidInfo.getCreativeId());
        assertEquals("id", bidInfo.getRequestId());
    }

    @Test
    public void fetchDemandBidInfo_ErrorReceived_ReturnsResultCodeOnly() {
        PrebidMobile.setPrebidServerAccountId("id");
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        baseAdUnit.fetchDemandBidInfo(listener);
        bidRequesterListener(baseAdUnit).onError(new AdException(AdException.INTERNAL_ERROR, ""));

        BidInfo bidInfo = captureBidInfo(listener);
        assertEquals(ResultCode.PREBID_SERVER_ERROR, bidInfo.getResultCode());
        assertNull(bidInfo.getTargetingKeywords());
        assertNull(bidInfo.getPrice());
        assertNull(bidInfo.getRequestId());
    }

    @Test
    public void fetchDemandBidInfo_NoAccountId_ReturnsInvalidAccountId() {
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);

        baseAdUnit.fetchDemandBidInfo(listener);

        BidInfo bidInfo = captureBidInfo(listener);
        assertEquals(ResultCode.INVALID_ACCOUNT_ID, bidInfo.getResultCode());
        assertNull(bidInfo.getPrice());
    }

    @Test
    public void fetchDemandBidInfo_ErrorAfterResponse_DoesNotReportPreviousBid() throws IOException {
        PrebidMobile.setPrebidServerAccountId("id");
        OnFetchDemandResult listener = mock(OnFetchDemandResult.class);
        BidRequesterListener bidRequesterListener = bidRequesterListener(baseAdUnit);

        // The second callback stands for an auto refresh that fails
        baseAdUnit.fetchDemandBidInfo(listener);
        bidRequesterListener.onFetchCompleted(createBidResponse(baseAdUnit));
        bidRequesterListener.onError(new AdException(AdException.INTERNAL_ERROR, "No bids"));

        ArgumentCaptor<BidInfo> captor = ArgumentCaptor.forClass(BidInfo.class);
        verify(listener, times(2)).onComplete(captor.capture());
        List<BidInfo> bidInfos = captor.getAllValues();
        assertEquals(ResultCode.SUCCESS, bidInfos.get(0).getResultCode());
        assertEquals(ResultCode.NO_BIDS, bidInfos.get(1).getResultCode());
        assertNull(bidInfos.get(1).getPrice());
        assertNull(bidInfos.get(1).getRequestId());
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

    static BidRequesterListener bidRequesterListener(MediationBaseAdUnit adUnit) {
        return WhiteBox.getInternalState(adUnit, "bidRequesterListener");
    }

    static BidResponse createBidResponse(MediationBaseAdUnit adUnit) throws IOException {
        return new BidResponse(
                ResourceUtils.convertResourceToString("BidResponseTest/keywords_all_with_cache_id.json"),
                adUnit.adUnitConfig
        );
    }

    static BidInfo captureBidInfo(OnFetchDemandResult listener) {
        ArgumentCaptor<BidInfo> captor = ArgumentCaptor.forClass(BidInfo.class);
        verify(listener).onComplete(captor.capture());
        return captor.getValue();
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