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

package org.prebid.mobile.rendering.interstitial;

import android.content.pm.ActivityInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class InterstitialLayoutConfiguratorTest {

    private InterstitialDisplayPropertiesInternal mDisplayProperties = new InterstitialDisplayPropertiesInternal();
    private AdConfiguration mAdConfiguration = new AdConfiguration();

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void orientationPortrait_whenNoLayoutAndSize() {
        InterstitialLayoutConfigurator.configureDisplayProperties(mAdConfiguration, mDisplayProperties);
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, mDisplayProperties.orientation);
        assertFalse(mDisplayProperties.isRotationEnabled);
    }

    @Test
    public void rotationEnabled_whenNoLayoutAndSizeIsAspectRatio() {
        mAdConfiguration.setInterstitialSize(InterstitialSizes.InterstitialSize.ASPECT_RATIO_300x200);
        InterstitialLayoutConfigurator.configureDisplayProperties(mAdConfiguration, mDisplayProperties);
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, mDisplayProperties.orientation);
        assertTrue(mDisplayProperties.isRotationEnabled);
    }

    @Test
    public void orientationLandscapeAndNoRotation_whenSizeIsLandscape() {
        mAdConfiguration.setInterstitialSize(InterstitialSizes.InterstitialSize.LANDSCAPE_480x320);
        InterstitialLayoutConfigurator.configureDisplayProperties(mAdConfiguration, mDisplayProperties);
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE, mDisplayProperties.orientation);
        assertFalse(mDisplayProperties.isRotationEnabled);
    }

    @Test
    public void orientationPortraitAndNoRotation_whenSizeIsVertical() {
        mAdConfiguration.setInterstitialSize(InterstitialSizes.InterstitialSize.VERTICAL_270x480);
        InterstitialLayoutConfigurator.configureDisplayProperties(mAdConfiguration, mDisplayProperties);
        assertEquals(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT, mDisplayProperties.orientation);
        assertFalse(mDisplayProperties.isRotationEnabled);
    }
}