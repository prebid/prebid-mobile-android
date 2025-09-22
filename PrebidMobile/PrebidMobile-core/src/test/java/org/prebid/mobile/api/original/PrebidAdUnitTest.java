package org.prebid.mobile.api.original;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.rendering.models.AdPosition;

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

    @Test
    public void requestWithNoAdPositionSet_equalsUndefined() {
        subject = new PrebidAdUnit(configId);

        OnFetchDemandResult listenerMock = mock(OnFetchDemandResult.class);

        PrebidRequest prebidRequest = new PrebidRequest();
        VideoParameters parameters = new VideoParameters(Lists.newArrayList("video/mp4"));
        parameters.setAdSize(new org.prebid.mobile.AdSize(320, 480));
        prebidRequest.setVideoParameters(parameters);

        subject.fetchDemand(prebidRequest, listenerMock);

        assertNotNull(subject.getAdConfigurationPosition());
        assertEquals(AdPosition.UNDEFINED, subject.getAdConfigurationPosition());
    }

    @Test
    public void requestWithCustomAdPosition_equalsCustomOne() {
        subject = new PrebidAdUnit(configId);

        OnFetchDemandResult listenerMock = mock(OnFetchDemandResult.class);

        PrebidRequest prebidRequest = new PrebidRequest();
        VideoParameters parameters = new VideoParameters(Lists.newArrayList("video/mp4"));
        prebidRequest.setVideoParameters(parameters);

        subject.setAdPosition(AdPosition.SIDEBAR);
        subject.fetchDemand(prebidRequest, listenerMock);

        assertNotNull(subject.getAdConfigurationPosition());
        assertEquals(AdPosition.SIDEBAR, subject.getAdConfigurationPosition());
    }
}