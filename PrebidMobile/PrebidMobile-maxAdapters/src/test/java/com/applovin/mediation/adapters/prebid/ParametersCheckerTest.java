package com.applovin.mediation.adapters.prebid;

import android.os.Bundle;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.PrebidMaxMediationAdapter;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(minSdk = 29)
public class ParametersCheckerTest {

    private MaxAdapterResponseParameters parameters;
    private ParametersChecker.OnError onError;

    @Before
    public void setup() {
        parameters = mock(MaxAdapterResponseParameters.class);
        onError = mock(ParametersChecker.OnError.class);
    }

    @Test
    public void getResponseIdAndCheckKeywords_putNullParameters_returnNull() {
        String result = ParametersChecker.getResponseIdAndCheckKeywords(null, onError);

        assertNull(result);
        verify(onError).onError(eq(1001), anyString());
    }

    @Test
    public void getResponseIdAndCheckKeywords_nullCustomParameters_returnNull() {
        when(parameters.getCustomParameters()).thenReturn(null);

        String result = ParametersChecker.getResponseIdAndCheckKeywords(parameters, onError);

        assertNull(result);
        verify(onError).onError(eq(1001), anyString());
    }

    @Test
    public void getResponseIdAndCheckKeywords_nullExtraParameters_returnNull() {
        when(parameters.getLocalExtraParameters()).thenReturn(null);

        String result = ParametersChecker.getResponseIdAndCheckKeywords(parameters, onError);

        assertNull(result);
        verify(onError).onError(eq(1001), anyString());
    }

    @Test
    public void getResponseIdAndCheckKeywords_emptyExtraParameters_returnNull() {
        Map<String, Object> extras = new HashMap<>();
        when(parameters.getLocalExtraParameters()).thenReturn(extras);

        Bundle customParameters = new Bundle();
        when(parameters.getCustomParameters()).thenReturn(customParameters);

        String result = ParametersChecker.getResponseIdAndCheckKeywords(parameters, onError);

        assertNull(result);
        verify(onError).onError(eq(1002), anyString());
    }

    @Test
    public void getResponseIdAndCheckKeywords_emptyCustomParameters_returnNull() {
        Map<String, Object> extras = new HashMap<>();
        extras.put(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, "test");
        when(parameters.getLocalExtraParameters()).thenReturn(extras);

        Bundle customParameters = new Bundle();
        when(parameters.getCustomParameters()).thenReturn(customParameters);

        String result = ParametersChecker.getResponseIdAndCheckKeywords(parameters, onError);

        assertNull(result);
        verify(onError).onError(eq(1003), anyString());
    }

    @Test
    public void getResponseIdAndCheckKeywords_returnResponseId() {
        setBidResponseCache();

        Map<String, Object> extras = new HashMap<>();
        extras.put(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, responseId);
        when(parameters.getLocalExtraParameters()).thenReturn(extras);

        Bundle customParameters = new Bundle();
        customParameters.putString("hb_pb", "0.10");
        when(parameters.getCustomParameters()).thenReturn(customParameters);

        String result = ParametersChecker.getResponseIdAndCheckKeywords(parameters, onError);

        assertNotNull(result);
        verifyNoInteractions(onError);
    }

    @SuppressWarnings("ConstantConditions")
    @Test
    public void getBidResponse_putNull_returnNull() {
        BidResponse response = ParametersChecker.getBidResponse(null, onError);

        assertNull(response);
    }

    @Test
    public void getBidResponse_returnResponseId() {
        setBidResponseCache();

        BidResponse result = ParametersChecker.getBidResponse(responseId, onError);

        assertNotNull(result);
        verifyNoInteractions(onError);
    }

    @Test
    public void getNativeAd_localExtraParametersWithoutKeywords_returnNull() {
        Map<String, Object> extras = new HashMap<>();
        extras.put(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, responseId);
        when(parameters.getLocalExtraParameters()).thenReturn(extras);

        Bundle customParameters = new Bundle();
        customParameters.putString("hb_pb", "0.10");
        when(parameters.getCustomParameters()).thenReturn(customParameters);

        PrebidNativeAd result = ParametersChecker.getNativeAd(parameters, onError);

        assertNull(result);
        verify(onError).onError(eq(1005), anyString());
    }

    @Test
    public void getNativeAd_cacheEmpty_returnNull() {
        HashMap<String, String> keywords = new HashMap<>();
        keywords.put("hb_pb", "0.10");

        Map<String, Object> extras = new HashMap<>();
        extras.put(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, responseId);
        extras.put(PrebidMaxMediationAdapter.EXTRA_KEYWORDS_ID, keywords);
        when(parameters.getLocalExtraParameters()).thenReturn(extras);

        Bundle customParameters = new Bundle();
        customParameters.putString("hb_pb", "0.10");
        when(parameters.getCustomParameters()).thenReturn(customParameters);

        PrebidNativeAd result = ParametersChecker.getNativeAd(parameters, onError);

        assertNull(result);
        verify(onError).onError(eq(1007), anyString());
    }

    @Test
    public void getNativeAd_emptyLocalExtraParameters_() {
        HashMap<String, String> keywords = new HashMap<>();
        keywords.put("hb_pb", "0.10");

        Map<String, Object> extras = new HashMap<>();
        extras.put(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, responseId);
        extras.put(PrebidMaxMediationAdapter.EXTRA_KEYWORDS_ID, keywords);
        when(parameters.getLocalExtraParameters()).thenReturn(extras);

        Bundle customParameters = new Bundle();
        customParameters.putString("hb_pb", "0.10");
        when(parameters.getCustomParameters()).thenReturn(customParameters);

        PrebidNativeAd result = ParametersChecker.getNativeAd(parameters, onError);

        assertNull(result);
        verify(onError).onError(eq(1007), anyString());
    }

    private static final String responseId = "07e8b9f9-74cd-4209-87ec-c9cc46245754";

    private void setBidResponseCache() {
        String json = "{\n" + "    \"cur\": \"USD\",\n" + "    \"ext\": {\n" + "        \"prebid\": {\n" + "            \"auctiontimestamp\": 1649865494641\n" + "        },\n" + "        \"responsetimemillis\": {\n" + "            \"cache\": 30,\n" + "            \"openx\": 0\n" + "        },\n" + "        \"tmaxrequest\": 900\n" + "    },\n" + "    \"id\": \"07e8b9f9-74cd-4209-87ec-c9cc46245754\",\n" + "    \"seatbid\": [\n" + "        {\n" + "            \"bid\": [\n" + "                {\n" + "                    \"adm\": \"<div id='beacon_97599' style='position:absolute;left:0px;top:0px;visibility:hidden;'><img src='https://prebid-server-test-j.prebid.org/win/prebid?p=FIRST&t=2DAABBgABAAECAAIBAAsAAgAAAJocGApiQ2JVRndacTlxHBaNw_Xgj_HhqLMBFpmN0Jvj_avD_AEAHBbh9P2d_Pyv_hgW_YfWn7jth6SYAQAW_rHY9gsVBgAsHBUCABwVAgAAHCa6ofGDBBUOFQQmuKHxgwQWoICcgATWrAIAHCaew6CABBaEovGDBBaIovGDBBaGovGDBBUUHBRkFIAFABUEFQoWrAImrAJFCgAAAA&ph=a51065ab-17ee-4394-b5a7-32debc04780a'/></div><script src=\\\"https://prebid-mobile-ad-assets.s3.amazonaws.com/scripts/omid-validation-verification-script-v1.js\\\"></script>\\n<a href=\\\"https://www.openx.com/\\\"><img width=\\\"320\\\" height=\\\"50\\\" src=\\\"https://prebid-mobile-ad-assets.s3.amazonaws.com/banner/Pbs_banner_320x50.png\\\"></img></a>\",\n" + "                    \"crid\": \"540944516\",\n" + "                    \"ext\": {\n" + "                        \"origbidcpm\": 0.1,\n" + "                        \"origbidcur\": \"USD\",\n" + "                        \"prebid\": {\n" + "                            \"cache\": {\n" + "                                \"bids\": {\n" + "                                    \"cacheId\": \"89a13f59-0168-4384-9f1d-d62f4d9f5b7b\",\n" + "                                    \"url\": \"https://prebid-server-test-j.prebid.org/cache?uuid=89a13f59-0168-4384-9f1d-d62f4d9f5b7b\"\n" + "                                }\n" + "                            },\n" + "                            \"targeting\": {\n" + "                                \"hb_bidder\": \"openx\",\n" + "                                \"hb_bidder_openx\": \"openx\",\n" + "                                \"hb_cache_host\": \"prebid-server-test-j.prebid.org\",\n" + "                                \"hb_cache_host_openx\": \"prebid-server-test-j.prebid.org\",\n" + "                                \"hb_cache_id\": \"89a13f59-0168-4384-9f1d-d62f4d9f5b7b\",\n" + "                                \"hb_cache_id_openx\": \"89a13f59-0168-4384-9f1d-d62f4d9f5b7b\",\n" + "                                \"hb_cache_path\": \"/cache\",\n" + "                                \"hb_cache_path_openx\": \"/cache\",\n" + "                                \"hb_env\": \"mobile-app\",\n" + "                                \"hb_env_openx\": \"mobile-app\",\n" + "                                \"hb_pb\": \"0.10\",\n" + "                                \"hb_pb_openx\": \"0.10\",\n" + "                                \"hb_size\": \"320x50\",\n" + "                                \"hb_size_openx\": \"320x50\"\n" + "                            },\n" + "                            \"type\": \"banner\"\n" + "                        }\n" + "                    },\n" + "                    \"h\": 50,\n" + "                    \"id\": \"response-prebid-banner-320-50\",\n" + "                    \"impid\": \"07e8b9f9-74cd-4209-87ec-c9cc46245754\",\n" + "                    \"price\": 0.1,\n" + "                    \"w\": 320\n" + "                }\n" + "            ],\n" + "            \"group\": 0,\n" + "            \"seat\": \"openx\"\n" + "        }\n" + "    ]\n" + "}";
        BidResponseCache.getInstance().putBidResponse(new BidResponse(json, new AdUnitConfiguration()));
    }

}