package org.prebid.mobile.api.original;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerParameters;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
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
    public void autoRefreshIntervalSetBeforeFetchDemand_appliesToEveryFetch() {
        subject = new PrebidAdUnit(configId);
        subject.setAutoRefreshInterval(30);

        PrebidRequest prebidRequest = new PrebidRequest();
        prebidRequest.setBannerParameters(new BannerParameters());

        subject.fetchDemand(prebidRequest, bidInfo -> {});
        AdUnit firstAdUnit = innerAdUnit();
        assertEquals(30_000, firstAdUnit.getConfiguration().getAutoRefreshDelay());

        // Every fetch creates a new inner ad unit, so the interval must carry over
        subject.fetchDemand(prebidRequest, bidInfo -> {});
        assertNotSame(firstAdUnit, innerAdUnit());
        assertEquals(30_000, innerAdUnit().getConfiguration().getAutoRefreshDelay());
    }

    private AdUnit innerAdUnit() {
        return Reflection.getFieldOf(subject, "adUnit");
    }

}
