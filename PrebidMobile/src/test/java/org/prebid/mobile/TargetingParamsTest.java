/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class TargetingParamsTest extends BaseSetup {

    @Before
    public void setup() {
        super.setup();
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
    }

    @Override
    public void tearDown() {
        super.tearDown();

        TargetingParams.clearAccessControlList();
        TargetingParams.clearUserData();
        TargetingParams.clearContextData();
        TargetingParams.clearContextKeywords();
        TargetingParams.clearUserKeywords();
    }

    @Test
    public void testYearOfBirth() throws Exception {
        // force initial state since it's static might be changed from other tests
        FieldUtils.writeStaticField(TargetingParams.class, "yob", 0, true);
        assertEquals(0, TargetingParams.getYearOfBirth());
        boolean errorThrown1 = false;
        try {
            TargetingParams.setYearOfBirth(-1);
        } catch (Exception e) {
            errorThrown1 = true;
            assertEquals(0, TargetingParams.getYearOfBirth());
        }
        assertTrue(errorThrown1);
        boolean errorThrown2 = false;
        try {
            TargetingParams.setYearOfBirth(Calendar.getInstance().get(Calendar.YEAR) + 5);
        } catch (Exception e) {
            errorThrown2 = true;
            assertEquals(0, TargetingParams.getYearOfBirth());
        }
        assertTrue(errorThrown2);
        int yearOfBirth = Calendar.getInstance().get(Calendar.YEAR) - 20;
        TargetingParams.setYearOfBirth(yearOfBirth);
        assertEquals(yearOfBirth, TargetingParams.getYearOfBirth());
    }

    @Test
    public void testGender() throws Exception {
        TargetingParams.setGender(TargetingParams.GENDER.UNKNOWN);
        assertEquals(TargetingParams.GENDER.UNKNOWN, TargetingParams.getGender());
        TargetingParams.setGender(TargetingParams.GENDER.FEMALE);
        assertEquals(TargetingParams.GENDER.FEMALE, TargetingParams.getGender());
        TargetingParams.setGender(TargetingParams.GENDER.MALE);
        assertEquals(TargetingParams.GENDER.MALE, TargetingParams.getGender());
    }

    @Test
    public void testBundleName() throws Exception {
        FieldUtils.writeStaticField(TargetingParams.class, "bundleName", null, true);
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        assertEquals("org.robolectric.default", TargetingParams.getBundleName());
        TargetingParams.setBundleName("org.prebid.mobile");
        assertEquals("org.prebid.mobile", TargetingParams.getBundleName());
    }

    @Test
    public void testDomain() throws Exception {
        TargetingParams.setDomain("prebid.org");
        assertEquals("prebid.org", TargetingParams.getDomain());
    }

    @Test
    public void testStoreUrl() throws Exception {
        TargetingParams.setStoreUrl("store://testapp");
        assertEquals("store://testapp", TargetingParams.getStoreUrl());
    }

    @Test
    public void testCOPPAFlag() throws Exception {
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        TargetingParams.setSubjectToCOPPA(true);
        assertEquals(true, TargetingParams.isSubjectToCOPPA());
        TargetingParams.setSubjectToCOPPA(false);
        assertEquals(false, TargetingParams.isSubjectToCOPPA());
    }

    @Test
    public void testCOPPAFlagWithoutContext() throws Exception {
        //given
        PrebidMobile.setApplicationContext(null);

        //when
        boolean result = TargetingParams.isSubjectToCOPPA();

        //then
        assertEquals(false, result);

        //defer
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
    }

    @Test
    public void testGDPRFlag() throws Exception {
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        TargetingParams.setSubjectToGDPR(true);
        assertEquals(Boolean.TRUE, TargetingParams.isSubjectToGDPR());
        TargetingParams.setSubjectToGDPR(false);
        assertEquals(Boolean.FALSE, TargetingParams.isSubjectToGDPR());
        TargetingParams.setSubjectToGDPR(null);
        assertEquals(null, TargetingParams.isSubjectToGDPR());
    }

    @Test
    public void testGdprSubjectUndefined() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();

        //given
        TargetingParams.setSubjectToGDPR(null);
        editor.remove(StorageUtils.IABConsent_SubjectToGDPRKey);
        editor.remove(StorageUtils.IABTCF_SUBJECT_TO_GDPR);
        editor.apply();

        //when
        Boolean gdprSubject = TargetingParams.isSubjectToGDPR();

        //then
        assertEquals(null, gdprSubject);
    }

    @Test
    public void testGdprSubjectTCFv1() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.IABConsent_SubjectToGDPRKey, "1");
        editor.apply();

        //when
        Boolean gdprSubject = TargetingParams.isSubjectToGDPR();

        //then
        assertEquals(Boolean.TRUE, gdprSubject);
    }

    @Test
    public void testGdprSubjectTCFv2() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(StorageUtils.IABTCF_SUBJECT_TO_GDPR, 1);
        editor.apply();

        //when
        Boolean gdprSubject = TargetingParams.isSubjectToGDPR();

        //then
        assertEquals(Boolean.TRUE, gdprSubject);
    }

    //GDPR Consent
    @Test
    public void testGDPRConsentPBString() {
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
        TargetingParams.setGDPRConsentString("testString");
        assertEquals("testString", TargetingParams.getGDPRConsentString());
    }

    @Test
    public void testGdprConsentUndefined() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();

        //given
        TargetingParams.setGDPRConsentString(null);
        editor.remove(StorageUtils.IABConsent_ConsentStringKey);
        editor.remove(StorageUtils.IABTCF_CONSENT_STRING);
        editor.apply();

        //when
        String gdprConsent = TargetingParams.getGDPRConsentString();

        //then
        assertEquals(null, gdprConsent);
    }

    @Test
    public void testGdprConsentTCFv1() {

        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.IABConsent_ConsentStringKey, "testconsent TCFv1");
        editor.apply();

        //when
        String gdprConsent = TargetingParams.getGDPRConsentString();

        //then
        assertEquals("testconsent TCFv1", gdprConsent);

    }

    @Test
    public void testGdprConsentTCFv2() {

        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.IABTCF_CONSENT_STRING, "testconsent TCFv1");
        editor.apply();

        //when
        String gdprConsent = TargetingParams.getGDPRConsentString();

        //then
        assertEquals("testconsent TCFv1", gdprConsent);

    }

    //PurposeConsents
    @Test
    public void testPurposeConsentsPB() {
        //given
        TargetingParams.setPurposeConsents("test PurposeConsents PB");

        //when
        String purposeConsents = TargetingParams.getPurposeConsents();

        //then
        assertEquals("test PurposeConsents PB", purposeConsents);
    }

    @Test
    public void testPurposeConsentsUndefined() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();

        //given
        TargetingParams.setPurposeConsents(null);
        editor.remove(StorageUtils.IABTCF_PurposeConsents);
        editor.apply();

        //when
        String purposeConsents = TargetingParams.getPurposeConsents();

        //then
        assertEquals(null, purposeConsents);
    }

    @Test
    public void testPurposeConsentsTCFv2() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.IABTCF_PurposeConsents, "test PurposeConsents TCFv2");
        editor.apply();

        //when
        String purposeConsents = TargetingParams.getPurposeConsents();

        //then
        assertEquals("test PurposeConsents TCFv2", purposeConsents);
    }

    @Test
    public void testGetDeviceAccessConsent() {
        //given
        TargetingParams.setPurposeConsents("100000000000000000000000");

        //when
        Boolean deviceAccessConsent = TargetingParams.getDeviceAccessConsent();

        //then
        assertEquals(Boolean.TRUE, deviceAccessConsent);
    }

    @Test
    public void testGetPurposeConsent() {
        //given
        TargetingParams.setPurposeConsents("101000000000000000000000");

        //when
        Boolean purpose1 = TargetingParams.getPurposeConsent(0);
        Boolean purpose2 = TargetingParams.getPurposeConsent(1);
        Boolean purpose3 = TargetingParams.getPurposeConsent(2);

        //then
        assertEquals(Boolean.TRUE, purpose1);
        assertEquals(Boolean.FALSE, purpose2);
        assertEquals(Boolean.TRUE, purpose3);
    }

    @Test
    public void testContextData() {
        // given
        TargetingParams.addContextData("key1", "value10");

        //when
        Map<String, Set<String>> dictionary = TargetingParams.getContextDataDictionary();
        Set<String> set = dictionary.get("key1");

        //then
        Assert.assertEquals(1, dictionary.size());

        Assert.assertEquals(1, set.size());
        assertThat(set, containsInAnyOrder("value10"));
    }

    @Test
    public void testUserData() {
        // given
        TargetingParams.addUserData("key1", "value10");

        //when
        Map<String, Set<String>> dictionary = TargetingParams.getUserDataDictionary();
        Set<String> set = dictionary.get("key1");

        //then
        Assert.assertEquals(1, dictionary.size());

        Assert.assertEquals(1, set.size());
        assertThat(set, containsInAnyOrder("value10"));
    }

    @Test
    public void testContextKeyword() {
        // given
        TargetingParams.addContextKeyword("value10");

        //when
        Set<String> set = TargetingParams.getContextKeywordsSet();

        //then
        Assert.assertEquals(1, set.size());
        assertThat(set, containsInAnyOrder("value10"));
    }

    @Test
    public void testUserKeyword() {

        //given
        TargetingParams.addUserKeyword("value10");

        //when
        Set<String> set = TargetingParams.getUserKeywordsSet();

        //then
        Assert.assertEquals(1, set.size());
        assertThat(set, containsInAnyOrder("value10"));
    }
}
