package org.prebid.mobile.core;

import android.location.Location;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.ArrayList;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 21)
public class TargetingParamsTest {

    @Test
    public void testSetGender() {
        TargetingParams.setGender(TargetingParams.GENDER.FEMALE);
        assertEquals(TargetingParams.GENDER.FEMALE, TargetingParams.getGender());
        TargetingParams.setGender(TargetingParams.GENDER.MALE);
        assertEquals(TargetingParams.GENDER.MALE, TargetingParams.getGender());
        TargetingParams.setGender(TargetingParams.GENDER.UNKNOWN);
        assertEquals(TargetingParams.GENDER.UNKNOWN, TargetingParams.getGender());
    }

    @Test
    public void testSetLocation() {
        TargetingParams.setLocationEnabled(true);
        assertEquals(true, TargetingParams.getLocationEnabled());
        TargetingParams.setLocationEnabled(false);
        assertEquals(false, TargetingParams.getLocationEnabled());
        Location location = new Location("Test");
        location.setLatitude(20.1);
        location.setLongitude(20.2);
        TargetingParams.setLocation(location);
        assertEquals(location, TargetingParams.getLocation());
        TargetingParams.setLocationDecimalDigits(8);
        assertEquals(6, TargetingParams.getLocationDecimalDigits());
        TargetingParams.setLocationDecimalDigits(2);
        assertEquals(2, TargetingParams.getLocationDecimalDigits());
        TargetingParams.setLocationDecimalDigits(-1);
        assertEquals(-1, TargetingParams.getLocationDecimalDigits());
        TargetingParams.setLocationDecimalDigits(-2);
        assertEquals(-1, TargetingParams.getLocationDecimalDigits());
    }

    @Test
    public void testCustomKeywords() {
        // Clearing the custom keywords
        TargetingParams.clearCustomKeywords();
        assertEquals(0, TargetingParams.getCustomKeywords().size());
        // Set one value for one key
        TargetingParams.setCustomTargeting("TestKey1", "TestValue1");
        assertEquals(1, TargetingParams.getCustomKeywords().size());
        assertEquals(1, TargetingParams.getCustomKeywords().get("TestKey1").size());
        assertEquals("TestValue1", TargetingParams.getCustomKeywords().get("TestKey1").get(0));
        // Set another value for the same key
        TargetingParams.setCustomTargeting("TestKey1", "TestValue2");
        assertEquals(1, TargetingParams.getCustomKeywords().size());
        assertEquals(1, TargetingParams.getCustomKeywords().get("TestKey1").size());
        assertEquals("TestValue2", TargetingParams.getCustomKeywords().get("TestKey1").get(0));
        // Set value for a different key
        TargetingParams.setCustomTargeting("TestKey2", "TestValue2");
        assertEquals(2, TargetingParams.getCustomKeywords().size());
        assertEquals(1, TargetingParams.getCustomKeywords().get("TestKey2").size());
        assertEquals("TestValue2", TargetingParams.getCustomKeywords().get("TestKey2").get(0));
        // Override values array for existing key
        ArrayList<String> values = new ArrayList<String>();
        values.add("TestValue3");
        values.add("TestValue4");
        TargetingParams.setCustomTargeting("TestKey1", values);
        assertEquals(2, TargetingParams.getCustomKeywords().size());
        assertEquals(2, TargetingParams.getCustomKeywords().get("TestKey1").size());
        assertEquals("TestValue3", TargetingParams.getCustomKeywords().get("TestKey1").get(0));
        assertEquals("TestValue4", TargetingParams.getCustomKeywords().get("TestKey1").get(1));
        assertEquals(1, TargetingParams.getCustomKeywords().get("TestKey2").size());
        assertEquals("TestValue2", TargetingParams.getCustomKeywords().get("TestKey2").get(0));
        // Remove values for the first key
        TargetingParams.removeCustomKeyword("TestKey1");
        assertEquals(1, TargetingParams.getCustomKeywords().size());
        assertEquals(1, TargetingParams.getCustomKeywords().get("TestKey2").size());
        assertEquals(false, TargetingParams.getCustomKeywords().containsKey("TestKey1"));
    }
}
