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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.HashSet;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdUnitConfigurationTest {

    private AdUnitConfiguration adUnitConfig;

    @Before
    public void setUp() throws Exception {
        adUnitConfig = new AdUnitConfiguration();
    }

    @Test
    public void whenAddSize_SetContainsSize() {
        AdSize adSize = new AdSize(0, 0);
        assertTrue(adUnitConfig.getSizes().isEmpty());

        adUnitConfig.addSize(adSize);
        assertEquals(1, adUnitConfig.getSizes().size());
        assertTrue(adUnitConfig.getSizes().contains(adSize));
    }

    @Test
    public void whenAddSizes_SetContainsSizes() {
        AdSize adSize = new AdSize(0, 0);
        AdSize adSize1 = new AdSize(1, 1);
        assertTrue(adUnitConfig.getSizes().isEmpty());

        adUnitConfig.addSizes(adSize, adSize1);
        assertEquals(2, adUnitConfig.getSizes().size());
        assertTrue(adUnitConfig.getSizes().contains(adSize));
        assertTrue(adUnitConfig.getSizes().contains(adSize1));
    }
}