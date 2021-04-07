package com.openx.apollo.models;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.ntv.NativeAdConfiguration;
import com.openx.apollo.sdk.ApolloSettings;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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
        assertEquals(ApolloSettings.AUTO_REFRESH_DELAY_DEFAULT, adConfig.getAutoRefreshDelay());
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