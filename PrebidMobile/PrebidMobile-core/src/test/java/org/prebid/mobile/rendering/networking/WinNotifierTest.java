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

    private WinNotifier mWinNotifier;
    private String mCacheHost;
    private Bid mBid;
    private MockWebServer mMockWebServer;
    private HashMap<String, String> mTargeting;

    // Set to TRUE to dispatch an error through MockWebServer
    private boolean mDispatchError;

    @Mock
    private WinNotifier.WinNotifierListener mMockListener;
    @Mock
    private BidResponse mMockBidResponse;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mDispatchError = false;

        mWinNotifier = new WinNotifier();
        mWinNotifier.enableTestFlag();

        mMockWebServer = new MockWebServer();
        mMockWebServer.setDispatcher(getDispatcher());
        mMockWebServer.start();

        String cachePath = "/cache";
        HttpUrl httpUrl = mMockWebServer.url(cachePath);

        mCacheHost = httpUrl.host() + ":" + httpUrl.port();
        mTargeting = new HashMap<>();
        mTargeting.put("hb_cache_host", mCacheHost);
        mTargeting.put("hb_cache_path", cachePath);

        mBid = Bid.fromJSONObject(new JSONObject(ResourceUtils.convertResourceToString(PATH_BID_SHORT_JSON)));
        mBid.setAdm("test");
        WhiteBox.setInternalState(mBid, "mPrebid", mock(Prebid.class));

        when(mMockBidResponse.getWinningBid()).thenReturn(mBid);
        when(mBid.getPrebid().getTargeting()).thenReturn(mTargeting);
    }

    @After
    public void cleanup() throws IOException {
        mMockWebServer.shutdown();
    }

    @Test
    public void whenAllUrlsInTargetingAndNoAdm_AllRequestsSent_AdmWasChanged()
    throws InterruptedException, IOException {
        mTargeting.put(KEY_CACHE_ID, CACHE_ID);
        mTargeting.put(KEY_UUID, CACHE_UUID);

        WhiteBox.setInternalState(mBid, "mNurl", String.format("http://%1$s/cache?uuid=nurl", mCacheHost));
        mBid.setAdm(null);

        mWinNotifier.notifyWin(mMockBidResponse, mMockListener);

        assertEquals(3, mMockWebServer.getRequestCount());
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=id");
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=uuid");
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=nurl");

        verify(mMockListener).onResult();
        assertNotNull(mBid.getAdm());
    }

    @Test
    public void whenAllUrlsInTargetingAndAdmPresents_AllRequestsSent_AdmWasNotChanged()
    throws InterruptedException {
        mTargeting.put(KEY_CACHE_ID, CACHE_ID);
        mTargeting.put(KEY_UUID, CACHE_UUID);

        WhiteBox.setInternalState(mBid, "mNurl", String.format("http://%1$s/cache?uuid=nurl", mCacheHost));

        mWinNotifier.notifyWin(mMockBidResponse, mMockListener);

        assertEquals(3, mMockWebServer.getRequestCount());
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=id");
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=uuid");
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=nurl");

        verify(mMockListener).onResult();
        assertEquals("test", mBid.getAdm());
    }

    @Test
    public void whenNurlInTargetingAndNoAdm_AdmWasExtractedFromReceivedBid()
    throws InterruptedException {
        mBid.setAdm(null);
        WhiteBox.setInternalState(mBid, "mNurl", String.format("http://%1$s/cache?uuid=nurl", mCacheHost));

        mWinNotifier.notifyWin(mMockBidResponse, mMockListener);

        assertEquals(1, mMockWebServer.getRequestCount());
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=nurl");

        verify(mMockListener).onResult();
        assertEquals("VAST", mBid.getAdm());
    }

    @Test
    public void whenCacheIdInTargetingAndNoAdm_AdmWasExtractedFromReceivedBid()
    throws InterruptedException {
        mBid.setAdm(null);
        mTargeting.put(KEY_CACHE_ID, CACHE_ID);

        mWinNotifier.notifyWin(mMockBidResponse, mMockListener);

        assertEquals(1, mMockWebServer.getRequestCount());
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=id");

        verify(mMockListener).onResult();
        assertEquals("test", mBid.getAdm());
    }

    @Test
    public void whenUuidInTargetingAndNoAdm_ResponseStringWasSetToAdm()
    throws InterruptedException {
        mBid.setAdm(null);
        mTargeting.put(KEY_UUID, CACHE_UUID);

        mWinNotifier.notifyWin(mMockBidResponse, mMockListener);

        assertEquals(1, mMockWebServer.getRequestCount());
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=uuid");

        verify(mMockListener).onResult();
        assertEquals("VAST", mBid.getAdm());
    }

    @Test
    public void whenBidIsNull_NoRequestWasMade_OnResultCalled() {
        when(mMockBidResponse.getWinningBid()).thenReturn(null);
        mWinNotifier.notifyWin(mMockBidResponse, mMockListener);
        assertEquals(0, mMockWebServer.getRequestCount());
        verify(mMockListener).onResult();
    }

    @Test
    public void whenTargetingIsEmpty_NoRequestWasMade_OnResultCalled() {
        mTargeting.clear();
        mWinNotifier.notifyWin(mMockBidResponse, mMockListener);
        assertEquals(0, mMockWebServer.getRequestCount());
        verify(mMockListener).onResult();
    }

    @Test
    public void whenNoAdmAndRequestFails_AllRequestAttemptsWereMade() throws InterruptedException {
        mDispatchError = true;
        mTargeting.put(KEY_CACHE_ID, CACHE_ID);
        mTargeting.put(KEY_UUID, CACHE_UUID);

        WhiteBox.setInternalState(mBid, "mNurl", String.format("http://%1$s/cache?uuid=nurl", mCacheHost));
        mBid.setAdm(null);

        mWinNotifier.notifyWin(mMockBidResponse, mMockListener);

        assertEquals(3, mMockWebServer.getRequestCount());
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=id");
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=uuid");
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=nurl");

        verify(mMockListener).onResult();
        assertNull(mBid.getAdm());
    }

    @Test
    public void whenWinUrlIsEpty_WinUrlWasSkipped() throws InterruptedException {
        mTargeting.put(KEY_CACHE_ID, CACHE_ID);
        mTargeting.put(KEY_UUID, CACHE_UUID);
        WhiteBox.setInternalState(mBid, "mNurl", "");

        String test = "";
        int length = test.length();
        boolean isEmpty = TextUtils.isEmpty(test);

        mWinNotifier.notifyWin(mMockBidResponse, mMockListener);

        assertEquals(2, mMockWebServer.getRequestCount());
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=id");
        verifyRequest(mMockWebServer.takeRequest(), "/cache?uuid=uuid");

        verify(mMockListener).onResult();
        assertNotNull(mBid.getAdm());
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
                if (mDispatchError) {
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