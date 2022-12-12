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

package org.prebid.mobile.rendering.sdk.deviceData.managers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.reflection.sdk.UserConsentManagerReflection;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UserConsentManagerTest {

    protected Context context = mock(Context.class);
    protected SharedPreferences sharedPreferences = mock(SharedPreferences.class);
    private UserConsentManager userConsentManager;

    @Before
    public void setUp() throws Exception {
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        userConsentManager = new UserConsentManager(activity);

        resetAllPreferences();
    }

    @After
    public void destroy() {
        resetAllPreferences();
    }

    private void resetAllPreferences() {
        sharedPreferences
            .edit()
            .remove(UserConsentManagerReflection.getConstGdpr2Subject(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2Consent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2PurposeConsent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstUsPrivacyString(userConsentManager))
            .remove(UserConsentManager.GPP_STRING_KEY)
            .remove(UserConsentManager.GPP_SID_KEY)
            .apply();

        UserConsentManagerReflection.resetAllFields(userConsentManager);
    }

    @Test
    public void checkConstants() {
        // GDPR 2
        assertEquals(
            "IABTCF_gdprApplies",
            UserConsentManagerReflection.getConstGdpr2Subject(userConsentManager)
        );
        assertEquals(
            "IABTCF_TCString",
            UserConsentManagerReflection.getConstGdpr2Consent(userConsentManager)
        );
        assertEquals(
            "IABTCF_PurposeConsents",
            UserConsentManagerReflection.getConstGdpr2PurposeConsent(userConsentManager)
        );

        // CCPA
        assertEquals(
            "IABUSPrivacy_String",
            UserConsentManagerReflection.getConstUsPrivacyString(userConsentManager)
        );

        // GPP
        assertEquals(
            "IABGPP_HDR_GppString",
            UserConsentManager.GPP_STRING_KEY
        );
        assertEquals(
            "IABGPP_GppSID",
            UserConsentManager.GPP_SID_KEY
        );
    }

    @Test
    public void subjectToCoppa() {
        Boolean subjectToCoppa = userConsentManager.getSubjectToCoppa();
        assertNull(subjectToCoppa);

        userConsentManager.setSubjectToCoppa(true);
        assertTrue(userConsentManager.getSubjectToCoppa());
        assertTrue(UserConsentManagerReflection.getPrebidCoppaConsent(userConsentManager));

        userConsentManager.setSubjectToCoppa(false);
        assertFalse(userConsentManager.getSubjectToCoppa());
        assertFalse(UserConsentManagerReflection.getPrebidCoppaConsent(userConsentManager));

        userConsentManager.setSubjectToCoppa(null);
        assertNull(userConsentManager.getSubjectToCoppa());
        assertNull(UserConsentManagerReflection.getPrebidCoppaConsent(userConsentManager));
    }

    @Test
    public void subjectToGdpr() {
        String gdpr2SubjectKey = UserConsentManagerReflection.getConstGdpr2Subject(userConsentManager);

        assertNull(userConsentManager.getSubjectToGdpr());
        assertNull(userConsentManager.getRealSubjectToGdprBoolean());

        userConsentManager.setSubjectToGdpr(true);
        assertTrue(userConsentManager.getSubjectToGdpr());
        assertTrue(UserConsentManagerReflection.getPrebidGdprSubject(userConsentManager));
        assertNull(userConsentManager.getRealSubjectToGdprBoolean());

        userConsentManager.setSubjectToGdpr(false);
        assertFalse(userConsentManager.getSubjectToGdpr());
        assertFalse(UserConsentManagerReflection.getPrebidGdprSubject(userConsentManager));
        assertNull(userConsentManager.getRealSubjectToGdprBoolean());

        userConsentManager.setSubjectToGdpr(null);
        assertNull(userConsentManager.getSubjectToGdpr());
        assertNull(userConsentManager.getRealSubjectToGdprBoolean());
        assertNull(UserConsentManagerReflection.getPrebidGdprSubject(userConsentManager));

        // Update from outside
        sharedPreferences
            .edit()
            .putInt(gdpr2SubjectKey, 1)
            .apply();
        assertEquals(true, userConsentManager.getSubjectToGdpr());
        assertTrue(userConsentManager.getRealSubjectToGdprBoolean());
        assertNull(UserConsentManagerReflection.getPrebidGdprSubject(userConsentManager));
    }

    @Test
    public void gdprConsent() {
        String gdpr2ConsentKey = UserConsentManagerReflection.getConstGdpr2Consent(userConsentManager);

        assertNull(userConsentManager.getGdprConsent());

        userConsentManager.setGdprConsent("0");
        assertEquals("0", userConsentManager.getGdprConsent());
        assertEquals("0", UserConsentManagerReflection.getPrebidGdprConsent(userConsentManager));

        userConsentManager.setGdprConsent("1");
        assertEquals("1", userConsentManager.getGdprConsent());
        assertEquals("1", UserConsentManagerReflection.getPrebidGdprConsent(userConsentManager));

        userConsentManager.setGdprConsent(null);
        assertNull(userConsentManager.getGdprConsent());
        assertNull(UserConsentManagerReflection.getPrebidGdprConsent(userConsentManager));

        // Update from outside
        sharedPreferences
            .edit()
            .putString(gdpr2ConsentKey, "2")
            .apply();
        assertEquals("2", userConsentManager.getGdprConsent());
        preferencesValueEqual("2", gdpr2ConsentKey);
    }

    @Test
    public void gdprPurposeConsents() {
        String purposeConsentKey = UserConsentManagerReflection.getConstGdpr2PurposeConsent(userConsentManager);

        assertNull(userConsentManager.getGdprPurposeConsents());

        userConsentManager.setGdprPurposeConsents("0");
        assertEquals("0", userConsentManager.getGdprPurposeConsents());
        assertEquals("0", UserConsentManagerReflection.getPrebidGdprPurposeConsent(userConsentManager));

        userConsentManager.setGdprPurposeConsents("1");
        assertEquals("1", userConsentManager.getGdprPurposeConsents());
        assertEquals("1", UserConsentManagerReflection.getPrebidGdprPurposeConsent(userConsentManager));

        userConsentManager.setGdprPurposeConsents(null);
        assertNull(userConsentManager.getGdprPurposeConsents());
        assertNull(UserConsentManagerReflection.getPrebidGdprPurposeConsent(userConsentManager));

        // Update from outside
        sharedPreferences
            .edit()
            .putString(purposeConsentKey, "2")
            .apply();
        assertEquals("2", userConsentManager.getGdprPurposeConsents());
        assertNull(UserConsentManagerReflection.getPrebidGdprPurposeConsent(userConsentManager));
    }

    @Test
    public void gdprPurposeConsent() {
        assertNull(userConsentManager.getGdprPurposeConsent(0));
        assertNull(userConsentManager.getGdprPurposeConsents());

        userConsentManager.setGdprPurposeConsents("01");
        assertEquals(Boolean.FALSE, userConsentManager.getGdprPurposeConsent(0));
        assertEquals(Boolean.TRUE, userConsentManager.getGdprPurposeConsent(1));
        assertNull(userConsentManager.getGdprPurposeConsent(2));
    }

    @Test
    public void usPrivacyString() {
        String usPrivacyKey = UserConsentManagerReflection.getConstUsPrivacyString(userConsentManager);

        assertNull(userConsentManager.getUsPrivacyString());

        userConsentManager.setUsPrivacyString("test_1");
        assertEquals("test_1", userConsentManager.getUsPrivacyString());
        assertEquals("test_1", UserConsentManagerReflection.getPrebidUsPrivacy(userConsentManager));

        userConsentManager.setUsPrivacyString("test_2");
        assertEquals("test_2", userConsentManager.getUsPrivacyString());
        assertEquals("test_2", UserConsentManagerReflection.getPrebidUsPrivacy(userConsentManager));

        userConsentManager.setUsPrivacyString(null);
        assertNull(userConsentManager.getUsPrivacyString());
        assertNull(UserConsentManagerReflection.getPrebidUsPrivacy(userConsentManager));

        // Update from outside
        sharedPreferences
            .edit()
            .putString(usPrivacyKey, "test_3")
            .apply();
        assertEquals("test_3", userConsentManager.getUsPrivacyString());
        preferencesValueEqual("test_3", usPrivacyKey);
    }


    @Test
    public void canAccessDeviceData_SubjectToGdprFalse() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.getRealSubjectToGdprBoolean()).thenReturn(Boolean.FALSE); // false

        WhiteBox.setInternalState(spyConsentManager, "realGdpr2PurposeConsents", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "realGdpr2PurposeConsents", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "realGdpr2PurposeConsents", null); // undefined
        assertTrue(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void canAccessDeviceData_SubjectToGdprTrue() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.getRealSubjectToGdprBoolean()).thenReturn(Boolean.TRUE); // true

        WhiteBox.setInternalState(spyConsentManager, "realGdpr2PurposeConsents", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "realGdpr2PurposeConsents", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "realGdpr2PurposeConsents", null); // undefined
        assertFalse(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void canAccessDeviceData_SubjectToGdprUndefined() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.getRealSubjectToGdprBoolean()).thenReturn(null);// undefined

        WhiteBox.setInternalState(spyConsentManager, "realGdpr2PurposeConsents", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "realGdpr2PurposeConsents", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "realGdpr2PurposeConsents", null); // undefined
        assertTrue(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void getPurposeConsent_validString_ReturnValueAtIndexAsBoolean() {
        WhiteBox.setInternalState(userConsentManager, "realGdpr2PurposeConsents", "01019");

        assertEquals(Boolean.TRUE, userConsentManager.getGdprPurposeConsent(1));
        assertEquals(Boolean.FALSE, userConsentManager.getGdprPurposeConsent(2));
        assertNull(userConsentManager.getGdprPurposeConsent(4));
        assertNull(userConsentManager.getGdprPurposeConsent(5));
    }

    @Test
    public void getGdprPurposeConsent_nullString_ReturnNull() {
        WhiteBox.setInternalState(userConsentManager, "realGdpr2PurposeConsents", null);

        assertNull(userConsentManager.getGdprPurposeConsent(1));
        assertNull(userConsentManager.getGdprPurposeConsent(0));
    }

    @Test
    public void getGppString() {
        String realGppString = userConsentManager.getRealGppString();
        assertNull(realGppString);
        String realGppSid = userConsentManager.getRealGppSid();
        assertNull(realGppSid);

        sharedPreferences
            .edit()
            .putString(UserConsentManager.GPP_STRING_KEY, "testString")
            .putString(UserConsentManager.GPP_SID_KEY, "testSid")
            .apply();
        assertEquals("testString", userConsentManager.getRealGppString());
        assertEquals("testSid", userConsentManager.getRealGppSid());
    }


    private void preferencesValueEqual(
        boolean expected,
        String key
    ) {
        if (sharedPreferences.contains(key)) {
            boolean value = sharedPreferences.getBoolean(key, false);
            assertEquals(expected, value);
        } else {
            fail("Preferences don't have key: " + key);
        }
    }

    private void preferencesValueEqual(
        String expected,
        String key
    ) {
        if (sharedPreferences.contains(key)) {
            String value = sharedPreferences.getString(key, null);
            assertEquals(expected, value);
        } else {
            fail("Preferences don't have key: " + key);
        }
    }

    private void preferencesValueEqual(
        int expected,
        String key
    ) {
        if (sharedPreferences.contains(key)) {
            int value = sharedPreferences.getInt(key, -999);
            assertEquals(expected, value);
        } else {
            fail("Preferences don't have key: " + key);
        }
    }

}