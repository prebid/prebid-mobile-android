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

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class InterstitialSizesTest {

    @Test
    public void isPortraitFalse_whenNullPassed() {
        assertFalse(InterstitialSizes.isPortrait(null));
    }

    @Test
    public void isPortraitFalse_whenNotPortraitSize() {
        assertFalse(InterstitialSizes.isPortrait("111x111"));
    }

    @Test
    public void isPortraitTrue_whenPortraitSize() {
        assertTrue(InterstitialSizes.isPortrait("270x480"));
    }

    @Test
    public void isLandscapeFalse_whenNullPassed() {
        assertFalse(InterstitialSizes.isLandscape(null));
    }

    @Test
    public void isLandscape_whenNotLandscapeSize() {
        assertFalse(InterstitialSizes.isLandscape("111x111"));
    }

    @Test
    public void isLandscapeTrue_whenLandscapeSize() {
        assertTrue(InterstitialSizes.isLandscape("480x320"));
    }
}