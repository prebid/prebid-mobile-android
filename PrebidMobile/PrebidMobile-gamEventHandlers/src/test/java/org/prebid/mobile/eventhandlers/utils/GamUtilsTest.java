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

package org.prebid.mobile.eventhandlers.utils;

import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.nativead.NativeCustomFormatAd;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.NativeFetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAdParser;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.listeners.NativeAdCallback;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class GamUtilsTest {

    public static final String KEY_IS_PREBID = "isPrebid";

    @After
    public void cleanup() {
        GamUtils.RESERVED_KEYS.clear();
    }

    @Test
    public void didPrebidWin_customTemplate_adIsNullOrNoEvents_ReturnFalse() {
        NativeCustomFormatAd ad = null;
        assertFalse(GamUtils.didPrebidWin(ad));

        ad = mock(NativeCustomFormatAd.class);
        when(ad.getText(KEY_IS_PREBID)).thenReturn("0");

        assertFalse(GamUtils.didPrebidWin(ad));
    }

    @Test
    public void didPrebidWin_customTemplate_adContainsEvents_ReturnTrue() {
        NativeCustomFormatAd ad = mock(NativeCustomFormatAd.class);

        when(ad.getText(KEY_IS_PREBID)).thenReturn("0");
        assertFalse(GamUtils.didPrebidWin(ad));

        when(ad.getText(KEY_IS_PREBID)).thenReturn("1");
        assertTrue(GamUtils.didPrebidWin(ad));
    }

    @Test
    public void didPrebidWin_unifiedAd_adIsNullOrNoEvents_ReturnFalse() {
        com.google.android.gms.ads.nativead.NativeAd ad = null;
        assertFalse(GamUtils.didPrebidWin(ad));

        ad = mock(com.google.android.gms.ads.nativead.NativeAd.class);
        when(ad.getBody()).thenReturn("");

        assertFalse(GamUtils.didPrebidWin(ad));
    }

    @Test
    public void findNativeAd_customTemplate_adIsNull_DoNothing() {
        final NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        final NativeCustomFormatAd customTemplateAd = null;
        GamUtils.findNativeAd(customTemplateAd, mockCallback);

        verifyZeroInteractions(mockCallback);
    }

    @Test
    public void findNativeAd_customTemplate_validAdNoBidResponse_InvokeCallbackWithNull() {
        NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        NativeCustomFormatAd mockCustomTemplateAd = mock(NativeCustomFormatAd.class);

        GamUtils.findNativeAd(mockCustomTemplateAd, mockCallback);

        verify(mockCallback).onNativeAdReceived(eq(null));
    }

    @Test
    public void findNativeAd_customTemplate_validAdWithBidResponse_InvokeCallbackWithNativeAd()
    throws InvocationTargetException, IllegalAccessException {
        final BidResponseCache instance = BidResponseCache.getInstance();
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);
        final String adm = "";
        final NativeAd expectedNativeAd = new NativeAdParser().parse(adm);

        NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        NativeCustomFormatAd mockCustomTemplateAd = mock(NativeCustomFormatAd.class);
        when(mockCustomTemplateAd.getText("hb_cache_id_local")).thenReturn("123");

        when(mockBidResponse.getId()).thenReturn("123");
        when(mockBid.getAdm()).thenReturn(adm);
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        WhiteBox.method(BidResponseCache.class, "putBidResponse", BidResponse.class).invoke(instance, mockBidResponse);

        GamUtils.findNativeAd(mockCustomTemplateAd, mockCallback);

        verify(mockCallback).onNativeAdReceived(eq(expectedNativeAd));
    }

    @Test
    public void findNativeAd_unifiedAd_adIsNull_DoNothing() {
        final NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        final com.google.android.gms.ads.nativead.NativeAd ad = null;
        GamUtils.findNativeAd(ad, mockCallback);

        verifyZeroInteractions(mockCallback);
    }

    @Test
    public void findNativeAd_unifiedAd_validAdNoBidResponse_InvokeCallbackWithNull() {
        NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        com.google.android.gms.ads.nativead.NativeAd mockAd = mock(com.google.android.gms.ads.nativead.NativeAd.class);

        GamUtils.findNativeAd(mockAd, mockCallback);

        verify(mockCallback).onNativeAdReceived(eq(null));
    }

    @Test
    public void findNativeAd_unifiedAd_validAdWithBidResponse_InvokeCallbackWithNativeAd()
    throws InvocationTargetException, IllegalAccessException {
        final BidResponseCache instance = BidResponseCache.getInstance();
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);
        final String adm = "";
        final NativeAd expectedNativeAd = new NativeAdParser().parse(adm);

        NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        com.google.android.gms.ads.nativead.NativeAd mockAd = mock(com.google.android.gms.ads.nativead.NativeAd.class);
        when(mockAd.getCallToAction()).thenReturn("123");

        when(mockBidResponse.getId()).thenReturn("123");
        when(mockBid.getAdm()).thenReturn(adm);
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        WhiteBox.method(BidResponseCache.class, "putBidResponse", BidResponse.class).invoke(instance, mockBidResponse);

        GamUtils.findNativeAd(mockAd, mockCallback);

        verify(mockCallback).onNativeAdReceived(eq(expectedNativeAd));
    }

    @Test
    public void prepare_AddReservedKeys() {
        final AdManagerAdRequest publisherAdRequest = new AdManagerAdRequest.Builder().build();
        final NativeFetchDemandResult fetchDemandResult = new NativeFetchDemandResult(FetchDemandResult.SUCCESS);
        final HashMap<String, String> keyWordsMap = new HashMap<>();
        keyWordsMap.put("key", "value");
        fetchDemandResult.setKeyWordsMap(keyWordsMap);

        GamUtils.prepare(publisherAdRequest, fetchDemandResult);

        assertEquals(1, GamUtils.RESERVED_KEYS.size());
        assertEquals("[key]", GamUtils.RESERVED_KEYS.toString());
    }

    @Test
    public void handleGamKeywordsUpdate_KeyWordsShouldMatchExpected() {
        AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();
        builder.addCustomTargeting("Key", "Value");
        HashMap<String, String> bids = new HashMap<>();
        bids.put("hb_pb", "0.50");
        bids.put("hb_cache_id", "123456");
        AdManagerAdRequest request = builder.build();
        GamUtils.handleGamCustomTargetingUpdate(request, bids);

        Assert.assertEquals(3, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        Assert.assertEquals("Value", request.getCustomTargeting().get("Key"));
        assertTrue(request.getCustomTargeting().containsKey("hb_pb"));
        Assert.assertEquals("0.50", request.getCustomTargeting().get("hb_pb"));
        assertTrue(request.getCustomTargeting().containsKey("hb_cache_id"));
        Assert.assertEquals("123456", request.getCustomTargeting().get("hb_cache_id"));

        GamUtils.handleGamCustomTargetingUpdate(request, null);
        Assert.assertEquals(1, request.getCustomTargeting().size());
        assertTrue(request.getCustomTargeting().containsKey("Key"));
        Assert.assertEquals("Value", request.getCustomTargeting().get("Key"));
    }
}