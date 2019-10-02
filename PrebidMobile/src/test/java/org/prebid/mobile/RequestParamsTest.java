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

import java.util.HashSet;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class RequestParamsTest {
    @Test
    public void testCreation() throws Exception {
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(320, 50));
        RequestParams requestParams = new RequestParams("123456", AdType.BANNER, sizes);
        assertEquals("123456", FieldUtils.readField(requestParams, "configId", true));
        assertEquals(AdType.BANNER, FieldUtils.readField(requestParams, "adType", true));
        assertEquals(sizes, FieldUtils.readField(requestParams, "sizes", true));
        requestParams = new RequestParams("123456", AdType.INTERSTITIAL, null);
        assertEquals("123456", FieldUtils.readField(requestParams, "configId", true));
        assertEquals(AdType.INTERSTITIAL, FieldUtils.readField(requestParams, "adType", true));
        assertEquals(null, FieldUtils.readField(requestParams, "sizes", true));
    }

    @Test
    public void testCreationWithAdditionalMap() throws Exception {
        HashSet<AdSize> sizes = new HashSet<>();
        sizes.add(new AdSize(500, 700));

        AdSize minSizePerc = new AdSize(50, 70);

        RequestParams requestParams = new RequestParams("123456", AdType.INTERSTITIAL, sizes, null, null, minSizePerc);

        AdSize minAdSizePerc = requestParams.getMinSizePerc();
        assertNotNull(minAdSizePerc);

        assertTrue(minAdSizePerc.getWidth() == 50 && minAdSizePerc.getHeight() == 70);
    }
}
