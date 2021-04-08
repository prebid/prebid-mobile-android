package org.prebid.mobile.rendering.networking.parameters;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.assertEquals;

@RunWith(RobolectricTestRunner.class)
public class UserConsentParameterBuilderTest {

    private UserConsentParameterBuilder mBuilder;
    private SharedPreferences mSharedPreferences;

    @Before
    public void setUp() throws Exception {
        Activity robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(robolectricActivity);

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(robolectricActivity);
        mSharedPreferences.edit().putString("IABConsent_ConsentString", "foobar_consent_string").commit();

        mBuilder = new UserConsentParameterBuilder();
    }

    @Test
    public void setAndGetUserConsentParamteterValuesWhenSubjectToGdgprIs1() throws JSONException {
        mSharedPreferences.edit().putString("IABConsent_SubjectToGDPR", "1").commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        mBuilder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"ext\":{\"gdpr\":1}},\"user\":{\"ext\":{\"consent\":\"foobar_consent_string\"}}}";
        assertEquals("Wrong values are set on pub Imp for the given adType", expectedJSON,
                     adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void setAndGetUserConsentParamteterValuesWhenSubjectToGdprIs0() throws JSONException {
        mSharedPreferences.edit().putString("IABConsent_SubjectToGDPR", "0").commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        mBuilder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"ext\":{\"gdpr\":0}},\"user\":{\"ext\":{\"consent\":\"foobar_consent_string\"}}}";
        assertEquals("Wrong values are set on pub Imp for the given adType", expectedJSON,
                     adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void WhenSubjectToGdprIsNullOrEmptyDontSendUserConsentValues() throws JSONException {
        mSharedPreferences.edit().remove("IABConsent_SubjectToGDPR").commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        mBuilder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{}";
        assertEquals("Wrong values are set on pub Imp for the given adType", expectedJSON,
                     adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void usPrivacyStringIsEmpty_DontAppendToUserConsentValues() throws JSONException {
        mSharedPreferences.edit().putString("IABUSPrivacy_String", "").commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        mBuilder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{}";
        assertEquals(expectedJSON, adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void usPrivacyStringIsNull_DontAppendToUserConsentValues() throws JSONException {
        mSharedPreferences.edit().remove("IABUSPrivacy_String").commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        mBuilder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{}";
        assertEquals(expectedJSON, adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void usPrivacyStringIsNotEmpty_AppendToUserConsentValues() throws JSONException {
        mSharedPreferences.edit().putString("IABUSPrivacy_String", "1YY").commit();
        AdRequestInput adRequestInput = new AdRequestInput();

        mBuilder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"ext\":{\"us_privacy\":\"1YY\"}}}";
        assertEquals(expectedJSON, adRequestInput.getBidRequest().getJsonObject().toString());
    }

    @Test
    public void usPrivacAndGdprStringsNotEmtpy_AppendToUserConsentValues() throws JSONException {
        mSharedPreferences.edit().putString("IABUSPrivacy_String", "1YY").commit();
        mSharedPreferences.edit().putString("IABConsent_SubjectToGDPR", "0").commit();

        AdRequestInput adRequestInput = new AdRequestInput();

        mBuilder.appendBuilderParameters(adRequestInput);

        String expectedJSON = "{\"regs\":{\"ext\":{\"us_privacy\":\"1YY\",\"gdpr\":0}},\"user\":{\"ext\":{\"consent\":\"foobar_consent_string\"}}}";
        assertEquals("Wrong values are set on pub Imp for the given adType", expectedJSON,
                     adRequestInput.getBidRequest().getJsonObject().toString());
    }
}