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