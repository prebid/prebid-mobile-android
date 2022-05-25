package org.prebid.mobile.rendering.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.reflection.sdk.UserConsentManagerReflection;
import org.prebid.mobile.rendering.sdk.deviceData.managers.UserConsentManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.util.Hashtable;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

@RunWith(RobolectricTestRunner.class)
public class UserConsentUtilsTest {

    private Context context;
    private SharedPreferences sharedPreferences = mock(SharedPreferences.class);

    @Before
    public void setUp() throws Exception {
        context = Robolectric.buildActivity(Activity.class).create().get();
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);

        resetAllPreferences();
    }

    @After
    public void destroy() {
        resetAllPreferences();
    }

    private void resetAllPreferences() {
        UserConsentManager userConsentManager = new UserConsentManager();
        sharedPreferences
            .edit()
            .remove(UserConsentManagerReflection.getConstGdpr1Subject(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr1Consent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2CmpSdkId(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2Subject(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2Consent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstGdpr2PurposeConsent(userConsentManager))
            .remove(UserConsentManagerReflection.getConstUsPrivacyString(userConsentManager))
            .remove(UserConsentManagerReflection.getConstCoppaCustomKey(userConsentManager))
            .apply();

        Reflection.setVariableTo(
            ManagersResolver.getInstance(),
            "registeredManagers",
            new Hashtable<ManagersResolver.ManagerType, Manager>()
        );
    }

    @Test
    public void subjectToCoppa() {
        UserConsentUtils.tryToSetSubjectToCoppa(false);
        assertNull(UserConsentUtils.tryToGetSubjectToCoppa());

        ManagersResolver.getInstance().prepare(context);

        assertNull(UserConsentUtils.tryToGetSubjectToCoppa());

        UserConsentUtils.tryToSetSubjectToCoppa(false);
        assertEquals(Boolean.FALSE, UserConsentUtils.tryToGetSubjectToCoppa());
    }

    @Test
    public void gdprConsent() {
        UserConsentUtils.tryToSetPrebidGdprConsent("1");
        assertNull(UserConsentUtils.tryToGetAnyGdprConsent());

        ManagersResolver.getInstance().prepare(context);

        assertNull(UserConsentUtils.tryToGetAnyGdprConsent());

        UserConsentUtils.tryToSetPrebidGdprConsent("1");
        assertEquals("1", UserConsentUtils.tryToGetAnyGdprConsent());
    }

    @Test
    public void gdprPurposeConsents() {
        UserConsentUtils.tryToSetPrebidGdprPurposeConsents("1");
        assertNull(UserConsentUtils.tryToGetAnyGdprPurposeConsents());

        ManagersResolver.getInstance().prepare(context);

        assertNull(UserConsentUtils.tryToGetAnyGdprPurposeConsents());

        UserConsentUtils.tryToSetPrebidGdprPurposeConsents("1");
        assertEquals("1", UserConsentUtils.tryToGetAnyGdprPurposeConsents());
    }

    @Test
    public void tryToGetDeviceAccessConsent() {
        UserConsentUtils.tryToSetPrebidGdprPurposeConsents("1");

        assertNull(UserConsentUtils.tryToGetAnyDeviceAccessConsent());

        ManagersResolver.getInstance().prepare(context);

        assertTrue(UserConsentUtils.tryToGetAnyDeviceAccessConsent());

        UserConsentUtils.tryToSetPrebidSubjectToGdpr(true);
        UserConsentUtils.tryToSetPrebidGdprPurposeConsents("0");
        assertFalse(UserConsentUtils.tryToGetAnyDeviceAccessConsent());
    }

}