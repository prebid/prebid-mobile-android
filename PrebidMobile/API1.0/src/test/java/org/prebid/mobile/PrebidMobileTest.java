/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class PrebidMobileTest extends BaseSetup {
    @Test
    public void testPrebidMobileSettings() {
        PrebidMobile.setPrebidServerAccountId("123456");
        assertEquals("123456", PrebidMobile.getPrebidServerAccountId());
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        assertEquals(activity.getApplicationContext(), PrebidMobile.getApplicationContext());
        PrebidMobile.setShareGeoLocation(true);
        assertTrue(PrebidMobile.isShareGeoLocation());
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        assertEquals(Host.RUBICON, PrebidMobile.getPrebidServerHost());
    }
}
