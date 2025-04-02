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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.robolectric.annotation.LooperMode.Mode.LEGACY;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.reflection.sdk.ManagersResolverReflection;
import org.prebid.mobile.reflection.sdk.UserConsentManagerReflection;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.PrebidContextHolder;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;
import org.prebid.mobile.testutils.BaseSetup;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = BaseSetup.testSDK)
@LooperMode(LEGACY)
public class TargetingParamsTest extends BaseSetup {

    @Before
    public void setup() {
        super.setup();

        ManagersResolver resolver = ManagersResolver.getInstance();
        ManagersResolverReflection.resetManagers(resolver);

        ManagersResolver.getInstance().prepare(activity);
        UserConsentManager userConsentManager = resolver.getUserConsentManager();
        UserConsentManagerReflection.resetAllFields(userConsentManager);
    }

    @Override
    public void tearDown() {
        super.tearDown();

        TargetingParams.setExternalUserIds(null);
        TargetingParams.clearAccessControlList();
        TargetingParams.setExternalUserIds(null);
        TargetingParams.clearUserKeywords();
    }

    @Test
    public void testBundleName() throws Exception {
        Context mockContext = mock(Context.class);
        PrebidContextHolder.setContext(mockContext);
        when(mockContext.getPackageName()).thenReturn("org.prebid.mobile.core.test");

        assertEquals("org.prebid.mobile.core.test", TargetingParams.getBundleName());
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
        TargetingParams.setSubjectToCOPPA(true);
        assertEquals(true, TargetingParams.isSubjectToCOPPA());
        TargetingParams.setSubjectToCOPPA(false);
        assertEquals(false, TargetingParams.isSubjectToCOPPA());
    }

    @Test
    public void testCOPPAFlagWithoutContext() {
        //given

        //when
        Boolean result = TargetingParams.isSubjectToCOPPA();

        //then
        assertNull(result);

        //defer
    }

    @Test
    public void testGDPRFlag() throws Exception {
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
        editor.remove(UserConsentManager.GDPR_2_SUBJECT_KEY);
        editor.apply();

        //when
        Boolean gdprSubject = TargetingParams.isSubjectToGDPR();

        //then
        assertEquals(null, gdprSubject);
    }

    @Test
    public void testGdprSubjectTCFv2() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt(UserConsentManager.GDPR_2_SUBJECT_KEY, 1);
        editor.apply();

        //when
        Boolean gdprSubject = TargetingParams.isSubjectToGDPR();

        //then
        assertEquals(Boolean.TRUE, gdprSubject);
    }

    //GDPR Consent
    @Test
    public void testGDPRConsentPBString() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.remove(UserConsentManager.GDPR_2_SUBJECT_KEY);
        editor.apply();

        TargetingParams.setGDPRConsentString("testString");
        assertEquals("testString", TargetingParams.getGDPRConsentString());
    }

    @Test
    public void testGdprConsentUndefined() {
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();

        //given
        TargetingParams.setGDPRConsentString(null);
        editor.remove(UserConsentManager.GDPR_2_CONSENT_KEY);
        editor.apply();

        //when
        String gdprConsent = TargetingParams.getGDPRConsentString();

        //then
        assertEquals(null, gdprConsent);

    }

    @Test
    public void testGdprConsentTCFv2() {
        //given
        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(activity);
        SharedPreferences.Editor editor = pref.edit();
        editor.putString(UserConsentManager.GDPR_2_CONSENT_KEY, "testconsent TCFv1");
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
        editor.remove(UserConsentManager.GDPR_2_PURPOSE_CONSENT_KEY);
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
        editor.putString(UserConsentManager.GDPR_2_PURPOSE_CONSENT_KEY, "test PurposeConsents TCFv2");
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
        TargetingParams.addExtData("key1", "value10");

        //when
        Map<String, Set<String>> dictionary = TargetingParams.getExtDataDictionary();
        Set<String> set = dictionary.get("key1");

        //then
        Assert.assertEquals(1, dictionary.size());

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

    @Test
    public void testOmidPartnerNameAndVersion() {

        //given
        String partnerName = "PartnerName";
        String partnerVersion = "1.0";

        TargetingParams.setOmidPartnerName(partnerName);
        TargetingParams.setOmidPartnerVersion(partnerVersion);

        //when
        String omidPartnerName = TargetingParams.getOmidPartnerName();
        String omidPartnerVersion = TargetingParams.getOmidPartnerVersion();

        //then
        Assert.assertEquals(partnerName, omidPartnerName);
        Assert.assertEquals(partnerVersion, omidPartnerVersion);
    }

    @Test
    public void testStoreAndFetchExternalUserIds() {
        TargetingParams.setExternalUserIds(createTestExternalUserIds());

        ArrayList<String> arrSource = new ArrayList<>(Arrays.asList("adserver.org", "netid.de", "criteo.com", "liveramp.com", "sharedid.org"));

        List<ExternalUserId> externalUserIdList = TargetingParams.getExternalUserIds();
        assertTrue(externalUserIdList.size() == arrSource.size());

        for (ExternalUserId externalUserId : externalUserIdList) {
            assertTrue(arrSource.contains(externalUserId.getSource()));
            arrSource.remove(externalUserId.getSource());
        }

        assertTrue(arrSource.size() == 0);
    }

    @Test
    public void testStoreDuplicateAndFetchExternalUserIds() {
        TargetingParams.setExternalUserIds(createTestExternalUserIds());

        ArrayList<String> arrSource = new ArrayList<>(Arrays.asList("adserver.org", "netid.de", "criteo.com", "liveramp.com", "sharedid.org"));

        List<ExternalUserId> externalUserIdList = TargetingParams.getExternalUserIds();
        assertTrue(externalUserIdList.size() == arrSource.size());

        for (ExternalUserId externalUserId : externalUserIdList) {
            assertTrue(arrSource.contains(externalUserId.getSource()));
            arrSource.remove(externalUserId.getSource());
        }

        assertTrue(arrSource.size() == 0);
    }

    @Test
    public void testStoreDuplicateAndOverwriteFetchExternalUserIds() {
        TargetingParams.setExternalUserIds(createTestExternalUserIds());

        ArrayList<String> arrSource = new ArrayList<>(Arrays.asList("adserver.org", "netid.de", "criteo.com", "liveramp.com", "sharedid.org"));

        List<ExternalUserId> externalUserIdList = TargetingParams.getExternalUserIds();
        assertTrue(externalUserIdList.size() == arrSource.size());

        for (ExternalUserId externalUserId : externalUserIdList) {
            assertTrue(arrSource.contains(externalUserId.getSource()));
            arrSource.remove(externalUserId.getSource());

            if (externalUserId.getSource().equals("adserver.org")) {
                assertEquals("111111111111", externalUserId.getUniqueIds().get(0).getId());
            }
        }

        assertTrue(arrSource.size() == 0);
    }

    @Test
    public void testStoreClearAndFetchExternalUserIds() {
        TargetingParams.setExternalUserIds(createTestExternalUserIds());
        // clearing externalUserId
        TargetingParams.setExternalUserIds(new ArrayList<>());

        List<ExternalUserId> externalUserIdList = TargetingParams.getExternalUserIds();
        assertTrue(externalUserIdList.isEmpty());
    }

    @Test
    public void testStoreClearNullAndFetchExternalUserIds() {
        TargetingParams.setExternalUserIds(createTestExternalUserIds());
        // clearing externalUserId
        TargetingParams.setExternalUserIds(null);

        List<ExternalUserId> externalUserIdList = TargetingParams.getExternalUserIds();
        assertTrue(externalUserIdList.isEmpty());
    }

    @Test
    public void testStoreAndFetchExternalUserId() {
        TargetingParams.setExternalUserIds(createTestExternalUserIds());

        ExternalUserId externalUserId = TargetingParams.getExternalUserIds().get(2);
        assertEquals("sharedid.org", externalUserId.getSource());
        assertTrue(externalUserId.getUniqueIds().get(0).getId().equals("111111111111"));
        assertTrue(externalUserId.getUniqueIds().get(0).getAtype() == 1);
        assertTrue(externalUserId.getExt().get("third").equals("01ERJWE5FS4RAZKG6SKQ3ZYSKV"));
    }

    @Test
    public void testNewConstructor() {
        TargetingParams.setExternalUserIds(createNewExternalUserIds());

        List<ExternalUserId> externalUserIds = TargetingParams.getExternalUserIds();
        JSONArray jsonArray = new JSONArray();
        for (ExternalUserId id : externalUserIds) {
            JSONObject idJson = id.getJson();
            if (idJson != null) {
                jsonArray.put(idJson);
            }
        }

        assertEquals("[{\"source\":\"adserver1.com\",\"uids\":[{\"id\":\"11\",\"atype\":111,\"ext\":{\"category\":\"shopping\"}},{\"id\":\"12\",\"atype\":222}],\"ext\":{\"user\":\"1000\"}},{\"source\":\"adserver2.com\",\"uids\":[{\"id\":\"22\",\"atype\":333}]}]", jsonArray.toString());
    }


    @NonNull
    private ArrayList<ExternalUserId> createNewExternalUserIds() {
        ExternalUserId.UniqueId uid1 = new ExternalUserId.UniqueId("11", 111);
        uid1.setExt(new HashMap() {{
            put("category", "shopping");
        }});
        ExternalUserId.UniqueId uid2 = new ExternalUserId.UniqueId("12", 222);
        ExternalUserId id1 = new ExternalUserId("adserver1.com", Arrays.asList(uid1, uid2));
        id1.setExt(new HashMap() {{
            put("user", "1000");
        }});

        ExternalUserId.UniqueId uid3 = new ExternalUserId.UniqueId("22", 333);
        ExternalUserId id2 = new ExternalUserId("adserver2.com", List.of(uid3));

        // With empty unique ids, must be ignored
        ExternalUserId id3 = new ExternalUserId("adserver3.com", List.of());

        return new ArrayList<>(Arrays.asList(id1, id2, id3));
    }


    @NotNull
    private static ArrayList<ExternalUserId> createTestExternalUserIds() {
        ExternalUserId id1 = new ExternalUserId("adserver.org", List.of(new ExternalUserId.UniqueId("111111111111", 1)));
        id1.setExt(new HashMap() {{
            put("rtiPartner", "TDID");
        }});

        ExternalUserId id2 = new ExternalUserId("netid.de", List.of(new ExternalUserId.UniqueId("999888777", 2)));
        ExternalUserId id3 = new ExternalUserId("criteo.com", List.of(new ExternalUserId.UniqueId("_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", 3)));
        ExternalUserId id4 = new ExternalUserId("liveramp.com", List.of(new ExternalUserId.UniqueId("AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", 3)));
        ExternalUserId id5 = new ExternalUserId("sharedid.org", List.of(new ExternalUserId.UniqueId("111111111111", 1)));
        id5.setExt(new HashMap() {{
            put("third", "01ERJWE5FS4RAZKG6SKQ3ZYSKV");
        }});

        return new ArrayList<>(Arrays.asList(id1, id2, id3, id4, id5));
    }

}
