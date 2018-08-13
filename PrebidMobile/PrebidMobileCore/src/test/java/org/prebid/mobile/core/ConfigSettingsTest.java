package org.prebid.mobile.core;

import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.unittestutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class ConfigSettingsTest extends BaseSetup {

    @Test
    public void testSetPriceGranularity() {
        assertEquals(ConfigSettings.PriceGranularity.UNKNOWN, ConfigSettings.getPriceGranularity());
        ConfigSettings.setPriceGranularity(ConfigSettings.PriceGranularity.LOW);
        assertEquals(ConfigSettings.PriceGranularity.LOW, ConfigSettings.getPriceGranularity());
        ConfigSettings.setPriceGranularity(ConfigSettings.PriceGranularity.MED);
        assertEquals(ConfigSettings.PriceGranularity.MED, ConfigSettings.getPriceGranularity());
        ConfigSettings.setPriceGranularity(ConfigSettings.PriceGranularity.HIGH);
        assertEquals(ConfigSettings.PriceGranularity.HIGH, ConfigSettings.getPriceGranularity());
        ConfigSettings.setPriceGranularity(ConfigSettings.PriceGranularity.DENSE);
        assertEquals(ConfigSettings.PriceGranularity.DENSE, ConfigSettings.getPriceGranularity());
        ConfigSettings.setPriceGranularity(ConfigSettings.PriceGranularity.AUTO);
        assertEquals(ConfigSettings.PriceGranularity.AUTO, ConfigSettings.getPriceGranularity());
    }

    @Test
    public void testSetStoreRequestId() {
        ConfigSettings.setStoreRequestId("testStoredRequestId");
        assertEquals("testStoredRequestId2", ConfigSettings.getStoreRequestIdtoreRequestId());
    }
}
