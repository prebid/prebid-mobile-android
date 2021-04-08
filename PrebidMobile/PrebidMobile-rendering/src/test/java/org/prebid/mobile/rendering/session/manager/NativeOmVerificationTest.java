package org.prebid.mobile.rendering.session.manager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAdEventTracker;
import org.prebid.mobile.rendering.models.openrtb.bidRequests.Ext;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

@RunWith(RobolectricTestRunner.class)
public class NativeOmVerificationTest {

    @Test
    public void constructor_ValuesProperlyExtracted() {
        Ext ext = new Ext();
        ext.put("vendorKey", "key");
        ext.put("verification_parameters", "parameters");

        NativeAdEventTracker nativeAdEventTracker = Mockito.mock(NativeAdEventTracker.class);
        Mockito.when(nativeAdEventTracker.getUrl()).thenReturn("url");
        Mockito.when(nativeAdEventTracker.getExt()).thenReturn(ext);

        NativeOmVerification nativeOmVerification = new NativeOmVerification(nativeAdEventTracker);
        assertEquals("url", nativeOmVerification.getOmidJsUrl());
        assertEquals("key", nativeOmVerification.getVendorKey());
        assertEquals("parameters", nativeOmVerification.getVerificationParameters());
    }

    @Test
    public void constructor_EventTrackerMissingExt_ValuesNotSet() {
        NativeAdEventTracker nativeAdEventTracker = Mockito.mock(NativeAdEventTracker.class);
        Mockito.when(nativeAdEventTracker.getUrl()).thenReturn("url");

        NativeOmVerification nativeOmVerification = new NativeOmVerification(nativeAdEventTracker);
        assertEquals("url", nativeOmVerification.getOmidJsUrl());
        assertNull(nativeOmVerification.getVendorKey());
        assertNull(nativeOmVerification.getVerificationParameters());
    }
}