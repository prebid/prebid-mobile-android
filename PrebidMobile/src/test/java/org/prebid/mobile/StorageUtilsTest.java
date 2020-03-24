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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
public class StorageUtilsTest extends BaseSetup {

    @Before
    public void setup() {
        super.setup();
        PrebidMobile.setApplicationContext(activity.getApplicationContext());
    }

    @Test
    public void testPB_COPPAKey() throws Exception {
        assertEquals("Prebid_COPPA", StorageUtils.PB_COPPAKey);
    }

    @Test
    public void testPBConsent_SubjectToGDPRKey() throws Exception {
        assertEquals("Prebid_GDPR", StorageUtils.PBConsent_SubjectToGDPRKey);
    }

    @Test
    public void testPBConsent_ConsentStringKey() throws Exception {
        assertEquals("Prebid_GDPR_consent_strings", StorageUtils.PBConsent_ConsentStringKey);
    }

    @Test
    public void testPBConsent_PurposeConsents() throws Exception {
        assertEquals("Prebid_GDPR_PurposeConsents", StorageUtils.PBConsent_PurposeConsents);
    }

    @Test
    public void testIABConsent_SubjectToGDPRKey() throws Exception {
        assertEquals("IABConsent_SubjectToGDPR", StorageUtils.IABConsent_SubjectToGDPRKey);
    }

    @Test
    public void testIABConsent_ConsentStringKey() throws Exception {
        assertEquals("IABConsent_ConsentString", StorageUtils.IABConsent_ConsentStringKey);
    }

    @Test
    public void testIABTCF_CONSENT_STRING() throws Exception {
        assertEquals("IABTCF_TCString", StorageUtils.IABTCF_CONSENT_STRING);
    }

    @Test
    public void testIABTCF_SUBJECT_TO_GDPR() throws Exception {
        assertEquals("IABTCF_gdprApplies", StorageUtils.IABTCF_SUBJECT_TO_GDPR);
    }

    @Test
    public void testIABTCF_PurposeConsents() throws Exception {
        assertEquals("IABTCF_PurposeConsents", StorageUtils.IABTCF_PurposeConsents);
    }

    @Test
    public void testIABUSPrivacy_StringKey() throws Exception {
        assertEquals("IABUSPrivacy_String", StorageUtils.IABUSPrivacy_StringKey);
    }

    //GDPR Subject

    @Test
    public void testGdprSubjectPb() {
        //given
        StorageUtils.setPbGdprSubject(true);

        //when
        Boolean gdprSubject = StorageUtils.getPbGdprSubject();

        //then
        assertEquals(Boolean.TRUE, gdprSubject);
    }

    @Test
    public void testGdprSubjectPbUndefined() {
        //given
        StorageUtils.setPbGdprSubject(true);
        StorageUtils.setPbGdprSubject(null);

        //when
        Boolean gdprSubject = StorageUtils.getPbGdprSubject();

        //then
        assertEquals(null, gdprSubject);
    }


    @Test
    public void testGdprSubjectIabUndefined() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(StorageUtils.IABConsent_SubjectToGDPRKey);
        editor.remove(StorageUtils.IABTCF_SUBJECT_TO_GDPR);
        editor.apply();

        //when
        Boolean gdprSubject = StorageUtils.getPbGdprSubject();

        //then
        assertEquals(null, gdprSubject);
    }

    @Test
    public void testGdprSubjectTCFv1Filled() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.IABConsent_SubjectToGDPRKey, "1");
        editor.apply();

        //when
        Boolean gdprSubject = StorageUtils.getIabGdprSubject();

        //then
        assertEquals(Boolean.TRUE, gdprSubject);
    }

    @Test
    public void testGdprSubjectTCFv2Filled() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(StorageUtils.IABTCF_SUBJECT_TO_GDPR, 1);
        editor.apply();

        //when
        Boolean gdprSubject = StorageUtils.getIabGdprSubject();

        //then
        assertEquals(Boolean.TRUE, gdprSubject);
    }

    //GDPR Consent
    @Test
    public void testGdprConsentPb() {
        //given
        StorageUtils.setPbGdprConsent("testPbGdprConsent");

        //when
        String gdprConsent = StorageUtils.getPbGdprConsent();

        //then
        assertEquals("testPbGdprConsent", gdprConsent);
    }

    @Test
    public void testGdprConsentPbUndefined() {
        //given
        StorageUtils.setPbGdprConsent("testPbGdprConsent");
        StorageUtils.setPbGdprConsent(null);

        //when
        String gdprConsent = StorageUtils.getPbGdprConsent();

        //then
        assertEquals(null, gdprConsent);
    }

    @Test
    public void testGdprConsentIabUndefined() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(StorageUtils.IABConsent_ConsentStringKey);
        editor.remove(StorageUtils.IABTCF_CONSENT_STRING);
        editor.apply();

        //when
        String gdprConsent = StorageUtils.getPbGdprConsent();

        //then
        assertEquals(null, gdprConsent);
    }

    @Test
    public void testGdprConsentTCFv1Filled() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.IABConsent_ConsentStringKey, "testIabGdprConsentFilled");
        editor.apply();

        //when
        String gdprConsent = StorageUtils.getIabGdprConsent();

        //then
        assertEquals("testIabGdprConsentFilled", gdprConsent);
    }

    @Test
    public void testGdprConsentTCFv2Filled() {

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.IABTCF_CONSENT_STRING, "testIabGdprConsentFilled");
        editor.apply();

        //when
        String gdprConsent = StorageUtils.getIabGdprConsent();

        //then
        assertEquals("testIabGdprConsentFilled", gdprConsent);
    }

    //MARK: - Purpose Consent

    @Test
    public void testPurposeConsentPb() {
        //given
        StorageUtils.setPbPurposeConsents("PurposeConsents");

        //when
        String purposeConsent = StorageUtils.getPbPurposeConsents();

        //then
        assertEquals("PurposeConsents", purposeConsent);
    }

    @Test
    public void testPurposeConsentPbUndefined() {
        //given
        StorageUtils.setPbPurposeConsents(null);

        //when
        String purposeConsent = StorageUtils.getPbPurposeConsents();

        //then
        assertEquals(null, purposeConsent);
    }

    @Test
    public void testPurposeConsentIab() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.IABTCF_PurposeConsents, "testPurposeConsentIab");
        editor.apply();

        //when
        String purposeConsent = StorageUtils.getIabPurposeConsents();

        //then
        assertEquals("testPurposeConsentIab", purposeConsent);
    }

    public void testPurposeConsentIabUndefined() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(StorageUtils.IABTCF_PurposeConsents, null);
        editor.apply();

        //when
        String purposeConsent = StorageUtils.getIabPurposeConsents();

        //then
        assertEquals(null, purposeConsent);
    }

}
