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

    protected Context mContext = mock(Context.class);
    protected SharedPreferences mSharedPreferences = mock(SharedPreferences.class);
    private UserConsentManager mUserConsentManager;
    private String mIsSubjectToGdpr = "";
    private String mConsentString;
    private String mPurposeConsent;

    private static final String SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";
    private static final String CONSENT_STRING = "IABConsent_ConsentString";
    private static final String PURPOSE_CONSENT = "IABTCF_PurposeConsents";

    @Before
    public void setUp() throws Exception {
        Activity robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(robolectricActivity);
        mSharedPreferences.edit().putString(SUBJECT_TO_GDPR, "1").commit();
        mSharedPreferences.edit().putString(CONSENT_STRING, "consent_string").commit();
        mSharedPreferences.edit().putString(PURPOSE_CONSENT, "01").commit();

        mUserConsentManager = new UserConsentManager();
        mUserConsentManager.init(robolectricActivity);
    }

    @Test
    public void initConsentValuesAtStartTest() throws Exception {
        mIsSubjectToGdpr = WhiteBox.getInternalState(mUserConsentManager, "mIsSubjectToGdpr");
        assertEquals("1", mIsSubjectToGdpr);

        mConsentString = WhiteBox.getInternalState(mUserConsentManager, "mConsentString");
        assertEquals("consent_string", mConsentString);

        mPurposeConsent = WhiteBox.getInternalState(mUserConsentManager, "mPurposeConsent");
        assertEquals("01", mPurposeConsent);
    }

    @Test
    public void getConsentValuesTest() throws Exception {
        Method methodGetConsent = WhiteBox.method(UserConsentManager.class, "getConsentValues", SharedPreferences.class, String.class);

        mConsentString = (String) methodGetConsent.invoke(mUserConsentManager, mSharedPreferences, CONSENT_STRING);
        assertEquals("consent_string", mConsentString);

        mIsSubjectToGdpr = (String) methodGetConsent.invoke(mUserConsentManager, mSharedPreferences, SUBJECT_TO_GDPR);
        assertEquals("1", mIsSubjectToGdpr);

        mPurposeConsent = (String) methodGetConsent.invoke(mUserConsentManager, mSharedPreferences, PURPOSE_CONSENT);
        assertEquals("01", mPurposeConsent);
    }

    @Test
    public void getSubjectToGdprTest() {

        WhiteBox.setInternalState(mUserConsentManager, "mIsSubjectToGdpr", "1");
        assertEquals(mUserConsentManager.getSubjectToGdpr(), "1");

        WhiteBox.setInternalState(mUserConsentManager, "mIsSubjectToGdpr", "0");
        assertEquals(mUserConsentManager.getSubjectToGdpr(), "0");

        WhiteBox.setInternalState(mUserConsentManager, "mIsSubjectToGdpr", "");
        assertEquals(mUserConsentManager.getSubjectToGdpr(), "");
    }

    @Test
    public void getUserConsentStringTest() {
        WhiteBox.setInternalState(mUserConsentManager, "mConsentString", "some_consent_string");
        assertEquals(mUserConsentManager.getUserConsentString(), "some_consent_string");

        WhiteBox.setInternalState(mUserConsentManager, "mConsentString", "");
        assertEquals(mUserConsentManager.getUserConsentString(), "");
    }

    @Test
    public void shouldUseTcfV2WithValidCmpId_ReturnTrue() {
        WhiteBox.setInternalState(mUserConsentManager, "mCmpSdkId", 1);

        assertTrue(mUserConsentManager.shouldUseTcfV2());
    }

    @Test
    public void shouldUseTcfV2WithInValidCmpId_ReturnFalse() {
        WhiteBox.setInternalState(mUserConsentManager, "mCmpSdkId", -2);

        assertFalse(mUserConsentManager.shouldUseTcfV2());
    }

    @Test
    public void getTcfV2GdprAppliesWhenGdprValueIsNotAssigned_ReturnNull() {
        WhiteBox.setInternalState(mUserConsentManager, "mTcfV2GdprApplies", -1);

        assertNull(mUserConsentManager.getTcfV2GdprApplies());
    }

    @Test
    public void getTcfV2GdprAppliesWhenGdprValueIsAssigned_ReturnStringValue() {
        WhiteBox.setInternalState(mUserConsentManager, "mTcfV2GdprApplies", 1);

        assertEquals("1", mUserConsentManager.getTcfV2GdprApplies());
    }

    @Test
    public void getSubjectToGdprShouldUseTcfV2True_ReturnTcfV2GdprApplies() {
        UserConsentManager spyConsentManager = spy(mUserConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);

        WhiteBox.setInternalState(spyConsentManager, "mTcfV2GdprApplies", 1);
        WhiteBox.setInternalState(spyConsentManager, "mIsSubjectToGdpr", "0");

        assertEquals("1", spyConsentManager.getSubjectToGdpr());
    }

    @Test
    public void getSubjectToGdprShouldUseTcfV2False_ReturnTcfV1IsSubjectToGdpr() {
        UserConsentManager spyConsentManager = spy(mUserConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(false);

        WhiteBox.setInternalState(spyConsentManager, "mTcfV2GdprApplies", 1);
        WhiteBox.setInternalState(spyConsentManager, "mIsSubjectToGdpr", "0");

        assertEquals("0", spyConsentManager.getSubjectToGdpr());
    }

    @Test
    public void getUserConsentStringShouldUseTcfV2True_ReturnTcfV2ConsentString() {
        UserConsentManager spyConsentManager = spy(mUserConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);

        WhiteBox.setInternalState(spyConsentManager, "mTcfV2ConsentString", "tcf_v2_consent");
        WhiteBox.setInternalState(spyConsentManager, "mConsentString", "tcf_v1_consent");

        assertEquals("tcf_v2_consent", spyConsentManager.getUserConsentString());
    }

    @Test
    public void getUserConsentStringShouldUseTcfV2False_ReturnTcfV1ConsentString() {
        UserConsentManager spyConsentManager = spy(mUserConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(false);

        WhiteBox.setInternalState(spyConsentManager, "mTcfV2ConsentString", "tcf_v2_consent");
        WhiteBox.setInternalState(spyConsentManager, "mConsentString", "tcf_v1_consent");

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
        UserConsentManager spyConsentManager = spy(mUserConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);
        when(spyConsentManager.getSubjectToGdpr()).thenReturn("0"); // false

        WhiteBox.setInternalState(spyConsentManager, "mPurposeConsent", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "mPurposeConsent", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "mPurposeConsent", null); // undefined
        assertTrue(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void canAccessDeviceData_SubjectToGdprTrue() {
        UserConsentManager spyConsentManager = spy(mUserConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);
        when(spyConsentManager.getSubjectToGdpr()).thenReturn("1"); // true

        WhiteBox.setInternalState(spyConsentManager, "mPurposeConsent", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "mPurposeConsent", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "mPurposeConsent", null); // undefined
        assertFalse(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void canAccessDeviceData_SubjectToGdprUndefined() {
        UserConsentManager spyConsentManager = spy(mUserConsentManager);
        when(spyConsentManager.shouldUseTcfV2()).thenReturn(true);
        when(spyConsentManager.getSubjectToGdpr()).thenReturn(""); // undefined

        WhiteBox.setInternalState(spyConsentManager, "mPurposeConsent", "10"); // true
        assertTrue(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "mPurposeConsent", "00"); // false
        assertFalse(spyConsentManager.canAccessDeviceData());

        WhiteBox.setInternalState(spyConsentManager, "mPurposeConsent", null); // undefined
        assertTrue(spyConsentManager.canAccessDeviceData());
    }

    @Test
    public void getPurposeConsent_validString_ReturnValueAtIndexAsBoolean() {
        WhiteBox.setInternalState(mUserConsentManager, "mPurposeConsent", "01019");

        assertTrue(mUserConsentManager.getPurposeConsent(1));
        assertFalse(mUserConsentManager.getPurposeConsent(2));
        assertFalse(mUserConsentManager.getPurposeConsent(4));
    }

    @Test
    public void getPurposeConsent_nullString_ReturnNull() {
        WhiteBox.setInternalState(mUserConsentManager, "mPurposeConsent", null);

        assertNull(mUserConsentManager.getPurposeConsent(1));
        assertNull(mUserConsentManager.getPurposeConsent(0));
    }
}