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
    public void testSetStoreRequestId() {
        ConfigSettings.setStoreRequestId("testStoredRequestId");
        assertEquals("testStoredRequestId", ConfigSettings.getStoreRequestIdtoreRequestId());
    }
}
