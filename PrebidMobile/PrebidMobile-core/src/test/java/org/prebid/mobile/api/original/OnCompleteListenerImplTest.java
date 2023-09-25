package org.prebid.mobile.api.original;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.prebid.mobile.NativeParameters;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.HashMap;

public class OnCompleteListenerImplTest {

    @Mock
    private PrebidRequest mockPrebidRequest;
    @Mock
    private MultiformatAdUnitFacade mockAdUnit;
    @Mock
    private OnFetchDemandResult mockListener;
    @Mock
    private BidResponse mockBidResponse;

    private AutoCloseable closeable;

    @Before
    public void setup() {
        closeable = openMocks(this);
    }

    @After
    public void destroy() throws Exception {
        closeable.close();
    }


    @Test
    public void emptyBidResponse() {
        OnCompleteListenerImpl subject = new OnCompleteListenerImpl(
                mockAdUnit,
                mockPrebidRequest,
                null,
                mockListener
        );

        when(mockAdUnit.getBidResponse()).thenReturn(null);

        subject.onComplete(ResultCode.NO_BIDS);

        ArgumentCaptor<BidInfo> argumentCaptor = ArgumentCaptor.forClass(BidInfo.class);
        verify(mockListener).onComplete(argumentCaptor.capture());

        BidInfo bidInfo = argumentCaptor.getValue();
        assertNotNull(bidInfo);
        assertEquals(ResultCode.NO_BIDS, bidInfo.getResultCode());
        assertNull(bidInfo.getNativeCacheId());
        assertNull(bidInfo.getExpirationTimeSeconds());
        assertNull(bidInfo.getTargetingKeywords());
    }

    @Test
    public void fullBidResponse() {
        OnCompleteListenerImpl subject = new OnCompleteListenerImpl(
                mockAdUnit,
                mockPrebidRequest,
                null,
                mockListener
        );

        HashMap<String, String> keywords = new HashMap<>();
        keywords.put("key1", "value1");
        keywords.put("key2", "value2");
        when(mockBidResponse.getTargeting()).thenReturn(keywords);
        when(mockAdUnit.getBidResponse()).thenReturn(mockBidResponse);

        subject.onComplete(ResultCode.SUCCESS);

        ArgumentCaptor<BidInfo> argumentCaptor = ArgumentCaptor.forClass(BidInfo.class);
        verify(mockListener).onComplete(argumentCaptor.capture());

        BidInfo bidInfo = argumentCaptor.getValue();
        assertNotNull(bidInfo);
        assertEquals(ResultCode.SUCCESS, bidInfo.getResultCode());
        assertEquals(keywords, bidInfo.getTargetingKeywords());
        assertNull(bidInfo.getNativeCacheId());
        assertNull(bidInfo.getExpirationTimeSeconds());
    }

    @Test
    public void fullBidResponseWithNative() {
        OnCompleteListenerImpl subject = new OnCompleteListenerImpl(
                mockAdUnit,
                mockPrebidRequest,
                null,
                mockListener
        );

        HashMap<String, String> keywords = new HashMap<>();
        keywords.put("key1", "value1");
        keywords.put("key2", "value2");
        when(mockPrebidRequest.getNativeParameters()).thenReturn(mock(NativeParameters.class));
        when(mockBidResponse.getTargeting()).thenReturn(keywords);
        when(mockAdUnit.getBidResponse()).thenReturn(mockBidResponse);
        when(mockBidResponse.getExpirationTimeSeconds()).thenReturn(300);

        subject.onComplete(ResultCode.SUCCESS);

        ArgumentCaptor<BidInfo> argumentCaptor = ArgumentCaptor.forClass(BidInfo.class);
        verify(mockListener).onComplete(argumentCaptor.capture());

        BidInfo bidInfo = argumentCaptor.getValue();
        assertNotNull(bidInfo);
        assertEquals(ResultCode.SUCCESS, bidInfo.getResultCode());
        assertEquals(keywords, bidInfo.getTargetingKeywords());
        assertEquals(Integer.valueOf(300), bidInfo.getExpirationTimeSeconds());
    }

}