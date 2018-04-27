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
public class TargetingParamsTest extends BaseSetup {
    @Test
    public void testIsSubjectToGDPR() {
        assertEquals(null, TargetingParams.isSubjectToGDPR(activity));
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(TargetingParams.IABConsent_SubjectToGDPR, "1");
        editor.commit();
        assertEquals(Boolean.TRUE, TargetingParams.isSubjectToGDPR(activity));
        TargetingParams.setSubjectToGDPR(activity, false);
        assertEquals(Boolean.FALSE, TargetingParams.isSubjectToGDPR(activity));
        TargetingParams.setSubjectToGDPR(activity, true);
        assertEquals(Boolean.TRUE, TargetingParams.isSubjectToGDPR(activity));
        editor = pref.edit();
        // test that PREBID GDPR settings have higher priority than IAB settings
        editor.putString(TargetingParams.IABConsent_SubjectToGDPR, "0");
        editor.commit();
        assertEquals(Boolean.TRUE, TargetingParams.isSubjectToGDPR(activity));
        assertEquals(null, TargetingParams.getGDPRConsentString(activity));
        editor.putString(TargetingParams.IABConsent_ConsentString, "Hello");
        editor.commit();
        assertEquals("Hello", TargetingParams.getGDPRConsentString(activity));
        TargetingParams.setGDPRConsentString(activity, "World");
        assertEquals("World", TargetingParams.getGDPRConsentString(activity));
        editor.putString(TargetingParams.IABConsent_ConsentString, "Hahaha");
        editor.commit();
        assertEquals("World", TargetingParams.getGDPRConsentString(activity));
    }

    @Test
    public void testSetYearOfBirth() throws Exception {
        TargetingParams.setYearOfBirth(-1);
        assertTrue(TargetingParams.getYearOfBirth() != -1);
        int year = Calendar.getInstance().get(Calendar.YEAR) + 1;
        TargetingParams.setYearOfBirth(year);
        assertTrue(TargetingParams.getYearOfBirth() != year);
        year = Calendar.getInstance().get(Calendar.YEAR) - 5;
        TargetingParams.setYearOfBirth(year);
        assertTrue(TargetingParams.getYearOfBirth() == year);
    }

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
    public void testSetUserKeywords() throws Exception {
        TargetingParams.clearUserKeywords();
        TargetingParams.setUserTargeting("key1", "value2");
        assertEquals(1, TargetingParams.getUserKeywords().size());
        assertEquals("key1=value2", TargetingParams.getUserKeywords().get(0));
        TargetingParams.removeUserKeyword("key1");
        assertEquals(0, TargetingParams.getUserKeywords().size());
        TargetingParams.setUserTargeting("key2", null);
        assertEquals(1, TargetingParams.getUserKeywords().size());
        assertEquals("key2", TargetingParams.getUserKeywords().get(0));
        TargetingParams.setUserTargeting(null, "value2");
        assertEquals(1, TargetingParams.getUserKeywords().size());
        assertEquals("key2", TargetingParams.getUserKeywords().get(0));
        TargetingParams.removeUserKeyword("key2");
        assertEquals(0, TargetingParams.getUserKeywords().size());
        TargetingParams.setUserTargeting(null, null);
        assertEquals(0, TargetingParams.getUserKeywords().size());
    }

    @Test
    public void testSetBundleName() throws Exception {
        TargetingParams.setBundleName("Prebid Mobile DemoApp");
        assertEquals("Prebid Mobile DemoApp", TargetingParams.getBundleName());
    }

    @Test
    public void testSetDomain() throws Exception {
        TargetingParams.setDomain("http://www.prebid.org");
        assertEquals("http://www.prebid.org", TargetingParams.getDomain());
    }

    @Test
    public void testSetStoreUrl() throws Exception {
        TargetingParams.setStoreUrl("https://play.google.com/store/apps/details?id=com.appnexus.opensdkapp&hl=en");
        assertEquals("https://play.google.com/store/apps/details?id=com.appnexus.opensdkapp&hl=en", TargetingParams.getStoreUrl());
    }

    @Test
    public void testSetPrivacyPolicy() throws Exception {
        TargetingParams.setPrivacyPolicy(1);
        assertEquals(1, TargetingParams.getPrivacyPolicy());
        TargetingParams.setPrivacyPolicy(2);
        assertEquals(1, TargetingParams.getPrivacyPolicy());
        TargetingParams.setPrivacyPolicy(0);
        assertEquals(0, TargetingParams.getPrivacyPolicy());
    }
}
