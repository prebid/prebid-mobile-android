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

package org.prebid.mobile.rendering.models;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdConfigurationTest {

    private AdConfiguration mAdConfiguration;

    @Before
    public void setUp() throws Exception {
        mAdConfiguration = new AdConfiguration();
    }

    @Test
    public void testAdConfiguration() {
        AdConfiguration adConfig = null;
        AdException err = null;
        adConfig = new AdConfiguration();
        assertNotNull(adConfig);
        assertNull(err);
        assertEquals(PrebidRenderingSettings.AUTO_REFRESH_DELAY_DEFAULT, adConfig.getAutoRefreshDelay());
    }

    @Test
    public void whenIsNativeAndNativeAdConfigurationIsNull_ReturnFalse() {
        assertFalse(mAdConfiguration.isNative());
    }

    @Test
    public void whenIsNativeAndNativeAdConfigurationIsNotNull_ReturnTrue() {
        mAdConfiguration.setNativeAdConfiguration(mock(NativeAdConfiguration.class));
        assertTrue(mAdConfiguration.isNative());
    }
}