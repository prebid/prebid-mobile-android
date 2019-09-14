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

import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class AdUnitTest {

    @Test
    public void testSetUserKeyword() throws Exception {
        AdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addUserKeyword("key", "value");
        adUnit.addUserKeyword("key1", null);
        @SuppressWarnings("unchecked")
        Set<String> keywords = TargetingParams.getUserKeywordsSet();
        assertEquals(1, keywords.size());
        assertTrue(keywords.contains("value"));
        adUnit.addUserKeyword("key", "value2");
        assertEquals(2, keywords.size());
        assertTrue(keywords.contains("value") && keywords.contains("value2"));
        adUnit.removeUserKeyword("value");
        assertEquals(1, keywords.size());
        adUnit.clearUserKeywords();
        assertEquals(0, keywords.size());
    }

    @Test
    public void testSetUserKeywords() throws Exception {
        AdUnit adUnit = new BannerAdUnit("123456", 320, 50);
        adUnit.addUserKeyword("key1", "value1");
        String[] values = {"value1", "value2"};
        adUnit.addUserKeywords("key2", values);
        @SuppressWarnings("unchecked")
        Set<String> keywords = TargetingParams.getUserKeywordsSet();
        assertEquals(2, keywords.size());
        assertTrue(keywords.contains("value1") && keywords.contains("value2"));
        adUnit.addUserKeywords("key1", values);
        assertEquals(2, keywords.size());
        assertTrue(keywords.contains("value1") && keywords.contains("value2"));
    }
}
