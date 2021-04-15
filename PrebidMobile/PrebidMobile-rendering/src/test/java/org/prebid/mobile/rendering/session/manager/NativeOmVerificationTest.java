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