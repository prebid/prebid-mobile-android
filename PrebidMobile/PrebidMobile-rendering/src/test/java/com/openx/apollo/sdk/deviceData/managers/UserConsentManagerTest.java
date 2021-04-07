package com.openx.apollo.sdk.deviceData.managers;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.apollo.test.utils.WhiteBox;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.lang.reflect.Method;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class UserConsentManagerTest {

    protected Context mContext = mock(Context.class);
    protected SharedPreferences mSharedPreferences = mock(SharedPreferences.class);
    private UserConsentManager mUserConsentManager;
    private String mIsSubjectToGdpr = "";
    private String mConsentString;
    private Boolean mCMPPresent;
    private String mParsedPurposeConsents;
    private String mParsedVendorConsents;

    private static final String CMP_PRESENT = "IABConsent_CMPPresent";
    private static final String SUBJECT_TO_GDPR = "IABConsent_SubjectToGDPR";
    private static final String CONSENT_STRING = "IABConsent_ConsentString";
    private static final String PARSED_PURPOSE_CONSENTS = "IABConsent_ParsedPurposeConsents";
    private static final String PARSED_VENDOR_CONSENTS = "IABConsent_ParsedVendorConsents";

    @Before
    public void setUp() throws Exception {
        Activity robolectricActivity = Robolectric.buildActivity(Activity.class).create().get();

        mSharedPreferences = PreferenceManager.getDefaultSharedPreferences(robolectricActivity);
        mSharedPreferences.edit().putString(SUBJECT_TO_GDPR, "1").commit();
        mSharedPreferences.edit().putString(CONSENT_STRING, "consent_string").commit();
        mSharedPreferences.edit().putBoolean(CMP_PRESENT, true).commit();
        mSharedPreferences.edit().putString(PARSED_PURPOSE_CONSENTS, "parsed_purpose_consents").commit();
        mSharedPreferences.edit().putString(PARSED_VENDOR_CONSENTS, "parsed_vendor_consents").commit();

        mUserConsentManager = new UserConsentManager();
        mUserConsentManager.init(robolectricActivity);
    }

    @Test
    public void initConsentValuesAtStartTest() throws Exception {
        mIsSubjectToGdpr = WhiteBox.getInternalState(mUserConsentManager, "mIsSubjectToGdpr");
        assertEquals("1", mIsSubjectToGdpr);

        mConsentString = WhiteBox.getInternalState(mUserConsentManager, "mConsentString");
        assertEquals("consent_string", mConsentString);

        mCMPPresent = WhiteBox.getInternalState(mUserConsentManager, "mCMPPresent");
        assertEquals(true, mCMPPresent);

        mParsedPurposeConsents = WhiteBox.getInternalState(mUserConsentManager, "mParsedPurposeConsents");
        assertEquals("parsed_purpose_consents", mParsedPurposeConsents);

        mParsedVendorConsents = WhiteBox.getInternalState(mUserConsentManager, "mParsedVendorConsents");
        assertEquals("parsed_vendor_consents", mParsedVendorConsents);
    }

    @Test
    public void getConsentValuesTest() throws Exception {
        Method methodGetConsent = WhiteBox.method(UserConsentManager.class, "getConsentValues", SharedPreferences.class, String.class);

        mConsentString = (String) methodGetConsent.invoke(mUserConsentManager, mSharedPreferences, CONSENT_STRING);
        assertEquals("consent_string", mConsentString);

        mCMPPresent = (Boolean) methodGetConsent.invoke(mUserConsentManager, mSharedPreferences, CMP_PRESENT);
        assertEquals(true, mCMPPresent);

        mIsSubjectToGdpr = (String) methodGetConsent.invoke(mUserConsentManager, mSharedPreferences, SUBJECT_TO_GDPR);
        assertEquals("1", mIsSubjectToGdpr);

        mParsedPurposeConsents = (String) methodGetConsent.invoke(mUserConsentManager, mSharedPreferences, PARSED_PURPOSE_CONSENTS);
        assertEquals("parsed_purpose_consents", mParsedPurposeConsents);

        mParsedVendorConsents = (String) methodGetConsent.invoke(mUserConsentManager, mSharedPreferences, PARSED_VENDOR_CONSENTS);
        assertEquals("parsed_vendor_consents", mParsedVendorConsents);
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
}