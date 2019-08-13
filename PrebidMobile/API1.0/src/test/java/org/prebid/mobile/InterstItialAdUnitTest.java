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

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class InterstItialAdUnitTest {
    @Test
    public void testInterstitialAdUnitCreation() throws Exception {
        InterstitialAdUnit adUnit = new InterstitialAdUnit("12345");
        assertEquals("12345", FieldUtils.readField(adUnit, "configId", true));
        assertEquals(AdType.INTERSTITIAL, FieldUtils.readField(adUnit, "adType", true));
    }


    @Test
    public void testAdvancedInterstitialAdUnitCreation() throws Exception {
        InterstitialAdUnit adUnit = new InterstitialAdUnit("12345", 50, 70);
        assertEquals(AdType.INTERSTITIAL, FieldUtils.readField(adUnit, "adType", true));

        assertNotNull(adUnit.getMinSizePerc());
        assertTrue(adUnit.getMinSizePerc().getWidth() == 50 && adUnit.getMinSizePerc().getHeight() == 70);
    }

}
