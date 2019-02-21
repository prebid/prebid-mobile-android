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

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

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
    public void testSetUserKeyword() throws Exception {
        InterstitialAdUnit adUnit = new InterstitialAdUnit("12345");
        adUnit.addUserKeyword("key", "value");
        adUnit.addUserKeyword("key1", null);
        @SuppressWarnings("unchecked")
        ArrayList<String> keywords = (ArrayList<String>) FieldUtils.readField(adUnit, "keywords", true);
        assertEquals(2, keywords.size());
        assertEquals("key=value", keywords.get(0));
        assertEquals("key1", keywords.get(1));
        adUnit.addUserKeyword("key", "value2");
        assertEquals(3, keywords.size());
        assertEquals("key=value", keywords.get(0));
        assertEquals("key1", keywords.get(1));
        assertEquals("key=value2", keywords.get(2));
        adUnit.removeUserKeyword("key");
        assertEquals(1, keywords.size());
        assertEquals("key1", keywords.get(0));
        adUnit.clearUserKeywords();
        assertEquals(0, keywords.size());
    }

    @Test
    public void testSetUserKeywords() throws Exception {
        InterstitialAdUnit adUnit = new InterstitialAdUnit("123456");
        adUnit.addUserKeyword("key1", "value1");
        String[] values = {"value1", "value2"};
        adUnit.addUserKeywords("key2", values);
        @SuppressWarnings("unchecked")
        ArrayList<String> keywords = (ArrayList<String>) FieldUtils.readField(adUnit, "keywords", true);
        assertEquals(2, keywords.size());
        assertEquals("key2=value1", keywords.get(0));
        assertEquals("key2=value2", keywords.get(1));
        adUnit.addUserKeywords("key1", values);
        assertEquals(2, keywords.size());
        assertEquals("key1=value1", keywords.get(0));
        assertEquals("key1=value2", keywords.get(1));
    }
}
