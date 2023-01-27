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

package org.prebid.mobile.rendering.networking.parameters;

import static org.junit.Assert.assertEquals;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.reflection.sdk.ManagersResolverReflection;
import org.prebid.mobile.reflection.sdk.UserConsentManagerReflection;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class UserConsentParameterBuilderTest {

    private UserConsentParameterBuilder builder;
    private SharedPreferences sharedPreferences;

    @Before
    public void setUp() throws Exception {
        Activity robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver resolver = ManagersResolver.getInstance();
        ManagersResolverReflection.resetManagers(resolver);
        resolver.prepare(robolectricActivity);
        UserConsentManager userConsentManager = ManagersResolver.getInstance().getUserConsentManager();
        UserConsentManagerReflection.resetAllFields(userConsentManager);

        PrebidMobile.setPbsDebug(false);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(robolectricActivity);
        sharedPreferences
            .edit()
            .putString(UserConsentManager.GDPR_2_CONSENT_KEY, "foobar_consent_string")
            .commit();

        builder = new UserConsentParameterBuilder();
    }

    @Test
    public void setAndGetUserConsentParamteterValuesWhenSubjectToGdgprIs1() throws JSONException {
        sharedPreferences.edit().putInt(UserConsentManager.GDPR_2_SUBJECT_KEY, 1).commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"ext\":{\"gdpr\":1}},\"user\":{\"ext\":{\"consent\":\"foobar_consent_string\"}}}";
        assertEquals("Wrong values are set on pub Imp for the given adType", expectedJSON,
                adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void setAndGetUserConsentParamteterValuesWhenSubjectToGdprIs0() throws JSONException {
        sharedPreferences.edit().putInt(UserConsentManager.GDPR_2_SUBJECT_KEY, 0).commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"ext\":{\"gdpr\":0}},\"user\":{\"ext\":{\"consent\":\"foobar_consent_string\"}}}";
        assertEquals(
            "Wrong values are set on pub Imp for the given adType",
            expectedJSON,
            adRequestInput.getBidRequest().getJsonObject().toString()
        );
    }

    @Test
    public void WhenSubjectToGdprIsNullOrEmptyDontSendUserConsentValues() throws JSONException {
        sharedPreferences.edit().remove(UserConsentManager.GDPR_2_SUBJECT_KEY).commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{}";
        assertEquals("Wrong values are set on pub Imp for the given adType", expectedJSON,
                adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void usPrivacyStringIsEmpty_DontAppendToUserConsentValues() throws JSONException {
        sharedPreferences.edit().putString(UserConsentManager.US_PRIVACY_KEY, "").commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{}";
        assertEquals(expectedJSON, adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void usPrivacyStringIsNull_DontAppendToUserConsentValues() throws JSONException {
        sharedPreferences.edit().remove(UserConsentManager.US_PRIVACY_KEY).commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{}";
        assertEquals(expectedJSON, adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void usPrivacyStringIsNotEmpty_AppendToUserConsentValues() throws JSONException {
        sharedPreferences.edit().putString(UserConsentManager.US_PRIVACY_KEY, "1YY").commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"ext\":{\"us_privacy\":\"1YY\"}}}";
        assertEquals(expectedJSON, adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void usPrivacAndGdprStringsNotEmtpy_AppendToUserConsentValues() throws JSONException {
        sharedPreferences.edit().putString(UserConsentManager.US_PRIVACY_KEY, "1YY").commit();
        sharedPreferences.edit().putInt(UserConsentManager.GDPR_2_SUBJECT_KEY, 0).commit();

        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"ext\":{\"us_privacy\":\"1YY\",\"gdpr\":0}},\"user\":{\"ext\":{\"consent\":\"foobar_consent_string\"}}}";
        assertEquals(
            "Wrong values are set on pub Imp for the given adType",
            expectedJSON,
            adRequestInput.getBidRequest().getJsonObject().toString()
        );
    }

    @Test
    public void gppString_AppendToUserConsentValues() throws JSONException {
        sharedPreferences.edit().putString(UserConsentManager.GPP_STRING_KEY, "testString").commit();

        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"gpp\":\"testString\"}}";
        assertEquals(
            "Generated JSON is wrong!",
            expectedJSON,
            adRequestInput.getBidRequest().getJsonObject().toString()
        );
    }

    @Test
    public void gppSid_OneVersion() throws JSONException {
        sharedPreferences.edit().putString(UserConsentManager.GPP_SID_KEY, "2").commit();

        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"gpp_sid\":[2]}}";
        assertEquals(
            "Generated JSON is wrong!",
            expectedJSON,
            adRequestInput.getBidRequest().getJsonObject().toString()
        );
    }

    @Test
    public void gppSid_TwoVersions() throws JSONException {
        sharedPreferences.edit().putString(UserConsentManager.GPP_SID_KEY, "2_3").commit();

        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"gpp_sid\":[2,3]}}";
        assertEquals(
            "Generated JSON is wrong!",
            expectedJSON,
            adRequestInput.getBidRequest().getJsonObject().toString()
        );
    }

    @Test
    public void gppSid_BlankValue() throws JSONException {
        sharedPreferences.edit().putString(UserConsentManager.GPP_SID_KEY, " ").commit();

        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{}}";
        assertEquals(
            "Generated JSON is wrong!",
            expectedJSON,
            adRequestInput.getBidRequest().getJsonObject().toString()
        );
    }

    @Test
    public void gppSid_WrongValue() throws JSONException {
        sharedPreferences.edit().putString(UserConsentManager.GPP_SID_KEY, "testValue").commit();

        AdRequestInput adRequestInput = new AdRequestInput();

        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{}}";
        assertEquals(
            "Generated JSON is wrong!",
            expectedJSON,
            adRequestInput.getBidRequest().getJsonObject().toString()
        );
    }

    @Test
    public void gppAll_AppendToUserConsentValues() throws JSONException {
        sharedPreferences
            .edit()
            .putString(UserConsentManager.GPP_SID_KEY, "2_3")
            .putString(UserConsentManager.GPP_STRING_KEY, "testString")
            .commit();

        AdRequestInput adRequestInput = new AdRequestInput();
        builder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"gpp\":\"testString\",\"gpp_sid\":[2,3]}}";
        assertEquals(
            "Generated JSON is wrong!",
            expectedJSON,
            adRequestInput.getBidRequest().getJsonObject().toString()
        );
    }

}