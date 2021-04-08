package org.prebid.mobile.eventhandlers.utils;

import com.apollo.test.utils.WhiteBox;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.formats.NativeCustomTemplateAd;
import com.google.android.gms.ads.formats.UnifiedNativeAd;

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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

public class GamUtilsTest {

    public static final String KEY_IS_APOLLO_CREATIVE = "isApolloCreative";
    public static final String KEY_IS_PREBID = "isPrebid";

    @Test
    public void didApolloWin_customTemplate_adIsNullOrNoEvents_ReturnFalse() {
        NativeCustomTemplateAd ad = null;
        assertFalse(GamUtils.didApolloWin(ad));

        ad = mock(NativeCustomTemplateAd.class);
        when(ad.getText(KEY_IS_APOLLO_CREATIVE)).thenReturn("0");
        when(ad.getText(KEY_IS_PREBID)).thenReturn("0");

        assertFalse(GamUtils.didApolloWin(ad));
    }

    @Test
    public void didApolloWin_customTemplate_adContainsEvents_ReturnTrue() {
        NativeCustomTemplateAd ad = mock(NativeCustomTemplateAd.class);

        when(ad.getText(KEY_IS_APOLLO_CREATIVE)).thenReturn("1");
        when(ad.getText(KEY_IS_PREBID)).thenReturn("0");
        assertTrue(GamUtils.didApolloWin(ad));

        when(ad.getText(KEY_IS_PREBID)).thenReturn("1");
        when(ad.getText(KEY_IS_APOLLO_CREATIVE)).thenReturn("0");
        assertTrue(GamUtils.didApolloWin(ad));

        when(ad.getText(KEY_IS_PREBID)).thenReturn("1");
        when(ad.getText(KEY_IS_APOLLO_CREATIVE)).thenReturn("1");
        assertTrue(GamUtils.didApolloWin(ad));
    }

    @Test
    public void didApolloWin_unifiedAd_adIsNullOrNoEvents_ReturnFalse() {
        UnifiedNativeAd ad = null;
        assertFalse(GamUtils.didApolloWin(ad));

        ad = mock(UnifiedNativeAd.class);
        when(ad.getBody()).thenReturn("");

        assertFalse(GamUtils.didApolloWin(ad));
    }

    @Test
    public void didApolloWin_unifiedAd_adContainsEvents_ReturnTrue() {
        UnifiedNativeAd ad = mock(UnifiedNativeAd.class);

        when(ad.getBody()).thenReturn(KEY_IS_APOLLO_CREATIVE);

        assertTrue(GamUtils.didApolloWin(ad));
    }

    @Test
    public void findNativeAd_customTemplate_adIsNull_DoNothing() {
        final NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        final NativeCustomTemplateAd customTemplateAd = null;
        GamUtils.findNativeAd(customTemplateAd, mockCallback);

        verifyZeroInteractions(mockCallback);
    }

    @Test
    public void findNativeAd_customTemplate_validAdNoBidResponse_InvokeCallbackWithNull() {
        NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        NativeCustomTemplateAd mockCustomTemplateAd = mock(NativeCustomTemplateAd.class);

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
        NativeCustomTemplateAd mockCustomTemplateAd = mock(NativeCustomTemplateAd.class);
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
        final UnifiedNativeAd ad = null;
        GamUtils.findNativeAd(ad, mockCallback);

        verifyZeroInteractions(mockCallback);
    }

    @Test
    public void findNativeAd_unifiedAd_validAdNoBidResponse_InvokeCallbackWithNull() {
        NativeAdCallback mockCallback = mock(NativeAdCallback.class);
        UnifiedNativeAd mockAd = mock(UnifiedNativeAd.class);

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
        UnifiedNativeAd mockAd = mock(UnifiedNativeAd.class);
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
        final PublisherAdRequest.Builder publisherAdRequest = new PublisherAdRequest.Builder();
        final NativeFetchDemandResult fetchDemandResult = new NativeFetchDemandResult(FetchDemandResult.SUCCESS);
        final HashMap<String, String> keyWordsMap = new HashMap<>();
        keyWordsMap.put("key", "value");
        fetchDemandResult.setKeyWordsMap(keyWordsMap);

        GamUtils.prepare(publisherAdRequest, fetchDemandResult);

        assertEquals(1, GamUtils.RESERVED_KEYS.size());
        assertEquals("[key]", GamUtils.RESERVED_KEYS.toString());
    }
}