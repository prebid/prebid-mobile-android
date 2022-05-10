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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class UserConsentManagerTest {

    protected Context context = mock(Context.class);
    protected SharedPreferences sharedPreferences = mock(SharedPreferences.class);
    private UserConsentManager userConsentManager;
    private String isSubjectToGdpr = "";
    private String consentString;
    private String purposeConsent;

    private static final String SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";
    private static final String CONSENT_STRING = "IABConsent_ConsentString";
    private static final String PURPOSE_CONSENT = "IABTCF_PurposeConsents";

    @Before
    public void setUp() throws Exception {
        Activity robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();

        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(robolectricActivity);
        sharedPreferences.edit().putString(SUBJECT_TO_GDPR, "1").commit();
        sharedPreferences.edit().putString(CONSENT_STRING, "consent_string").commit();
        sharedPreferences.edit().putString(PURPOSE_CONSENT, "01").commit();

        userConsentManager = new UserConsentManager();
        userConsentManager.init(robolectricActivity);
    }

    @Test
    public void initConsentValuesAtStartTest() throws Exception {
        isSubjectToGdpr = WhiteBox.getInternalState(userConsentManager, "isSubjectToGdpr");
        assertEquals("1", isSubjectToGdpr);

        consentString = WhiteBox.getInternalState(userConsentManager, "consentString");
        assertEquals("consent_string", consentString);

        purposeConsent = WhiteBox.getInternalState(userConsentManager, "purposeConsent");
        assertEquals("01", purposeConsent);
    }

    @Test
    public void getConsentValuesTest() throws Exception {
        Method methodGetConsent = WhiteBox.method(UserConsentManager.class, "getConsentValues", SharedPreferences.class, String.class);

        consentString = (String) methodGetConsent.invoke(userConsentManager, sharedPreferences, CONSENT_STRING);
        assertEquals("consent_string", consentString);

        isSubjectToGdpr = (String) methodGetConsent.invoke(userConsentManager, sharedPreferences, SUBJECT_TO_GDPR);
        assertEquals("1", isSubjectToGdpr);

        purposeConsent = (String) methodGetConsent.invoke(userConsentManager, sharedPreferences, PURPOSE_CONSENT);
        assertEquals("01", purposeConsent);
    }

    @Test
    public void getSubjectToGdprTest() {

        WhiteBox.setInternalState(userConsentManager, "isSubjectToGdpr", "1");
        assertEquals(userConsentManager.getSubjectToGdpr(), "1");

        WhiteBox.setInternalState(userConsentManager, "isSubjectToGdpr", "0");
        assertEquals(userConsentManager.getSubjectToGdpr(), "0");

        WhiteBox.setInternalState(userConsentManager, "isSubjectToGdpr", "");
        assertEquals(userConsentManager.getSubjectToGdpr(), "");
    }

    @Test
    public void getUserConsentStringTest() {
        WhiteBox.setInternalState(userConsentManager, "consentString", "some_consent_string");
        assertEquals(userConsentManager.getUserConsentString(), "some_consent_string");

        WhiteBox.setInternalState(userConsentManager, "consentString", "");
        assertEquals(userConsentManager.getUserConsentString(), "");
    }

    @Test
    public void shouldUseTcfV2WithValidCmpId_ReturnTrue() {
        WhiteBox.setInternalState(userConsentManager, "cmpSdkId", 1);

        assertTrue(userConsentManager.shouldUseTcfV2());
    }

    @Test
    public void shouldUseTcfV2WithInValidCmpId_ReturnFalse() {
        WhiteBox.setInternalState(userConsentManager, "cmpSdkId", -2);

        assertFalse(userConsentManager.shouldUseTcfV2());
    }

    @Test
    public void getTcfV2GdprAppliesWhenGdprValueIsNotAssigned_ReturnNull() {
        WhiteBox.setInternalState(userConsentManager, "tcfV2GdprApplies", -1);

        assertNull(userConsentManager.getTcfV2GdprApplies());
    }

    @Test
    public void getTcfV2GdprAppliesWhenGdprValueIsAssigned_ReturnStringValue() {
        WhiteBox.setInternalState(userConsentManager, "tcfV2GdprApplies", 1);

        assertEquals("1", userConsentManager.getTcfV2GdprApplies());
    }

    @Test
    public void getSubjectToGdprShouldUseTcfV2True_ReturnTcfV2GdprApplies() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);

        WhiteBox.setInternalState(spyConsentManager, "tcfV2GdprApplies", 1);
        WhiteBox.setInternalState(spyConsentManager, "isSubjectToGdpr", "0");

        assertEquals("1", spyConsentManager.getSubjectToGdpr());
    }

    @Test
    public void getSubjectToGdprShouldUseTcfV2False_ReturnTcfV1IsSubjectToGdpr() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(false);

        WhiteBox.setInternalState(spyConsentManager, "tcfV2GdprApplies", 1);
        WhiteBox.setInternalState(spyConsentManager, "isSubjectToGdpr", "0");

        assertEquals("0", spyConsentManager.getSubjectToGdpr());
    }

    @Test
    public void getUserConsentStringShouldUseTcfV2True_ReturnTcfV2ConsentString() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);

        WhiteBox.setInternalState(spyConsentManager, "tcfV2ConsentString", "tcf_v2_consent");
        WhiteBox.setInternalState(spyConsentManager, "consentString", "tcf_v1_consent");

        assertEquals("tcf_v2_consent", spyConsentManager.getUserConsentString());
    }

    @Test
    public void getUserConsentStringShouldUseTcfV2False_ReturnTcfV1ConsentString() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(false);

        WhiteBox.setInternalState(spyConsentManager, "tcfV2ConsentString", "tcf_v2_consent");
        WhiteBox.setInternalState(spyConsentManager, "consentString", "tcf_v1_consent");

        assertEquals("tcf_v1_consent", spyConsentManager.getUserConsentString());
    }


    //fetch advertising identifier based TCF 2.0 Purpose1 value
    //truth table
    /*
                         deviceAccessConsent=true   deviceAccessConsent=false  deviceAccessConsent undefined
    gdprApplies=false        Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
    gdprApplies=true         Yes, read IDFA             No, don’t read IDFA           No, don’t read IDFA
    gdprApplies=undefined    Yes, read IDFA             No, don’t read IDFA           Yes, read IDFA
    */
    @Test
    public void canAccessDeviceData_SubjectToGdprFalse() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);
        when(spyConsentManager.getSubjectToGdpr()).thenReturn("0"); // false

        WhiteBox.setInternalState(spyConsentManager, "purposeConsent", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "purposeConsent", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "purposeConsent", null); // undefined
        assertTrue(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void canAccessDeviceData_SubjectToGdprTrue() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);
        when(spyConsentManager.getSubjectToGdpr()).thenReturn("1"); // true

        WhiteBox.setInternalState(spyConsentManager, "purposeConsent", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "purposeConsent", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "purposeConsent", null); // undefined
        assertFalse(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void canAccessDeviceData_SubjectToGdprUndefined() {
        UserConsentManager spyConsentManager = spy(userConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);
        when(spyConsentManager.getSubjectToGdpr()).thenReturn(""); // undefined

        WhiteBox.setInternalState(spyConsentManager, "purposeConsent", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "purposeConsent", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "purposeConsent", null); // undefined
        assertTrue(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void getPurposeConsent_validString_ReturnValueAtIndexAsBoolean() {
        WhiteBox.setInternalState(userConsentManager, "purposeConsent", "01019");

        assertTrue(userConsentManager.getPurposeConsent(1));
        assertFalse(userConsentManager.getPurposeConsent(2));
        assertFalse(userConsentManager.getPurposeConsent(4));
    }

    @Test
    public void getPurposeConsent_nullString_ReturnNull() {
        WhiteBox.setInternalState(userConsentManager, "purposeConsent", null);

        assertNull(userConsentManager.getPurposeConsent(1));
        assertNull(userConsentManager.getPurposeConsent(0));
    }
}