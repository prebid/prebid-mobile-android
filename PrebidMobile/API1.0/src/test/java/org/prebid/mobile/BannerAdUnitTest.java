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

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class BannerAdUnitTest {
    @Test
    public void testBannerAdUnitCreation() throws Exception {
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        assertEquals(1, adUnit.getSizes().size());
        assertEquals("123456", FieldUtils.readField(adUnit, "configId", true));
        assertEquals(AdType.BANNER, FieldUtils.readField(adUnit, "adType", true));
        assertEquals(0, FieldUtils.readField(adUnit, "periodMillis", true));
    }

    @Test
    public void testBannerAdUnitAddSize() throws Exception {
        BannerAdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addAdditionalSize(300, 250);
        assertEquals(2, adUnit.getSizes().size());
        adUnit.addAdditionalSize(320, 50);
        assertEquals(2, adUnit.getSizes().size());
    }
}
