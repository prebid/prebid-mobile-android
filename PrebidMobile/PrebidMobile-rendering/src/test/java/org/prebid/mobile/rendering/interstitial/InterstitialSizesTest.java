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