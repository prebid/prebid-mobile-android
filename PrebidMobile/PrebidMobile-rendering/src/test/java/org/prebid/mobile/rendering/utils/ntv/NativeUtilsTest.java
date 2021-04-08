package org.prebid.mobile.rendering.utils.ntv;

import com.apollo.test.utils.WhiteBox;

import org.junit.Test;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.NativeFetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAdParser;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.listeners.NativeAdCallback;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class NativeUtilsTest {

    @Test
    public void findNativeAd_nullKeyWordMap_DoNothing() {
        final NativeFetchDemandResult fetchDemandResult = new NativeFetchDemandResult(FetchDemandResult.SUCCESS);
        final NativeAdCallback mockCallback = mock(NativeAdCallback.class);

        NativeUtils.findNativeAd(fetchDemandResult, mockCallback);

        verifyZeroInteractions(mockCallback);
    }

    @Test
    public void findNativeAd_validAdWithBidResponse_InvokeCallbackWithNativeAd()
    throws InvocationTargetException, IllegalAccessException {
        final NativeFetchDemandResult fetchDemandResult = new NativeFetchDemandResult(FetchDemandResult.SUCCESS);
        final BidResponseCache instance = BidResponseCache.getInstance();
        final BidResponse mockBidResponse = mock(BidResponse.class);
        final Bid mockBid = mock(Bid.class);
        final HashMap<String, String> keyWordsMap = new HashMap<>();
        final String adm = "";
        final NativeAd expectedNativeAd = new NativeAdParser().parse(adm);

        NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        keyWordsMap.put("hb_cache_id_local", "123");
        fetchDemandResult.setKeyWordsMap(keyWordsMap);

        when(mockBidResponse.getId()).thenReturn("123");
        when(mockBid.getAdm()).thenReturn(adm);
        when(mockBidResponse.getWinningBid()).thenReturn(mockBid);

        WhiteBox.method(BidResponseCache.class, "putBidResponse", BidResponse.class).invoke(instance, mockBidResponse);

        NativeUtils.findNativeAd(fetchDemandResult, mockCallback);

        verify(mockCallback).onNativeAdReceived(eq(expectedNativeAd));
    }

    @Test
    public void findNativeAd_validAdNoBidResponse_InvokeCallbackWithNull() {
        NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        final NativeFetchDemandResult fetchDemandResult = new NativeFetchDemandResult(FetchDemandResult.SUCCESS);
        final HashMap<String, String> keyWordsMap = new HashMap<>();
        keyWordsMap.put("key", "value");
        fetchDemandResult.setKeyWordsMap(keyWordsMap);

        NativeUtils.findNativeAd(fetchDemandResult, mockCallback);

        verify(mockCallback).onNativeAdReceived(eq(null));
    }
}