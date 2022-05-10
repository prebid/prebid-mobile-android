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

package org.prebid.mobile.rendering.networking;

import android.text.TextUtils;
import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.QueueDispatcher;
import okhttp3.mockwebserver.RecordedRequest;
import org.jetbrains.annotations.NotNull;
import org.json.JSONObject;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.bid.Prebid;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;

import java.io.IOException;
import java.util.HashMap;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(JUnit4.class)
public class WinNotifierTest {

    private static final String PATH_BID_SHORT_JSON = "bidding_bid_short.json";
    private static final String KEY_CACHE_ID = "hb_cache_id";
    private static final String KEY_UUID = "hb_uuid";
    private static final String CACHE_ID = "id";
    private static final String CACHE_UUID = "uuid";

    private WinNotifier winNotifier;
    private String cacheHost;
    private Bid bid;
    private MockWebServer mockWebServer;
    private HashMap<String, String> targeting;

    // Set to TRUE to dispatch an error through MockWebServer
    private boolean dispatchError;

    @Mock
    private WinNotifier.WinNotifierListener mockListener;
    @Mock
    private BidResponse mockBidResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        dispatchError = false;

        winNotifier = new WinNotifier();
        winNotifier.enableTestFlag();

        mockWebServer = new MockWebServer();
        mockWebServer.setDispatcher(getDispatcher());
        mockWebServer.start();

        String cachePath = "/cache";
        HttpUrl httpUrl = mockWebServer.url(cachePath);

        cacheHost = httpUrl.host() + ":" + httpUrl.port();
        targeting = new HashMap<>();
        targeting.put("hb_cache_host", cacheHost);
        targeting.put("hb_cache_path", cachePath);

        bid = Bid.fromJSONObject(new JSONObject(ResourceUtils.convertResourceToString(PATH_BID_SHORT_JSON)));
        bid.setAdm("test");
        WhiteBox.setInternalState(bid, "prebid", mock(Prebid.class));

        when(mockBidResponse.getWinningBid()).thenReturn(bid);
        when(bid.getPrebid().getTargeting()).thenReturn(targeting);
    }

    @After
    public void cleanup() throws IOException {
        mockWebServer.shutdown();
    }

    @Test
    public void whenAllUrlsInTargetingAndNoAdm_AllRequestsSent_AdmWasChanged()
    throws InterruptedException, IOException {
        targeting.put(KEY_CACHE_ID, CACHE_ID);
        targeting.put(KEY_UUID, CACHE_UUID);

        WhiteBox.setInternalState(bid, "nurl", String.format("http://%1$s/cache?uuid=nurl", cacheHost));
        bid.setAdm(null);

        winNotifier.notifyWin(mockBidResponse, mockListener);

        assertEquals(3, mockWebServer.getRequestCount());
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=id");
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=uuid");
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=nurl");

        verify(mockListener).onResult();
        assertNotNull(bid.getAdm());
    }

    @Test
    public void whenAllUrlsInTargetingAndAdmPresents_AllRequestsSent_AdmWasNotChanged()
    throws InterruptedException {
        targeting.put(KEY_CACHE_ID, CACHE_ID);
        targeting.put(KEY_UUID, CACHE_UUID);

        WhiteBox.setInternalState(bid, "nurl", String.format("http://%1$s/cache?uuid=nurl", cacheHost));

        winNotifier.notifyWin(mockBidResponse, mockListener);

        assertEquals(3, mockWebServer.getRequestCount());
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=id");
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=uuid");
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=nurl");

        verify(mockListener).onResult();
        assertEquals("test", bid.getAdm());
    }

    @Test
    public void whenNurlInTargetingAndNoAdm_AdmWasExtractedFromReceivedBid()
    throws InterruptedException {
        bid.setAdm(null);
        WhiteBox.setInternalState(bid, "nurl", String.format("http://%1$s/cache?uuid=nurl", cacheHost));

        winNotifier.notifyWin(mockBidResponse, mockListener);

        assertEquals(1, mockWebServer.getRequestCount());
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=nurl");

        verify(mockListener).onResult();
        assertEquals("VAST", bid.getAdm());
    }

    @Test
    public void whenCacheIdInTargetingAndNoAdm_AdmWasExtractedFromReceivedBid()
    throws InterruptedException {
        bid.setAdm(null);
        targeting.put(KEY_CACHE_ID, CACHE_ID);

        winNotifier.notifyWin(mockBidResponse, mockListener);

        assertEquals(1, mockWebServer.getRequestCount());
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=id");

        verify(mockListener).onResult();
        assertEquals("test", bid.getAdm());
    }

    @Test
    public void whenUuidInTargetingAndNoAdm_ResponseStringWasSetToAdm()
    throws InterruptedException {
        bid.setAdm(null);
        targeting.put(KEY_UUID, CACHE_UUID);

        winNotifier.notifyWin(mockBidResponse, mockListener);

        assertEquals(1, mockWebServer.getRequestCount());
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=uuid");

        verify(mockListener).onResult();
        assertEquals("VAST", bid.getAdm());
    }

    @Test
    public void whenBidIsNull_NoRequestWasMade_OnResultCalled() {
        when(mockBidResponse.getWinningBid()).thenReturn(null);
        winNotifier.notifyWin(mockBidResponse, mockListener);
        assertEquals(0, mockWebServer.getRequestCount());
        verify(mockListener).onResult();
    }

    @Test
    public void whenTargetingIsEmpty_NoRequestWasMade_OnResultCalled() {
        targeting.clear();
        winNotifier.notifyWin(mockBidResponse, mockListener);
        assertEquals(0, mockWebServer.getRequestCount());
        verify(mockListener).onResult();
    }

    @Test
    public void whenNoAdmAndRequestFails_AllRequestAttemptsWereMade() throws InterruptedException {
        dispatchError = true;
        targeting.put(KEY_CACHE_ID, CACHE_ID);
        targeting.put(KEY_UUID, CACHE_UUID);

        WhiteBox.setInternalState(bid, "nurl", String.format("http://%1$s/cache?uuid=nurl", cacheHost));
        bid.setAdm(null);

        winNotifier.notifyWin(mockBidResponse, mockListener);

        assertEquals(3, mockWebServer.getRequestCount());
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=id");
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=uuid");
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=nurl");

        verify(mockListener).onResult();
        assertNull(bid.getAdm());
    }

    @Test
    public void whenWinUrlIsEpty_WinUrlWasSkipped() throws InterruptedException {
        targeting.put(KEY_CACHE_ID, CACHE_ID);
        targeting.put(KEY_UUID, CACHE_UUID);
        WhiteBox.setInternalState(bid, "nurl", "");

        String test = "";
        int length = test.length();
        boolean isEmpty = TextUtils.isEmpty(test);

        winNotifier.notifyWin(mockBidResponse, mockListener);

        assertEquals(2, mockWebServer.getRequestCount());
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=id");
        verifyRequest(mockWebServer.takeRequest(), "/cache?uuid=uuid");

        verify(mockListener).onResult();
        assertNotNull(bid.getAdm());
    }

    private void verifyRequest(RecordedRequest recordedRequest, String path) {
        assertNotNull(recordedRequest);
        assertEquals(path, recordedRequest.getPath());
    }

    private QueueDispatcher getDispatcher() {
        return new QueueDispatcher() {
            @Override
            public MockResponse dispatch(
                @NotNull
                    RecordedRequest request) {
                MockResponse mockResponse = new MockResponse().setResponseCode(200);
                if (dispatchError) {
                    mockResponse.setResponseCode(404);
                }
                String path = request.getPath();
                String adMarkup = "";
                switch (path) {
                    case "/cache?uuid=id":
                        try {
                            adMarkup = ResourceUtils.convertResourceToString(PATH_BID_SHORT_JSON);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        break;
                    case "/cache?uuid=nurl":
                    case "/cache?uuid=uuid":
                        adMarkup = "VAST";
                }

                mockResponse.setBody(adMarkup);

                return mockResponse;
            }
        };
    }
}