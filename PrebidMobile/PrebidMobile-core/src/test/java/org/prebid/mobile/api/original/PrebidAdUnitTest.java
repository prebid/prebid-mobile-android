package org.prebid.mobile.api.original;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;

public class PrebidAdUnitTest {

    private PrebidAdUnit subject;

    private String configId = "testConfigId";

    @Test
    public void nullUserListener_noException() {
        subject = new PrebidAdUnit(configId);

        PrebidRequest prebidRequest = new PrebidRequest();
        subject.fetchDemand(prebidRequest, null);
    }

    @Test
    public void requestWithoutAnyParameters_invalidPrebidRequest() {
        subject = new PrebidAdUnit(configId);

        OnFetchDemandResult listenerMock = mock(OnFetchDemandResult.class);

        PrebidRequest prebidRequest = new PrebidRequest();
        subject.fetchDemand(prebidRequest, listenerMock);

        ArgumentCaptor<BidInfo> captor = ArgumentCaptor.forClass(BidInfo.class);
        verify(listenerMock).onComplete(captor.capture());

        BidInfo bidInfo = captor.getValue();
        assertNotNull(bidInfo);
        assertEquals(ResultCode.INVALID_PREBID_REQUEST_OBJECT, bidInfo.getResultCode());
        assertNull(bidInfo.getTargetingKeywords());
    }

}