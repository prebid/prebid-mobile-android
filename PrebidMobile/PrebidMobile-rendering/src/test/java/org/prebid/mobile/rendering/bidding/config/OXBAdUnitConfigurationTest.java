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

package org.prebid.mobile.rendering.bidding.config;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class OXBAdUnitConfigurationTest {

    private AdConfiguration mAdUnitConfig;

    @Before
    public void setUp() throws Exception {
        mAdUnitConfig = new AdConfiguration();
    }

    @Test
    public void whenAddSize_SetContainsSize() {
        AdSize adSize = new AdSize(0, 0);
        assertTrue(mAdUnitConfig.getAdSizes().isEmpty());

        mAdUnitConfig.addSize(adSize);
        assertEquals(1, mAdUnitConfig.getAdSizes().size());
        assertTrue(mAdUnitConfig.getAdSizes().contains(adSize));
    }

    @Test
    public void whenAddSizes_SetContainsSizes() {
        AdSize adSize = new AdSize(0, 0);
        AdSize adSize1 = new AdSize(1, 1);
        assertTrue(mAdUnitConfig.getAdSizes().isEmpty());

        mAdUnitConfig.addSizes(adSize, adSize1);
        assertEquals(2, mAdUnitConfig.getAdSizes().size());
        assertTrue(mAdUnitConfig.getAdSizes().contains(adSize));
        assertTrue(mAdUnitConfig.getAdSizes().contains(adSize1));
    }

    @Test
    public void whenAddContextData_ContextDataAdded() {
        mAdUnitConfig.addContextData("key", "value");
        assertTrue(mAdUnitConfig.getContextDataDictionary().containsKey("key"));
        assertTrue(mAdUnitConfig.getContextDataDictionary().get("key").contains("value"));
    }

    @Test
    public void whenUpdateContextData_ContextDataUpdated() {
        mAdUnitConfig.addContextData("key", "test");
        Set<String> stringSet = new HashSet<>();
        stringSet.add("value0");
        stringSet.add("value1");
        mAdUnitConfig.updateContextData("key", stringSet);
        assertTrue(mAdUnitConfig.getContextDataDictionary().get("key").containsAll(stringSet));
    }

    @Test
    public void whenRemoveContextData_ContextDataRemoved() {
        mAdUnitConfig.addContextData("key", "value");
        mAdUnitConfig.addContextData("key1", "value");
        assertTrue(mAdUnitConfig.getContextDataDictionary().containsKey("key"));
        mAdUnitConfig.removeContextData("key");
        assertFalse(mAdUnitConfig.getContextDataDictionary().containsKey("key"));
    }

    @Test
    public void whenClearContextData_ContextDataCleared() {
        mAdUnitConfig.addContextData("key", "value");
        mAdUnitConfig.addContextData("key1", "value");
        assertFalse(mAdUnitConfig.getContextDataDictionary().isEmpty());
        mAdUnitConfig.clearContextData();
        assertTrue(mAdUnitConfig.getContextDataDictionary().isEmpty());
    }

    @Test
    public void whenAddContextKeyword_ContextKeywordAdded() {
        mAdUnitConfig.addContextKeyword("test");
        assertTrue(mAdUnitConfig.getContextKeywordsSet().contains("test"));
    }

    @Test
    public void whenAddContextKeywords_ContextKeywordsAdded() {
        Set<String> stringSet = new HashSet<>();
        stringSet.add("value0");
        stringSet.add("value1");
        mAdUnitConfig.addContextKeywords(stringSet);
        assertTrue(mAdUnitConfig.getContextKeywordsSet().containsAll(stringSet));
    }

    @Test
    public void whenRemoveContextKeyword_ContextKeywordRemoved() {
        mAdUnitConfig.addContextKeyword("test");
        mAdUnitConfig.addContextKeyword("keyword");
        mAdUnitConfig.removeContextKeyword("test");
        assertFalse(mAdUnitConfig.getContextKeywordsSet().isEmpty());
        assertFalse(mAdUnitConfig.getContextKeywordsSet().contains("test"));
    }

    @Test
    public void whenClearContextKeyword_ContextKeywordCleared() {
        mAdUnitConfig.addContextKeyword("test");
        mAdUnitConfig.addContextKeyword("keyword");
        assertFalse(mAdUnitConfig.getContextKeywordsSet().isEmpty());

        mAdUnitConfig.clearContextKeywords();
        assertTrue(mAdUnitConfig.getContextKeywordsSet().isEmpty());
    }
}