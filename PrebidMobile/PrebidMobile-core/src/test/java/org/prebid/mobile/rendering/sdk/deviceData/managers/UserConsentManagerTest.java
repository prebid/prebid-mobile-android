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

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class UserConsentManagerTest {

    protected Context context = mock(Context.class);
    protected SharedPreferences sharedPreferences = mock(SharedPreferences.class);
    private UserConsentManager userConsentManager;

    @Before
    public void setUp() throws Exception {
        Activity activity = Robolectric.buildActivity(Activity.class).create().get();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(activity);

        userConsentManager = new UserConsentManager();
        userConsentManager.init(activity);

        resetAllPreferences();
    }

    @After
    public void destroy() {
        resetAllPreferences();
    }

    private void resetAllPreferences() {
        sharedPreferences
            .edit()
            .remove(UserConsentManagerReflection.getConstGdpr1Subject(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr1Consent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2CmpSdkId(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2Subject(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2Consent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2PurposeConsent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdprPrebidSubject(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdprPrebidConsent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdprPrebidPurposeConsent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstUsPrivacyString(userConsentManager))
            .remove(UserConsentManagerReflection.getConstCoppaCustomKey(userConsentManager))
            .apply();
    }

    @Test
    public void checkConstants() {
        // GDPR 1
        assertEquals(
            "IABConsent_SubjectToGDPR",
            UserConsentManagerReflection.getConstGdpr1Subject(userConsentManager)
        );
        assertEquals(
            "IABConsent_ConsentString",
            UserConsentManagerReflection.getConstGdpr1Consent(userConsentManager)
        );

        // GDPR 2
        assertEquals(
            "IABTCF_CmpSdkID",
            UserConsentManagerReflection.getConstGdpr2CmpSdkId(userConsentManager)
        );
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

        // GDPR Prebid
        assertEquals(
            "Prebid_GDPR",
            UserConsentManagerReflection.getConstGdprPrebidSubject(userConsentManager)
        );
        assertEquals(
            "Prebid_GDPR_consent_strings",
            UserConsentManagerReflection.getConstGdprPrebidConsent(userConsentManager)
        );
        assertEquals(
            "Prebid_GDPR_PurposeConsents",
            UserConsentManagerReflection.getConstGdprPrebidPurposeConsent(userConsentManager)
        );

        // CCPA
        assertEquals(
            "IABUSPrivacy_String",
            UserConsentManagerReflection.getConstUsPrivacyString(userConsentManager)
        );

        // COPPA
        assertEquals(
            "Prebid_COPPA",
            UserConsentManagerReflection.getConstCoppaCustomKey(userConsentManager)
        );
    }

    @Test
    public void subjectToCoppa() {
        String coppaKey = UserConsentManagerReflection.getConstCoppaCustomKey(userConsentManager);

        Boolean subjectToCoppa = userConsentManager.getSubjectToCoppa();
        assertNull(subjectToCoppa);

        userConsentManager.setSubjectToCoppa(true);
        assertTrue(userConsentManager.getSubjectToCoppa());
        preferencesValueEqual(true, coppaKey);

        userConsentManager.setSubjectToCoppa(false);
        assertFalse(userConsentManager.getSubjectToCoppa());
        preferencesValueEqual(false, coppaKey);

        // Update from outside
        sharedPreferences
            .edit()
            .putBoolean(coppaKey, true)
            .apply();
        assertTrue(userConsentManager.getSubjectToCoppa());
        preferencesValueEqual(true, coppaKey);

        userConsentManager.setSubjectToCoppa(null);
        assertNull(userConsentManager.getSubjectToCoppa());
        assertFalse(sharedPreferences.contains(coppaKey));
    }


    @Test
    public void cmpSdkIdForGdprTcf2() {
        String cmpSdkIdKey = UserConsentManagerReflection.getConstGdpr2CmpSdkId(userConsentManager);

        assertEquals(Integer.valueOf(-1), userConsentManager.getCmpSdkIdForGdprTcf2());
        assertFalse(userConsentManager.shouldUseTcfV2());

        userConsentManager.setCmpSdkIdForGdprTcf2(0);
        assertEquals(Integer.valueOf(0), userConsentManager.getCmpSdkIdForGdprTcf2());
        assertTrue(userConsentManager.shouldUseTcfV2());
        preferencesValueEqual(0, cmpSdkIdKey);

        userConsentManager.setCmpSdkIdForGdprTcf2(1);
        assertEquals(Integer.valueOf(1), userConsentManager.getCmpSdkIdForGdprTcf2());
        assertTrue(userConsentManager.shouldUseTcfV2());
        preferencesValueEqual(1, cmpSdkIdKey);

        // Update from outside
        sharedPreferences
            .edit()
            .putInt(cmpSdkIdKey, 2)
            .apply();
        assertEquals(Integer.valueOf(2), userConsentManager.getCmpSdkIdForGdprTcf2());
        assertTrue(userConsentManager.shouldUseTcfV2());
        preferencesValueEqual(2, cmpSdkIdKey);

        userConsentManager.setCmpSdkIdForGdprTcf2(null);
        assertEquals(Integer.valueOf(-1), userConsentManager.getCmpSdkIdForGdprTcf2());
        assertFalse(userConsentManager.shouldUseTcfV2());
        preferencesValueEqual(-1, cmpSdkIdKey);
    }

    @Test
    public void subjectToGdpr_tcf1() {
        String gdpr1SubjectKey = UserConsentManagerReflection.getConstGdpr1Subject(userConsentManager);

        assertNull(userConsentManager.getSubjectToGdpr());
        assertNull(userConsentManager.getSubjectToGdprBoolean());

        userConsentManager.setSubjectToGdpr(true);
        assertEquals("1", userConsentManager.getSubjectToGdpr());
        assertEquals(Boolean.TRUE, userConsentManager.getSubjectToGdprBoolean());
        preferencesValueEqual("1", gdpr1SubjectKey);

        userConsentManager.setSubjectToGdpr(false);
        assertEquals("0", userConsentManager.getSubjectToGdpr());
        assertEquals(Boolean.FALSE, userConsentManager.getSubjectToGdprBoolean());
        preferencesValueEqual("0", gdpr1SubjectKey);

        // Update from outside
        sharedPreferences
            .edit()
            .putString(gdpr1SubjectKey, "1")
            .apply();
        assertEquals("1", userConsentManager.getSubjectToGdpr());
        assertEquals(Boolean.TRUE, userConsentManager.getSubjectToGdprBoolean());
        preferencesValueEqual("1", gdpr1SubjectKey);

        userConsentManager.setSubjectToGdpr(null);
        assertNull(userConsentManager.getSubjectToGdpr());
        assertNull(userConsentManager.getSubjectToGdprBoolean());
        assertFalse(sharedPreferences.contains(gdpr1SubjectKey));
    }

    @Test
    public void subjectToGdpr_tcf2() {
        String gdpr2SubjectKey = UserConsentManagerReflection.getConstGdpr2Subject(userConsentManager);

        userConsentManager.setCmpSdkIdForGdprTcf2(0);
        assertTrue(userConsentManager.shouldUseTcfV2());

        assertNull(userConsentManager.getSubjectToGdpr());
        assertNull(userConsentManager.getSubjectToGdprBoolean());

        userConsentManager.setSubjectToGdpr(true);
        assertEquals("1", userConsentManager.getSubjectToGdpr());
        assertEquals(Boolean.TRUE, userConsentManager.getSubjectToGdprBoolean());
        preferencesValueEqual(1, gdpr2SubjectKey);

        userConsentManager.setSubjectToGdpr(false);
        assertEquals("0", userConsentManager.getSubjectToGdpr());
        assertEquals(Boolean.FALSE, userConsentManager.getSubjectToGdprBoolean());
        preferencesValueEqual(0, gdpr2SubjectKey);

        // Update from outside
        sharedPreferences
            .edit()
            .putInt(gdpr2SubjectKey, 1)
            .apply();
        assertEquals("1", userConsentManager.getSubjectToGdpr());
        assertEquals(Boolean.TRUE, userConsentManager.getSubjectToGdprBoolean());
        preferencesValueEqual(1, gdpr2SubjectKey);

        userConsentManager.setSubjectToGdpr(null);
        assertNull(userConsentManager.getSubjectToGdpr());
        assertNull(userConsentManager.getSubjectToGdprBoolean());
        assertFalse(sharedPreferences.contains(gdpr2SubjectKey));
    }

    @Test
    public void gdprConsent_tcf1() {
        String gdpr1ConsentKey = UserConsentManagerReflection.getConstGdpr1Consent(userConsentManager);

        assertFalse(userConsentManager.shouldUseTcfV2());
        assertNull(userConsentManager.getGdprConsent());

        userConsentManager.setGdprConsent("0");
        assertEquals("0", userConsentManager.getGdprConsent());
        preferencesValueEqual("0", gdpr1ConsentKey);

        userConsentManager.setGdprConsent("1");
        assertEquals("1", userConsentManager.getGdprConsent());
        preferencesValueEqual("1", gdpr1ConsentKey);

        // Update from outside
        sharedPreferences
            .edit()
            .putString(gdpr1ConsentKey, "2")
            .apply();
        assertEquals("2", userConsentManager.getGdprConsent());
        preferencesValueEqual("2", gdpr1ConsentKey);

        userConsentManager.setGdprConsent(null);
        assertNull(userConsentManager.getGdprConsent());
        assertFalse(sharedPreferences.contains(gdpr1ConsentKey));
    }

    @Test
    public void gdprConsent_tcf2() {
        String gdpr2ConsentKey = UserConsentManagerReflection.getConstGdpr2Consent(userConsentManager);

        userConsentManager.setCmpSdkIdForGdprTcf2(0);
        assertTrue(userConsentManager.shouldUseTcfV2());

        assertNull(userConsentManager.getGdprConsent());

        userConsentManager.setGdprConsent("0");
        assertEquals("0", userConsentManager.getGdprConsent());
        preferencesValueEqual("0", gdpr2ConsentKey);

        userConsentManager.setGdprConsent("1");
        assertEquals("1", userConsentManager.getGdprConsent());
        preferencesValueEqual("1", gdpr2ConsentKey);

        // Update from outside
        sharedPreferences
            .edit()
            .putString(gdpr2ConsentKey, "2")
            .apply();
        assertEquals("2", userConsentManager.getGdprConsent());
        preferencesValueEqual("2", gdpr2ConsentKey);

        userConsentManager.setGdprConsent(null);
        assertNull(userConsentManager.getGdprConsent());
        assertFalse(sharedPreferences.contains(gdpr2ConsentKey));
    }

    @Test
    public void gdprPurposeConsents() {
        String purposeConsentKey = UserConsentManagerReflection.getConstGdpr2PurposeConsent(userConsentManager);

        assertNull(userConsentManager.getGdprPurposeConsents());

        userConsentManager.setGdprPurposeConsents("0");
        assertEquals("0", userConsentManager.getGdprPurposeConsents());
        preferencesValueEqual("0", purposeConsentKey);

        userConsentManager.setGdprPurposeConsents("1");
        assertEquals("1", userConsentManager.getGdprPurposeConsents());
        preferencesValueEqual("1", purposeConsentKey);

        // Update from outside
        sharedPreferences
            .edit()
            .putString(purposeConsentKey, "2")
            .apply();
        assertEquals("2", userConsentManager.getGdprPurposeConsents());
        preferencesValueEqual("2", purposeConsentKey);

        userConsentManager.setGdprPurposeConsents(null);
        assertNull(userConsentManager.getGdprPurposeConsents());
        assertFalse(sharedPreferences.contains(purposeConsentKey));
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
        preferencesValueEqual("test_1", usPrivacyKey);

        userConsentManager.setUsPrivacyString("test_2");
        assertEquals("test_2", userConsentManager.getUsPrivacyString());
        preferencesValueEqual("test_2", usPrivacyKey);

        // Update from outside
        sharedPreferences
            .edit()
            .putString(usPrivacyKey, "test_3")
            .apply();
        assertEquals("test_3", userConsentManager.getUsPrivacyString());
        preferencesValueEqual("test_3", usPrivacyKey);

        userConsentManager.setUsPrivacyString(null);
        assertNull(userConsentManager.getUsPrivacyString());
        assertFalse(sharedPreferences.contains(usPrivacyKey));
    }

    @Test
    public void getAnySubjectToGdpr_testPriority() {
        userConsentManager.setPrebidSubjectToGdpr(true);
        userConsentManager.setSubjectToGdpr(false);

        assertEquals(Boolean.TRUE, userConsentManager.getAnySubjectToGdpr());

        userConsentManager.setPrebidSubjectToGdpr(null);

        assertEquals(Boolean.FALSE, userConsentManager.getAnySubjectToGdpr());

        userConsentManager.setSubjectToGdpr(null);

        assertNull(userConsentManager.getAnySubjectToGdpr());
    }

    @Test
    public void getAnyGdprConsent_testPriority() {
        userConsentManager.setPrebidGdprConsent("1");
        userConsentManager.setGdprConsent("2");

        assertEquals("1", userConsentManager.getAnyGdprConsent());

        userConsentManager.setPrebidGdprConsent(null);

        assertEquals("2", userConsentManager.getAnyGdprConsent());

        userConsentManager.setGdprConsent(null);

        assertNull(userConsentManager.getAnyGdprConsent());
    }

    @Test
    public void getAnyGdprPurposeConsent_testPriority() {
        userConsentManager.setPrebidGdprPurposeConsents("10");
        userConsentManager.setGdprPurposeConsents("01");

        assertEquals("10", userConsentManager.getAnyGdprPurposeConsents());

        userConsentManager.setPrebidGdprPurposeConsents(null);

        assertEquals("01", userConsentManager.getAnyGdprPurposeConsents());

        userConsentManager.setGdprPurposeConsents(null);

        assertNull(userConsentManager.getAnyGdprPurposeConsents());
    }


    @Test
    public void getSubjectToGdprShouldUseTcfV2True_ReturnTcfV2GdprApplies() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);

        WhiteBox.setInternalState(spyConsentManager, "gdpr2Subject", 1);
        WhiteBox.setInternalState(spyConsentManager, "gdprSubject", "0");

        assertEquals("1", spyConsentManager.getSubjectToGdpr());
    }

    @Test
    public void getSubjectToGdprShouldUseTcfV2False_ReturnTcfV1IsSubjectToGdpr() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(false);

        WhiteBox.setInternalState(spyConsentManager, "gdpr2Subject", 1);
        WhiteBox.setInternalState(spyConsentManager, "gdprSubject", "0");

        assertEquals("0", spyConsentManager.getSubjectToGdpr());
    }

    @Test
    public void getUserConsentStringShouldUseTcfV2True_ReturnTcfV2ConsentString() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);

        WhiteBox.setInternalState(spyConsentManager, "gdpr2Consent", "tcf_v2_consent");
        WhiteBox.setInternalState(spyConsentManager, "gdprConsent", "tcf_v1_consent");

        assertEquals("tcf_v2_consent", spyConsentManager.getGdprConsent());
    }

    @Test
    public void getUserConsentStringShouldUseTcfV2False_ReturnTcfV1ConsentString() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(false);

        WhiteBox.setInternalState(spyConsentManager, "gdpr2Consent", "tcf_v2_consent");
        WhiteBox.setInternalState(spyConsentManager, "gdprConsent", "tcf_v1_consent");

        assertEquals("tcf_v1_consent", spyConsentManager.getGdprConsent());
    }


    @Test
    public void canAccessDeviceData_SubjectToGdprFalse() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);
        when(spyConsentManager.getSubjectToGdpr()).thenReturn("0"); // false

        WhiteBox.setInternalState(spyConsentManager, "gdpr2PurposeConsent", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "gdpr2PurposeConsent", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "gdpr2PurposeConsent", null); // undefined
        assertTrue(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void canAccessDeviceData_SubjectToGdprTrue() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);
        when(spyConsentManager.getSubjectToGdpr()).thenReturn("1"); // true

        WhiteBox.setInternalState(spyConsentManager, "gdpr2PurposeConsent", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "gdpr2PurposeConsent", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "gdpr2PurposeConsent", null); // undefined
        assertFalse(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void canAccessDeviceData_SubjectToGdprUndefined() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);
        when(spyConsentManager.getSubjectToGdpr()).thenReturn(""); // undefined

        WhiteBox.setInternalState(spyConsentManager, "gdpr2PurposeConsent", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "gdpr2PurposeConsent", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "gdpr2PurposeConsent", null); // undefined
        assertTrue(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void getPurposeConsent_validString_ReturnValueAtIndexAsBoolean() {
        WhiteBox.setInternalState(userConsentManager, "gdpr2PurposeConsent", "01019");

        assertEquals(Boolean.TRUE, userConsentManager.getGdprPurposeConsent(1));
        assertEquals(Boolean.FALSE, userConsentManager.getGdprPurposeConsent(2));
        assertNull(userConsentManager.getGdprPurposeConsent(4));
        assertNull(userConsentManager.getGdprPurposeConsent(5));
    }

    @Test
    public void getGdprPurposeConsent_nullString_ReturnNull() {
        WhiteBox.setInternalState(userConsentManager, "gdpr2PurposeConsent", null);

        assertNull(userConsentManager.getGdprPurposeConsent(1));
        assertNull(userConsentManager.getGdprPurposeConsent(0));
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